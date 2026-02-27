// src/features/rebates/components/parts/ResultsTable.tsx
import { ReactElement } from 'react';
import GenericTable from '@shared/components/ui/table/GenericTable';

/* ---------- Types ---------- */
export interface RebateRow {
    id: string | number;
    vendorName: string;
    documentNumber: string;
    referenceDocument: string;
    rebateType: string;
    sapDocument: string;
    amount: number;
    periodName: string;
    status: string;
    applyDate: string;
    expirationDate: string;
}

interface ResultsTableProps {
    rows: RebateRow[];
    loading?: boolean;
}

/* ---------- Component ---------- */
export default function ResultsTable({
    rows = [],
    loading = false,
}: ResultsTableProps): ReactElement {
    const columns = [
        { header: 'Id Proveedor', render: (r: RebateRow) => r.id },
        { header: 'Proveedor', render: (r: RebateRow) => r.vendorName },
        { header: 'Número Documento', render: (r: RebateRow) => r.documentNumber },
        { header: 'Referencia Documento', render: (r: RebateRow) => r.referenceDocument },
        { header: 'Tipo Rebate', render: (r: RebateRow) => r.rebateType },
        { header: 'Documento SAP', render: (r: RebateRow) => r.sapDocument },
        { header: 'Importe', render: (r: RebateRow) => `$${r.amount.toFixed(2)}` },
        { header: 'Periodo', render: (r: RebateRow) => r.periodName },
        { header: 'Estatus', render: (r: RebateRow) => r.status },
        { header: 'Fecha Aplicación', render: (r: RebateRow) => r.applyDate },
        { header: 'Fecha Vencimiento', render: (r: RebateRow) => r.expirationDate },
    ];

    return (
        <GenericTable<RebateRow>
            rows={rows}
            columns={columns}
            emptyLabel={loading ? 'Cargando...' : 'Sin resultados'}
            perPage={25}
            page={1}
            totalPages={1}
        />
    );
}
