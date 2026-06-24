import { useEffect, useState } from 'react';
import {
    useFinanceListScreenSession,
    useFinanceListRefetchOnReturn,
    FINANCE_LIST_KEYS,
    readFinanceListFilters,
    saveFinanceListFilters,
} from '@/shared/hooks';
import { useNavigate } from 'react-router-dom';
import { Breadcrumb, GenericModal } from '@shared/components/ui';
import { withFinanceBreadcrumb } from '@shared/components/ui/navigation/financeBreadcrumb';

import AuditLogsFilters from './components/AuditLogsFilters';
import AuditLogsGridTable from './components/AuditLogsGridTable';
import AuditLogsToolbar from './components/AuditLogsToolbar';

import { listAuditLogs, exportAuditLogsCsv } from './api';

import type { AuditLogRecord } from './interfaces';

import './styles/AuditLogsContainer.css';

export default function AuditLogsContainer() {
    const navigate = useNavigate();

    const [rows, setRows] = useState<AuditLogRecord[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [page, setPage] = useState<number>(1);
    const [perPage, setPerPage] = useState<number>(10);
    const [totalPages, setTotalPages] = useState<number>(1);
    const [totalItems, setTotalItems] = useState<number>(0);

    const [filters, setFilters] = useState<any>({});

    const [initialFilters, setInitialFilters] = useState<any>(undefined);

    const [selectedIds, setSelectedIds] = useState<string[]>([]);

    const [errorModal, setErrorModal] = useState({
        visible: false,
        message: '',
    });

    const returningFromDetail = useFinanceListScreenSession(
        FINANCE_LIST_KEYS.auditLogs
    );

    const hasData = rows.length > 0;

    const fetchData = async (f: any, p: number, size: number): Promise<void> => {
        setLoading(true);

        try {
            const result: any = await listAuditLogs(f, p, size);

            const items = result?.data ?? [];
            const total = Number(result?.total ?? 0);
            const limit = Number(result?.limit ?? size ?? 10);

            setRows(items);
            setTotalItems(total);
            setTotalPages(Math.max(1, Math.ceil(total / Math.max(1, limit))));
        } catch (error: any) {
            let message = 'Error inesperado.';

            if (error?.code === 'ECONNABORTED') message = 'La consulta tardó demasiado.';
            else if (error?.response) message = 'Error en servidor.';

            setErrorModal({ visible: true, message });

            setRows([]);
            setTotalPages(1);
            setTotalItems(0);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const saved = readFinanceListFilters(FINANCE_LIST_KEYS.auditLogs.filters);
        if (saved) setInitialFilters(saved);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const handleClear = (): void => {
        setRows([]);
        setTotalItems(0);
        setTotalPages(1);
        setPage(1);
        setSelectedIds([]);
        setFilters({});
        setInitialFilters(undefined);
    };

    const handleSearch = async (newFilters: any): Promise<void> => {
        saveFinanceListFilters(FINANCE_LIST_KEYS.auditLogs.filters, newFilters);
        setFilters(newFilters);
        setInitialFilters(newFilters);
        setPage(1);
        setSelectedIds([]);

        await fetchData(newFilters, 1, perPage);
    };

    useFinanceListRefetchOnReturn(
        FINANCE_LIST_KEYS.auditLogs,
        returningFromDetail,
        (saved) => {
            void handleSearch(saved);
        }
    );

    const handleChangePage = async (nextPage: number) => {
        if (!filters || Object.keys(filters).length === 0) return;
        const p = Math.max(1, Number(nextPage || 1));
        setPage(p);

        await fetchData(filters, p, perPage);
    };

    const handleChangePerPage = async (n: number) => {
        if (!filters || Object.keys(filters).length === 0) return;
        const size = Math.max(1, Number(n || 10));

        setPerPage(size);
        setPage(1);
        setSelectedIds([]);

        await fetchData(filters, 1, size);
    };

    const handleSelectRow = (id: any, selected: boolean) => {
        const safeId = String(id);

        setSelectedIds((prev) => {
            const set = new Set(prev);
            if (selected) set.add(safeId);
            else set.delete(safeId);
            return Array.from(set);
        });
    };

    const handleExportCsv = async () => {
        if (!hasData) return;

        try {
            if (selectedIds.length > 0) {
                await exportAuditLogsCsv({
                    ...filters,
                    ids: selectedIds,
                });
                return;
            }

            await exportAuditLogsCsv({
                ...filters,
                page,
                limit: perPage,
            });
        } catch {
            setErrorModal({
                visible: true,
                message: 'Error exportando CSV',
            });
        }
    };

    const handleViewDetail = (row: AuditLogRecord) => {
        const traceId = (row as any)?.trace_id;
        if (!traceId) {
            setErrorModal({
                visible: true,
                message: 'No se encontró IdTransacción (trace_id) para abrir el tren.',
            });
            return;
        }

        navigate(`/util/auditoria/bitacora-actividades/tren/${traceId}`, {
            state: { filters },
        });
    };

    return (
        <div className="al-layout">
            <Breadcrumb
                items={withFinanceBreadcrumb([
                    { label: 'Bitácora de Actividades' },
                ])}
            />

            <div className="al-box">
                <div className="al-header">
                    <div>
                        <h3 className="al-title">Bitácora de Actividades</h3>
                        <p className="al-description">
                            Busca y consulta eventos registrados por transacción para trazabilidad y análisis.
                        </p>
                    </div>

                    <AuditLogsToolbar onExportCsv={handleExportCsv} disabled={!hasData} />
                </div>

                <div className="al-filters-section">
                    <AuditLogsFilters
                        onSearch={handleSearch}
                        onClear={handleClear}
                        initialFilters={initialFilters}
                    />
                </div>

                <div className="al-grid-section">
                    <AuditLogsGridTable
                        rows={rows}
                        page={page}
                        perPage={perPage}
                        totalPages={totalPages}
                        totalItems={totalItems}
                        onChangePage={handleChangePage}
                        onChangePerPage={handleChangePerPage}
                        loading={loading}
                        onViewDetail={handleViewDetail}
                        selectedIds={selectedIds}
                        onSelectRow={handleSelectRow}
                    />
                </div>

                {loading && <GenericModal visible variant="loading" message="Cargando…" />}

                <GenericModal
                    visible={errorModal.visible}
                    variant="alert"
                    severity="error"
                    title="Error"
                    message={errorModal.message}
                    buttonText="Aceptar"
                    onClose={() => setErrorModal({ visible: false, message: '' })}
                />
            </div>
        </div>
    );
}