import type { Readable } from 'node:stream';
import { Readable as StreamReadable } from 'node:stream';
import { getDataSource } from '@/config/typeorm-datasource.js';
import * as r from '@/repositories/accountStatement.repo.js';
import * as poRepo from '@/repositories/purchaseOrder.repo.js';
import * as recRepo from '@/repositories/reception.repo.js';
import * as rebateRepo from '@/repositories/rebate.repo.js';
import * as fpRepo from '@/repositories/fiscalPayment.repo.js';
import { Invoice } from '@/entities/tenant_fiscal.invoice.entity.js';
import { Addendum } from '@/entities/tenant_fiscal.addendum.entity.js';
import type { PurchaseOrder } from '@/entities/PurchaseOrder.entity.js';
import type { Reception } from '@/entities/Reception.entity.js';
import type { Rebate } from '@/entities/Rebate.entity.js';
import type { FiscalPayment } from '@/entities/FiscalPayment.entity.js';
import { Catalogs } from '@/utils/mockups.js';

async function findInvoicesByVendorAndPeriod(vendorNumber: number, start: Date, end: Date) {
    return getDataSource()
        .getRepository(Invoice)
        .createQueryBuilder('i')
        .innerJoin(Addendum, 'a', 'a.invoiceUuid = i.invoiceUuid')
        .where('a.supplierNumber = :vendor', { vendor: vendorNumber })
        .andWhere('i.issueDate BETWEEN :start AND :end', { start, end })
        .orderBy('i.issueDate', 'ASC')
        .getMany();
}

const ISSUER = {
    name: 'Comercializadora SDMHC S.A. de C.V. (SODIMAC MÉXICO)',
    address: 'Av. Adolfo López Mateos No. 201, Col. Santa Cruz Acatlán, CP 53150, Naucalpan de Juárez, Estado de México.',
    rfc: 'CSD161207R2A',
    phone: '800 0625 222',
    email: 'analista@sodimac.com.mx',
};

function parseNum(v: string | number | null | undefined): number {
    if (v == null) return 0;
    if (typeof v === 'number') return v;
    const n = parseFloat(String(v).replace(/,/g, ''));
    return Number.isFinite(n) ? n : 0;
}

