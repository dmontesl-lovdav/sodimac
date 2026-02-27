import { ReactElement, useState } from 'react';
import { InvoiceRecord } from '../../interfaces';

interface ResultsTableProps {
    rows: InvoiceRecord[];
    loading?: boolean;
    isAdmin?: boolean;
    page: number;
    perPage: number;
    totalPages: number;
    totalItems: number;
    onPageChange: (page: number) => void;
    onPerPageChange: (perPage: number) => void;
    onDownloadXML: (invoice: InvoiceRecord) => void;
    onDownloadPDF: (invoice: InvoiceRecord) => void;
    onViewCreditNotes: (invoice: InvoiceRecord) => void;
    onCancelInvoice?: (invoice: InvoiceRecord) => void;
    selectedInvoices: Set<string>;
    onSelectInvoice: (invoiceId: string) => void;
    onSelectAll: (selected: boolean) => void;
}

const styles = {
    container: {
        marginTop: '1rem',
    },
    tableWrapper: {
        border: '1px solid #e5e7eb',
        borderRadius: '0.5rem',
        overflow: 'hidden',
        backgroundColor: '#ffffff',
    },
    tableScroll: {
        overflowX: 'auto' as const,
    },
    table: {
        minWidth: '100%',
        fontSize: '0.875rem',
    },
    thead: {
        backgroundColor: '#eaf5fc',
        color: '#002d4c',
        fontWeight: 500,
    },
    th: {
        padding: '0.75rem 1rem',
        textAlign: 'left' as const,
    },
    tr: {
        borderTop: '1px solid #e5e7eb',
    },
    trHover: {
        backgroundColor: '#f9fafb',
    },
    td: {
        padding: '0.75rem 1rem',
    },
    tdCenter: {
        textAlign: 'center' as const,
    },
    tdRight: {
        textAlign: 'right' as const,
    },
    emptyCell: {
        padding: '1.5rem',
        textAlign: 'center' as const,
        color: '#6b7280',
    },
    checkbox: {
        borderRadius: '0.25rem',
        border: '1px solid #d1d5db',
    },
    uuidText: {
        fontSize: '0.75rem',
    },
    providerName: {
        fontWeight: 500,
    },
    actionsCell: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '0.5rem',
    },
    actionButton: {
        padding: '0.25rem',
        background: 'none',
        border: 'none',
        cursor: 'pointer',
        borderRadius: '0.25rem',
        transition: 'background-color 150ms',
    },
    actionButtonHover: {
        backgroundColor: '#f3f4f6',
    },
    actionIcon: {
        color: '#4b5563',
    },
    footer: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '0.75rem',
        borderTop: '1px solid #e5e7eb',
        backgroundColor: '#f9fafb',
    },
    footerLeft: {
        display: 'flex',
        alignItems: 'center',
        gap: '0.75rem',
        fontSize: '0.875rem',
        color: '#4b5563',
    },
    navButton: {
        color: '#9ca3af',
        background: 'none',
        border: 'none',
        cursor: 'pointer',
        transition: 'color 150ms',
    },
    navButtonDisabled: {
        color: '#d1d5db',
        cursor: 'default',
    },
    footerRight: {
        display: 'flex',
        alignItems: 'center',
        gap: '0.5rem',
    },
    perPageLabel: {
        fontSize: '0.875rem',
        color: '#4b5563',
    },
    perPageSelect: {
        marginLeft: '0.5rem',
        border: '1px solid #d1d5db',
        borderRadius: '0.25rem',
        padding: '0.25rem 0.5rem',
        fontSize: '0.875rem',
    },
};

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
    onDownloadXML,
    onDownloadPDF,
    onViewCreditNotes,
    onCancelInvoice,
    selectedInvoices,
    onSelectInvoice,
    onSelectAll
}: ResultsTableProps): ReactElement {
    const [hoveredRow, setHoveredRow] = useState<number | null>(null);

    const columns = [
        {
            header: (
                <input
                    type="checkbox"
                    style={styles.checkbox}
                    onChange={(e) => onSelectAll(e.target.checked)}
                    checked={rows.length > 0 && rows.every(r => selectedInvoices.has(r.id?.toString() || ''))}
                />
            ),
            render: (r: InvoiceRecord) => (
                <input
                    type="checkbox"
                    style={styles.checkbox}
                    checked={selectedInvoices.has(r.id?.toString() || '')}
                    onChange={() => onSelectInvoice(r.id?.toString() || '')}
                />
            ),
            align: 'center' as const
        },
        {
            header: 'Serie',
            render: (r: InvoiceRecord) => r.serie
        },
        {
            header: 'Folio',
            render: (r: InvoiceRecord) => r.folio
        },
        {
            header: 'Subtotal',
            render: (r: InvoiceRecord) => `$${r.subtotal.toLocaleString('es-MX', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            })}`,
            align: 'right' as const
        },
        {
            header: 'Total',
            render: (r: InvoiceRecord) => `$${r.total.toLocaleString('es-MX', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            })}`,
            align: 'right' as const
        },
        {
            header: 'Orden compra',
            render: (r: InvoiceRecord) => r.purchaseOrder
        },
        {
            header: 'Recepcion',
            render: (r: InvoiceRecord) => r.reception
        },
        {
            header: 'UUID',
            render: (r: InvoiceRecord) => (
                <span style={styles.uuidText} title={r.uuid}>
                    {r.uuid.substring(0, 8)}...
                </span>
            )
        },
        {
            header: 'Num NC',
            render: (r: InvoiceRecord) => r.creditNotesCount,
            align: 'center' as const
        },
        ...(isAdmin ? [
            {
                header: 'ID Proveedor',
                render: (r: InvoiceRecord) => r.providerId
            },
            {
                header: 'Nombre Proveedor',
                render: (r: InvoiceRecord) => (
                    <span style={styles.providerName}>{r.providerName}</span>
                )
            }
        ] : []),
        {
            header: 'Fecha emision',
            render: (r: InvoiceRecord) => r.issueDate
        },
        {
            header: 'Fecha recepcion',
            render: (r: InvoiceRecord) => r.receptionDate
        },
        {
            header: 'Fecha envio',
            render: (r: InvoiceRecord) => r.sendDate
        },
        {
            header: 'Acciones',
            render: (r: InvoiceRecord) => (
                <div style={styles.actionsCell}>
                    <button
                        onClick={() => onDownloadXML(r)}
                        title="Descargar XML"
                        style={styles.actionButton}
                        onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f3f4f6'}
                        onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                    >
                        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" style={styles.actionIcon}>
                            <path d="M8 11L4 7h2V2h4v5h2l-4 4z" fill="currentColor"/>
                            <path d="M2 13h12" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                        </svg>
                    </button>
                    <button
                        onClick={() => onDownloadPDF(r)}
                        title="Descargar PDF"
                        style={styles.actionButton}
                        onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f3f4f6'}
                        onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                    >
                        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" style={styles.actionIcon}>
                            <path d="M3 2h10v12H3V2z" stroke="currentColor" strokeWidth="1.5" fill="none"/>
                            <path d="M6 5h4M6 8h4M6 11h2" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                        </svg>
                    </button>
                    {r.creditNotesCount > 0 && (
                        <button
                            onClick={() => onViewCreditNotes(r)}
                            title="Ver notas de crédito"
                            style={styles.actionButton}
                            onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f3f4f6'}
                            onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                        >
                            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" style={styles.actionIcon}>
                                <circle cx="8" cy="8" r="6" stroke="currentColor" strokeWidth="1.5" fill="none"/>
                                <path d="M10.5 6.5L8 9l-2-2" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                        </button>
                    )}
                    {isAdmin && (r.statusId === 1 || r.statusId === 2) && onCancelInvoice && (
                        <button
                            onClick={() => onCancelInvoice(r)}
                            title="Cancelar factura"
                            style={styles.actionButton}
                            onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f3f4f6'}
                            onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                        >
                            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" style={styles.actionIcon}>
                                <circle cx="8" cy="8" r="6" stroke="currentColor" strokeWidth="1.5" fill="none"/>
                                <path d="M5 5l6 6M11 5l-6 6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                            </svg>
                        </button>
                    )}
                </div>
            ),
            align: 'center' as const
        }
    ];

    const getTdStyle = (align?: 'left' | 'center' | 'right') => ({
        ...styles.td,
        ...(align === 'center' ? styles.tdCenter : align === 'right' ? styles.tdRight : {}),
    });

    return (
        <div style={styles.container}>
            <div style={styles.tableWrapper}>
                <div style={styles.tableScroll}>
                    <table style={styles.table}>
                        <thead style={styles.thead}>
                            <tr>
                                {columns.map(({ header }, index) => (
                                    <th key={`header-${index}`} style={styles.th}>
                                        {header}
                                    </th>
                                ))}
                            </tr>
                        </thead>
                        <tbody>
                            {rows.length === 0 && (
                                <tr>
                                    <td colSpan={columns.length} style={styles.emptyCell}>
                                        {loading ? 'Cargando...' : 'No se encontraron facturas con los criterios establecidos'}
                                    </td>
                                </tr>
                            )}
                            {rows.map((row, idx) => (
                                <tr
                                    key={row.id || idx}
                                    style={{
                                        ...styles.tr,
                                        ...(hoveredRow === idx ? styles.trHover : {}),
                                    }}
                                    onMouseEnter={() => setHoveredRow(idx)}
                                    onMouseLeave={() => setHoveredRow(null)}
                                >
                                    {columns.map(({ render, align = 'left' }, colIdx) => (
                                        <td key={`cell-${idx}-${colIdx}`} style={getTdStyle(align)}>
                                            {render(row)}
                                        </td>
                                    ))}
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                {rows.length > 0 && (
                    <div style={styles.footer}>
                        <div style={styles.footerLeft}>
                            <span>{((page - 1) * perPage) + 1}-{Math.min(page * perPage, totalItems)} of {totalItems}</span>
                            <button
                                style={{
                                    ...styles.navButton,
                                    ...(page === 1 ? styles.navButtonDisabled : {}),
                                }}
                                title="Previous"
                                disabled={page === 1}
                                onClick={() => onPageChange(Math.max(1, page - 1))}
                            >
                                ❮
                            </button>
                            <button
                                style={{
                                    ...styles.navButton,
                                    ...(page === totalPages ? styles.navButtonDisabled : {}),
                                }}
                                title="Next"
                                disabled={page === totalPages}
                                onClick={() => onPageChange(Math.min(totalPages, page + 1))}
                            >
                                ❯
                            </button>
                        </div>

                        <div style={styles.footerRight}>
                            <label style={styles.perPageLabel}>
                                Filas por página:
                                <select
                                    value={perPage}
                                    onChange={(e) => onPerPageChange(Number(e.target.value))}
                                    style={styles.perPageSelect}
                                >
                                    <option value="5">5</option>
                                    <option value="10">10</option>
                                    <option value="25">25</option>
                                    <option value="50">50</option>
                                </select>
                            </label>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
