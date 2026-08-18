// FILE: src/features/payments/pages/PaymentDetail.tsx

import { useEffect, useState } from "react";
import {
    Link,
    useLocation,
    useNavigate,
    useSearchParams,
} from "react-router-dom";

import {
    GenericButton,
    GenericModal,
    GenericTable,
} from "@shared/components/ui";
import { GenericMarqueeBar } from "@/shared/components/ui/progress";
import { FINANCE_HOME_PATH } from "@/shared/components/ui/navigation/financeBreadcrumb";
import { StatusPill } from "@/shared/components/ui/statusPill/StatusPill";

import { getErrorMessage } from "@/utils/errorMessage";
import { formatDate, fetchProviders } from "@/utils/utils";
import { buildFiscalDocumentViewUrl } from "@/utils/fiscalSpaUrl";

import {
    paymentsService,
    sortPaymentDocuments,
} from "../api/paymentsService";
import {
    PaymentDocument,
    PaymentRecord,
} from "../interfaces";
import { resolvePaymentStatusDisplay } from "../paymentStatusDisplay";

import eyeIcon from "@assets/eye-show.svg";
import downloadIconUrl from "@assets/download.svg";

import {
    FINANCE_LIST_KEYS,
    useFinanceListReturnFromDetail,
} from "@/shared/hooks";

import "../styles/PaymentDetail.css";

type ModalSeverity =
    | "success"
    | "error"
    | "warning"
    | "info";

