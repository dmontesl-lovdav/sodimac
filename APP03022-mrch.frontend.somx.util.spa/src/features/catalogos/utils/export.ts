import { saveAs } from 'file-saver';
import * as XLSX from 'xlsx';

export interface ExportColumn {
  key: string;
  label: string;
}

/**
 * Timestamp para nombres de archivo en formato `yyyymmdd.hh24mmss` (hora local).
 * Ej: 2026-09-22 14:32:01 -> "20260922.143201".
 */
export const formatFilenameTimestamp = (date: Date = new Date()): string => {
  const pad = (n: number): string => String(n).padStart(2, '0');
  const yyyy = date.getFullYear();
  const mm = pad(date.getMonth() + 1);
  const dd = pad(date.getDate());
  const hh = pad(date.getHours());
  const min = pad(date.getMinutes());
  const ss = pad(date.getSeconds());
  return `${yyyy}${mm}${dd}.${hh}${min}${ss}`;
};

const normalizeFilename = (filename: string): string => {
  return filename
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-zA-Z0-9._-]+/g, '_')
    .replace(/_+/g, '_')
    .replace(/(?:^_+)|(?:_+$)/g, '')
    .toLowerCase();
};

export const exportToCSV = (
  data: Record<string, unknown>[],
  columns: ExportColumn[],
  filename: string
): void => {
  const headers = columns.map((col) => col.label).join(',');
  const rows = data.map((row) =>
    columns
      .map((col) => {
        const value = row[col.key];
        if (value === null || value === undefined) return '';
        const strValue = String(value);
        if (strValue.includes(',') || strValue.includes('"') || strValue.includes('\n')) {
          return `"${strValue.replace(/"/g, '""')}"`;
        }
        return strValue;
      })
      .join(',')
  );

  const csvContent = [headers, ...rows].join('\n');
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' });
  saveAs(blob, `${normalizeFilename(filename)}.csv`);
};

export const exportToExcel = (
  data: Record<string, unknown>[],
  columns: ExportColumn[],
  filename: string
): void => {
  const worksheetData = [
    columns.map((col) => col.label),
    ...data.map((row) =>
      columns.map((col) => {
        const value = row[col.key];
        return value ?? '';
      })
    ),
  ];

  const worksheet = XLSX.utils.aoa_to_sheet(worksheetData);
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Datos');

  const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
  const blob = new Blob([excelBuffer], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  });
  saveAs(blob, `${normalizeFilename(filename)}.xlsx`);
};








