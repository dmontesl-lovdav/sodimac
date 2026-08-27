export interface ThreeWayMatchRecord {
    id: string;

    numeroProveedor: string | number;

    ordenCompra: string;
    fechaOrdenCompra: string;
    montoOrdenCompra: string | null;

    estatusOrdenCompra?: number | string | null;

    recepcion: string;
    fechaRecepcion: string;
    montoRecepcion: string | null;

    estatusRecepcion?: number | string | null;

    serie: string | null;
    folio: string | null;
    uuid: string | null;

    fechaTimbrado: string | null;
    montoFactura: string | null;

    numeroNotaCredito: string | null;
    montoNotaCredito: string | null;

    numeroDocumento: string | null;
    documentoSap: string | null;

    fechaContable: string | null;
    montoContable: string | null;

    referenciaPago: string | null;
    fechaPago: string | null;
    montoPago: string | null;

    /** Razón social del proveedor si el backend la envía. */
    nombreProveedor?: string | null;

    /** Descripción del tipo de proveedor si el backend la envía. */
    tipoProveedor?: string | null;

    /** ID del tipo de proveedor como respaldo si el backend lo envía. */
    tipoProveedorId?: string | number | null;

    currency: string | null;
    exchangeRate: string | null;
    estatus: number;

    createdBy: string | null;
    createdAt: string;
    updatedBy: string | null;
    updatedAt: string;
}

export interface ThreeWayMatchFiltersProps {
    isAdmin: boolean;
    onSearch: (filters: {
        dateType: string;
        startDate: string;
        endDate: string;
        supplier?: string;
        po?: string;
        reception?: string;
        supplierType?: string;
    }) => void;
    onClear: () => void;
}