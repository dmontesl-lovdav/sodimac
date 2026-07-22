// FILE: src/features/payments/pages/PaymentDetail.tsx
import { useState, useEffect } from 'react';
import { useNavigate, useLocation, useSearchParams, Link } from 'react-router-dom';
import { GenericButton, GenericTable, GenericModal } from '@shared/components/ui';
import { GenericMarqueeBar } from '@/shared/components/ui/progress';
import { FINANCE_HOME_PATH } from '@/shared/components/ui/navigation/financeBreadcrumb';
import { getErrorMessage } from '@/utils/errorMessage';
import { formatDate } from '@/utils/utils';
import { buildFiscalDocumentViewUrl } from '@/utils/fiscalSpaUrl';
import { resolvePaymentStatusDisplay } from '../paymentStatusDisplay';
import { StatusPill } from '@/shared/components/ui/statusPill/StatusPill';
import { paymentsService } from '../api/paymentsService';
import { PaymentRecord, PaymentDocument } from '../interfaces';
import eyeIcon from '@assets/eye-show.svg';
import downloadIconUrl from "@assets/download.svg";

import {
    FINANCE_LIST_KEYS,
    useFinanceListReturnFromDetail,
} from '@/shared/hooks';

import '../styles/PaymentDetail.css';

type ModalSeverity = 'success' | 'error' | 'warning' | 'info';

