import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Breadcrumb from '@shared/components/ui/navigation/Breadcrumb';
import FiltersBar from './parts/FiltersBar';
import ResultsTable from './parts/ResultsTable';
import { paymentsService } from '../api/paymentsService';
import { PaymentRecord, PaymentSearchParams } from '../interfaces';

export default function PaymentsContainer() {
    const navigate = useNavigate();
    const [payments, setPayments] = useState<PaymentRecord[]>([]);
    const [loading, setLoading] = useState(false);
    const [searchApplied, setSearchApplied] = useState(false);
    const [error, setError] = useState<string>('');
    const [messages, setMessages] = useState<Record<string, string>>({});
    
    const [page, setPage] = useState(1);
    const [perPage, setPerPage] = useState(10);
    const [totalPages, setTotalPages] = useState(1);
    const [totalItems, setTotalItems] = useState(0);
    
  
    const [isAdmin] = useState(true); 

    useEffect(() => {
        loadMessages();
    }, []);

    const loadMessages = async () => {
        try {
            const msgs = await paymentsService.getMessages();
            setMessages(msgs);
        } catch (err) {
            console.error('Error loading messages:', err);
        }
    };

    const handleSearch = async (filters: any) => {
        setLoading(true);
        setError('');
        setSearchApplied(true);
        
        try {
            const params: PaymentSearchParams = {
                ...filters,
                page: 1, 
                size: perPage
            };
            
            const result = await paymentsService.searchPayments(params);
            
            if (result.items.length === 0) {
                setError(messages['INF6000'] || 'No existe información con los criterios establecidos.');
                setPayments([]);
                setTotalPages(1);
                setTotalItems(0);
            } else {
                setPayments(result.items);
                setPage(result.currentPage);
                setTotalPages(result.totalPages);
                setTotalItems(result.totalItems);
            }
        } catch (err) {
            console.error('Error searching payments:', err);
            setError('Error al buscar pagos. Por favor intente nuevamente.');
            setPayments([]);
        } finally {
            setLoading(false);
        }
    };

    const handlePageChange = async (newPage: number) => {
        setLoading(true);
        setPage(newPage);
        
        try {
            const result = await paymentsService.searchPayments({
                page: newPage,
                size: perPage,
                startDate: new Date().toISOString().split('T')[0],
                endDate: new Date().toISOString().split('T')[0]
            });
            
            setPayments(result.items);
            setTotalPages(result.totalPages);
        } catch (err) {
            console.error('Error changing page:', err);
        } finally {
            setLoading(false);
        }
    };

    const handlePerPageChange = async (newPerPage: number) => {
        setPerPage(newPerPage);
        setPage(1); 
        
        await handlePageChange(1);
    };

    const handleViewDetail = (payment: PaymentRecord) => {
        navigate(`/finanzas/pagos/${payment.paymentNumber}`);
    };

    const handleExport = async (format: 'csv' | 'xlsx') => {
        try {
            const blob = await paymentsService.exportPayments(
                {
                    startDate: new Date().toISOString().split('T')[0],
                    endDate: new Date().toISOString().split('T')[0]
                },
                format,
                isAdmin
            );
            
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `pagos_${new Date().toISOString().split('T')[0]}.${format}`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
        } catch (err) {
            console.error('Error exporting payments:', err);
            setError('Error al exportar los datos. Por favor intente nuevamente.');
        }
    };

    return (
        <div className="w-full px-8 py-6">
            <Breadcrumb
                items={[
                    { label: 'Home', to: '/' },
                    { label: 'Finanzas', to: '/' },
                    { label: 'Pagos' }
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
                        Listado de pagos
                    </h1>
                </div>
                
                <div className="flex items-center gap-2">
                    <span className="text-sm text-gray-500">toxid</span>
                    <div className="relative">
                        <select className="appearance-none border border-gray-300 rounded px-3 py-1.5 pr-8 text-sm bg-white focus:outline-none focus:border-gray-400 min-w-[260px]">
                            <option>PLA971117NBA-PLASTITRIM, SA ...-MX</option>
                        </select>
                        <span className="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none text-xs">
                            ▼
                        </span>
                    </div>
                </div>
            </div>

            <FiltersBar 
                onSearch={handleSearch} 
                isAdmin={isAdmin}
                messages={messages}
            />

            {error && (
                <div className="mt-4 bg-yellow-50 border border-yellow-300 text-yellow-800 px-4 py-3 rounded-md">
                    {error}
                </div>
            )}

            {searchApplied && (
                <div className="mt-6">
                    <ResultsTable
                        rows={payments}
                        loading={loading}
                        isAdmin={isAdmin}
                        page={page}
                        perPage={perPage}
                        totalPages={totalPages}
                        totalItems={totalItems}
                        onPageChange={handlePageChange}
                        onPerPageChange={handlePerPageChange}
                        onViewDetail={handleViewDetail}
                        onExport={handleExport}
                    />
                </div>
            )}

            {!searchApplied && !loading && (
                <div className="mt-6 border border-gray-200 rounded-lg p-8">
                    <div className="text-center text-gray-500">
                        <p className="text-sm">No hay resultados para mostrar.</p>
                        <p className="text-sm">Por favor, ingrese criterios de búsqueda.</p>
                    </div>
                </div>
            )}

        </div>
    );
}