export default function PaymentDetail() {
    const navigate = useNavigate();
    const location = useLocation();
    const [searchParams] = useSearchParams();

    useFinanceListReturnFromDetail(
        FINANCE_LIST_KEYS.payments.moduleKey,
        FINANCE_LIST_KEYS.payments.listPath
    );

    const statePayment = (location.state as any)
        ?.payment as PaymentRecord | undefined;

    const ref = searchParams.get("ref") ?? "";
    const provider =
        searchParams.get("provider") ?? "";
    const year = searchParams.get("year") ?? "";
    const headerUuid =
        searchParams.get("headerUuid") ?? "";

    const [payment, setPayment] =
        useState<PaymentRecord | null>(
            statePayment ?? null
        );

    /**
     * allDocuments conserva el resultado completo y ordenado.
     * documents contiene únicamente la página visible.
     */
    const [allDocuments, setAllDocuments] = useState<
        PaymentDocument[]
    >([]);
    const [documents, setDocuments] = useState<
        PaymentDocument[]
    >([]);

    const [loading, setLoading] = useState(true);
    const [error, setError] =
        useState<string>("");

    const [docPage, setDocPage] = useState(1);
    const [docPerPage, setDocPerPage] =
        useState(10);
    const [docTotalPages, setDocTotalPages] =
        useState(1);
    const [docTotalItems, setDocTotalItems] =
        useState(0);

    const [useLocalPagination, setUseLocalPagination] =
        useState(false);

    const [modalTitle, setModalTitle] =
        useState<string>("");
    const [modalSeverity, setModalSeverity] =
        useState<ModalSeverity>("error");

    useEffect(() => {
        loadDetailData();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [ref, provider, year, headerUuid]);

    const formatAmount = (
        amount: number
    ): string =>
        `$${amount.toLocaleString("es-MX", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
        })}`;

    const handleBack = () => {
        navigate("/finanzas/pagos");
    };

    const applyDocumentPage = (
        source: PaymentDocument[],
        pageNumber: number,
        pageSize: number
    ) => {
        const totalItems = source.length;
        const totalPages = Math.max(
            1,
            Math.ceil(totalItems / pageSize)
        );
        const safePage = Math.min(
            Math.max(pageNumber, 1),
            totalPages
        );
        const startIndex =
            (safePage - 1) * pageSize;

        setDocuments(
            source.slice(
                startIndex,
                startIndex + pageSize
            )
        );
        setDocTotalItems(totalItems);
        setDocTotalPages(totalPages);
        setDocPage(safePage);
        setDocPerPage(pageSize);
    };

    const handleExportCsv = () => {
        if (allDocuments.length === 0) {
            return;
        }

        const blob =
            paymentsService.exportDetailCsv(
                allDocuments,
                payment?.providerName ?? "",
                payment?.providerNumber ?? provider,
                payment?.documentReference ?? ref
            );

        const url =
            window.URL.createObjectURL(blob);
        const anchor =
            document.createElement("a");
        const now = new Date();
        const pad2 = (value: number) =>
            value.toString().padStart(2, "0");

        const fileName =
            `detalle_pago_${ref || "pago"}_${now.getFullYear()}_${pad2(
                now.getMonth() + 1
            )}_${pad2(now.getDate())}.csv`;

        anchor.href = url;
        anchor.download = fileName;

        document.body.appendChild(anchor);
        anchor.click();
        document.body.removeChild(anchor);

        window.URL.revokeObjectURL(url);
    };

    const handleViewDocument = (
        document: PaymentDocument
    ) => {
        const providerNumber =
            payment?.providerNumber || provider;

        window.location.href =
            buildFiscalDocumentViewUrl({
                documentType:
                    document.documentType,
                providerNumber,
                uuid: document.uuid,
                serie: document.serie,
                folio: document.folio,
                documentNumber:
                    document.documentNumber,
            });
    };

    const getDocButtonLabel = (
        documentType: string
    ): string => {
        const normalized =
            documentType?.toLowerCase() ?? "";

        if (
            normalized === "nc" ||
            normalized.includes("nota") ||
            normalized.includes("credito") ||
            normalized.includes("crédito")
        ) {
            return "Ver Nota de crédito";
        }

        return "Ver Factura";
    };

    /**
     * finanzasPaymentUuid identifica internamente el detalle del pago.
     * uuid/fiscalUuid/invoiceUuid representa el UUID fiscal de la factura/NC.
     * No se debe usar finanzasPaymentUuid como UUID fiscal.
     */
    const mapDetailsToDocsFromHeader = (
        content: any[]
    ): PaymentDocument[] =>
        (content ?? []).map((detail: any) => {
            const detailUuid =
                detail.finanzasPaymentUuid ??
                detail.id ??
                "";

            return {
                id: detailUuid,
                finanzasPaymentUuid:
                    detailUuid || undefined,
                documentNumber:
                    detail.documentNumber ?? "",
                documentType:
                    detail.documentType ?? "",
                reference:
                    detail.documentReference ?? "",
                documentDate: detail.createdAt
                    ? formatDate(detail.createdAt)
                    : "",
                dueDate: "",
                currency:
                    detail.currency ?? "MXN",
                amount: ((numberValue) =>
                    Number.isFinite(numberValue)
                        ? numberValue
                        : 0)(Number(detail.amount)),
                serie:
                    detail.serie ??
                    detail.series ??
                    "",
                folio: detail.folio ?? "",
                uuid:
                    detail.uuid ??
                    detail.fiscalUuid ??
                    detail.invoiceUuid ??
                    "",
                sapDocument:
                    detail.sapDocument ?? "",
                paymentDate: detail.paymentDate
                    ? formatDate(detail.paymentDate)
                    : "",
                status:
                    typeof detail.status ===
                        "number"
                        ? String(detail.status)
                        : detail.status ?? "",
                createdAt: detail.createdAt
                    ? formatDate(detail.createdAt)
                    : "",
                updatedAt: detail.updatedAt
                    ? formatDate(detail.updatedAt)
                    : "",
            };
        });

    /**
     * El listado principal resuelve visualmente el proveedor con
     * catálogo, pero el PaymentRecord puede llegar sin providerName.
     * Aquí se completa únicamente cuando hace falta.
     */
    const resolveProviderName = async (
        currentPayment: PaymentRecord
    ): Promise<PaymentRecord> => {
        if (currentPayment.providerName) {
            return currentPayment;
        }

        try {
            const providerList =
                await fetchProviders();

            const foundProvider = (
                providerList ?? []
            ).find(
                (item: any) =>
                    String(item.supplierNumber) ===
                    String(
                        currentPayment.providerNumber
                    )
            );

            const providerName =
                foundProvider?.businessName ??
                foundProvider?.supplierName ??
                foundProvider?.name ??
                "";

            if (!providerName) {
                return currentPayment;
            }

            return {
                ...currentPayment,
                providerName,
            };
        } catch (providerError) {
            console.error(
                "[PaymentDetail] Error resolving provider name:",
                providerError
            );

            return currentPayment;
        }
    };

    const loadDetailData = async () => {
        setLoading(true);
        setError("");
        setUseLocalPagination(false);

        try {
            let resolvedPayment =
                (payment || statePayment) ?? null;

            if (
                !resolvedPayment &&
                ref &&
                provider
            ) {
                const result =
                    await paymentsService.searchAllPayments(
                        {
                            startDate: year
                                ? `${year}-01-01`
                                : "2020-01-01",
                            endDate: new Date()
                                .toISOString()
                                .split("T")[0],
                            providerId: provider,
                            page: 1,
                            size: 200,
                        }
                    );

                resolvedPayment =
                    result.items.find(
                        (item) =>
                            item.documentReference ===
                            ref &&
                            item.providerNumber ===
                            provider &&
                            (!year ||
                                item.paymentYear ===
                                year)
                    ) ?? null;
            }

            if (!resolvedPayment) {
                setModalSeverity("info");
                setModalTitle("Sin información");
                setError(
                    "No se encontró el pago solicitado."
                );
                setAllDocuments([]);
                setDocuments([]);
                setDocTotalItems(0);
                setDocTotalPages(1);
                setDocPage(1);

                return;
            }

            resolvedPayment =
                await resolveProviderName(
                    resolvedPayment
                );

            setPayment(resolvedPayment);

            const resolvedHeaderUuid =
                resolvedPayment.paymentHeaderUuid ||
                headerUuid ||
                "";

            let resolvedDocuments:
                | PaymentDocument[]
                | undefined;

            if (resolvedHeaderUuid) {
                /*
                 * Se consulta sin paginación para ordenar globalmente:
                 * facturas primero, luego notas de crédito, y dentro
                 * de cada grupo por importe descendente.
                 */
                const response =
                    await paymentsService.getHeaderWithDetails(
                        resolvedHeaderUuid
                    );

                const payload =
                    response?.data ?? response ?? {};

                const content =
                    payload?.details ??
                    payload?.detailsPage?.content ??
                    [];

                resolvedDocuments =
                    mapDetailsToDocsFromHeader(
                        content
                    );
            } else {
                const detail =
                    await paymentsService.getPaymentDetail(
                        resolvedPayment.documentNumber
                    );

                resolvedDocuments =
                    detail.documents ?? [];
            }

            const orderedDocuments =
                sortPaymentDocuments(
                    resolvedDocuments
                );

            setUseLocalPagination(true);
            setAllDocuments(orderedDocuments);
            applyDocumentPage(
                orderedDocuments,
                1,
                docPerPage
            );
        } catch (loadError: any) {
            setModalSeverity("error");
            setModalTitle("Error");
            setError(
                getErrorMessage(
                    loadError,
                    "Error al cargar el detalle del pago."
                )
            );

            setAllDocuments([]);
            setDocuments([]);
            setDocTotalItems(0);
            setDocTotalPages(1);
            setDocPage(1);
        } finally {
            setLoading(false);
        }
    };

    const handleDocPageChange = (
        newPage: number
    ) => {
        applyDocumentPage(
            allDocuments,
            newPage,
            docPerPage
        );
    };

    const handleDocPerPageChange = (
        newPerPage: number
    ) => {
        applyDocumentPage(
            allDocuments,
            1,
            newPerPage
        );
    };

    /**
     * Orden y nombres solicitados:
     * Número Documento, Documento SAP, UUID, Moneda, Importe,
     * Tipo Documento, Fecha Pago, Fecha Registro,
     * Fecha de Actualización y Factura / NC.
     *
     * La columna Estatus se elimina únicamente del detalle.
     */
    const documentColumns = [
        {
            header: "Número Documento",
            render: (
                document: PaymentDocument
            ) =>
                document.documentNumber ||
                "—",
        },
        {
            header: "Documento SAP",
            render: (
                document: PaymentDocument
            ) =>
                document.sapDocument ||
                "—",
        },
        {
            header: "UUID",
            render: (
                document: PaymentDocument
            ) =>
                document.uuid || "—",
        },
        {
            header: "Moneda",
            render: (
                document: PaymentDocument
            ) => document.currency || "—",
        },
        {
            header: "Importe",
            render: (
                document: PaymentDocument
            ) => formatAmount(document.amount),
            align: "right" as const,
        },
        {
            header: "Tipo Documento",
            render: (
                document: PaymentDocument
            ) =>
                document.documentType || "—",
        },
        {
            header: "Fecha Pago",
            render: (
                document: PaymentDocument
            ) =>
                document.paymentDate ??
                document.documentDate ??
                "—",
        },
        {
            header: "Fecha Registro",
            render: (
                document: PaymentDocument
            ) => document.createdAt ?? "—",
        },
        {
            header: "Fecha de Actualización",
            render: (
                document: PaymentDocument
            ) => document.updatedAt ?? "—",
        },
        {
            header: "Factura / NC",
            render: (
                document: PaymentDocument
            ) => (
                <button
                    onClick={() =>
                        handleViewDocument(
                            document
                        )
                    }
                    className="payment-detail__view-btn"
                    aria-label={getDocButtonLabel(
                        document.documentType
                    )}
                    title={getDocButtonLabel(
                        document.documentType
                    )}
                    type="button"
                >
                    <img
                        src={eyeIcon}
                        alt=""
                        className="payment-detail__view-icon"
                    />
                </button>
            ),
            align: "center" as const,
        },
    ];

    if (loading && !payment) {
        return (
            <div className="payment-detail__layout">
                <div className="payment-detail__breadcrumb">
                    <Link
                        to={FINANCE_HOME_PATH}
                        className="payment-detail__breadcrumb-link"
                    >
                        Inicio
                    </Link>
                    <span className="payment-detail__breadcrumb-sep">
                        &gt;
                    </span>
                    <Link
                        to={FINANCE_HOME_PATH}
                        className="payment-detail__breadcrumb-link"
                    >
                        Finanzas
                    </Link>
                    <span className="payment-detail__breadcrumb-sep">
                        &gt;
                    </span>
                    <Link
                        to="/finanzas/pagos"
                        className="payment-detail__breadcrumb-link"
                    >
                        Pagos
                    </Link>
                    <span className="payment-detail__breadcrumb-sep">
                        &gt;
                    </span>
                    <span className="payment-detail__breadcrumb-current">
                        Detalle pago
                    </span>
                </div>

                <div className="payment-detail__box">
                    <div className="payment-detail__loading-wrap">
                        <div className="payment-detail__loading-text">
                            Cargando detalle del pago...
                        </div>
                    </div>
                </div>

                <GenericModal
                    visible
                    variant="loading"
                    message="Cargando…"
                />
            </div>
        );
    }

    return (
        <div className="payment-detail__layout">
            <div className="payment-detail__breadcrumb">
                <Link
                    to={FINANCE_HOME_PATH}
                    className="payment-detail__breadcrumb-link"
                >
                    Inicio
                </Link>
                <span className="payment-detail__breadcrumb-sep">
                    &gt;
                </span>
                <Link
                    to={FINANCE_HOME_PATH}
                    className="payment-detail__breadcrumb-link"
                >
                    Finanzas
                </Link>
                <span className="payment-detail__breadcrumb-sep">
                    &gt;
                </span>

                <button
                    onClick={handleBack}
                    className="payment-detail__breadcrumb-btn"
                    type="button"
                >
                    Pagos
                </button>

                <span className="payment-detail__breadcrumb-sep">
                    &gt;
                </span>
                <span className="payment-detail__breadcrumb-current">
                    Detalle pago
                </span>
            </div>

            <div className="payment-detail__box">
                {loading && <GenericMarqueeBar />}

                <div className="payment-detail__header">
                    <div className="payment-detail__header-left">
                        <div className="payment-detail__header-icon">
                            <svg
                                width="40"
                                height="40"
                                viewBox="0 0 24 24"
                                fill="none"
                                xmlns="http://www.w3.org/2000/svg"
                            >
                                <path
                                    d="M14 2H6C4.9 2 4 2.9 4 4v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zm-1 7V3.5L18.5 9H13zM6 20V4h5v7h7v9H6z"
                                    fill="#003865"
                                />
                                <path
                                    d="M8 16h8v1.5H8V16zm0-3h8v1.5H8V13z"
                                    fill="#003865"
                                />
                            </svg>
                        </div>

                        <div>
                            <h2 className="payment-detail__title">
                                Detalle de pago
                            </h2>
                            <p className="payment-detail__subtitle">
                                Consulta la información detallada del pago y los documentos relacionados.
                            </p>
                        </div>
                    </div>

                    <div className="payment-detail__header-actions finz-toolbar-actions">
                        <GenericButton
                            onClick={handleExportCsv}
                            disabled={
                                loading ||
                                allDocuments.length ===
                                0
                            }
                        >
                            <span
                                style={{
                                    display:
                                        "flex",
                                    alignItems:
                                        "center",
                                    gap: 6,
                                }}
                            >
                                <span
                                    className="pay-download-ico"
                                    aria-hidden="true"
                                    style={{
                                        WebkitMaskImage:
                                            `url(${downloadIconUrl})`,
                                        maskImage:
                                            `url(${downloadIconUrl})`,
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
                                        {payment.documentReference ??
                                            "—"}
                                    </p>
                                </div>

                                <div className="payment-detail__info-cell">
                                    <p className="payment-detail__info-label">
                                        Año Pago
                                    </p>
                                    <p className="payment-detail__info-value">
                                        {payment.paymentYear ??
                                            "—"}
                                    </p>
                                </div>

                                <div className="payment-detail__info-cell">
                                    <p className="payment-detail__info-label">
                                        Moneda
                                    </p>
                                    <p className="payment-detail__info-value">
                                        {payment.currency ??
                                            "—"}
                                    </p>
                                </div>

                                <div className="payment-detail__info-cell">
                                    <p className="payment-detail__info-label">
                                        Monto
                                    </p>
                                    <p className="payment-detail__info-value payment-detail__info-value--large">
                                        {formatAmount(
                                            payment.amount
                                        )}
                                    </p>
                                </div>
                            </div>

                            <div className="payment-detail__info-row payment-detail__info-row--band payment-detail__info-row--second">
                                <div className="payment-detail__info-cell">
                                    <p className="payment-detail__info-label">
                                        Número Proveedor
                                    </p>
                                    <p className="payment-detail__info-value">
                                        {payment.providerNumber ??
                                            "—"}
                                    </p>
                                </div>

                                <div className="payment-detail__info-cell">
                                    <p className="payment-detail__info-label">
                                        Nombre Proveedor
                                    </p>
                                    <p className="payment-detail__info-value">
                                        {payment.providerName ||
                                            "—"}
                                    </p>
                                </div>

                                <div className="payment-detail__info-cell">
                                    <p className="payment-detail__info-label">
                                        Estatus
                                    </p>
                                    <p className="payment-detail__info-value">
                                        {payment.statusId !=
                                            null ? (
                                            <StatusPill
                                                type={
                                                    resolvePaymentStatusDisplay(
                                                        payment.statusId
                                                    )
                                                        .type
                                                }
                                            >
                                                {
                                                    resolvePaymentStatusDisplay(
                                                        payment.statusId
                                                    )
                                                        .label
                                                }
                                            </StatusPill>
                                        ) : (
                                            payment.status ??
                                            "—"
                                        )}
                                    </p>
                                </div>

                                <div className="payment-detail__info-cell">
                                    <p className="payment-detail__info-label">
                                        Fecha Pago
                                    </p>
                                    <p className="payment-detail__info-value">
                                        {payment.paymentDate ??
                                            "—"}
                                    </p>
                                </div>

                                <div className="payment-detail__info-cell">
                                    <p className="payment-detail__info-label">
                                        Fecha Registro
                                    </p>
                                    <p className="payment-detail__info-value">
                                        {payment.createdAt ??
                                            "—"}
                                    </p>
                                </div>

                                <div className="payment-detail__info-cell">
                                    <p className="payment-detail__info-label">
                                        Fecha Actualización
                                    </p>
                                    <p className="payment-detail__info-value">
                                        {payment.updatedAt ??
                                            "—"}
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
                            {useLocalPagination
                                ? ""
                                : ""}
                        </h3>
                    </div>

                    <div className="payment-detail__table-wrap">
                        <GenericTable<PaymentDocument>
                            rows={documents}
                            columns={documentColumns}
                            actions={[]}
                            emptyLabel={
                                loading
                                    ? "Cargando documentos..."
                                    : "No hay documentos relacionados"
                            }
                            perPage={docPerPage}
                            page={docPage}
                            totalPages={
                                docTotalPages
                            }
                            totalItems={
                                docTotalItems
                            }
                            onChangePerPage={
                                handleDocPerPageChange
                            }
                            onChangePage={
                                handleDocPageChange
                            }
                        />
                    </div>
                </div>

                <div className="payment-detail__footer-actions finz-page-actions">
                    <GenericButton
                        variant="back"
                        type="button"
                        onClick={handleBack}
                    >
                        Volver
                    </GenericButton>
                </div>
            </div>

            <GenericModal
                visible={Boolean(error)}
                variant="alert"
                title={modalTitle || "Aviso"}
                severity={modalSeverity}
                message={error}
                buttonText="Aceptar"
                onClose={() => setError("")}
            />

            {loading && (
                <GenericModal
                    visible
                    variant="loading"
                    message="Cargando…"
                />
            )}
        </div>
    );
}