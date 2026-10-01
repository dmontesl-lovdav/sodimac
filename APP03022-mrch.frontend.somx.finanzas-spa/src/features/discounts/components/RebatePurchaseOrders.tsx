import { useEffect, useMemo, useState } from "react";
import type { ReactElement } from "react";
import { GenericButton } from "@shared/components/ui";
import { APP_EVENT, PermissionGate } from "@shared/security";
import GenericTable from "@/shared/components/ui/table/GenericTable";
import type { Column } from "@/shared/components/ui/table/GenericTable";
import { exportToCSV, formatFilenameTimestamp } from "@/utils/utils";
import { api, money, styles } from "./rebateDetailView.helpers";

interface PurchaseOrderDetail {
    purchaseOrder: string;
    receivedAmount: string;
    receptionDate: string;
    discountType: number;
    discountAmount: string;
}
interface OrdersDetail {
    rebateId: string;
    orders: PurchaseOrderDetail[];
    message: string | null;
}

interface Props {
    rebateId: string;
    tipoRebate: string;
    documentNumber: string;
}

export default function RebatePurchaseOrders({ rebateId,
    tipoRebate,
    documentNumber, }: Props): ReactElement {
    const [ordersDetail, setOrdersDetail] = useState<OrdersDetail | null>(null);
    const [ordersError, setOrdersError] = useState("");
    const [ordersLoading, setOrdersLoading] = useState(false);
    const [ordersPage, setOrdersPage] = useState(1);
    const [ordersPerPage, setOrdersPerPage] = useState(10);

    useEffect(() => {
        let cancelled = false;
        setOrdersDetail(null);
        setOrdersError("");
        setOrdersPage(1);
        setOrdersLoading(Boolean(rebateId));
        if (!rebateId) return;
        void (async () => {
            try {
                const response = await api.request<OrdersDetail | { data: OrdersDetail }>(
                    `rebates/${encodeURIComponent(rebateId)}/purchase-order-details`, "get"
                );
                const payload = response && "data" in response ? response.data : response;
                if (!payload || payload.rebateId !== rebateId || !Array.isArray(payload.orders)) {
                    throw new Error("Detalle de órdenes inválido");
                }
                if (!cancelled) setOrdersDetail(payload);
            } catch {
                if (!cancelled) setOrdersError("No fue posible consultar las órdenes de compra. Vuelve a abrir el detalle.");
            } finally {
                if (!cancelled) setOrdersLoading(false);
            }
        })();
        return () => { cancelled = true; };
    }, [rebateId]);

    const handleExportOrdersCsv = () => {
        if (
            ordersLoading || ordersError ||
            ordersDetail?.rebateId !== rebateId ||
            !ordersDetail?.orders.length
        ) return;

        const body = ordersDetail.orders.map((row) => {
            const parts = row.receptionDate?.split("-");

            return [
                row.purchaseOrder,
                row.receivedAmount,
                parts?.length === 3 ? `${parts[2]}/${parts[1]}/${parts[0]}` : "N/D",
                tipoRebate || `Tipo ${row.discountType}`,
                row.discountAmount,
            ];
        });

        exportToCSV(
            [
                "Orden de compra",
                "Monto recibido",
                "Fecha de recepción",
                "Tipo de descuento",
                "Monto de descuento",
            ],
            body,
            `ordenes_descuento_${documentNumber.trim().replace(/[\x00-\x1F\x22\x2A\x2F\x3A\x3C\x3E\x3F\x5C\x7C]/g, "_")}_${formatFilenameTimestamp()}`
        );
    };

    const orderColumns = useMemo<Column<PurchaseOrderDetail>[]>(() => [
        { header: "Orden de compra", render: row => row.purchaseOrder },
        { header: "Monto recibido", align: "right", render: row => money(row.receivedAmount) },
        {
            header: "Fecha de recepción", render: row => {
                // Fecha civil: no convertir medianoche UTC a la zona local.
                const parts = row.receptionDate?.split("-");
                return parts?.length === 3 ? `${parts[2]}/${parts[1]}/${parts[0]}` : "N/D";
            }
        },
        {
            header: "Tipo de descuento", render: row =>
                tipoRebate || `Tipo ${row.discountType}`
        },
        { header: "Monto de descuento", align: "right", render: row => money(row.discountAmount) },
    ], [tipoRebate]);

    return (
        <>
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: "1rem", flexWrap: "wrap" }}>
                <div style={styles.sectionTitle}>Órdenes de compra del descuento</div>
                <PermissionGate appEvent={APP_EVENT.DISCOUNTS.DOWNLOAD_CSV}>
                    <GenericButton
                        variant="primary"
                        onClick={handleExportOrdersCsv}
                        disabled={ordersLoading || Boolean(ordersError) ||
                            ordersDetail?.rebateId !== rebateId || !ordersDetail?.orders.length}
                        type="button"
                    >
                        Exportar CSV
                    </GenericButton>
                </PermissionGate>
            </div>
            {ordersError ? (
                <div role="alert" style={styles.errorBox}>{ordersError}</div>
            ) : (
                <div style={styles.tableCard}>
                    <GenericTable<PurchaseOrderDetail>
                        rows={ordersDetail?.rebateId === rebateId ? ordersDetail.orders : []}
                        columns={orderColumns}
                        emptyLabel={ordersLoading ? "Cargando órdenes de compra…" :
                            ordersDetail?.message || "Sin órdenes de compra relacionadas"}
                        page={ordersPage}
                        perPage={ordersPerPage}
                        onChangePage={setOrdersPage}
                        onChangePerPage={value => { setOrdersPerPage(value); setOrdersPage(1); }}
                    />
                </div>
            )}
        </>
    );
}
