export interface ThreeWayMatchRecord {
    id: string;

    numeroProveedor: string | number;

    ordenCompra: string;
    fechaOrdenCompra: string;
    montoOrdenCompra: string;

    recepcion: string;
    fechaRecepcion: string;
    montoRecepcion: string;

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

    /** Razón social del proveedor (si el backend la envía). */
    nombreProveedor?: string | null;

    currency: string;
    exchangeRate: string;
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
    }) => void;
    onClear: () => void;
}
