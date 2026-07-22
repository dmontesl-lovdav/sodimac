import { decorate } from "@/shared/components/ui/decorator/SimpleDecorator";
import { GenericModal } from "@/shared/components/ui";
import GenericTable, {
    Column,
    RowAction,
} from "@/shared/components/ui/table/GenericTable";
import viewIcon from "@assets/eye-show.svg";
import { BreadcrumbItem } from "@/shared/components/ui/navigation/Breadcrumb";
import { withFinanceBreadcrumb } from "@/shared/components/ui/navigation/financeBreadcrumb";
import { useFinanceAlertModal } from "@/shared/hooks/useFinanceAlertModal";
import { FINANCE_LIST_KEYS } from "@/shared/hooks";
import { fetchCatalog, formatAmount, formatDate, formatDateTime } from "@/utils/utils";
import { ReactElement, ReactNode, useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import { shippingGuideService } from "../api/ShippingGuideClient";
import {
    getDeliveryTypeLabel,
    getNumericGuideStatus,
    type ShippingGuideDetail,
    type ShippingGuidePurchaseOrderLink,
} from "../interfaces";
import {
    getRegisteredShippingGuideStatusLabels,
    resolveShippingGuideStatusDescription,
} from "../shippingGuideStatusCatalog";

import "../styles/shippingGuides.css";

const EMPTY_DETAIL: ShippingGuideDetail = {
    shippingGuideId: "",
    guideNumber: "",
    vendorNumber: 0,
    truckPlate: "",
    trailerPlate: null,
    originId: 0,
    deliveryType: 0,
    status: {
        key: "",
        value: "",
        color: "",
        externalKey: "",
        internalStatus: 0,
        description: "",
    },
    comments: null,
    deliveryDate: null,
    shippingDate: null,
    createdBy: "",
    createdAt: "",
    updatedBy: null,
    updatedAt: null,
    isStatusUpdated: false,
    shippingGuidePurchaseOrders: [],
};

const fmt = (v: unknown): string => {
    if (v === null || v === undefined || v === "") return "N/D";
    return String(v);
};

function guideStatusLabel(detail: ShippingGuideDetail): string {
    const code = getNumericGuideStatus(detail.status);
    if (code == null) return "N/D";
    const statusObj =
        typeof detail.status === "object" && detail.status !== null
            ? (detail.status as {
                  description?: string;
                  value?: string;
                  key?: string;
                  internalStatus?: number;
              })
            : null;
    return resolveShippingGuideStatusDescription(
        code,
        statusObj,
        getRegisteredShippingGuideStatusLabels() ?? undefined
    );
}

function DetailCell({
    label,
    children,
}: {
    label: string;
    children: ReactNode;
}) {
    return (
        <div className="sg-detail-cell">
            <div className="sg-detail-label">{label}</div>
            <div className="sg-detail-value">{children}</div>
        </div>
    );
}

type PurchaseOrderLinkRow = ShippingGuidePurchaseOrderLink & { id: string };

export default function ShippingGuideDetailView(): ReactElement {
    const { guideId } = useParams<{ guideId: string }>();

    const financeAlert = useFinanceAlertModal();

    const [detail, setDetail] = useState<ShippingGuideDetail>(EMPTY_DETAIL);
    const [loading, setLoading] = useState<boolean>(false);
    const [statusPurchaseOrder, setStatusPurchaseOrder] = useState<any[]>([]);

    const fetchDetail = async (id: string) => {
        setLoading(true);
        try {
            const response = await shippingGuideService.getDetail(id);
            const statuses = await fetchCatalog("CatEstatusRecepcion");
            if (response) {
                setDetail(response);
            }
            if (statuses) {
                //@ts-ignore
                setStatusPurchaseOrder(statuses?.details ?? []);
            }
        } catch (error) {
            financeAlert.showErrorFrom(
                "Error",
                error,
                "No fue posible obtener el detalle de la guía."
            );
            setDetail({
                ...EMPTY_DETAIL,
                shippingGuideId: id,
                guideNumber: "",
            });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (guideId) fetchDetail(guideId);
    }, [guideId]);

    const guideTitleNumber =
        ((t) => (t == null || t === "" ? "—" : t))(detail.guideNumber?.trim());

    const breadcrumb: BreadcrumbItem[] = useMemo(
        () =>
            withFinanceBreadcrumb([
                { label: "Guías de Embarque", to: "/finanzas/guias" },
                {
                    label: detail.guideNumber?.trim()
                        ? `Guía ${detail.guideNumber}`
                        : "Detalle",
                },
            ]),
        [detail.guideNumber]
    );

    const poLinks: ShippingGuidePurchaseOrderLink[] =
        detail.shippingGuidePurchaseOrders ?? [];

    const poTableRows = useMemo<PurchaseOrderLinkRow[]>(
        () =>
            poLinks.map((link) => ({
                ...link,
                id: link.shippingGuidePurchaseOrderId,
            })),
        [poLinks]
    );

    const poSectionTitle =
        poLinks.length === 1
            ? `Orden de compra vinculada (${poLinks.length})`
            : `Órdenes de compra vinculadas (${poLinks.length})`;

    const poColumns = useMemo<Column<PurchaseOrderLinkRow>[]>(
        () => [
            {
                header: "Orden Compra",
                render: (link) => link.purchaseOrder?.orderNumber ?? "N/D",
            },
            {
                header: "Monto",
                align: "right",
                render: (link) => {
                    const amount = link.purchaseOrder?.amount;
                    return amount != null
                        ? formatAmount(Number(amount))
                        : "N/D";
                },
            },
            {
                header: "Número Proveedor",
                render: (link) => {
                    const sn = link.purchaseOrder?.supplierNumber;
                    return sn != null ? String(sn) : "N/D";
                },
            },
            {
                header: "Nombre Proveedor",
                render: (link) => {
                    const name =
                        link.purchaseOrder?.supplierBusinessName?.trim() ||
                        (detail.supplier?.businessName &&
                        link.purchaseOrder?.supplierNumber != null &&
                        String(link.purchaseOrder.supplierNumber) ===
                            String(detail.vendorNumber)
                            ? detail.supplier.businessName
                            : "");
                    return name ? String(name) : "N/D";
                },
            },
            {
                header: "Fecha Recepción",
                render: (link) => {
                    const date = link.purchaseOrder?.purchaseOrderDate;
                    return date ? formatDate(String(date)) : "N/D";
                },
            },
            {
                header: "Fecha Vinculación",
                render: (link) =>
                    link.createdAt
                        ? formatDateTime(link.createdAt, { seconds: true })
                        : "N/D",
            },
            {
                header: "Estatus",
                render: (link) => detail.status.value == "9" ? "Cancelada" : statusPurchaseOrder.find((status) => status.value == link.purchaseOrder?.status)?.description ?? "N/D",
            },
        ],
        [detail.supplier?.businessName, detail.vendorNumber, statusPurchaseOrder]
    );

    console.log(detail);

    const poActions = useMemo<RowAction<PurchaseOrderLinkRow>[]>(
        () => [
            {
                title: "Ver OC",
                icon: viewIcon,
                onClick: (link, nav) => {
                    const orderNumber = link.purchaseOrder?.orderNumber?.trim();
                    const startDate = link.purchaseOrder?.purchaseOrderDate;
                    const endDate = link.purchaseOrder?.purchaseOrderDate;
                    if (orderNumber) {
                        nav(
                            `/finanzas/recepciones?startDate=${startDate}&endDate=${endDate}&supplierNumber=${link.purchaseOrder?.supplierNumber}&orderNumber=${encodeURIComponent(orderNumber)}`
                        );
                    }
                },
                isDisabled: (link) =>
                    !link.purchaseOrder?.orderNumber?.trim(),
            },
        ],
        []
    );

    return (
        <>
            {decorate(
                breadcrumb,
                "/finanzas/guias",
                <div className="sg-detail-stack">
                    <div className="sg-detail-header">
                        <div className="sg-detail-title">
                            Detalle Guía {guideTitleNumber}
                        </div>
                        <div className="sg-detail-subtitle">
                            Información relacionada con la guía de embarque
                        </div>
                    </div>

                    <div className="sg-detail-fields">
                        <DetailCell label="Número Guía">
                            {fmt(detail.guideNumber)}
                        </DetailCell>
                        <DetailCell label="Número proveedor">
                            {fmt(detail.vendorNumber)}
                        </DetailCell>
                        <DetailCell label="Estatus">
                            {guideStatusLabel(detail)}
                        </DetailCell>

                        <DetailCell label="Placa Tractocamión">
                            {fmt(detail.truckPlate)}
                        </DetailCell>
                        <DetailCell label="Placa Remolque">
                            {fmt(detail.trailerPlate)}
                        </DetailCell>
                        <DetailCell label="Origen (ID)">
                            {fmt(detail.originId)}
                        </DetailCell>

                        <DetailCell label="Comentarios">{fmt(detail.comments)}</DetailCell>
                        <DetailCell label="Fecha Entrega">
                            {detail.deliveryDate
                                ? formatDate(String(detail.deliveryDate))
                                : "N/D"}
                        </DetailCell>
                        <DetailCell label="Fecha Envío">
                            {formatDateTime(detail.shippingDate, { seconds: true })}
                        </DetailCell>

                        <DetailCell label="Fecha Registro">
                            {formatDateTime(detail.createdAt, { seconds: true })}
                        </DetailCell>
                        <DetailCell label="Última Actualización">
                            {formatDateTime(detail.updatedAt, { seconds: true })}
                        </DetailCell>
                        <DetailCell label="Estatus Actualizado">
                            {detail.isStatusUpdated ? "Sí" : "No"}
                        </DetailCell>

                        <DetailCell label="Usuario Registro">
                            {fmt(detail.createdBy)}
                        </DetailCell>
                        <DetailCell label="Usuario Actualización">
                            {fmt(detail.updatedBy)}
                        </DetailCell>
                        <DetailCell label="Tipo Entrega">
                            {getDeliveryTypeLabel(detail.deliveryType)}
                        </DetailCell>
                    </div>

                    <div className="sg-detail-section-title">{poSectionTitle}</div>
                    <div className="sg-detail-po-grid">
                        <GenericTable<PurchaseOrderLinkRow>
                            rows={poTableRows}
                            columns={poColumns}
                            actions={poActions}
                            emptyLabel="Sin órdenes de compra asociadas"
                            perPage={10}
                            page={1}
                            totalPages={1}
                        />
                    </div>
                </div>,
                loading,
                undefined,
                { financeListSession: FINANCE_LIST_KEYS.shippingGuides }
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
        </>
    );
}
