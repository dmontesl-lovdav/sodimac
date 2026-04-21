import { ReactElement, useState, useCallback, useMemo } from 'react';
import { useNavigate, useLocation, useSearchParams } from 'react-router-dom';
import { Breadcrumb, GenericModal, GenericButton } from '@shared/components/ui';
import GenericDateRangePicker from '@/shared/components/ui/date/GenericDateRangePicker';
import GenericTable from '@/shared/components/ui/table/GenericTable';
import type { Column, RowAction } from '@/shared/components/ui/table/GenericTable';
import { StatusPill } from '@/shared/components/ui/statusPill/StatusPill';

import eyeIcon from '@assets/eye-show.svg';
import csvIcon from '@assets/csv.svg';
import editIcon from '@assets/edit.svg';
import deleteIcon from '@assets/delete.svg';

import { migoService } from './api/MigoClient';
import type { MigoDocument, MigoSearchFilters } from './interfaces';
import { MIGO_STATUS_MAP } from './interfaces';

import './styles/MigoContainer.css';

type DateRange = [Date | null, Date | null];

interface PaymentNavigationContext {
    documentReference: string;
    providerNumber: string;
    paymentYear: string;
    currency: string;
    amount: number | null;
    paymentDate: string;
}

function statusPillType(status: number): string {
    if (status === 0) return 'success';
    if (status === 8) return 'error';
    return 'info';
}

function formatDate(d?: string): string {
    if (!d) return '-';

    const directMatch = d.match(/^(\d{1,2})([/-])(\d{1,2})\2(\d{4})$/);
    if (directMatch) {
        const day = directMatch[1].padStart(2, '0');
        const separator = directMatch[2];
        const month = directMatch[3].padStart(2, '0');
        const year = directMatch[4];
        return `${day}${separator}${month}${separator}${year}`;
    }

    const parsed = new Date(d);
    if (Number.isNaN(parsed.getTime())) return d;

    return parsed.toLocaleDateString('es-MX', {
        day: '2-digit', month: '2-digit', year: 'numeric',
    });
}

function formatCurrency(val: number | undefined | null): string {
    if (val == null || Number.isNaN(Number(val))) return '-';
    return Number(val).toLocaleString('es-MX', { minimumFractionDigits: 2 });
}

