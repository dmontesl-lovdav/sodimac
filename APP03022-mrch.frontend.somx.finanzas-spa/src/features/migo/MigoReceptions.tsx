import { ReactElement, useState, useEffect, useCallback, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Breadcrumb, GenericModal, GenericButton } from '@shared/components/ui';
import GenericTable from '@/shared/components/ui/table/GenericTable';
import type { Column, RowAction } from '@/shared/components/ui/table/GenericTable';
import { StatusPill } from '@/shared/components/ui/statusPill/StatusPill';
import { Divider } from '@/shared/components/ui/misc';

import eyeIcon from '@assets/eye-show.svg';

import { migoService } from './api/MigoClient';
import type { MigoDocument, MigoReception } from './interfaces';
import { MIGO_STATUS_MAP } from './interfaces';

import './styles/MigoContainer.css';

interface GroupedReception {
    key: string;
    nroOc: number;
    nroRecepcion: number;
    sucursal: number;
    nroGuia: string;
    origen: string;
    fechaRecepcion: string;
    importeSinImpuesto: number;
    montoOc: number;
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

function formatDate(d?: string): string {
    if (!d) return '-';
    return new Date(d).toLocaleDateString('es-MX');
}

export default function MigoReceptions(): ReactElement {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    const [doc, setDoc] = useState<MigoDocument | null>(null);
    const [allReceptions, setAllReceptions] = useState<MigoReception[]>([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(1);
    const [perPage, setPerPage] = useState(10);

    const [alertModal, setAlertModal] = useState({ visible: false, message: '' });

    const loadDocument = useCallback(async () => {
        if (!id) return;
        try {
            const res: any = await migoService.getById(id);
            setDoc(res?.data ?? res);
        } catch (err) {
            console.error('[MIGO] getById error', err);
        }
    }, [id]);

    const loadAllReceptions = useCallback(async () => {
        if (!id) return;
        setLoading(true);
        try {
            const res: any = await migoService.getReceptions(id, 1, 5000);
            const pageData = res?.data ?? res;
            setAllReceptions(pageData?.content ?? []);
        } catch (err) {
            console.error('[MIGO] getReceptions error', err);
            setAllReceptions([]);
        } finally {
            setLoading(false);
        }
    }, [id]);

    useEffect(() => {
        loadDocument();
        loadAllReceptions();
    }, [id]);

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
                    nroGuia: r.nroGuia ?? '-',
                    origen: r.origen ?? '-',
                    fechaRecepcion: r.fechaRecepcion,
                    importeSinImpuesto: r.importeSinImpuesto,
                    montoOc: r.montoOc ?? 0,
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
            console.error('[MIGO] export error', err);
            setAlertModal({ visible: true, message: 'Error al exportar las recepciones.' });
        }
    };

    const st = doc ? MIGO_STATUS_MAP[doc.status] : null;

    const columns: Column<GroupedReception>[] = [
        { header: 'Nro. Orden de Compra', render: (r) => r.nroOc },
        { header: 'Nro. Recepción', render: (r) => r.nroRecepcion },
        { header: 'Sucursal', align: 'center', render: (r) => r.sucursal },
        { header: 'Nro. Guía', render: (r) => r.nroGuia },
        { header: 'Origen', render: (r) => r.origen },
        { header: 'Fecha Recepción', render: (r) => formatDate(r.fechaRecepcion) },
        { header: 'Importe sin Impuestos', align: 'right', render: (r) => formatCurrency(r.importeSinImpuesto) },
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
                items={[
                    { label: 'Finanzas', to: '/' },
                    { label: 'Publicación de recepción MIGO', to: '/finanzas/migo' },
                    { label: doc?.folio ?? 'Recepciones' },
                ]}
            />

            <div className="migo-box">
                {doc && (
                    <div className="migo-summary-card">
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
                            <div className="migo-summary-label">Fecha Publicación</div>
                            <div className="migo-summary-value">{formatDate(doc.publishedAt)}</div>
                        </div>
                    </div>
                )}

                <div className="migo-header">
                    <div>
                        <h3 className="migo-title">Órdenes de Compra y Recepciones</h3>
                        <p className="migo-description">Listado de OC y recepciones publicadas en el documento.</p>
                    </div>
                    <div className="migo-toolbar">
                        <GenericButton variant="outline" onClick={() => navigate('/finanzas/migo')}>
                            Regresar
                        </GenericButton>
                        <GenericButton variant="primary" onClick={handleExportCsv}>
                            Exportar CSV
                        </GenericButton>
                    </div>
                </div>

                <Divider />

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

                {loading && <GenericModal visible variant="loading" message="Cargando recepciones..." />}

                <GenericModal
                    visible={alertModal.visible}
                    variant="alert"
                    severity="error"
                    title="Error"
                    message={alertModal.message}
                    buttonText="Aceptar"
                    onClose={() => setAlertModal({ visible: false, message: '' })}
                />
            </div>
        </div>
    );
}
