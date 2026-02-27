import { useState, ChangeEvent, useEffect } from 'react';
import { fetchComplementPayments, ComplementPayment } from './ComplementPaymentService';
import { downloadComplementReport } from '../api/xlsxHelper';

export function useComplementPayment() {
    const [filters, setFilters] = useState({
        uuid: '',
        serie: '',
        folio: '',
        rfcEmisor: '',
        rfcReceptor: '',
        fechaPagoInicio: '',
        fechaPagoFin: '',
        fechaEmisionInicio: '',
        fechaEmisionFin: '',
        status: '',
    });
    const [searchResults, setSearchResults] = useState<ComplementPayment[]>([]);
    const [page, setPage] = useState(1); // 👈 base 1 para UI
    const [perPage, setPerPage] = useState(10);
    const [totalPages, setTotalPages] = useState(1);
    const [totalItems, setTotalItems] = useState(0);
    const [loading, setLoading] = useState(false);
    const [selectedIds, setSelectedIds] = useState<string[]>([]);
    const [alert, setAlert] = useState({
        visible: false,
        title: '',
        message: '',
        severity: 'info' as 'info' | 'success' | 'warning' | 'error',
    });

    const handleFilterChange =
        (field: keyof typeof filters) =>
            (e: ChangeEvent<HTMLInputElement>) =>
                setFilters({ ...filters, [field]: e.target.value });

    const handleSearch = async () => {
        setLoading(true);
        try {
            // backend usa paginación base 0
            const data = await fetchComplementPayments(filters, page - 1, perPage);
            const rowsWithId = (data.content || []).map((r: any) => ({
                ...r,
                id: r.paymentsUuid,
            }));

            setSearchResults(rowsWithId);
            setTotalPages(data.totalPages || 1);
            setTotalItems(data.totalElements || rowsWithId.length);
        } catch {
            setAlert({
                visible: true,
                title: 'Error',
                message: 'Ocurrió un error al obtener los complementos de pago.',
                severity: 'error',
            });
        } finally {
            setLoading(false);
        }
    };

    // 👇 para refrescar automáticamente cuando cambie página o tamaño
    useEffect(() => {
        handleSearch();
    }, [page, perPage]);

    const handleClear = () => {
        setFilters({
            uuid: '',
            serie: '',
            folio: '',
            rfcEmisor: '',
            rfcReceptor: '',
            fechaPagoInicio: '',
            fechaPagoFin: '',
            fechaEmisionInicio: '',
            fechaEmisionFin: '',
            status: '',
        });
        setSearchResults([]);
        setSelectedIds([]);
        setTotalItems(0);
        setPage(1);
    };

    const handleSelectRow = (uuid: string, checked: boolean) => {
        setSelectedIds((prev) => (checked ? [...prev, uuid] : prev.filter((x) => x !== uuid)));
    };

    const handleDownload = () => {
        if (!selectedIds.length) {
            setAlert({
                visible: true,
                title: 'Advertencia',
                message: 'Selecciona al menos un registro antes de exportar.',
                severity: 'warning',
            });
            return;
        }
        downloadComplementReport(searchResults, selectedIds);
        setAlert({
            visible: true,
            title: 'Reporte XLSX',
            message: 'El reporte XLSX se generó correctamente.',
            severity: 'success',
        });
    };

    return {
        filters,
        setFilters,
        searchResults,
        page,
        setPage,
        perPage,
        setPerPage,
        totalPages,
        totalItems,
        loading,
        selectedIds,
        alert,
        setAlert,
        handleFilterChange,
        handleSearch,
        handleClear,
        handleSelectRow,
        handleDownload,
    };
}
