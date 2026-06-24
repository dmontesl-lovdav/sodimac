import { ReactElement, useState, useEffect, useCallback, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Breadcrumb, GenericModal, GenericButton } from '@shared/components/ui';
import { withFinanceBreadcrumb } from '@shared/components/ui/navigation/financeBreadcrumb';
import { useFinanceAlertModal } from '@/shared/hooks/useFinanceAlertModal';
import {
    FINANCE_LIST_KEYS,
    useFinanceListReturnFromDetail,
} from '@/shared/hooks';
import GenericTable from '@/shared/components/ui/table/GenericTable';
import type { Column } from '@/shared/components/ui/table/GenericTable';
import BackLinkButton from '@shared/components/ui/button/BackLinkButton';

import { formatDate } from '@/utils/utils';
import { migoService } from './api/MigoClient';
import type { MigoDocument, MigoReception } from './interfaces';

import './styles/MigoContainer.css';

function formatCurrency(val: number | undefined | null): string {
    if (val == null) return '-';
    return Number(val).toLocaleString('es-MX', { minimumFractionDigits: 2 });
}

export default function MigoArticles(): ReactElement {
    const { id, nroOc, nroRecepcion } = useParams<{ id: string; nroOc: string; nroRecepcion: string }>();
    const navigate = useNavigate();

    const financeAlert = useFinanceAlertModal();

    useFinanceListReturnFromDetail(
        FINANCE_LIST_KEYS.migo.moduleKey,
        FINANCE_LIST_KEYS.migo.listPath
    );

    const [doc, setDoc] = useState<MigoDocument | null>(null);
    const [allReceptions, setAllReceptions] = useState<MigoReception[]>([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(1);
    const [perPage, setPerPage] = useState(10);

    const loadDocument = useCallback(async () => {
        if (!id) return;
        try {
            const res: any = await migoService.getById(id);
            setDoc(res?.data ?? res);
        } catch (err) {
            financeAlert.showErrorFrom(
                'Error',
                err,
                'No fue posible obtener el documento MIGO.',
            );
        }
    }, [id]);

    const loadReceptions = useCallback(async () => {
        if (!id || nroOc == null || nroRecepcion == null) return;
        setLoading(true);
        try {
            const res: any = await migoService.getReceptions(id, 1, 5000);
            const pageData = res?.data ?? res;
            const list = pageData?.content ?? [];
            setAllReceptions(list);
            const filtered = list.filter(
                (r: MigoReception) =>
                    String(r.nroOc) === nroOc && String(r.nroRecepcion) === nroRecepcion,
            );
            if (filtered.length === 0) {
                financeAlert.showWarning(
                    'Sin registros',
                    'No se encontraron artículos para la orden de compra y recepción indicadas.',
                );
            }
        } catch (err) {
            financeAlert.showErrorFrom(
                'Error',
                err,
                'No fue posible cargar los artículos de la recepción.',
            );
            setAllReceptions([]);
        } finally {
            setLoading(false);
        }
    }, [id, nroOc, nroRecepcion]);

    useEffect(() => {
        loadDocument();
        loadReceptions();
    }, [loadDocument, loadReceptions]);

    const articles = useMemo(() => {
        return allReceptions.filter(
            r => String(r.nroOc) === nroOc && String(r.nroRecepcion) === nroRecepcion
        );
    }, [allReceptions, nroOc, nroRecepcion]);

    const headerData = articles[0];

    const totalItems = articles.length;
    const totalPages = Math.max(1, Math.ceil(totalItems / perPage));
    const clampedPage = Math.min(page, totalPages);
    const pageItems = articles.slice((clampedPage - 1) * perPage, clampedPage * perPage);

    const handleExportCsv = () => {
        if (articles.length === 0) return;

        const headers = ['SKU', 'Descripcion_Sku', 'Cantidad', 'Importe_Unitario', 'Importe_Total'];
        const rows = articles.map(a => [
            a.sku ?? '',
            `"${(a.descripcionSku ?? '').replace(/"/g, '""')}"`,
            String(a.cantidad),
            String(a.importeUnitario),
            String(a.importeSinImpuestoDet),
        ]);

        const csv = [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
        const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `articulos-OC${nroOc}-REC${nroRecepcion}.csv`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    };

    const columns: Column<MigoReception>[] = [
        { header: 'SKU', render: (r) => r.sku ?? '-' },
        { header: 'Descripción SKU', render: (r) => r.descripcionSku ?? '-' },
        { header: 'Cantidad', align: 'right', render: (r) => r.cantidad },
        { header: 'Importe Unitario', align: 'right', render: (r) => formatCurrency(r.importeUnitario) },
        { header: 'Importe Total', align: 'right', render: (r) => formatCurrency(r.importeSinImpuestoDet) },
    ];

    return (
        <div className="migo-layout">
            <Breadcrumb
                items={withFinanceBreadcrumb([
                    {
                        label: 'Publicación de recepción MIGO',
                        onClick: () =>
                            navigate('/finanzas/migo', { state: { resetFilters: true } }),
                    },
                    { label: doc?.folio ?? 'Documento', to: `/finanzas/migo/${id}/recepciones` },
                    { label: `OC ${nroOc} - Recepción ${nroRecepcion}` },
                ])}
            />

            <div className="migo-box">
                <div className="migo-header">
                    <div>
                        <h3 className="migo-title">Artículos Relacionados</h3>
                        <p className="migo-description">Detalle de artículos de la recepción seleccionada.</p>
                    </div>
                    <div className="migo-toolbar finz-toolbar-actions">
                        <GenericButton variant="primary" onClick={handleExportCsv} disabled={articles.length === 0}>
                            Exportar CSV
                        </GenericButton>
                    </div>
                </div>

                {headerData && (
                    <div className="migo-summary-card" style={{ marginTop: 16 }}>
                        <div>
                            <div className="migo-summary-label">Nro. Orden de Compra</div>
                            <div className="migo-summary-value">{headerData.nroOc}</div>
                        </div>
                        <div>
                            <div className="migo-summary-label">Nro. Recepción</div>
                            <div className="migo-summary-value">{headerData.nroRecepcion}</div>
                        </div>
                        <div>
                            <div className="migo-summary-label">Sucursal</div>
                            <div className="migo-summary-value">{headerData.sucursal}</div>
                        </div>
                        <div>
                            <div className="migo-summary-label">Nro. Guía</div>
                            <div className="migo-summary-value">{headerData.nroGuia ?? '-'}</div>
                        </div>
                        <div>
                            <div className="migo-summary-label">Origen</div>
                            <div className="migo-summary-value">{headerData.origen ?? '-'}</div>
                        </div>
                        <div>
                            <div className="migo-summary-label">Fecha Recepción</div>
                            <div className="migo-summary-value">{formatDate(headerData.fechaRecepcion)}</div>
                        </div>
                        <div>
                            <div className="migo-summary-label">Importe sin Impuestos</div>
                            <div className="migo-summary-value">{formatCurrency(headerData.importeSinImpuesto)}</div>
                        </div>
                        <div>
                            <div className="migo-summary-label">Monto OC</div>
                            <div className="migo-summary-value">
                                {formatCurrency(
                                    headerData.montoOc && headerData.montoOc > 0
                                        ? headerData.montoOc
                                        : headerData.importeSinImpuesto,
                                )}
                            </div>
                        </div>
                    </div>
                )}

                <div className="migo-grid-section">
                    <GenericTable<MigoReception>
                        rows={pageItems}
                        columns={columns}
                        emptyLabel={loading ? 'Cargando...' : 'No se encontraron artículos.'}
                        perPage={perPage}
                        page={clampedPage}
                        totalPages={totalPages}
                        totalItems={totalItems}
                        onChangePage={setPage}
                        onChangePerPage={(s) => { setPerPage(s); setPage(1); }}
                    />
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 16 }}>
                    <BackLinkButton onClick={() => navigate(`/finanzas/migo/${id}/recepciones`)}>
                        Volver
                    </BackLinkButton>
                </div>

                {loading && <GenericModal visible variant="loading" message="Cargando artículos..." />}

                <GenericModal
                    visible={financeAlert.alertVisible}
                    variant="alert"
                    severity={financeAlert.alertSeverity}
                    title={financeAlert.alertTitle}
                    message={financeAlert.alertMessage}
                    buttonText="Aceptar"
                    onClose={financeAlert.closeAlert}
                />
            </div>
        </div>
    );
}
