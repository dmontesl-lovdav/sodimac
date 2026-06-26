import { ReactElement, useState, useCallback, useMemo, useRef, useEffect } from 'react';
import { useNavigate, useLocation, useSearchParams } from 'react-router-dom';
import { Breadcrumb, GenericModal, GenericButton } from '@shared/components/ui';
import { withFinanceBreadcrumb } from '@shared/components/ui/navigation/financeBreadcrumb';
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
import { APP_EVENT, PermissionGate, useSecurityContext } from '@shared/security';

import './styles/MigoContainer.css';
import { useFinanceAlertModal } from '@/shared/hooks/useFinanceAlertModal';
import {
    useFinanceListScreenSession,
    useFinanceListRefetchOnReturn,
    FINANCE_LIST_KEYS,
    readFinanceListFilters,
    saveFinanceListFilters,
    financeListTodayDateRange,
    formatFinanceListLocalDate,
    parseFinanceListDateRange,
    useFinanceListDefaultsOnUrlReset,
} from '@/shared/hooks';
import { formatDate } from '@/utils/utils';

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

function formatCurrency(val: number | undefined | null): string {
    if (val == null || Number.isNaN(Number(val))) return '-';
    return Number(val).toLocaleString('es-MX', { minimumFractionDigits: 2 });
}

