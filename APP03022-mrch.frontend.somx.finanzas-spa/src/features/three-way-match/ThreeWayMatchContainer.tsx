import { useRef, useState } from 'react';
import {
    useFinanceListScreenSession,
    useFinanceListRefetchOnReturn,
    FINANCE_LIST_KEYS,
    saveFinanceListFilters,
} from '@/shared/hooks';
import { Breadcrumb, GenericModal } from '@shared/components/ui';
import { withFinanceBreadcrumb } from '@shared/components/ui/navigation/financeBreadcrumb';
import ThreeWayMatchFilters from './components/ThreeWayMatchFilters';
import ThreeWayMatchGridTable from './components/ThreeWayMatchGridTable';
import ThreeWayMatchToolbar from './components/ThreeWayMatchToolbar';

import {
    searchThreeWayMatch,
    exportThreeWayMatchCsv,
    exportThreeWayMatchXlsx
} from './api';

import type { ThreeWayMatchRecord } from './interfaces';

import './styles/ThreeWayMatchContainer.css';
import { getErrorMessage } from '@/utils/errorMessage';

export default function ThreeWayMatchContainer() {

    const [rows, setRows] = useState<ThreeWayMatchRecord[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [page, setPage] = useState<number>(1);
    const [perPage, setPerPage] = useState<number>(10);
    const [totalPages, setTotalPages] = useState<number>(1);
    const [totalItems, setTotalItems] = useState<number>(0);
    const [filters, setFilters] = useState<any>({});
    const warnIfEmptyRef = useRef(false);

    const returningFromDetail = useFinanceListScreenSession(
        FINANCE_LIST_KEYS.threeWayMatch
    );

    const [errorModal, setErrorModal] = useState({
        visible: false,
        message: '',
        title: 'Error',
        severity: 'error' as 'error' | 'warning' | 'info' | 'success',
    });

    const isAdmin = true;
    const hasData = rows.length > 0;

    const fetchPage = async (
        criteria: any,
        p: number,
        size: number
    ): Promise<void> => {
        setLoading(true);

        try {
            const result = await searchThreeWayMatch(criteria, p, size);
            const list = result?.data ?? [];
            setRows(list);
            setPage(p);
            setPerPage(size);
            setTotalPages(result?.totalPages ?? 1);
            setTotalItems(result?.total ?? 0);

            if (list.length === 0 && warnIfEmptyRef.current) {
                setErrorModal({
                    visible: true,
                    title: 'Sin registros',
                    message: 'No se encontraron coincidencias con los criterios indicados.',
                    severity: 'warning',
                });
            }
        } catch (error: any) {
            warnIfEmptyRef.current = false;
            setErrorModal({
                visible: true,
                title: 'Error',
                message: getErrorMessage(error, 'Error inesperado al consultar Three Way Match.'),
                severity: 'error',
            });

            setRows([]);
            setTotalPages(1);
            setTotalItems(0);
        } finally {
            warnIfEmptyRef.current = false;
            setLoading(false);
        }
    };

    const handleSearch = async (newFilters: any): Promise<void> => {
        warnIfEmptyRef.current = true;
        saveFinanceListFilters(FINANCE_LIST_KEYS.threeWayMatch.filters, newFilters);
        setFilters(newFilters);
        await fetchPage(newFilters, 1, perPage);
    };

    useFinanceListRefetchOnReturn(
        FINANCE_LIST_KEYS.threeWayMatch,
        returningFromDetail,
        (saved) => {
            warnIfEmptyRef.current = false;
            handleSearch(saved);
        }
    );

    const handleExportCsv = async () => {
        if (!hasData) return;
        try {
            await exportThreeWayMatchCsv({
                ...filters,
                page,
                limit: perPage
            });
        } catch {
            setErrorModal({
                visible: true,
                title: 'Error',
                message: 'Error al exportar CSV.',
                severity: 'error',
            });
        }
    };

    const handleExportXlsx = async () => {
        if (!hasData) return;
        try {
            await exportThreeWayMatchXlsx({
                ...filters,
                page,
                limit: perPage
            });
        } catch {
            setErrorModal({
                visible: true,
                title: 'Error',
                message: 'Error al exportar Excel.',
                severity: 'error',
            });
        }
    };

    const handleClear = (): void => {
        warnIfEmptyRef.current = false;
        setRows([]);
        setPage(1);
        setPerPage(10);
        setTotalPages(1);
        setTotalItems(0);
        setFilters({});
    };

    return (
        <div className="twm-layout">

            <Breadcrumb
                items={withFinanceBreadcrumb([{ label: 'Three Way Match' }])}
            />

            <div className="twm-box">

                {/* HEADER con Toolbar */}
                <div className="twm-header">
                    <div>
                        <h3 className="twm-title">Three Way Match</h3>
                        <p className="twm-description">
                            Conciliación de la orden de compra, recepción y factura antes y después del pago
                        </p>
                    </div>

                    <ThreeWayMatchToolbar
                        onExportCsv={handleExportCsv}
                        onExportXlsx={handleExportXlsx}
                        disabled={!hasData}
                    />
                </div>

                <div className="twm-filters-section">
                    <ThreeWayMatchFilters
                        isAdmin={isAdmin}
                        onSearch={handleSearch}
                        onClear={handleClear}
                    />
                </div>

                <div className="twm-grid-section">

                    <ThreeWayMatchGridTable
                        rows={rows}
                        page={page}
                        perPage={perPage}
                        totalPages={totalPages}
                        totalItems={totalItems}
                        onChangePage={(p) => {
                            if (!filters || Object.keys(filters).length === 0) return;
                            fetchPage(filters, p, perPage);
                        }}
                        onChangePerPage={(n: number) => {
                            if (!filters || Object.keys(filters).length === 0) return;
                            fetchPage(filters, 1, n);
                        }}
                        loading={loading}
                        isAdmin={isAdmin}
                    />

                </div>

                {loading && (
                    <GenericModal
                        visible
                        variant="loading"
                        message="Cargando…"
                    />
                )}

                <GenericModal
                    visible={errorModal.visible}
                    variant="alert"
                    severity={errorModal.severity}
                    title={errorModal.title}
                    message={errorModal.message}
                    buttonText="Aceptar"
                    onClose={() =>
                        setErrorModal({
                            visible: false,
                            message: '',
                            title: 'Error',
                            severity: 'error',
                        })
                    }
                />

            </div>

        </div>
    );
}
