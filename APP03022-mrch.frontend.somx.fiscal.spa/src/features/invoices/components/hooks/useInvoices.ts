import { useState, useEffect } from 'react';
import { invoicesService } from '../../api/invoicesService';
import { InvoiceRecord, InvoiceSearchParams } from '../../interfaces';

export function useInvoices(isAdmin: boolean) {
    const [invoices, setInvoices] = useState<InvoiceRecord[]>([]);
    const [loading, setLoading] = useState(false);
    const [searchApplied, setSearchApplied] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [messages, setMessages] = useState<Record<string, string>>({});
    const [page, setPage] = useState(1);
    const [perPage, setPerPage] = useState(10);
    const [totalPages, setTotalPages] = useState(1);
    const [totalItems, setTotalItems] = useState(0);
    const [selectedInvoices, setSelectedInvoices] = useState<Set<string>>(new Set());

    useEffect(() => {
        setMessages(invoicesService.getMessages());
    }, []);

    const handleSearch = async (filters: any) => {
        setLoading(true);
        setError('');
        setSuccess('');
        setSearchApplied(true);
        setSelectedInvoices(new Set());

        try {
            const params: InvoiceSearchParams = { ...filters, page: 1, size: perPage };
            const result = await invoicesService.searchInvoices(params);

            if (result.items.length === 0) {
                setError(messages['INF6000'] || 'No existe información con los criterios establecidos');
                setInvoices([]);
                setTotalPages(1);
                setTotalItems(0);
            } else {
                setInvoices(result.items);
                setPage(result.currentPage);
                setTotalPages(result.totalPages);
                setTotalItems(result.totalItems);
            }
        } catch {
            setError('Error al buscar facturas. Por favor intente nuevamente.');
            setInvoices([]);
        } finally {
            setLoading(false);
        }
    };

    const handlePageChange = (p: number) => setPage(p);
    const handlePerPageChange = (p: number) => {
        setPerPage(p);
        setPage(1);
    };

    const handleSelectInvoice = (id: string) => {
        setSelectedInvoices(prev => {
            const copy = new Set(prev);
            copy.has(id) ? copy.delete(id) : copy.add(id);
            return copy;
        });
    };

    const handleSelectAll = (checked: boolean) => {
        setSelectedInvoices(checked ? new Set(invoices.map(i => i.id?.toString() || '')) : new Set());
    };

    const handleDownloadXML = async (invoice: InvoiceRecord) => {
        try {
            const blob = await invoicesService.downloadXML([invoice.id?.toString() || '']);
            downloadFile(blob, `factura_${invoice.serie}_${invoice.folio}.zip`);
        } catch {
            setError('Error al descargar XML');
        }
    };

    const handleDownloadPDF = async (invoice: InvoiceRecord) => {
        try {
            const blob = await invoicesService.downloadPDF([invoice.id?.toString() || '']);
            downloadFile(blob, `factura_${invoice.serie}_${invoice.folio}.pdf`);
        } catch {
            setError('Error al descargar PDF');
        }
    };

    const handleViewCreditNotes = (invoice: InvoiceRecord) => {
        alert(`Ver notas de crédito de factura ${invoice.serie}-${invoice.folio}`);
    };

    const handleCancelInvoice = (invoice: InvoiceRecord) => {
        if (window.confirm(`¿Cancelar factura ${invoice.serie}-${invoice.folio}?`)) {
            alert('Redirección a cancelación (pendiente)');
        }
    };

    const downloadFile = (blob: Blob, filename: string) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        a.click();
        window.URL.revokeObjectURL(url);
    };

    return {
        invoices,
        loading,
        searchApplied,
        error,
        success,
        messages,
        page,
        perPage,
        totalPages,
        totalItems,
        selectedInvoices,
        handleSearch,
        handlePageChange,
        handlePerPageChange,
        handleSelectInvoice,
        handleSelectAll,
        handleDownloadXML,
        handleDownloadPDF,
        handleViewCreditNotes,
        handleCancelInvoice,
        setError,
        setSuccess,
    };
}
