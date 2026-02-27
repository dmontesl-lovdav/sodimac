import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';

interface ComplementPayment {
    paymentsUuid: string;
    series: string;
    folio: string;
    subtotal: number;
    totalAmount: number;
    issuerRfc: string;
    issuerName: string;
    receiverRfc: string;
    receiverName: string;
    createdAt: string;
    paymentDate: string;
    statusDescription: string;
    relatedDocumentsCount?: number;
}

export function downloadComplementReport(
    data: ComplementPayment[],
    selectedIds: string[] = []
) {
    console.log('Data recibida en XLSX:', data);
    console.log('IDs seleccionados:', selectedIds);

    // Asegurar comparación correcta (string a string)
    const filtered =
        selectedIds.length > 0
            ? data.filter((r) => selectedIds.includes(String(r.paymentsUuid)))
            : data;

    if (!filtered || filtered.length === 0) {
        alert('No hay datos seleccionados para exportar.');
        return;
    }

    const hoja1 = filtered.map((r) => ({
        Serie: r.series,
        Folio: r.folio,
        Subtotal: r.subtotal ?? 0,
        Total: r.totalAmount ?? 0,
        'RFC Emisor': r.issuerRfc,
        'Nombre Emisor': r.issuerName,
        'RFC Receptor': r.receiverRfc,
        'Nombre Receptor': r.receiverName,
        'Fecha Emisión': r.createdAt,
        'Fecha Pago': r.paymentDate,
        Estatus: r.statusDescription,
        'UUID Complemento de Pago': r.paymentsUuid,
        'Documentos Relacionados': r.relatedDocumentsCount ?? 0,
    }));

    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(
        workbook,
        XLSX.utils.json_to_sheet(hoja1),
        'Complemento de Pago'
    );

    const wbout = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    const blob = new Blob([wbout], { type: 'application/octet-stream' });
    saveAs(blob, 'Reporte_Complemento_Pago.xlsx');
}