export default function MigoContainer(): ReactElement {
    const navigate = useNavigate();
    const location = useLocation();
    const financeAlert = useFinanceAlertModal();
    const notifyIfEmptySearch = useRef(false);
    const [searchParams] = useSearchParams();
    const { hasEvent } = useSecurityContext();

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

    const returningFromDetail = useFinanceListScreenSession(FINANCE_LIST_KEYS.migo);

    const applyFilterDefaults = useCallback(() => {
        setDateRange(financeListTodayDateRange());
        setFileNameFilter('');
        setRows([]);
        setSearchApplied(false);
        setTotalItems(0);
        setTotalPages(1);
        setPage(1);
    }, []);

    useFinanceListDefaultsOnUrlReset(
        FINANCE_LIST_KEYS.migo.moduleKey,
        applyFilterDefaults
    );

    const [dateRange, setDateRange] = useState<DateRange>(() => {
        const saved = readFinanceListFilters<{
            publishedAtStart?: string;
            publishedAtEnd?: string;
        }>(FINANCE_LIST_KEYS.migo.filters);
        if (saved?.publishedAtStart && saved?.publishedAtEnd) {
            const [start, end] = parseFinanceListDateRange(
                saved.publishedAtStart,
                saved.publishedAtEnd
            );
            if (start && end) return [start, end];
        }
        return financeListTodayDateRange();
    });
    const [fileNameFilter, setFileNameFilter] = useState('');

    const [rejectModalOpen, setRejectModalOpen] = useState(false);
    const [rejectTargetId, setRejectTargetId] = useState<string | null>(null);
    const [rejectReason, setRejectReason] = useState('');
    const [actionLoading, setActionLoading] = useState(false);

    const [authorizeTarget, setAuthorizeTarget] = useState<MigoDocument | null>(null);
    const [authorizeModalOpen, setAuthorizeModalOpen] = useState(false);

    const toIsoStartOfDay = (d: Date): string =>
        new Date(d.getFullYear(), d.getMonth(), d.getDate(), 0, 0, 0, 0).toISOString();
    const toIsoEndOfDay = (d: Date): string =>
        new Date(d.getFullYear(), d.getMonth(), d.getDate(), 23, 59, 59, 999).toISOString();

    const buildFilters = useCallback((p: number, ps: number): MigoSearchFilters => ({
        publishedAtStart: dateRange[0] ? toIsoStartOfDay(dateRange[0]) : '',
        publishedAtEnd: dateRange[1] ? toIsoEndOfDay(dateRange[1]) : '',
        fileName: fileNameFilter || undefined,
        pageNumber: p,
        pageSize: ps,
    }), [dateRange, fileNameFilter]);

    const fetchData = useCallback(async (p: number, ps: number) => {
        setLoading(true);
        try {
            const res: any = await migoService.search(buildFilters(p, ps));
            const pageData = res?.data ?? res;
            const content = pageData?.content ?? [];
            setRows(content);
            setTotalItems(pageData?.totalElements ?? 0);
            setTotalPages(pageData?.totalPages ?? 1);
            if (notifyIfEmptySearch.current && content.length === 0) {
                financeAlert.showWarning(
                    'Sin registros',
                    'No se encontraron documentos MIGO con los criterios indicados.',
                );
                notifyIfEmptySearch.current = false;
            }
        } catch (err) {
            notifyIfEmptySearch.current = false;
            setRows([]);
            financeAlert.showErrorFrom(
                'Error',
                err,
                'Ocurrió un error al conectar con el servidor para obtener los documentos MIGO.',
            );
        } finally {
            setLoading(false);
        }
    }, [buildFilters]);

    const handleSearch = () => {
        if (!dateRange[0] || !dateRange[1]) {
            financeAlert.showWarning(
                'Fechas requeridas',
                'Los filtros de fecha inicio y fecha final de publicación son obligatorios.',
            );
            return;
        }
        notifyIfEmptySearch.current = true;
        setPage(1);
        setSearchApplied(true);
        saveFinanceListFilters(FINANCE_LIST_KEYS.migo.filters, buildFilters(1, perPage));
        fetchData(1, perPage);
    };

    useEffect(() => {
        if (!locationState?.autoSearchAfterUpload) return;
        if (!dateRange[0] || !dateRange[1]) return;
        notifyIfEmptySearch.current = false;
        setPage(1);
        setSearchApplied(true);
        saveFinanceListFilters(FINANCE_LIST_KEYS.migo.filters, buildFilters(1, perPage));
        fetchData(1, perPage);
        navigate(location.pathname, { replace: true, state: null });
    }, [locationState?.autoSearchAfterUpload]);

    useFinanceListRefetchOnReturn<MigoSearchFilters>(
        FINANCE_LIST_KEYS.migo,
        returningFromDetail,
        async (saved) => {
            const p = Math.max(1, Number(saved.pageNumber ?? 1));
            const ps = Math.max(1, Number(saved.pageSize ?? perPage));
            notifyIfEmptySearch.current = false;
            setPage(p);
            setPerPage(ps);
            setSearchApplied(true);
            setLoading(true);
            try {
                const res: any = await migoService.search({
                    ...saved,
                    pageNumber: p,
                    pageSize: ps,
                });
                const pageData = res?.data ?? res;
                const content = pageData?.content ?? [];
                setRows(content);
                setTotalItems(pageData?.totalElements ?? 0);
                setTotalPages(pageData?.totalPages ?? 1);
            } catch (err) {
                setRows([]);
                financeAlert.showErrorFrom(
                    'Error',
                    err,
                    'Ocurrió un error al conectar con el servidor para obtener los documentos MIGO.',
                );
            } finally {
                setLoading(false);
            }
        }
    );

    const handleClear = () => {
        notifyIfEmptySearch.current = false;
        setDateRange(financeListTodayDateRange());
        setFileNameFilter('');
        setRows([]);
        setSearchApplied(false);
        setTotalItems(0);
        setTotalPages(1);
        setPage(1);
    };

    const handlePageChange = (newPage: number) => {
        if (!searchApplied) return;
        setPage(newPage);
        fetchData(newPage, perPage);
    };

    const handlePerPageChange = (newPerPage: number) => {
        if (!searchApplied) return;
        setPerPage(newPerPage);
        setPage(1);
        fetchData(1, newPerPage);
    };

    const openAuthorizeConfirm = (doc: MigoDocument) => {
        setAuthorizeTarget(doc);
        setAuthorizeModalOpen(true);
    };

    const handleAuthorizeConfirm = async () => {
        if (!authorizeTarget) return;
        const doc = authorizeTarget;
        setAuthorizeModalOpen(false);
        setActionLoading(true);
        try {
            await migoService.authorize(doc.migoDocumentId);
            financeAlert.showSuccess(
                'Operación exitosa',
                `Las recepciones del documento ${doc.folio} fueron publicadas correctamente. Los proveedores ya pueden relacionar sus facturas.`,
            );
            fetchData(page, perPage);
        } catch (err) {
            financeAlert.showErrorFrom(
                'Error al autorizar',
                err,
                'Error al autorizar el documento.',
            );
        } finally {
            setActionLoading(false);
            setAuthorizeTarget(null);
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
            financeAlert.showSuccess(
                'Operación exitosa',
                'Documento rechazado exitosamente. Estatus: 9 → 8.',
            );
            fetchData(page, perPage);
        } catch (err) {
            financeAlert.showErrorFrom(
                'Error al rechazar',
                err,
                'Error al rechazar el documento.',
            );
        } finally {
            setActionLoading(false);
        }
    };

    const handleExportCsv = async (doc: MigoDocument) => {
        try {
            await migoService.exportCsv(doc.migoDocumentId);
        } catch (err) {
            financeAlert.showErrorFrom(
                'Error',
                err,
                'Error al exportar las recepciones.',
            );
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

    const rowActionDescriptors: { gate: { app: string; event: string }; action: RowAction<MigoDocument> }[] = [
        {
            gate: APP_EVENT.MIGO.VIEW_DETAIL,
            action: {
                title: 'Ver Recepciones',
                icon: eyeIcon,
                onClick: (row) => navigate(`/finanzas/migo/${row.migoDocumentId}/recepciones`),
            },
        },
        {
            gate: APP_EVENT.MIGO.AUTHORIZE,
            action: {
                title: 'Autorizar',
                icon: editIcon,
                onClick: (row) => openAuthorizeConfirm(row),
                isDisabled: (row) => row.status !== 9 || actionLoading,
            },
        },
        {
            gate: APP_EVENT.MIGO.REJECT,
            action: {
                title: 'Rechazar',
                icon: deleteIcon,
                onClick: (row) => openRejectModal(row),
                isDisabled: (row) => row.status !== 9 || actionLoading,
            },
        },
        {
            gate: APP_EVENT.MIGO.DOWNLOAD_CSV,
            action: {
                title: 'Exportar CSV',
                icon: csvIcon,
                onClick: (row) => handleExportCsv(row),
            },
        },
    ];
    const rowActions: RowAction<MigoDocument>[] = rowActionDescriptors
        .filter(({ gate }) => hasEvent(gate.app, gate.event))
        .map(({ action }) => action);

    return (
        <div className="migo-layout">
            <Breadcrumb
                items={withFinanceBreadcrumb([{ label: 'Publicación de recepción MIGO' }])}
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
                        <PermissionGate appEvent={APP_EVENT.MIGO.PUBLISH}>
                            <GenericButton
                                variant="primary"
                                onClick={handleGoToPublish}
                            >
                                {paymentContext ? 'Publicar complemento' : 'Publicar OC'}
                            </GenericButton>
                        </PermissionGate>
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
                    <div className="migo-filters-row">
                        <div className="migo-date-filter">
                            <GenericDateRangePicker
                                value={dateRange}
                                onChange={setDateRange}
                                size="sm"
                            />
                        </div>

                        <div className="migo-filename-actions">
                            <input
                                value={fileNameFilter}
                                onChange={(e) => setFileNameFilter(e.target.value)}
                                placeholder="Nombre de archivo"
                                className="migo-filter-input"
                            />

                            <PermissionGate appEvent={APP_EVENT.MIGO.SEARCH}>
                                <GenericButton variant="outline" onClick={handleSearch}>
                                    Buscar
                                </GenericButton>
                            </PermissionGate>

                            <PermissionGate appEvent={APP_EVENT.MIGO.CLEAR_FILTERS}>
                                <GenericButton variant="outline" onClick={handleClear}>
                                    Limpiar
                                </GenericButton>
                            </PermissionGate>
                        </div>
                    </div>
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

                <GenericModal
                    visible={authorizeModalOpen}
                    variant="confirm"
                    severity="info"
                    title="Publicar recepciones MIGO"
                    message={
                        authorizeTarget
                            ? `¿Desea publicar las recepciones del documento "${authorizeTarget.folio}" para que los proveedores puedan relacionar sus facturas?`
                            : '¿Desea publicar las recepciones para que los proveedores puedan relacionar sus facturas?'
                    }
                    confirmText={actionLoading ? 'Procesando...' : 'Publicar'}
                    cancelText="Cancelar"
                    onConfirm={handleAuthorizeConfirm}
                    onCancel={() => {
                        if (!actionLoading) {
                            setAuthorizeModalOpen(false);
                            setAuthorizeTarget(null);
                        }
                    }}
                />

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