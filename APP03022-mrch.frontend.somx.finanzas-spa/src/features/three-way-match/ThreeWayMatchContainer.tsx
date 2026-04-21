import { useState } from 'react';
import { Breadcrumb, GenericModal } from '@shared/components/ui';
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

export default function ThreeWayMatchContainer() {

    const [rows, setRows] = useState<ThreeWayMatchRecord[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [page, setPage] = useState<number>(1);
    const [perPage, setPerPage] = useState<number>(10);
    const [totalPages, setTotalPages] = useState<number>(1);
    const [totalItems, setTotalItems] = useState<number>(0);
    const [filters, setFilters] = useState<any>({});

    const [errorModal, setErrorModal] = useState({
        visible: false,
        message: '',
    });

    const isAdmin = true;
    const hasData = rows.length > 0;

    const handleSearch = async (newFilters: any): Promise<void> => {

        setFilters(newFilters);
        setLoading(true);

        try {
            const result = await searchThreeWayMatch(newFilters, page, perPage);
            setRows(result?.data ?? []);
            setTotalPages(result?.totalPages ?? 1);
            setTotalItems(result?.total ?? 0);
        } catch (error: any) {

            let message = 'Error inesperado.';

            if (error.code === 'ECONNABORTED')
                message = 'La consulta tardó demasiado.';
            else if (error.response)
                message = 'Error en servidor.';

            setErrorModal({
                visible: true,
                message,
            });

            setRows([]);
            setTotalPages(1);
            setTotalItems(0);

        } finally {
            setLoading(false);
        }
    };

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
                message: 'Error exportando CSV',
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
                message: 'Error exportando Excel',
            });
        }
    };

    return (
        <div className="twm-layout">

            <Breadcrumb
                items={[
                    { label: 'Finanzas', to: '/' },
                    { label: 'Three Way Match' }
                ]}
            />

            <div className="twm-box">

                {/* HEADER con Toolbar */}
                <div className="twm-header">
                    <div>
                        <h3 className="twm-title">Three Way Match</h3>
                        <p className="twm-description">
                            Consulta y validación de documentos financieros.
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
                    />
                </div>

                <div className="twm-grid-section">

                    <ThreeWayMatchGridTable
                        rows={rows}
                        page={page}
                        perPage={perPage}
                        totalPages={totalPages}
                        totalItems={totalItems}
                        onChangePage={setPage}
                        onChangePerPage={(n: number) => {
                            setPerPage(n);
                            setPage(1);
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
                    severity="error"
                    title="Error"
                    message={errorModal.message}
                    buttonText="Aceptar"
                    onClose={() =>
                        setErrorModal({
                            visible: false,
                            message: '',
                        })
                    }
                />

            </div>

        </div>
    );
}
