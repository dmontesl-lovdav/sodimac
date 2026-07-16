import type { AccountStatementReportPayload } from '../interfaces/accountStatementReport';
import { buildAccountStatementHtml } from './accountStatementHtmlBuilder';

/**
 * Abre el estado de cuenta en una pestaña nueva y lanza el diálogo de impresión
 * (Guardar como PDF en el navegador). Sin dependencias extra.
 */
export function openAccountStatementPdfPreview(
    payload: AccountStatementReportPayload,
    receptionStatuses: any[]
): void {
    const html = buildAccountStatementHtml(payload, receptionStatuses);
    const blob = new Blob([html], { type: 'text/html;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const win = window.open(url, '_blank', 'noopener,noreferrer');
    if (!win) {
        URL.revokeObjectURL(url);
        throw new Error(
            'El navegador bloqueó la ventana emergente. Permite ventanas emergentes para ver el PDF.'
        );
    }
    setTimeout(() => URL.revokeObjectURL(url), 120000);
}
