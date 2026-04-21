import { GenericTable } from '@shared/components/ui';
import type { ThreeWayMatchRecord } from '../interfaces';

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

export default function ThreeWayMatchGridTable({
    rows,
    isAdmin,
    ...props
}: Props) {

    const columns = [
        ...(isAdmin ? [
            { header: 'Proveedor', render: (r: ThreeWayMatchRecord) => r.numeroProveedor },
            { header: 'Nombre proveedor', render: () => '-' },
        ] : []),

        { header: 'Orden de compra', render: (r: ThreeWayMatchRecord) => r.ordenCompra },
        { header: 'Recepción', render: (r: ThreeWayMatchRecord) => r.recepcion },
        { header: 'Fecha recepción', render: (r: ThreeWayMatchRecord) => r.fechaRecepcion },
        { header: 'Monto recepción', render: (r: ThreeWayMatchRecord) => r.montoRecepcion },
        { header: 'Serie', render: (r: ThreeWayMatchRecord) => r.serie },
        { header: 'Folio', render: (r: ThreeWayMatchRecord) => r.folio },
        { header: 'UUID', render: (r: ThreeWayMatchRecord) => r.uuid },
        { header: 'Monto factura', render: (r: ThreeWayMatchRecord) => r.montoFactura },
        { header: 'Documento SAP', render: (r: ThreeWayMatchRecord) => r.documentoSap },
        { header: 'Fecha contable', render: (r: ThreeWayMatchRecord) => r.fechaContable },
        { header: 'Monto contable', render: (r: ThreeWayMatchRecord) => r.montoContable },
        { header: 'Referencia pago', render: (r: ThreeWayMatchRecord) => r.referenciaPago },
        { header: 'Fecha pago', render: (r: ThreeWayMatchRecord) => r.fechaPago },
        { header: 'Monto pago', render: (r: ThreeWayMatchRecord) => r.montoPago },
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