function formatDate(date: Date | string | null | undefined): string {
    if (!date) return 'N/A';
    const d = typeof date === 'string' ? new Date(date) : date;
    return d.toLocaleDateString('es-MX', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(amount);
}

function escapeHtml(s: string): string {
    return String(s)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

interface PdfData {
    vendorNumber: number;
    vendorName: string;
    providerRfc: string;
    providerCp: string;
    providerAddress: string;
    providerContact: string;
    providerEmail: string;
    issueDate: string;
    periodStartStr: string;
    periodEndStr: string;
    totalOC: number;
    totalFacturasPendientes: number;
    totalFacturasPagadas: number;
    totalDescuentos: number;
    totalNotasCredito: number;
    saldoPendiente: number;
    purchaseOrders: PurchaseOrder[];
    receptions: Reception[];
    payments: FiscalPayment[];
    rebates: Rebate[];
    facturas: Invoice[];
    notasCredito: Invoice[];
}

function buildEstadoCuentaHtml(data: PdfData): string {
    const h = escapeHtml;
    const ocRows = data.purchaseOrders.map((po: PurchaseOrder) => {
        const rec = data.receptions.find((r: Reception) => r.purchaseOrderId === po.purchaseOrderId);
        return [
            data.vendorNumber,
            po.orderNumber ?? '',
            rec?.receptionNumber ?? '-',
            formatDate(po.purchaseOrderDate),
            rec?.receptionDate ? formatDate(rec.receptionDate) : '-',
            '-',
            'MXN',
            formatCurrency(Number(po.amount) || 0),
            '1',
            formatCurrency(Number(po.amount) || 0),
            po.status != null ? String(po.status) : 'N/A',
        ];
    });
    const fpRows = data.payments.map((p: FiscalPayment) => [
        data.vendorNumber,
        'Pago',
        p.documentNumber ?? '',
        (p.fiscalPaymentUuid ?? '').slice(0, 14) || '-',
        formatDate(p.paymentDate),
        '-',
        formatDate(p.paymentDate),
        formatDate(p.createdAt),
        p.currency ?? 'MXN',
        formatCurrency(parseNum(p.amount)),
        '1',
        formatCurrency(parseNum(p.amount)),
        'Pagada',
    ]);
    const descRows = data.rebates.map((b: Rebate) => [
        data.vendorNumber,
        'Desc. Comercial',
        b.documentNumber ?? '',
        (b.rebateId ?? '').slice(0, 14) || '-',
        formatDate(b.postingDate),
        formatDate(b.postingDate),
        'MXN',
        formatCurrency(Number(b.amount) || 0),
        '1',
        formatCurrency(Number(b.amount) || 0),
        b.status != null ? String(b.status) : 'Aplicado',
    ]);
    const pendRows = data.facturas.map((i: Invoice) => [
        data.vendorNumber,
        'Factura',
        i.folio ?? '',
        (i.invoiceUuid ?? '').slice(0, 14) || '-',
        formatDate(i.issueDate),
        '-',
        formatDate(i.certificationDate ?? i.issueDate),
        'MXN',
        formatCurrency(Number(i.total) || 0),
        '1',
        formatCurrency(Number(i.total) || 0),
        'Pendiente',
    ]);
    const ncRows = data.notasCredito.map((i: Invoice) => [
        data.vendorNumber,
        'Nota Crédito',
        i.folio ?? '',
        (i.invoiceUuid ?? '').slice(0, 14) || '-',
        formatDate(i.issueDate),
        formatDate(i.certificationDate ?? i.issueDate),
        'MXN',
        formatCurrency(Number(i.total) || 0),
        '1',
        formatCurrency(Number(i.total) || 0),
        'Compensada',
    ]);

    const renderTable = (headers: string[], rows: (string | number)[][], footerLabel: string, footerValue: string, tableName: string) => {
        const thead = headers.map((c) => `<th>${h(c)}</th>`).join('');
      const statusIdx = headers.findIndex((header) => header === 'Estatus');
        const body = rows
            .map(
          (row, i) =>{
            const cells = row
              .map((cell, idx) => {
                if (idx === statusIdx) {
                  return renderStatusCell(cell, tableName);
                }
                return `<td>${h(String(cell))}</td>`;
              })
              .join('');
            return `<tr class="row-${i % 2 === 0 ? 'even' : 'odd'}">${cells}</tr>`;
                }
            )
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

function renderStatusCell(status: string | number, tableName: string): string {
  const raw = String(status ?? '').trim();
  const numeric = Number(raw);

  const defaultBg = '#E5E7EB';
  const defaultText = '#111827';

  const statusCell = (label: string, bgColor?: string | null) =>
    `<td style="background: ${bgColor || defaultBg}; color: ${defaultText}; font-weight: bold;">${escapeHtml(label)}</td>`;

  let catalog: Array<{ key: string; description: string; color: string | null; sortOrder?: number }> = [];

  switch (tableName) {
    case 'Purchase':
      catalog = Catalogs.CatEstatusPago;
      break;
    case 'Invoice':
      catalog = Catalogs.CatEstatusFactura;
      break;
    case 'Credit':
      catalog = Catalogs.CatEstatusNotaCredito;
      break;
    case 'Rebate':
      catalog = [];
      break;
    default:
      catalog = [];
      break;
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

const renderHeader = () => `<header class="page-header">
    <div class="logo-placeholder"><img width="100" src="https://www.sodimac.cl/static/site/nuestra-empresa/RS-sodimac2016/img/logo-sodimac.png" alt="Logo"></div>
    <div class="header-bar">ESTADO DE CUENTA</div>
  </header>`;

    const chartData = [
        { label: 'Total OC', value: data.totalOC, color: '#0b66b1' },
        { label: 'Facturas Pendientes', value: data.totalFacturasPendientes, color: '#c53030' },
        { label: 'Facturas Pagadas', value: data.totalFacturasPagadas, color: '#276749' },
        { label: 'Descuentos', value: data.totalDescuentos, color: '#b7791f' },
        { label: 'Notas de Crédito', value: data.totalNotasCredito, color: '#6b46c1' },
        { label: 'Saldo Neto', value: data.saldoPendiente, color: '#0d9488' },
    ];
    const maxChartVal = Math.max(...chartData.map((d) => Math.abs(d.value)), 1);

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
    .meta-value { color: #b91c1c; font-weight: bold; }
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
      <p><strong>Nombre</strong>: ${h(ISSUER.name)}</p>
      <p><strong>Dirección</strong>: ${h(ISSUER.address)}</p>
      <p><strong>RFC</strong>: ${h(ISSUER.rfc)}</p>
      <p><strong>Teléfono</strong>: ${h(ISSUER.phone)}</p>
      <p><strong>Correo Electrónico</strong>: ${h(ISSUER.email)}</p>
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
      <p><strong style="color:blue">Nombre y/o Razón Social:</strong> ${h(data.vendorName)}</p>
      <p><strong style="color:blue">RFC:</strong> ${h(data.providerRfc)}</p>
      <p><strong style="color:blue">Código del Proveedor:</strong> ${data.vendorNumber}</p>
      <p><strong style="color:blue">CP:</strong> ${h(data.providerCp)}</p>
      <p><strong style="color:blue">Dirección:</strong> ${h(data.providerAddress)}</p>
      <p><strong style="color:blue">Contacto:</strong> ${h(data.providerContact)}</p>
      <p><strong style="color:blue">Correo Contacto:</strong> ${h(data.providerEmail)}</p>
    </div>
  </div>

  <div class="section-title-red">RESUMEN GENERAL</div>
  <div class="summary-with-charts">
    <div class="summary-table-wrap">
  ${renderTable(
      ['Concepto', 'Monto', 'No. Documentos'],
      [
        ['Total Orden de Compra', formatCurrency(data.totalOC), String(data.purchaseOrders.length)],
        ['Total Orden de Compra Pendiente', formatCurrency(0), '0'],
        ['Total Facturas Pendiente Pago', formatCurrency(data.totalFacturasPendientes), String(data.facturas.length)],
        ['Total Facturas Pagadas', formatCurrency(data.totalFacturasPagadas), String(data.payments.length)],
        ['Total Descuentos', formatCurrency(data.totalDescuentos), String(data.rebates.length)],
        ['Total Notas de Crédito', formatCurrency(data.totalNotasCredito), String(data.notasCredito.length)],
      ],
      'SALDO PENDIENTE',
      formatCurrency(data.saldoPendiente),
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
      formatCurrency(data.totalOC),
    'Purchase'
  )}

  <div class="section-title">FACTURAS PAGADAS</div>
  ${renderTable(
      ['Cuenta', 'Tipo', 'Ref', 'UUID', 'F. Doc', 'Venc', 'F. Pago', 'F. Contab', 'Mon', 'Monto Or.', 'TC', 'Monto Loc', 'Estatus'],
      fpRows,
      'TOTAL',
      formatCurrency(data.totalFacturasPagadas),
      'Invoice'
  )}

 

  <div class="section-title">FACTURAS PENDIENTES DE PAGO</div>
  ${renderTable(
      ['Cuenta', 'Tipo', 'Ref', 'UUID', 'F. Doc', 'Venc', 'F. Contab', 'Mon', 'Monto Or.', 'TC', 'Monto Loc', 'Estatus'],
      pendRows,
      'TOTAL',
      formatCurrency(data.totalFacturasPendientes),
      'Invoice'
  )}

   <div class="section-title">DESCUENTOS APLICADOS (REBATES)</div>
  ${renderTable(
      ['Cuenta', 'Tipo', 'Ref', 'UUID', 'F. Doc', 'F. Contab', 'Mon', 'Monto Or.', 'TC', 'Monto Loc', 'Estatus'],
      descRows,
      'TOTAL',
      formatCurrency(data.totalDescuentos),
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
      formatCurrency(data.totalNotasCredito),
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
  <div class="saldo-neto-final">Saldo Neto: ${formatCurrency(data.saldoPendiente)}</div>
${renderDisclaimer()}
  

  <div class="footer-doc">
    <p>Generado el ${new Date().toLocaleDateString('es-MX', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' })}</p>
    <p>Sistema de Finanzas - Documento generado automáticamente</p>
  </div>
</body>
</html>`;
}

async function htmlToPdfBuffer(html: string): Promise<Buffer> {
    const puppeteer = await import('puppeteer');
    const browser = await puppeteer.default.launch({
        headless: true,
        args: ['--no-sandbox', '--disable-setuid-sandbox'],
    });
    try {
        const page = await browser.newPage();
        await page.setContent(html, { waitUntil: 'networkidle0' });
        const pdfBuffer = await page.pdf({
            format: 'Letter',
            margin: { top: '40px', right: '40px', bottom: '40px', left: '40px' },
            printBackground: true,
        });
        return Buffer.from(pdfBuffer);
    } finally {
        await browser.close();
    }
}

export async function generatePdfStream(uuid: string): Promise<Readable | null> {
    const buffer = await generatePdfBuffer(uuid);
    if (!buffer) return null;
    const stream = new StreamReadable();
    stream.push(buffer);
    stream.push(null);
    return stream;
}

export async function generatePdfBuffer(uuid: string): Promise<Buffer | null> {
    const row = await r.findById(uuid);
    if (!row) return null;
    const vendorNumber = 1001;
    const periodStart = "2024-01-01";
    const startDate = periodStart
        ? new Date(periodStart)
        : new Date(row.year, row.month - 1, 1);
    const endDate = row.periodEnd
        ? new Date(row.periodEnd)
        : new Date(row.year, row.month, 0, 23, 59, 59);

    const [purchaseOrders, receptions, rebates, payments, invoices] = await Promise.all([
        poRepo.findByVendorAndDateRange(vendorNumber, startDate, endDate),
        recRepo.findByVendorAndDateRange(vendorNumber, startDate, endDate),
        rebateRepo.findByVendorAndPostingDateRange(vendorNumber, startDate, endDate),
        fpRepo.findByVendorAndPaymentDateRange(vendorNumber, startDate, endDate),
        findInvoicesByVendorAndPeriod(vendorNumber, startDate, endDate),
    ]);

    const facturas = invoices.filter((inv: Invoice) => inv.documentType === 'I');
    const notasCredito = invoices.filter((inv: Invoice) => inv.documentType === 'E');
    const totalOC = purchaseOrders.reduce((s: number, po: PurchaseOrder) => s + (Number(po.amount) || 0), 0);
    const totalFacturasPendientes = facturas.reduce((s: number, i: Invoice) => s + (Number(i.total) || 0), 0);
    const totalFacturasPagadas = payments.reduce((s: number, p: FiscalPayment) => s + parseNum(p.amount), 0);
    const totalDescuentos = rebates.reduce((s: number, b: Rebate) => s + (Number(b.amount) || 0), 0);
    const totalNotasCredito = notasCredito.reduce((s: number, i: Invoice) => s + (Number(i.total) || 0), 0);
    const saldoPendiente = parseNum(row.finalBalance);

    const data: PdfData = {
        vendorNumber,
        vendorName: `Proveedor ${vendorNumber}`,
        providerRfc: 'N/A',
        providerCp: '',
        providerAddress: '',
        providerContact: '',
        providerEmail: '',
        issueDate: row.issuedAt ? formatDate(row.issuedAt) : formatDate(new Date()),
        periodStartStr: formatDate(startDate),
        periodEndStr: formatDate(endDate),
        totalOC,
        totalFacturasPendientes,
        totalFacturasPagadas,
        totalDescuentos,
        totalNotasCredito,
        saldoPendiente,
        purchaseOrders,
        receptions,
        payments,
        rebates,
        facturas,
        notasCredito,
    };

    const html = buildEstadoCuentaHtml(data);
    const buffer = await htmlToPdfBuffer(html);
    return buffer;
}
