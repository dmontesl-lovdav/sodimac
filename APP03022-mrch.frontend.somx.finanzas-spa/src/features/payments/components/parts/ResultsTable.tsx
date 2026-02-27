import { ReactElement, useState } from 'react';
import GenericTable from '@shared/components/ui/table/GenericTable';
import { PaymentRecord } from '../../interfaces';

interface ResultsTableProps {
    rows: PaymentRecord[];
    loading?: boolean;
    isAdmin?: boolean;
    page: number;
    perPage: number;
    totalPages: number;
    totalItems: number;
    onPageChange: (page: number) => void;
    onPerPageChange: (perPage: number) => void;
    onViewDetail: (payment: PaymentRecord) => void;
    onExport?: (format: 'csv' | 'xlsx') => void;
}

export default function ResultsTable({
    rows = [],
    loading = false,
    isAdmin = false,
    page,
    perPage,
    totalPages,
    totalItems,
    onPageChange,
    onPerPageChange,
    onViewDetail,
    onExport
}: ResultsTableProps): ReactElement {
    const [selectedRows, setSelectedRows] = useState<Set<string>>(new Set());
    const [exportFormat] = useState<string>('csv');

    const handleRowSelect = (paymentNumber: string) => {
        const newSelected = new Set(selectedRows);
        if (newSelected.has(paymentNumber)) {
            newSelected.delete(paymentNumber);
        } else {
            newSelected.add(paymentNumber);
        }
        setSelectedRows(newSelected);
    };


    const columns = [
        {
            header: (
                <input 
                    type="checkbox" 
                    className="rounded border-gray-300"
                    onChange={(e) => {
                        if (e.target.checked) {
                            setSelectedRows(new Set(rows.map(r => r.paymentNumber)));
                        } else {
                            setSelectedRows(new Set());
                        }
                    }}
                />
            ),
            render: (r: PaymentRecord) => (
                <input 
                    type="checkbox" 
                    className="rounded border-gray-300"
                    checked={selectedRows.has(r.paymentNumber)}
                    onChange={() => handleRowSelect(r.paymentNumber)}
                />
            ),
            align: 'center' as const
        },
        { 
            header: 'PO', 
            render: (r: PaymentRecord) => r.providerNumber 
        },
        { 
            header: 'Reception number', 
            render: (r: PaymentRecord) => r.receptionNumber 
        },
        { 
            header: 'Guide number', 
            render: (r: PaymentRecord) => r.guideNumber 
        },
        { 
            header: 'Invoice number', 
            render: (r: PaymentRecord) => r.invoiceNumber || '' 
        },
        { 
            header: 'Bulk Qty', 
            render: (r: PaymentRecord) => r.bulkQty,
            align: 'center' as const
        },
        { 
            header: 'Pallet Qty', 
            render: (r: PaymentRecord) => r.palletQty,
            align: 'center' as const
        },
        { 
            header: 'Totes Qty', 
            render: (r: PaymentRecord) => r.totalQty,
            align: 'center' as const 
        },
        { 
            header: (
                <span>Issue date <span className="text-xs">↓</span></span>
            ), 
            render: (r: PaymentRecord) => r.issueDate
        },
        { 
            header: 'Report#', 
            render: (r: PaymentRecord) => (
                r.reportIds && r.reportIds.length > 0 
                    ? r.reportIds.join(', ')
                    : '-'
            )
        },
        {
            header: 'Acciones',
            render: (r: PaymentRecord) => (
                <button
                    onClick={() => onViewDetail(r)}
                    className="text-blue-600 hover:text-blue-800 underline text-sm"
                >
                    Ver
                </button>
            ),
            align: 'center' as const
        }
    ];

    const actions: any[] = [];

    const handleExport = () => {
        if (onExport) {
            onExport(exportFormat as 'csv' | 'xlsx');
        }
    };

    return (
        <div className="mt-4">
            <div className="border border-gray-200 rounded-lg overflow-hidden bg-white">
                <GenericTable<PaymentRecord>
                    rows={rows}
                    columns={columns}
                    actions={actions}
                    emptyLabel={loading ? 'Cargando...' : 'No se encontraron pagos con los criterios establecidos'}
                    perPage={perPage}
                    page={page}
                    totalPages={totalPages}
                    totalItems={totalItems}
                    onChangePerPage={onPerPageChange}
                    onChangePage={onPageChange}
                />
                
                {rows.length > 0 && (
                    <div className="flex justify-between items-center p-3 border-t border-gray-200 bg-gray-50">
                        <div className="flex items-center gap-3 text-sm text-gray-600">
                            <span>{((page - 1) * perPage) + 1}-{Math.min(page * perPage, totalItems)} of {totalItems}</span>
                            <button 
                                className="text-gray-400 hover:text-gray-600 disabled:text-gray-300" 
                                title="Previous"
                                disabled={page === 1}
                                onClick={() => onPageChange(Math.max(1, page - 1))}
                            >
                                ❮
                            </button>
                            <button 
                                className="text-gray-400 hover:text-gray-600 disabled:text-gray-300" 
                                title="Next"
                                disabled={page === totalPages}
                                onClick={() => onPageChange(Math.min(totalPages, page + 1))}
                            >
                                ❯
                            </button>
                        </div>
                        {onExport && (
                            <button
                                onClick={() => onExport('csv')}
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
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}