export default function MigoContainer(): ReactElement {
    const navigate = useNavigate();
    const location = useLocation();
    const [searchParams] = useSearchParams();

    const locationState = (location.state as any) || {};
    const paymentFromState = locationState?.payment;

    const paymentContext = useMemo<PaymentNavigationContext | null>(() => {
        const ref = paymentFromState?.documentReference || searchParams.get('ref') || '';
        const provider = paymentFromState?.providerNumber || searchParams.get('provider') || '';
        const year = paymentFromState?.paymentYear || searchParams.get('year') || '';
        const currency = paymentFromState?.currency || searchParams.get('currency') || '';
        const rawAmount =
            paymentFromState?.amount != null
                ? Number(paymentFromState.amount)
                : searchParams.get('amount') != null
                    ? Number(searchParams.get('amount'))
                    : null;
        const paymentDate = paymentFromState?.paymentDate || searchParams.get('paymentDate') || '';

        const hasAnyValue = !!(ref || provider || year || currency || paymentDate || rawAmount != null);

        if (!hasAnyValue) return null;

        return {
            documentReference: ref,
            providerNumber: provider,
            paymentYear: year,
            currency,
            amount: rawAmount != null && !Number.isNaN(rawAmount) ? rawAmount : null,
            paymentDate,
        };
    }, [paymentFromState, searchParams]);

    const paymentPublishQuery = useMemo(() => {
        if (!paymentContext) return '';

        const params = new URLSearchParams({
            ref: paymentContext.documentReference || '',
            provider: paymentContext.providerNumber || '',
            year: paymentContext.paymentYear || '',
            currency: paymentContext.currency || '',
            amount: paymentContext.amount != null ? String(paymentContext.amount) : '',
            paymentDate: paymentContext.paymentDate || '',
        });

        return params.toString();
    }, [paymentContext]);

    const [loading, setLoading] = useState(false);
    const [rows, setRows] = useState<MigoDocument[]>([]);
    const [page, setPage] = useState(1);
    const [perPage, setPerPage] = useState(10);
    const [totalPages, setTotalPages] = useState(1);
    const [totalItems, setTotalItems] = useState(0);
    const [searchApplied, setSearchApplied] = useState(false);

    const [dateRange, setDateRange] = useState<DateRange>([null, null]);
    const [fileNameFilter, setFileNameFilter] = useState('');

    const [rejectModalOpen, setRejectModalOpen] = useState(false);
    const [rejectTargetId, setRejectTargetId] = useState<string | null>(null);
    const [rejectReason, setRejectReason] = useState('');
    const [actionLoading, setActionLoading] = useState(false);

    const [alertModal, setAlertModal] = useState({ visible: false, message: '' });

    const buildFilters = useCallback((p: number, ps: number): MigoSearchFilters => ({
        publishedAtStart: dateRange[0]?.toISOString().split('T')[0] ?? '',
        publishedAtEnd: dateRange[1]?.toISOString().split('T')[0] ?? '',
        fileName: fileNameFilter || undefined,
        pageNumber: p,
        pageSize: ps,
    }), [dateRange, fileNameFilter]);

    const fetchData = useCallback(async (p: number, ps: number) => {
        setLoading(true);
        try {
            const res: any = await migoService.search(buildFilters(p, ps));
            const pageData = res?.data ?? res;
            setRows(pageData?.content ?? []);
            setTotalItems(pageData?.totalElements ?? 0);
            setTotalPages(pageData?.totalPages ?? 1);
        } catch (err) {
            console.error('[MIGO] search error', err);
            setRows([]);
            setAlertModal({ visible: true, message: 'Ocurrió un error al conectar con el servidor para obtener los documentos MIGO.' });
        } finally {
            setLoading(false);
        }
    }, [buildFilters]);

    const handleSearch = () => {
        if (!dateRange[0] || !dateRange[1]) {
            setAlertModal({ visible: true, message: 'Los filtros de fecha inicio y fecha final de publicación son obligatorios.' });
            return;
        }
        setPage(1);
        setSearchApplied(true);
        fetchData(1, perPage);
    };

    const handleClear = () => {
        setDateRange([null, null]);
        setFileNameFilter('');
        setRows([]);
        setSearchApplied(false);
        setTotalItems(0);
        setTotalPages(1);
        setPage(1);
    };

    const handlePageChange = (newPage: number) => {
        setPage(newPage);
        fetchData(newPage, perPage);
    };

    const handlePerPageChange = (newPerPage: number) => {
        setPerPage(newPerPage);
        setPage(1);
        fetchData(1, newPerPage);
    };

    const handleAuthorize = async (doc: MigoDocument) => {
        setActionLoading(true);
        try {
            await migoService.authorize(doc.migoDocumentId);
            setAlertModal({ visible: true, message: `El documento ${doc.folio} fue autorizado exitosamente. Estatus: 9 → 0.` });
            fetchData(page, perPage);
        } catch (err) {
            console.error('[MIGO] authorize error', err);
            setAlertModal({ visible: true, message: 'Error al autorizar el documento.' });
        } finally {
            setActionLoading(false);
        }
    };

    const openRejectModal = (doc: MigoDocument) => {
        setRejectTargetId(doc.migoDocumentId);
        setRejectReason('');
        setRejectModalOpen(true);
    };

    const handleRejectConfirm = async () => {
        if (!rejectTargetId || !rejectReason.trim()) return;
        setActionLoading(true);
        try {
            await migoService.reject(rejectTargetId, rejectReason.trim());
            setRejectModalOpen(false);
            setAlertModal({ visible: true, message: 'Documento rechazado exitosamente. Estatus: 9 → 8.' });
            fetchData(page, perPage);
        } catch (err) {
            console.error('[MIGO] reject error', err);
            setAlertModal({ visible: true, message: 'Error al rechazar el documento.' });
        } finally {
            setActionLoading(false);
        }
    };

    const handleExportCsv = async (doc: MigoDocument) => {
        try {
            await migoService.exportCsv(doc.migoDocumentId);
        } catch (err) {
            console.error('[MIGO] export error', err);
            setAlertModal({ visible: true, message: 'Error al exportar las recepciones.' });
        }
    };

    const handleGoToPublish = () => {
        const targetPath = paymentPublishQuery
            ? `/finanzas/migo/publicar?${paymentPublishQuery}`
            : '/finanzas/migo/publicar';

        navigate(targetPath, {
            state: {
                ...locationState,
                payment: paymentFromState ?? paymentContext,
                paymentContext: paymentContext,
                source: 'payments',
            },
        });
    };

    const columns: Column<MigoDocument>[] = [
        { header: 'Id Documento', render: (row) => row.folio },
        { header: 'Nombre Documento', render: (row) => row.fileName },
        { header: 'Número OC', align: 'center', render: (row) => row.numeroOc },
        { header: 'Núm. Recepción', align: 'center', render: (row) => row.numeroRecepcion },
        { header: 'Monto OC', align: 'right', render: (row) => formatCurrency(row.montoOc) },
        { header: 'Núm. OC Rechazadas', align: 'center', render: (row) => row.numeroRechazoOc },
        { header: 'Id Usuario Pub.', align: 'center', render: (row) => row.createdBy ?? '-' },
        { header: 'Fecha Publicación', render: (row) => formatDate(row.publishedAt) },
        { header: 'Fecha Flujo', render: (row) => formatDate(row.fechaFlujo) },
        {
            header: 'Estatus',
            align: 'center',
            render: (row) => (
                <StatusPill type={statusPillType(row.status)}>
                    {MIGO_STATUS_MAP[row.status]?.label ?? `${row.status}`}
                </StatusPill>
            ),
        },
    ];

    const rowActions: RowAction<MigoDocument>[] = [
        {
            title: 'Ver Recepciones',
            icon: eyeIcon,
            onClick: (row) => navigate(`/finanzas/migo/${row.migoDocumentId}/recepciones`),
        },
        {
            title: 'Autorizar',
            icon: editIcon,
            onClick: (row) => handleAuthorize(row),
            isDisabled: (row) => row.status !== 9 || actionLoading,
        },
        {
            title: 'Rechazar',
            icon: deleteIcon,
            onClick: (row) => openRejectModal(row),
            isDisabled: (row) => row.status !== 9 || actionLoading,
        },
        {
            title: 'Exportar CSV',
            icon: csvIcon,
            onClick: (row) => handleExportCsv(row),
        },
    ];

    return (
        <div className="migo-layout">
            <Breadcrumb
                items={[
                    { label: 'Finanzas', to: '/' },
                    { label: 'Publicación de recepción MIGO' },
                ]}
            />

            <div className="migo-box">
                <div className="migo-header">
                    <div>
                        <h3 className="migo-title">Publicación de Recepciones MIGO</h3>
                        <p className="migo-description">
                            Consultar, publicar, autorizar y rechazar recepciones de mercancía de proveedores indirectos.
                        </p>
                    </div>
                    <div className="migo-toolbar">
                        <GenericButton
                            variant="primary"
                            onClick={handleGoToPublish}
                        >
                            {paymentContext ? 'Publicar complemento' : 'Publicar OC'}
                        </GenericButton>
                    </div>
                </div>

                {paymentContext && (
                    <div className="migo-summary-card" style={{ marginBottom: 16 }}>
                        <div>
                            <div className="migo-summary-label">Referencia de pago</div>
                            <div className="migo-summary-value">{paymentContext.documentReference || '-'}</div>
                        </div>
                        <div>
                            <div className="migo-summary-label">Id Proveedor</div>
                            <div className="migo-summary-value">{paymentContext.providerNumber || '-'}</div>
                        </div>
                        <div>
                            <div className="migo-summary-label">Año de pago</div>
                            <div className="migo-summary-value">{paymentContext.paymentYear || '-'}</div>
                        </div>
                        <div>
                            <div className="migo-summary-label">Moneda</div>
                            <div className="migo-summary-value">{paymentContext.currency || '-'}</div>
                        </div>
                        <div>
                            <div className="migo-summary-label">Importe</div>
                            <div className="migo-summary-value">{formatCurrency(paymentContext.amount)}</div>
                        </div>
                        <div>
                            <div className="migo-summary-label">Fecha de pago</div>
                            <div className="migo-summary-value">{formatDate(paymentContext.paymentDate)}</div>
                        </div>
                    </div>
                )}

                <div className="migo-filters-section">
                    <GenericDateRangePicker
                        value={dateRange}
                        onChange={setDateRange}
                        placeholder="Fecha inicio – Fecha final *"
                        size="sm"
                    />
                    <input
                        type="text"
                        placeholder="Nombre de archivo"
                        value={fileNameFilter}
                        onChange={(e) => setFileNameFilter(e.target.value)}
                        className="migo-filter-input"
                    />
                    <GenericButton variant="primary" onClick={handleSearch} disabled={loading}>
                        Buscar
                    </GenericButton>
                    <GenericButton variant="outline" onClick={handleClear}>
                        Limpiar
                    </GenericButton>
                </div>

                <div className="migo-grid-section">
                    {!searchApplied && !loading && rows.length === 0 ? null : (
                        <GenericTable<MigoDocument>
                            rows={rows}
                            columns={columns}
                            actions={rowActions}
                            emptyLabel={loading ? 'Cargando...' : 'No se encontraron documentos MIGO.'}
                            perPage={perPage}
                            page={page}
                            totalPages={totalPages}
                            totalItems={totalItems}
                            onChangePage={handlePageChange}
                            onChangePerPage={handlePerPageChange}
                        />
                    )}
                </div>

                {loading && <GenericModal visible variant="loading" message="Cargando documentos MIGO..." />}

                {rejectModalOpen && (
                    <div className="gm-overlay">
                        <div className="gm-box gm-content" style={{ minWidth: 420 }}>
                            <h3 className="gm-title">Rechazar Documento MIGO</h3>
                            <div style={{ textAlign: 'left', marginTop: 12 }}>
                                <label style={{ display: 'block', marginBottom: 6, fontWeight: 500 }}>
                                    Motivo de rechazo
                                </label>
                                <textarea
                                    value={rejectReason}
                                    onChange={(e) => setRejectReason(e.target.value)}
                                    placeholder="Ingrese el motivo del rechazo..."
                                    maxLength={500}
                                    disabled={actionLoading}
                                    style={{
                                        width: '100%',
                                        minHeight: 80,
                                        padding: 8,
                                        border: '1px solid #d1d5db',
                                        borderRadius: 6,
                                        fontSize: 13,
                                        resize: 'vertical',
                                    }}
                                />
                            </div>
                            <div className="gm-actions" style={{ marginTop: 16 }}>
                                <button
                                    className="gm-btn gm-btn-cancel"
                                    onClick={() => !actionLoading && setRejectModalOpen(false)}
                                >
                                    Cancelar
                                </button>
                                <button
                                    className="gm-btn gm-btn-confirm"
                                    onClick={handleRejectConfirm}
                                    disabled={!rejectReason.trim() || actionLoading}
                                >
                                    {actionLoading ? 'Procesando...' : 'Rechazar'}
                                </button>
                            </div>
                        </div>
                    </div>
                )}

                <GenericModal
                    visible={alertModal.visible}
                    variant="alert"
                    severity="info"
                    title="Información"
                    message={alertModal.message}
                    buttonText="Aceptar"
                    onClose={() => setAlertModal({ visible: false, message: '' })}
                />
            </div>
        </div>
    );
}