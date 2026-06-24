import { formatDate, formatDateTime } from '@/utils/utils';
import type {
    AccountStatementReportPayload,
    CatalogStatusItem,
} from '../interfaces/accountStatementReport';

const ISSUER_FALLBACK = {
    name: 'Comercializadora SDMHC S.A. de C.V. (SODIMAC MÉXICO)',
    address:
        'Av. Adolfo López Mateos No. 201, Col. Santa Cruz Acatlán, CP 53150, Naucalpan de Juárez, Estado de México.',
    rfc: 'CSD161207R2A',
    phone: '800 0625 222',
    email: 'analista@sodimac.com.mx',
};

function parseNum(v: unknown): number {
    if (v == null) return 0;
    if (typeof v === 'number') return v;
    const n = parseFloat(String(v).replace(/,/g, ''));
    return Number.isFinite(n) ? n : 0;
}

function formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-MX', {
        style: 'currency',
        currency: 'MXN',
    }).format(amount);
}

function escapeHtml(s: string): string {
    return String(s)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

type ViewModel = {
    issuer: AccountStatementReportPayload['issuer'];
    vendor: AccountStatementReportPayload['vendor'];
    issueDate: string;
    periodStartStr: string;
    periodEndStr: string;
    totals: AccountStatementReportPayload['totals'];
    purchaseOrders: Record<string, unknown>[];
    receptions: Record<string, unknown>[];
    payments: Record<string, unknown>[];
    rebates: Record<string, unknown>[];
    facturas: Record<string, unknown>[];
    notasCredito: Record<string, unknown>[];
    catalogs: AccountStatementReportPayload['catalogs'];
};

function toViewModel(payload: AccountStatementReportPayload): ViewModel {
    return {
        issuer: payload.issuer ?? ISSUER_FALLBACK,
        vendor: payload.vendor,
        issueDate: payload.dates.issueDate
            ? formatDate(payload.dates.issueDate)
            : formatDate(new Date()),
        periodStartStr: payload.dates.periodStart
            ? formatDate(payload.dates.periodStart)
            : 'N/A',
        periodEndStr: payload.dates.periodEnd
            ? formatDate(payload.dates.periodEnd)
            : 'N/A',
        totals: payload.totals,
        purchaseOrders: payload.purchaseOrders ?? [],
        receptions: payload.receptions ?? [],
        payments: payload.payments ?? [],
        rebates: payload.rebates ?? [],
        facturas: payload.facturas ?? [],
        notasCredito: payload.notasCredito ?? [],
        catalogs: payload.catalogs,
    };
}

function renderStatusCell(
    status: string | number,
    tableName: string,
    catalogs: ViewModel['catalogs']
): string {
    const raw = String(status ?? '').trim();
    const numeric = Number(raw);
    const defaultBg = '#E5E7EB';
    const defaultText = '#111827';

    const statusCell = (label: string, bgColor?: string | null) =>
        `<td style="background: ${bgColor || defaultBg}; color: ${defaultText}; font-weight: bold;">${escapeHtml(label)}</td>`;

    let catalog: CatalogStatusItem[] = [];
    switch (tableName) {
        case 'Purchase':
            catalog = catalogs.paymentStatus ?? [];
            break;
        case 'Invoice':
            catalog = catalogs.invoiceStatus ?? [];
            break;
        case 'Credit':
            catalog = catalogs.creditNoteStatus ?? [];
            break;
        default:
            catalog = [];
    }

    const match = catalog.find(
        (item) =>
            item.key === raw ||
            item.description.toLowerCase() === raw.toLowerCase() ||
            (Number.isFinite(numeric) && item.sortOrder === numeric)
    );

    if (match) {
        return statusCell(match.description, match.color);
    }

    const normalized = raw.toLowerCase();
    switch (normalized) {
        case 'pagada':
            return statusCell('Pagada', '#A9E5BB');
        case 'pendiente':
            return statusCell('Pendiente', '#FFE69C');
        case 'compensada':
            return statusCell('Compensada', '#BFD7FF');
        case 'aplicado':
            return statusCell('Aplicado', '#D1D5DB');
        default:
            return statusCell(raw);
    }
}

/** HTML del estado de cuenta (misma estructura que accountStatementPdf.service.ts en back). */
export function buildAccountStatementHtml(
    payload: AccountStatementReportPayload
): string {
    const data = toViewModel(payload);
    const h = escapeHtml;
    const vn = data.vendor.vendorNumber;

    const ocRows = data.purchaseOrders.map((po) => {
        const purchaseOrderId = po.purchaseOrderId as string | undefined;
        const rec = data.receptions.find(
            (r) => r.purchaseOrderId === purchaseOrderId
        );
        return [
            vn,
            String(po.orderNumber ?? ''),
            rec?.receptionNumber != null ? String(rec.receptionNumber) : '-',
            formatDate(po.purchaseOrderDate as string),
            rec?.receptionDate
                ? formatDate(rec.receptionDate as string)
                : '-',
            '-',
            'MXN',
            formatCurrency(Number(po.amount) || 0),
            '1',
            formatCurrency(Number(po.amount) || 0),
            po.status != null ? String(po.status) : 'N/A',
        ];
    });

    const fpRows = data.payments.map((p) => [
        vn,
        'Pago',
        String(p.documentNumber ?? ''),
        String(p.fiscalPaymentUuid ?? '').slice(0, 14) || '-',
        formatDate(p.paymentDate as string),
        '-',
        formatDate(p.paymentDate as string),
        formatDate(p.createdAt as string),
        String(p.currency ?? 'MXN'),
        formatCurrency(parseNum(p.amount)),
        '1',
        formatCurrency(parseNum(p.amount)),
        'Pagada',
    ]);

    const descRows = data.rebates.map((b) => [
        vn,
        'Desc. Comercial',
        String(b.documentNumber ?? ''),
        String(b.rebateId ?? '').slice(0, 14) || '-',
        formatDate(b.postingDate as string),
        formatDate(b.postingDate as string),
        'MXN',
        formatCurrency(Number(b.amount) || 0),
        '1',
        formatCurrency(Number(b.amount) || 0),
        b.status != null ? String(b.status) : 'Aplicado',
    ]);

    const pendRows = data.facturas.map((i) => [
        vn,
        'Factura',
        String(i.folio ?? ''),
        String(i.invoiceUuid ?? '').slice(0, 14) || '-',
        formatDate(i.issueDate as string),
        '-',
        formatDate((i.certificationDate ?? i.issueDate) as string),
        'MXN',
        formatCurrency(Number(i.total) || 0),
        '1',
        formatCurrency(Number(i.total) || 0),
        'Pendiente',
    ]);

    const ncRows = data.notasCredito.map((i) => [
        vn,
        'Nota Crédito',
        String(i.folio ?? ''),
        String(i.invoiceUuid ?? '').slice(0, 14) || '-',
        formatDate(i.issueDate as string),
        formatDate((i.certificationDate ?? i.issueDate) as string),
        'MXN',
        formatCurrency(Number(i.total) || 0),
        '1',
        formatCurrency(Number(i.total) || 0),
        'Compensada',
    ]);

    const renderTable = (
        headers: string[],
        rows: (string | number)[][],
        footerLabel: string,
        footerValue: string,
        tableName: string
    ) => {
        const thead = headers.map((c) => `<th>${h(c)}</th>`).join('');
        const statusIdx = headers.findIndex((header) => header === 'Estatus');
        const body = rows
            .map((row, i) => {
                const cells = row
                    .map((cell, idx) => {
                        if (idx === statusIdx) {
                            return renderStatusCell(cell, tableName, data.catalogs);
                        }
                        return `<td>${h(String(cell))}</td>`;
                    })
                    .join('');
                return `<tr class="row-${i % 2 === 0 ? 'even' : 'odd'}">${cells}</tr>`;
            })
            .join('');
        return `
    <table class="data-table">
      <thead><tr>${thead}</tr></thead>
      <tbody>${body}</tbody>
      <tfoot><tr><td colspan="${headers.length - 1}" class="footer-label">${h(footerLabel)}</td><td class="footer-value">${h(footerValue)}</td></tr></tfoot>
    </table>`;
    };

    const renderDisclaimer = () => `<p class="disclaimer">
    Este documento y la información plasmada en él, es para uso exclusivo de Comercializadora SDMHC S.A. de C.V. (SODIMAC MÉXICO), de cualquiera de
sus empresas controladoras, filiales, subsidiarias y proveedores relacionados, por lo que podrán generarse consecuencias legales con la persona o grupo
de personas que revelen, reproduzcan o distribuyan, parcial o totalmente, la información contenida en el presente documento. En caso de que este
documento haya llegado a sus manos de forma accidental, por favor destrúyalo.
</p>`;

    const renderHeader = () => `<header class="page-header">
    <div class="logo-placeholder"><img width="100" src="https://www.sodimac.cl/static/site/nuestra-empresa/RS-sodimac2016/img/logo-sodimac.png" alt="Logo"></div>
    <div class="header-bar">ESTADO DE CUENTA</div>
  </header>`;

    const chartData = [
        { label: 'Total OC', value: data.totals.totalOC, color: '#0b66b1' },
        {
            label: 'Facturas Pendientes',
            value: data.totals.totalFacturasPendientes,
            color: '#c53030',
        },
        {
            label: 'Facturas Pagadas',
            value: data.totals.totalFacturasPagadas,
            color: '#276749',
        },
        { label: 'Descuentos', value: data.totals.totalDescuentos, color: '#b7791f' },
        {
            label: 'Notas de Crédito',
            value: data.totals.totalNotasCredito,
            color: '#6b46c1',
        },
        { label: 'Saldo Neto', value: data.totals.saldoPendiente, color: '#0d9488' },
    ];
    const maxChartVal = Math.max(...chartData.map((d) => Math.abs(d.value)), 1);
    const t = data.totals;

    return `<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Estado de Cuenta</title>
  <style>
    * { box-sizing: border-box; }
    body { font-family: Helvetica, Arial, sans-serif; font-size: 11px; color: #111; margin: 0; padding: 0; }
    .page-header { display: flex; align-items: stretch; margin-bottom: 10px; }
    .logo-placeholder { width: 180px; min-width: 180px; height: 50px; background: #f0f0f0; border: 1px solid #9ca3af; display: flex; align-items: center; justify-content: center; font-size: 8px; color: #6b7280; }
    .header-bar { flex: 1; background: #1470C0; color: white; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 14px; border-radius: 0 10px 10px 0; margin-left: 0; }
    .top-block { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
    .issuer { max-width: 55%; }
    .issuer p { margin: 0 0 4px; line-height: 1.3; }
    .meta { width: 320px; text-align: center; }
    .meta-cards { display: flex; flex-direction: column; gap: 10px; }
    .meta-card { width: 100%; }
    .meta-title { background: #ef1f2f; color: #fff; font-weight: bold; font-size: 12px; line-height: 1.1; padding: 8px 10px; border-radius: 0; }
    .meta-value-box { background: #ececec; color: #111; font-size: 12px; padding: 9px 10px; }
    .meta-date { color: #111; font-weight: normal; font-size: 12px; }
    .section-title { text-align:center; background: linear-gradient(90deg, #0b66b1, #ebebeb); color: white; padding: 8px; font-weight: bold; font-size: 14px; margin: 16px 0 0px; }
    .section-title-red { text-align:center; background: linear-gradient(90deg, #b91c1c, #ebebeb); color: white; padding: 8px; font-weight: bold; font-size: 14px; margin: 16px 0 0px;  }
    .two-cols { display: flex; gap: 16px; align-items: flex-start; margin-bottom: 12px; }
    .two-cols .col-left { flex: 0 0 auto; }
    .two-cols .col-right { flex: 1; min-width: 0; }
    .vendor-logo-box { width: 120px; height: 100px; background: #f3f4f6; border: 1px dashed #9ca3af; display: flex; align-items: center; justify-content: center; font-size: 8px; color: #6b7280; border-radius: 4px; }
    .summary-with-charts { display: flex; gap: 20px; align-items: flex-start; }
    .summary-with-charts .summary-table-wrap { flex: 1; min-width: 0; }
    .summary-with-charts .summary-charts-wrap { flex: 0 0 280px; }
    .disclaimer { font-size: 8px; color: #666; text-align: center; margin: 12px 0; }
    .data-table { width: 100%; border-collapse: collapse; margin-bottom: 8px; font-size: 9px; border: 1px solid #d1d5db; border-radius: 4px; overflow: hidden; }
    .data-table th { background: #666666; color: white; padding: 10px 8px; text-align: left; font-weight: bold; }
    .data-table td { padding: 6px 8px; }
    .data-table tbody tr.row-even { background: #f3f4f6; }
    .data-table tbody tr.row-odd { background: #e5e7eb; }
    .data-table tfoot tr { background: #666666; font-weight: bold; color:white; }
    .data-table tfoot .footer-value { background: #9b9b9b; }
    .data-table tfoot td { padding: 6px 8px; }
    .chart { margin: 12px 0; }
    .chart-row { display: flex; align-items: center; margin-bottom: 6px; font-size: 9px; }
    .chart-label { width: 120px; }
    .chart-bar-wrap { flex: 1; max-width: 280px; height: 18px; background: #f0f0f0; margin: 0 8px; overflow: hidden; }
    .chart-bar { height: 100%; min-width: 2px; }
    .chart-value { width: 90px; text-align: right; }
    .pie-charts { display: flex; flex-wrap: wrap; gap: 16px; align-items: flex-start; margin: 12px 0; }
    .pie-item { display: flex; flex-direction: column; align-items: center; }
    .pie-ring { width: 70px; height: 70px; border-radius: 50%; background: conic-gradient(var(--pie-color) calc(var(--pie-pct) * 1%), #e5e7eb 0); }
    .pie-item span { font-size: 8px; margin-top: 4px; text-align: center; max-width: 80px; }
    .saldo-neto-final { background: linear-gradient(135deg, #b91c1c, #dc2626); color: white; padding: 20px 24px; font-size: 22px; font-weight: bold; text-align: center; border-radius: 8px; margin-top: 16px; }
    .footer-doc { font-size: 8px; text-align: center; margin-top: 24px; color: #666; }
    @media print {
      body { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
      .page-break { page-break-before: always; }
    }
  </style>
</head>
<body style="padding: 40px; max-width: 800px; margin: 0 auto;">
  ${renderHeader()}

  <div class="top-block">
    <div class="issuer">
      <p><strong>Nombre</strong>: ${h(data.issuer.name)}</p>
      <p><strong>Dirección</strong>: ${h(data.issuer.address)}</p>
      <p><strong>RFC</strong>: ${h(data.issuer.rfc)}</p>
      <p><strong>Teléfono</strong>: ${h(data.issuer.phone)}</p>
      <p><strong>Correo Electrónico</strong>: ${h(data.issuer.email)}</p>
    </div>
    <div class="meta">
      <div class="meta-cards">
        <div class="meta-card">
          <div class="meta-title">Fecha de Emisión</div>
          <div class="meta-value-box"><span class="meta-date">${h(data.issueDate)}</span></div>
        </div>
        <div class="meta-card">
          <div class="meta-title">Periodo del Extracto</div>
          <div class="meta-value-box"><span class="meta-date">${h(data.periodStartStr)} al ${h(data.periodEndStr)}</span></div>
        </div>
      </div>
    </div>
  </div>

  <div class="section-title-red">DATOS DEL PROVEEDOR</div>
  <div class="two-cols">
    <div class="col-left">
      <div class="vendor-logo-box">Logo del proveedor</div>
    </div>
    <div class="col-right">
      <p><strong style="color:blue">Nombre y/o Razón Social:</strong> ${h(data.vendor.vendorName)}</p>
      <p><strong style="color:blue">RFC:</strong> ${h(data.vendor.providerRfc)}</p>
      <p><strong style="color:blue">Código del Proveedor:</strong> ${data.vendor.vendorNumber}</p>
      <p><strong style="color:blue">CP:</strong> ${h(data.vendor.providerCp)}</p>
      <p><strong style="color:blue">Dirección:</strong> ${h(data.vendor.providerAddress)}</p>
      <p><strong style="color:blue">Contacto:</strong> ${h(data.vendor.providerContact)}</p>
      <p><strong style="color:blue">Correo Contacto:</strong> ${h(data.vendor.providerEmail)}</p>
    </div>
  </div>

  <div class="section-title-red">RESUMEN GENERAL</div>
  <div class="summary-with-charts">
    <div class="summary-table-wrap">
  ${renderTable(
      ['Concepto', 'Monto', 'No. Documentos'],
      [
        ['Total Orden de Compra', formatCurrency(t.totalOC), String(t.counts.purchaseOrders)],
        ['Total Orden de Compra Pendiente', formatCurrency(0), '0'],
        ['Total Facturas Pendiente Pago', formatCurrency(t.totalFacturasPendientes), String(t.counts.facturas)],
        ['Total Facturas Pagadas', formatCurrency(t.totalFacturasPagadas), String(t.counts.payments)],
        ['Total Descuentos', formatCurrency(t.totalDescuentos), String(t.counts.rebates)],
        ['Total Notas de Crédito', formatCurrency(t.totalNotasCredito), String(t.counts.notasCredito)],
      ],
      'SALDO PENDIENTE',
      formatCurrency(t.saldoPendiente),
      ''
  )}
    </div>
    <div class="summary-charts-wrap">
  <div class="chart">
    ${chartData
        .map(
            (d) =>
                `<div class="chart-row">
      <span class="chart-label">${h(d.label)}</span>
      <div class="chart-bar-wrap"><div class="chart-bar" style="width:${(Math.abs(d.value) / maxChartVal) * 100}%; background:${d.color}"></div></div>
      <span class="chart-value">${formatCurrency(d.value)}</span>
    </div>`
        )
        .join('')}
  </div>
    </div>
  </div>

  ${renderDisclaimer()}
  <div class="page-break"></div>
  ${renderHeader()}
  <div class="section-title">ÓRDENES DE COMPRA</div>
  ${renderTable(
      ['Cuenta', 'No. OC', 'No. Rec.', 'F. Doc', 'F. Rec', 'Venc.', 'Mon', 'Monto Origen', 'TC', 'Monto Local', 'Estatus'],
      ocRows,
      'TOTAL',
      formatCurrency(t.totalOC),
      'Purchase'
  )}

  <div class="section-title">FACTURAS PAGADAS</div>
  ${renderTable(
      ['Cuenta', 'Tipo', 'Ref', 'UUID', 'F. Doc', 'Venc', 'F. Pago', 'F. Contab', 'Mon', 'Monto Or.', 'TC', 'Monto Loc', 'Estatus'],
      fpRows,
      'TOTAL',
      formatCurrency(t.totalFacturasPagadas),
      'Invoice'
  )}

  <div class="section-title">FACTURAS PENDIENTES DE PAGO</div>
  ${renderTable(
      ['Cuenta', 'Tipo', 'Ref', 'UUID', 'F. Doc', 'Venc', 'F. Contab', 'Mon', 'Monto Or.', 'TC', 'Monto Loc', 'Estatus'],
      pendRows,
      'TOTAL',
      formatCurrency(t.totalFacturasPendientes),
      'Invoice'
  )}

   <div class="section-title">DESCUENTOS APLICADOS (REBATES)</div>
  ${renderTable(
      ['Cuenta', 'Tipo', 'Ref', 'UUID', 'F. Doc', 'F. Contab', 'Mon', 'Monto Or.', 'TC', 'Monto Loc', 'Estatus'],
      descRows,
      'TOTAL',
      formatCurrency(t.totalDescuentos),
      'Rebate'
  )}
${renderDisclaimer()}
  <div class="page-break"></div>
  ${renderHeader()}

  <div class="section-title">NOTAS DE CRÉDITO</div>
  ${renderTable(
      ['Cuenta', 'Tipo', 'Ref', 'UUID', 'F. Doc', 'F. Contab', 'Mon', 'Monto Or.', 'TC', 'Monto Loc', 'Estatus'],
      ncRows,
      'TOTAL',
      formatCurrency(t.totalNotasCredito),
      'Credit'
  )}

  <div class="section-title-red">ESTADÍSTICAS</div>
  <div class="pie-charts">
    ${chartData
        .map((d) => {
            const pct = maxChartVal > 0 ? (Math.abs(d.value) / maxChartVal) * 100 : 0;
            return `<div class="pie-item" style="--pie-color: ${d.color}; --pie-pct: ${pct}">
      <div class="pie-ring"></div>
      <span>${h(d.label)}<br>${formatCurrency(d.value)}</span>
    </div>`;
        })
        .join('')}
  </div>
  <div class="saldo-neto-final">Saldo Neto: ${formatCurrency(t.saldoPendiente)}</div>
${renderDisclaimer()}

  <div class="footer-doc">
    <p>Generado el ${formatDateTime(new Date(), { seconds: true })}</p>
    <p>Sistema de Finanzas - Documento generado automáticamente</p>
  </div>
  <script>
    window.addEventListener('load', function () {
      setTimeout(function () { window.print(); }, 600);
    });
  </script>
</body>
</html>`;
}
