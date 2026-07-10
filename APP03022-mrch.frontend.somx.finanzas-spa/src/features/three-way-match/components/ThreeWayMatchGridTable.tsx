import { GenericTable } from "@shared/components/ui";
import { formatDate } from "@/utils/utils";
import type { ThreeWayMatchRecord } from "../interfaces";

interface Props {
    rows: ThreeWayMatchRecord[];
    isAdmin: boolean;
    page: number;
    perPage: number;
    totalPages: number;
    totalItems: number;
    loading: boolean;
    onChangePage: (page: number) => void;
    onChangePerPage: (size: number) => void;
}

function fmtDate(value: string | null | undefined): string {
    if (value == null || value === "") return "--";
    return formatDate(value);
}

function fmtMoney(value: string | number | null | undefined): string {
    if (value == null || value === "") return "--";

    const normalizedValue = String(value).replace(/[$,\s]/g, "");
    const amount = Number(normalizedValue);

    if (Number.isNaN(amount)) return String(value);

    return new Intl.NumberFormat("es-MX", {
        style: "currency",
        currency: "MXN",
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    }).format(amount);
}

export default function ThreeWayMatchGridTable({ rows, isAdmin: _isAdmin, ...props }: Props) {
    const columns = [
        { header: "Orden Compra", render: (r: ThreeWayMatchRecord) => r.ordenCompra ?? "--" },
        { header: "Recepción", render: (r: ThreeWayMatchRecord) => r.recepcion ?? "--" },
        { header: "Monto Recepción", render: (r: ThreeWayMatchRecord) => fmtMoney(r.montoRecepcion) },
        { header: "Fecha Recepción", render: (r: ThreeWayMatchRecord) => fmtDate(r.fechaRecepcion) },
        { header: "Serie", render: (r: ThreeWayMatchRecord) => r.serie ?? "--" },
        { header: "Folio", render: (r: ThreeWayMatchRecord) => r.folio ?? "--" },
        { header: "UUID", render: (r: ThreeWayMatchRecord) => r.uuid ?? "--" },
        { header: "Monto Factura", render: (r: ThreeWayMatchRecord) => fmtMoney(r.montoFactura) },
        {
            header: "Fecha Recepción Factura",
            render: (r: ThreeWayMatchRecord) => fmtDate(r.fechaTimbrado),
        },
        { header: "Documento SAP", render: (r: ThreeWayMatchRecord) => r.documentoSap ?? "--" },
        { header: "Monto Contable", render: (r: ThreeWayMatchRecord) => fmtMoney(r.montoContable) },
        { header: "Fecha Contable", render: (r: ThreeWayMatchRecord) => fmtDate(r.fechaContable) },
        {
            header: "Documento Pago",
            render: (r: ThreeWayMatchRecord) =>
                r.numeroDocumento ?? r.referenciaPago ?? "--",
        },
        { header: "Monto Pago", render: (r: ThreeWayMatchRecord) => fmtMoney(r.montoPago) },
        {
            header: "Número Proveedor",
            render: (r: ThreeWayMatchRecord) =>
                r.numeroProveedor != null ? String(r.numeroProveedor) : "--",
        },
        {
            header: "Nombre Proveedor",
            render: (r: ThreeWayMatchRecord) => r.nombreProveedor ?? "--",
        },
        { header: "Fecha Pago", render: (r: ThreeWayMatchRecord) => fmtDate(r.fechaPago) },
    ];

    return (
        <GenericTable
            rows={rows}
            columns={columns}
            emptyLabel="Sin resultados"
            {...props}
        />
    );
}