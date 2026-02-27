import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import GenericTable from '@shared/components/ui/table/GenericTable';
import { paymentsService } from '../api/paymentsService';
import { PaymentDetail as PaymentDetailType, PaymentDocument } from '../interfaces';

export default function PaymentDetail() {
    const { paymentNumber } = useParams<{ paymentNumber: string }>();
    const navigate = useNavigate();
    
    const [paymentDetail, setPaymentDetail] = useState<PaymentDetailType | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string>('');
    const [messages, setMessages] = useState<Record<string, string>>({});
    
    // Control de rol (mock por ahora)
    const [isAdmin] = useState(true);

    useEffect(() => {
        loadMessages();
        if (paymentNumber) {
            loadPaymentDetail(paymentNumber);
        }
    }, [paymentNumber]);

    const loadMessages = async () => {
        try {
            const msgs = await paymentsService.getMessages();
            setMessages(msgs);
        } catch (err) {
            console.error('Error loading messages:', err);
        }
    };

    const loadPaymentDetail = async (number: string) => {
        setLoading(true);
        setError('');
        try {
            const detail = await paymentsService.getPaymentDetail(number);
            setPaymentDetail(detail);
        } catch (err) {
            console.error('Error loading payment detail:', err);
            setError('Error al cargar el detalle del pago');
        } finally {
            setLoading(false);
        }
    };

    const handleExport = async () => {
        if (!paymentDetail) return;
        
        try {
            const blob = await paymentsService.exportPaymentDetail(paymentDetail.paymentNumber, isAdmin);
            
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `detalle_pago_${paymentDetail.paymentNumber}_${new Date().toISOString().split('T')[0]}.csv`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
        } catch (err) {
            console.error('Error exporting payment detail:', err);
            setError('Error al exportar el detalle');
        }
    };

    const handleUploadComplement = () => {
        if (!paymentDetail) return;
        
        if (!paymentDetail.documents || paymentDetail.documents.length === 0) {
            setError(messages['WRN7003'] || 'No es posible publicar el complemento de pago, faltan documentos fiscales por publicar');
            return;
        }
        
        alert('Redirección a pantalla de carga de complemento - Pendiente de implementación (STM-1006)');
    };

    if (loading) {
        return (
            <div className="w-full px-8 py-6">
                <div className="flex justify-center items-center h-64">
                    <div className="text-gray-500">Cargando detalle del pago...</div>
                </div>
            </div>
        );
    }

    if (!paymentDetail) {
        return (
            <div className="w-full px-8 py-6">
                <div className="bg-red-50 border border-red-300 text-red-800 px-4 py-3 rounded-md">
                    Pago no encontrado
                </div>
            </div>
        );
    }

    const documentColumns = [
        { 
            header: 'Número documento', 
            render: (doc: PaymentDocument) => doc.documentNumber 
        },
        { 
            header: 'Referencia', 
            render: (doc: PaymentDocument) => doc.reference || '-'
        },
        { 
            header: 'Fecha documento', 
            render: (doc: PaymentDocument) => doc.documentDate 
        },
        ...(isAdmin ? [{
            header: 'Fecha contable',
            render: (doc: PaymentDocument) => doc.accountingDate || '-'
        }] : []),
        { 
            header: 'Fecha vencimiento', 
            render: (doc: PaymentDocument) => doc.dueDate 
        },
        { 
            header: 'Moneda', 
            render: (doc: PaymentDocument) => doc.currency 
        },
        { 
            header: 'Monto', 
            render: (doc: PaymentDocument) => {
                const symbol = doc.currency === 'USD' ? '$' : '$';
                return `${symbol}${doc.amount.toLocaleString('es-MX', { 
                    minimumFractionDigits: 2, 
                    maximumFractionDigits: 2 
                })}`;
            }
        },
        { 
            header: 'Serie', 
            render: (doc: PaymentDocument) => doc.serie || '-'
        },
        { 
            header: 'Folio', 
            render: (doc: PaymentDocument) => doc.folio || '-'
        },
        { 
            header: 'UUID', 
            render: (doc: PaymentDocument) => doc.uuid ? (
                <span className="text-xs" title={doc.uuid}>
                    {doc.uuid.substring(0, 8)}...
                </span>
            ) : '-'
        },
        { 
            header: 'Estatus', 
            render: (doc: PaymentDocument) => (
                <span className={`px-2 py-1 rounded text-xs ${
                    doc.status === 'Activo' ? 'bg-green-100 text-green-800' :
                    doc.status === 'Cancelado' ? 'bg-red-100 text-red-800' :
                    'bg-gray-100 text-gray-800'
                }`}>
                    {doc.status}
                </span>
            )
        }
    ];

    return (
        <div className="w-full px-8 py-6">
            <Breadcrumb
                items={[
                    { label: 'Home', to: '/' },
                    { label: 'Finanzas', to: '/' },
                    { label: 'Pagos', to: '/finanzas/pagos' },
                    { label: `Detalle ${paymentDetail.paymentNumber}` }
                ]}
            />

            <div className="flex items-center justify-between mt-6 mb-6">
                <div className="flex items-center gap-3">
                    <div className="text-gray-600">
                        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                            <path d="M7 1V5H13V1H17C17.5523 1 18 1.44772 18 2V18C18 18.5523 17.5523 19 17 19H3C2.44772 19 2 18.5523 2 18V2C2 1.44772 2.44772 1 3 1H7Z" stroke="currentColor" strokeWidth="1.5"/>
                            <path d="M7 1H13V5H7V1Z" stroke="currentColor" strokeWidth="1.5"/>
                            <path d="M6 9H14M6 12H14M6 15H10" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                        </svg>
                    </div>
                    <h1 className="text-lg font-semibold text-gray-900">
                        Detalle del pago
                    </h1>
                </div>
                
                <div className="flex items-center gap-2">
                    <span className="text-sm text-gray-500">toxid</span>
                    <div className="relative">
                        <select className="appearance-none border border-gray-300 rounded px-3 py-1.5 pr-8 text-sm bg-white focus:outline-none focus:border-gray-400 min-w-[260px]">
                            <option>{paymentDetail.providerNumber} - {paymentDetail.providerName}</option>
                        </select>
                        <span className="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none text-xs">
                            ▼
                        </span>
                    </div>
                </div>
            </div>

            <div className="border border-gray-200 rounded-lg p-4 bg-white mb-6">
                <h2 className="text-base font-medium text-gray-700 mb-4">Datos generales</h2>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    <div>
                        <p className="text-sm text-gray-600">Número de pago/referencia</p>
                        <p className="font-medium">{paymentDetail.paymentNumber}</p>
                    </div>
                    <div>
                        <p className="text-sm text-gray-600">Año de pago</p>
                        <p className="font-medium">{paymentDetail.paymentYear || '2025'}</p>
                    </div>
                    {isAdmin && (
                        <div>
                            <p className="text-sm text-gray-600">Número y nombre del proveedor</p>
                            <p className="font-medium">{paymentDetail.providerNumber} - {paymentDetail.providerName}</p>
                        </div>
                    )}
                    <div>
                        <p className="text-sm text-gray-600">Fecha de pago</p>
                        <p className="font-medium">{paymentDetail.paymentDate.replace(/-/g, '/')}</p>
                    </div>
                    <div>
                        <p className="text-sm text-gray-600">Monto del pago</p>
                        <p className="font-medium text-lg">
                            ${paymentDetail.amount.toLocaleString('es-MX', { 
                                minimumFractionDigits: 2, 
                                maximumFractionDigits: 2 
                            })} {paymentDetail.currency}
                        </p>
                    </div>
                </div>
            </div>

            {error && (
                <div className="bg-yellow-50 border border-yellow-300 text-yellow-800 px-4 py-3 rounded-md mb-4">
                    {error}
                </div>
            )}

            <div className="border border-gray-200 rounded-lg bg-white">
                <div className="p-4 border-b border-gray-200">
                    <h2 className="text-base font-medium text-gray-700">Documentos relacionados</h2>
                </div>
                
                <GenericTable
                    rows={paymentDetail.documents || []}
                    columns={documentColumns}
                    emptyLabel="No hay documentos relacionados"
                    perPage={10}
                    page={1}
                    totalPages={1}
                    totalItems={paymentDetail.documents?.length || 0}
                />

                <div className="flex justify-between items-center p-3 border-t border-gray-200 bg-gray-50">
                    <button
                        onClick={() => navigate('/finanzas/pagos')}
                        className="px-4 py-1.5 text-sm text-gray-700 hover:bg-gray-200 rounded border border-gray-300 bg-white"
                    >
                        Volver
                    </button>
                    
                    <div className="flex gap-3">
                        <button
                            onClick={handleExport}
                            className="flex items-center gap-2 px-4 py-1.5 text-sm text-gray-700 hover:bg-gray-200 rounded border border-gray-300 bg-white transition-colors"
                        >
                            <svg width="14" height="14" viewBox="0 0 14 14" fill="none" className="text-gray-600">
                                <path d="M7 10L3 6H5V1H9V6H11L7 10Z" fill="currentColor"/>
                                <path d="M1 12H13V13H1V12Z" fill="currentColor"/>
                            </svg>
                            <span>Export as</span>
                            <svg width="10" height="6" viewBox="0 0 10 6" fill="none" className="text-gray-400 ml-1">
                                <path d="M1 1L5 5L9 1" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                        </button>
                        
                        {paymentDetail.statusId === 0 && (
                            <button
                                onClick={handleUploadComplement}
                                className="px-6 py-1.5 bg-[#002B55] text-white rounded text-sm font-medium hover:brightness-110"
                            >
                                Publicar complemento
                            </button>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}