export default function PaymentDetail() {
    const navigate = useNavigate();
    const location = useLocation();
    const [searchParams] = useSearchParams();

    useFinanceListReturnFromDetail(
        FINANCE_LIST_KEYS.payments.moduleKey,
        FINANCE_LIST_KEYS.payments.listPath
    );

    const statePayment = (location.state as any)?.payment as PaymentRecord | undefined;

    const ref = searchParams.get('ref') ?? '';
    const provider = searchParams.get('provider') ?? '';
    const year = searchParams.get('year') ?? '';
    const headerUuid = searchParams.get('headerUuid') ?? '';

    const [payment, setPayment] = useState<PaymentRecord | null>(statePayment ?? null);

    const [documents, setDocuments] = useState<PaymentDocument[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string>('');

    const [docPage, setDocPage] = useState(1);
    const [docPerPage, setDocPerPage] = useState(10);
    const [docTotalPages, setDocTotalPages] = useState(1);
    const [docTotalItems, setDocTotalItems] = useState(0);

    const [useLocalPagination, setUseLocalPagination] = useState(false);

    // ✅ Modal state
    const [modalTitle, setModalTitle] = useState<string>('');
    const [modalSeverity, setModalSeverity] = useState<ModalSeverity>('error');

    useEffect(() => {
        loadDetailData(1, docPerPage);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [ref, provider, year, headerUuid]);

    const formatAmount = (amount: number): string =>
        `$${amount.toLocaleString('es-MX', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

    const handleBack = () => {
        navigate('/finanzas/pagos');
    };

    const handleExportCsv = () => {
        if (documents.length === 0) return;

        const blob = paymentsService.exportDetailCsv(documents);
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        const now = new Date();
        const pad2 = (value: number) => value.toString().padStart(2, '0');

        const fileName = `detalle_pago_${ref ?? 'pago'}_${now.getFullYear()}_${pad2(
            now.getMonth() + 1
        )}_${pad2(now.getDate())}.csv`;

        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
    };

    const handleViewDocument = (doc: PaymentDocument) => {
        const providerNum = payment?.providerNumber || provider;
        window.location.href = buildFiscalDocumentViewUrl({
            documentType: doc.documentType,
            providerNumber: providerNum,
            uuid: doc.uuid,
            serie: doc.serie,
            folio: doc.folio,
            documentNumber: doc.documentNumber,
        });
    };

    const getDocButtonLabel = (docType: string): string => {
        const type = docType?.toLowerCase() ?? '';
        if (type.includes('nota') || type.includes('credito') || type.includes('crédito')) {
            return 'Ver Nota de crédito';
        }
        return 'Ver Factura';
    };

    const mapDetailsToDocsFromHeader = (content: any[]): PaymentDocument[] => {
        return (content ?? []).map((d: any) => ({
            id: d.finanzasPaymentUuid ?? d.id ?? '',
            documentNumber: d.documentNumber ?? '',
            documentType: d.documentType ?? '',
            reference: d.documentReference ?? '',
            documentDate: d.createdAt ? formatDate(d.createdAt) : '',
            dueDate: '',
            currency: d.currency ?? 'MXN',
            amount: ((n) => (Number.isFinite(n) ? n : 0))(Number(d.amount)),
            serie: d.serie ?? d.series ?? '',
            folio: d.folio ?? '',
            uuid: d.uuid ?? d.invoiceUuid ?? d.fiscalUuid ?? '',
            sapDocument: d.sapDocument ?? '',
            paymentDate: d.paymentDate ? formatDate(d.paymentDate) : '',
            status: typeof d.status === 'number' ? String(d.status) : (d.status ?? ''),
            createdAt: d.createdAt ? formatDate(d.createdAt) : '',
            updatedAt: d.updatedAt ? formatDate(d.updatedAt) : '',
        }));
    };

    const loadDetailData = async (pageNumber: number, pageSize: number) => {
        setLoading(true);
        setError('');
        setUseLocalPagination(false);

        try {
            let resolvedPayment = (payment || statePayment) ?? null;

            if (!resolvedPayment && ref && provider) {
                const result = await paymentsService.searchPayments({
                    startDate: year ? `${year}-01-01` : '2020-01-01',
                    endDate: new Date().toISOString().split('T')[0],
                    providerId: provider,
                    page: 1,
                    size: 10000,
                });

                const found = result.items.find((item) =>
                    item.documentReference === ref &&
                    item.providerNumber === provider &&
                    (!year || item.paymentYear === year)
                );

                if (found) {
                    resolvedPayment = found;
                    setPayment(found);
                }
            }

            if (!resolvedPayment) {
                setModalSeverity('info');
                setModalTitle('Sin información');
                setError('No se encontró el pago solicitado.');
                setDocuments([]);
                setDocTotalItems(0);
                setDocTotalPages(1);
                setDocPage(1);
                return;
            }

            const resolvedHeaderUuid = (resolvedPayment.paymentHeaderUuid || headerUuid) ?? '';

            if (resolvedHeaderUuid) {
                const response = await paymentsService.getHeaderWithDetails(resolvedHeaderUuid, {
                    pageNumber,
                    pageSize,
                });

                const payload = response?.data ?? response ?? {};
                const detailsPage = payload?.detailsPage ?? null;

                const content = detailsPage?.content ?? [];
                const totalPages = detailsPage?.totalPages ?? 1;
                const totalItems = detailsPage?.totalElements ?? content.length;
                const currentPage = detailsPage?.pageNumber ?? pageNumber;

                const docs = mapDetailsToDocsFromHeader(content);

                setDocuments(docs);
                setDocTotalItems(totalItems);
                setDocTotalPages(Math.max(1, totalPages));
                setDocPage(currentPage);
                setDocPerPage(pageSize);
                return;
            }

            const detail = await paymentsService.getPaymentDetail(resolvedPayment.documentNumber);
            const docsAll = detail.documents ?? [];

            setUseLocalPagination(true);

            const totalItems = docsAll.length;
            const totalPages = Math.max(1, Math.ceil(totalItems / pageSize));
            const startIndex = (pageNumber - 1) * pageSize;
            const pageItems = docsAll.slice(startIndex, startIndex + pageSize);

            setDocuments(pageItems);
            setDocTotalItems(totalItems);
            setDocTotalPages(totalPages);
            setDocPage(pageNumber);
            setDocPerPage(pageSize);
        } catch (err: any) {
            setModalSeverity('error');
            setModalTitle('Error');
            setError(
                getErrorMessage(err, 'Error al cargar el detalle del pago.'),
            );

            setDocuments([]);
            setDocTotalItems(0);
            setDocTotalPages(1);
            setDocPage(1);
        } finally {
            setLoading(false);
        }
    };

    const handleDocPageChange = (newPage: number) => {
        loadDetailData(newPage, docPerPage);
    };

    const handleDocPerPageChange = (newPerPage: number) => {
        loadDetailData(1, newPerPage);
    };

    const documentColumns = [
        { header: 'Número Documento', render: (doc: PaymentDocument) => doc.documentNumber },
        { header: 'Referencia Documento', render: (doc: PaymentDocument) => doc.reference ?? '-' },
        { header: 'Moneda', render: (doc: PaymentDocument) => doc.currency },
        {
            header: 'Importe',
            render: (doc: PaymentDocument) => formatAmount(doc.amount),
            align: 'right' as const
        },
        { header: 'Tipo Documento', render: (doc: PaymentDocument) => doc.documentType },
        { header: 'Referencia Pago', render: (doc: PaymentDocument) => doc.sapDocument ?? '-' },
        { header: 'Fecha Pago', render: (doc: PaymentDocument) => doc.paymentDate ?? doc.documentDate ?? '-' },
        { header: 'Estatus', render: (doc: PaymentDocument) => doc.status },
        { header: 'Fecha Registro', render: (doc: PaymentDocument) => doc.createdAt ?? '-' },
        { header: 'Fecha de Actualización', render: (doc: PaymentDocument) => doc.updatedAt ?? '-' },
        {
            header: 'Factura / NC',
            render: (doc: PaymentDocument) => (
                <button
                    onClick={() => handleViewDocument(doc)}
                    className="payment-detail__view-btn"
                    aria-label={getDocButtonLabel(doc.documentType)}
                    title={getDocButtonLabel(doc.documentType)}
                    type="button"
                >
                    <img src={eyeIcon} alt="" className="payment-detail__view-icon" />
                </button>
            ),
            align: 'center' as const,
        },
    ];

    if (loading && !payment) {
        return (
            <div className="payment-detail__layout">
                <div className="payment-detail__breadcrumb">
                    <Link to={FINANCE_HOME_PATH} className="payment-detail__breadcrumb-link">Inicio</Link>
                    <span className="payment-detail__breadcrumb-sep">&gt;</span>
                    <Link to={FINANCE_HOME_PATH} className="payment-detail__breadcrumb-link">Finanzas</Link>
                    <span className="payment-detail__breadcrumb-sep">&gt;</span>
                    <Link to="/finanzas/pagos" className="payment-detail__breadcrumb-link">Pagos</Link>
                    <span className="payment-detail__breadcrumb-sep">&gt;</span>
                    <span className="payment-detail__breadcrumb-current">Detalle pago</span>
                </div>

                <div className="payment-detail__box">
                    <div className="payment-detail__loading-wrap">
                        <div className="payment-detail__loading-text">Cargando detalle del pago...</div>
                    </div>
                </div>

                <GenericModal visible={true} variant="loading" message="Cargando…" />
            </div>
        );
    }

    return (
        <div className="payment-detail__layout">
            <div className="payment-detail__breadcrumb">
                <Link to={FINANCE_HOME_PATH} className="payment-detail__breadcrumb-link">Inicio</Link>
                <span className="payment-detail__breadcrumb-sep">&gt;</span>
                <Link to={FINANCE_HOME_PATH} className="payment-detail__breadcrumb-link">Finanzas</Link>
                <span className="payment-detail__breadcrumb-sep">&gt;</span>

                <button
                    onClick={handleBack}
                    className="payment-detail__breadcrumb-btn"
                    type="button"
                >
                    Pagos
                </button>

                <span className="payment-detail__breadcrumb-sep">&gt;</span>
                <span className="payment-detail__breadcrumb-current">Detalle pago</span>
            </div>

            <div className="payment-detail__box">
                {loading && <GenericMarqueeBar />}

                <div className="payment-detail__header">
                    <div className="payment-detail__header-left">
                        <div className="payment-detail__header-icon">
                            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M14 2H6C4.9 2 4 2.9 4 4v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zm-1 7V3.5L18.5 9H13zM6 20V4h5v7h7v9H6z" fill="#003865" />
                                <path d="M8 16h8v1.5H8V16zm0-3h8v1.5H8V13z" fill="#003865" />
                            </svg>
                        </div>

                        <div>
                            <h2 className="payment-detail__title">Detalle de pago</h2>
                            <p className="payment-detail__subtitle">
                                Consulta la información detallada del pago y los documentos relacionados.
                            </p>
                        </div>
                    </div>

                    <div className="payment-detail__header-actions finz-toolbar-actions">
                        <GenericButton
                            onClick={handleExportCsv}
                            disabled={loading || documents.length === 0}
                        >
                            <span
                                style={{
                                    display: "flex",
                                    alignItems: "center",
                                    gap: 6,
                                }}
                            >
                                <span
                                    className="pay-download-ico"
                                    aria-hidden="true"
                                    style={{
                                        WebkitMaskImage: `url(${downloadIconUrl})`,
                                        maskImage: `url(${downloadIconUrl})`,
                                    }}
                                />
                                Exportar CSV
                            </span>
                        </GenericButton>
                    </div>
                </div>

                {payment && (
                    <div
                        className="payment-detail__info-card"
                        role="region"
                        aria-label="Cabecera del pago"
                    >
                        <div className="payment-detail__info-rows">
                            <div className="payment-detail__info-row payment-detail__info-row--band">
                                <div className="payment-detail__info-cell">
                                    <p className="payment-detail__info-label">
                                        Referencia de pago
                                    </p>
                                    <p className="payment-detail__info-value">
                                        {payment.documentReference ?? "—"}
                                    </p>
                                </div>
                                <div className="payment-detail__info-cell">
                                    <p className="payment-detail__info-label">Año Pago</p>
                                    <p className="payment-detail__info-value">
                                        {payment.paymentYear ?? "—"}
                                    </p>
                                </div>
                                <div className="payment-detail__info-cell">
                                    <p className="payment-detail__info-label">Moneda</p>
                                    <p className="payment-detail__info-value">
                                        {payment.currency ?? "—"}
                                    </p>
                                </div>
                                <div className="payment-detail__info-cell">
                                    <p className="payment-detail__info-label">Monto</p>
                                    <p className="payment-detail__info-value payment-detail__info-value--large">
                                        {formatAmount(payment.amount)}
                                    </p>
                                </div>
                            </div>
                            <div className="payment-detail__info-row payment-detail__info-row--band payment-detail__info-row--second">
                                <div className="payment-detail__info-cell">
                                    <p className="payment-detail__info-label">
                                        Número Proveedor
                                    </p>
                                    <p className="payment-detail__info-value">
                                        {payment.providerNumber ?? "—"}
                                    </p>
                                </div>
                                <div className="payment-detail__info-cell">
                                    <p className="payment-detail__info-label">
                                        Nombre Proveedor
                                    </p>
                                    <p className="payment-detail__info-value">
                                        {payment.providerName ?? "—"}
                                    </p>
                                </div>
                                <div className="payment-detail__info-cell">
                                    <p className="payment-detail__info-label">Estatus</p>
                                    <p className="payment-detail__info-value">
                                        {payment.statusId != null ? (
                                            <StatusPill type={resolvePaymentStatusDisplay(payment.statusId).type}>
                                                {resolvePaymentStatusDisplay(payment.statusId).label}
                                            </StatusPill>
                                        ) : (
                                            payment.status ?? "—"
                                        )}
                                    </p>
                                </div>
                                <div className="payment-detail__info-cell">
                                    <p className="payment-detail__info-label">
                                        Fecha Registro
                                    </p>
                                    <p className="payment-detail__info-value">
                                        {payment.createdAt ?? "—"}
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                <div className="payment-detail__table-section payment-detail-results">
                    <div className="payment-detail__table-head">
                        <h3 className="payment-detail__table-title">
                            Relación del pago
                            {useLocalPagination ? ' (sin cabecera)' : ''}
                        </h3>
                    </div>

                    <div className="payment-detail__table-wrap">
                        <GenericTable<PaymentDocument>
                            rows={documents}
                            columns={documentColumns}
                            actions={[]}
                            emptyLabel={loading ? 'Cargando documentos...' : 'No hay documentos relacionados'}
                            perPage={docPerPage}
                            page={docPage}
                            totalPages={docTotalPages}
                            totalItems={docTotalItems}
                            onChangePerPage={handleDocPerPageChange}
                            onChangePage={handleDocPageChange}
                        />
                    </div>
                </div>

                <div className="payment-detail__footer-actions finz-page-actions">
                    <GenericButton variant="back" type="button" onClick={handleBack}>
                        Volver
                    </GenericButton>
                </div>
            </div>

            {/* Errors/Info via GenericModal */}
            <GenericModal
                visible={!!error}
                variant="alert"
                title={modalTitle ?? 'Aviso'}
                severity={modalSeverity}
                message={error}
                buttonText="Aceptar"
                onClose={() => setError('')}
            />

            {/* Loading via GenericModal */}
            {loading && (
                <GenericModal visible variant="loading" message="Cargando…" />
            )}
        </div>
    );
}