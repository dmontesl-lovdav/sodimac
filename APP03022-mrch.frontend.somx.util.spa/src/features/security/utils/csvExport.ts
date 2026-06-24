import type { SecurityRow } from '../types';

function buildCsvFilename(title: string): string {
  const now = new Date();
  const dd = String(now.getDate()).padStart(2, '0');
  const mm = String(now.getMonth() + 1).padStart(2, '0');
  const yyyy = String(now.getFullYear());
  const hh = String(now.getHours()).padStart(2, '0');
  const min = String(now.getMinutes()).padStart(2, '0');
  const safeTitle = title
    .trim()
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .replace(/[\\/:*?"<>|]/g, '')
    .replace(/\s+/g, '-');
  return `${safeTitle}-${dd}-${mm}-${yyyy}-${hh}-${min}.csv`;
}

export function exportSecurityRowsAsCsv(rows: SecurityRow[], title: string) {
  const headers = ['id', 'clave', 'nombre', 'estatus', 'totalAsignados'];
  const csvRows = rows.map((item) => [
    item.id,
    item.catalogKey,
    item.name,
    item.status === 1 ? 'Activo' : 'Inactivo',
    item.totalAssigned,
  ]);
  const csv = [
    headers.join(','),
    ...csvRows.map((row) =>
      row.map((value) => `"${String(value ?? '').replace(/"/g, '""')}"`).join(','),
    ),
  ].join('\n');

  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = buildCsvFilename(title);
  link.click();
  window.URL.revokeObjectURL(url);
}
