
import { ChangeEvent, useEffect, useState } from "react";
import GenericTable, { Column, RowAction } from "@/shared/components/ui/table/GenericTable";
import editIcon from "@assets/edit.svg";
import eyeIcon from "@assets/eye-show.svg";
import payment from "@assets/xml.svg";
import document from "@assets/document.svg";
import { ReactElement } from "react";
import { useNavigate } from "react-router-dom";
import { Reception, ReceptionStatusOptions } from "../../interfaces";
import { formatDate, formatAmount, exportToCSV } from "@/utils/utils";
import { OrderPillStatus } from "./ReceptionPillStatus";
import { GenericInput, GenericSelect } from "@/shared/components/ui";
import "./ReceptionsTable.css";

const ActionOptions = [{ label: "Exportar CSV", value: "csv" }];

export default function ResultsTable({ rows, loading }: { rows: Reception[]; loading: boolean }): ReactElement {
    const nav = useNavigate();
    const [isExporting, setIsExporting] = useState(false);
    const [selectableReceptions, setSelectableReceptions] = useState<Reception[]>([]);
    const [selectedAction, setSelectedAction] = useState("");

    useEffect(() => {
        if (selectedAction === "csv") {
            setIsExporting(true);
            handleDownloadCSV();
        }
    }, [selectedAction]);

    const handleDownloadCSV = () => {
        const selectedValues = selectableReceptions.length ? selectableReceptions : rows;
        const headers = [
            "Recepción", "Orden Compra", "Guía", "Sucursal", "Id Origen", "Descripción Origen",
            "Fecha Recepción", "Fecha Registro Documento", "Importe", "Estatus", "Id Proveedor",
            "Nombre Proveedor", "UUID", "Folio", "Monto Factura"
        ];

        const convertedRows = selectedValues.map(item => {
            const filtered = ReceptionStatusOptions.find(option => option.value === Number(item.status));
            return [
                item.receptionId,
                item.order.orderNumber,
                item.shipping?.guideNumber || "",
                item.originId,
                item.originId,
                item.originId,
                formatDate(item.receptionDate),
                formatDate(item.receptionDate),
                item.amount,
                filtered?.description ?? filtered?.label ?? "",
                item.supplierNumber,
                item.vendorName,
                item.purchaseOrderId,
                item.purchaseOrderId,
                item.amount
            ];
        });

        exportToCSV(headers, convertedRows, "Mis Recepciones");
        setTimeout(() => {
            setIsExporting(false);
            setSelectedAction("");
        }, 1000);
    };

    const addRemoveToSelectableReceptions = (reception: Reception) => {
        const exists = selectableReceptions.find(item => item.receptionId === reception.receptionId);
        if (exists) {
            setSelectableReceptions(selectableReceptions.filter(item => item.receptionId !== reception.receptionId));
        } else {
            setSelectableReceptions([...selectableReceptions, reception]);
        }
    };

    const columns: Column<Reception>[] = [
        { header: "", render: order => <GenericInput type="checkbox" onClick={() => addRemoveToSelectableReceptions(order)} /> },
        { header: "Recepción", render: order => <>{order.receptionId}</> },
        { header: "Orden", render: order => <>{order.order.orderNumber}</> },
        { header: "Guía", render: order => <>{order.shipping?.guideNumber}</> },
        { header: "Sucursal", render: () => <>--</> },
        { header: "Origen", render: order => <>{order.originId}</> },
        { header: "Descripción Origen", render: () => <>--</> },
        { header: "Fecha Recepción", render: order => <>{formatDate(order.receptionDate) || "N/D"}</> },
        { header: "Fecha Registro Documento", render: () => <>--</> },
        { header: "Importe", render: order => <>{formatAmount(order.amount)}</> },
        { header: "Estatus", render: order => <OrderPillStatus status={order.status} /> },
        { header: "Id Proveedor", render: order => <>{order.supplier?.supplierNumber}</> },
        { header: "Nombre Proveedor", render: order => <>{order.supplier?.businessName}</> },
        { header: "Serie", render: () => <>--</> },
        { header: "Folio", render: () => <>--</> },
        { header: "UUID", render: () => <>--</> }
    ];

    const actions: RowAction<Reception>[] = [
        { title: "Ver Detalles", icon: eyeIcon, onClick: order => nav(`/recepciones/${order.receptionId}`) },
        { title: "Editar Recepción", icon: editIcon, isDisabled: order =>order.status!=0, onClick: order => nav(`/recepciones/${order.receptionId}/editar`) },
        { title: "Agregar Factura (XML)", icon: payment, isDisabled: order =>order.status!=0, onClick: order => nav(`/recepciones/${order.receptionId}/factura`) },
        { title: "Notas de crédito", icon: document, onClick: order => nav(`/recepciones/${order.receptionId}/notas-credito`) }
    ];

    return (
        <div className="rc-results-table">
            <GenericTable<Reception>
                rows={rows}
                columns={columns}
                actions={actions}
                emptyLabel={loading ? "Cargando..." : "Sin resultados"}
                perPage={25}
                page={1}
                totalPages={1}
            />
            <div className="rc-actions-panel">
                {isExporting ? <p>Se está procesando tu acción...</p> : (
                    <GenericSelect
                        label="Acción"
                        placeholder="Selecciona una acción"
                        value={selectedAction}
                        onChange={(event: ChangeEvent<HTMLInputElement>) => setSelectedAction(event.target.value)}
                        options={ActionOptions}
                    />
                )}
            </div>
        </div>
    );
}
