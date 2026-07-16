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
import type { Column, RowAction } from '@/shared/components/ui/table/GenericTable';
import { StatusPill } from '@/shared/components/ui/statusPill/StatusPill';
import BackLinkButton from '@shared/components/ui/button/BackLinkButton';
import { APP_EVENT, PermissionGate } from '@shared/security';

import eyeIcon from '@assets/eye-show.svg';

import { formatDate } from '@/utils/utils';
import { migoService } from './api/MigoClient';
import type { MigoDocument, MigoReception } from './interfaces';
import { MIGO_STATUS_MAP } from './interfaces';

import './styles/MigoContainer.css';

interface GroupedReception {
    key: string;
    nroOc: number;
    nroRecepcion: number;
    sucursal: number;
    numeroProveedor: string;
    nroGuia: string;
    origen: string;
    fechaRecepcion: string;
    importeSinImpuesto: number;
    montoOc: number;
    vendorName: string;
    emailFinancial: string;
}

function statusPillType(status: number): string {
    if (status === 0) return 'success';
    if (status === 8) return 'error';
    return 'info';
}

function formatCurrency(val: number | undefined | null): string {
    if (val == null) return '-';
    return Number(val).toLocaleString('es-MX', { minimumFractionDigits: 2 });
}

export default function MigoReceptions(): ReactElement {
    const { id } = useParams<{ id: string }>();
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

    const loadAllReceptions = useCallback(async () => {
        if (!id) return;
        setLoading(true);
        try {
            const res: any = await migoService.getReceptions(id, 1, 5000);
            const pageData = res?.data ?? res;
            const list = pageData?.content ?? [];
            setAllReceptions(list);
            if (list.length === 0) {
                financeAlert.showWarning(
                    'Sin registros',
                    'Este documento no tiene recepciones agrupadas para mostrar.',
                );
            }
        } catch (err) {
            financeAlert.showErrorFrom(
                'Error',
                err,
                'No fue posible cargar las recepciones del documento.',
            );
            setAllReceptions([]);
        } finally {
            setLoading(false);
        }
    }, [id]);

    useEffect(() => {
        loadDocument();
        loadAllReceptions();
    }, [loadDocument, loadAllReceptions]);

    const groupedReceptions: GroupedReception[] = useMemo(() => {
        const groups = new Map<string, GroupedReception>();
        for (const r of allReceptions) {
            const key = `${r.nroOc}-${r.nroRecepcion}`;
            if (!groups.has(key)) {
                groups.set(key, {
                    key,
                    nroOc: r.nroOc,
                    nroRecepcion: r.nroRecepcion,
                    sucursal: r.sucursal,
                    numeroProveedor: (r.numeroProveedor ?? '').toString().trim(),
                    nroGuia: r.nroGuia ?? '-',
                    origen: r.origen ?? '-',
                    fechaRecepcion: r.fechaRecepcion,
                    importeSinImpuesto: r.importeSinImpuesto,
                    montoOc: r.montoOc ?? 0,
                    vendorName: (r.vendorName ?? '').trim(),
                    emailFinancial: (r.emailFinancial ?? '').trim(),
                });
            }
        }
        return Array.from(groups.values());
    }, [allReceptions]);

    const totalItems = groupedReceptions.length;
    const totalPages = Math.max(1, Math.ceil(totalItems / perPage));
    const clampedPage = Math.min(page, totalPages);
    const pageItems = groupedReceptions.slice((clampedPage - 1) * perPage, clampedPage * perPage);

    const handleExportCsv = async () => {
        if (!id) return;
        try {
            await migoService.exportCsv(id);
        } catch (err) {
            financeAlert.showErrorFrom(
                'Error',
                err,
                'Error al exportar las recepciones.',
            );
        }
    };

    const st = doc ? MIGO_STATUS_MAP[doc.status] : null;

    const columns: Column<GroupedReception>[] = [
        { header: 'Orden Compra', render: (r) => r.nroOc },
        { header: 'Recepción', render: (r) => r.nroRecepcion },
        { header: 'Sucursal', align: 'center', render: (r) => r.sucursal },
        { header: 'Número Proveedor', render: (r) => r.numeroProveedor || '--' },
        { header: 'Nombre Proveedor', render: (r) => r.vendorName || '--' },
        { header: 'Correo electrónico', render: (r) => r.emailFinancial || '--' },
        { header: 'Guía', render: (r) => r.nroGuia },
        { header: 'Origen', render: (r) => r.origen },
        { header: 'Fecha Recepción', render: (r) => formatDate(r.fechaRecepcion) },
        { header: 'Importe', align: 'right', render: (r) => formatCurrency(r.importeSinImpuesto) },
        { header: 'Monto OC', align: 'right', render: (r) => formatCurrency(r.montoOc) },
    ];

    const rowActions: RowAction<GroupedReception>[] = [
        {
            title: 'Ver artículos relacionados',
            icon: eyeIcon,
            onClick: (r) => navigate(`/finanzas/migo/${id}/recepciones/${r.nroOc}/${r.nroRecepcion}/articulos`),
        },
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
                    { label: doc?.folio ?? 'Recepciones' },
                ])}
            />

            <div className="migo-box">
                <div className="migo-header">
                    <div>
                        <h3 className="migo-title">
                            {'Órdenes de Compra y Recepciones'}
                        </h3>
                        <p className="migo-description">Listado de OC y recepciones publicadas en el documento.</p>
                    </div>
                    <div className="migo-toolbar finz-toolbar-actions">
                        <PermissionGate appEvent={APP_EVENT.MIGO.DOWNLOAD_CSV_DETAIL}>
                            <GenericButton variant="primary" onClick={handleExportCsv}>
                                Exportar CSV
                            </GenericButton>
                        </PermissionGate>
                    </div>
                </div>

                {doc && (
                    <div className="migo-summary-card" style={{ marginTop: 16 }}>
                        <div>
                            <div className="migo-summary-label">Folio</div>
                            <div className="migo-summary-value">{doc.folio}</div>
                        </div>
                        <div>
                            <div className="migo-summary-label">Nombre Archivo</div>
                            <div className="migo-summary-value">{doc.fileName}</div>
                        </div>
                        <div>
                            <div className="migo-summary-label">Total Registros</div>
                            <div className="migo-summary-value">{doc.totalRecords}</div>
                        </div>
                        <div>
                            <div className="migo-summary-label">Estatus</div>
                            <StatusPill type={statusPillType(doc.status)}>
                                {st?.label ?? `${doc.status}`}
                            </StatusPill>
                        </div>
                        <div>
                            <div className="migo-summary-label">{'Fecha Publicación'}</div>
                            <div className="migo-summary-value">{formatDate(doc.publishedAt)}</div>
                        </div>
                    </div>
                )}

                <div className="migo-grid-section">
                    <GenericTable<GroupedReception>
                        rows={pageItems}
                        columns={columns}
                        actions={rowActions}
                        emptyLabel={loading ? 'Cargando...' : 'No se encontraron recepciones.'}
                        perPage={perPage}
                        page={clampedPage}
                        totalPages={totalPages}
                        totalItems={totalItems}
                        onChangePage={setPage}
                        onChangePerPage={(s) => { setPerPage(s); setPage(1); }}
                    />
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 16 }}>
                    <BackLinkButton onClick={() => navigate('/finanzas/migo')}>
                        Volver
                    </BackLinkButton>
                </div>

                {loading && <GenericModal visible variant="loading" message="Cargando recepciones..." />}

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
