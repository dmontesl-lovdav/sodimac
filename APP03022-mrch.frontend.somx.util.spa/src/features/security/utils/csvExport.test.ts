import { exportSecurityRowsAsCsv } from './csvExport';
import type { SecurityRow } from '../types';

const OriginalBlob = global.Blob;

describe('exportSecurityRowsAsCsv', () => {
  const originalCreate = window.URL.createObjectURL;
  const originalRevoke = window.URL.revokeObjectURL;
  let createSpy: jest.Mock;
  let revokeSpy: jest.Mock;
  let clickSpy: jest.SpyInstance;
  let lastAnchor: HTMLAnchorElement | null;
  let capturedCsv: string;

  beforeEach(() => {
    capturedCsv = '';
    // Mock Blob to capture the CSV string (jsdom Blob lacks .text())
    (global as any).Blob = class {
      constructor(parts: any[]) {
        capturedCsv = parts.map((p) => String(p)).join('');
      }
    };

    createSpy = jest.fn(() => 'blob:mock-url');
    revokeSpy = jest.fn();
    window.URL.createObjectURL = createSpy as any;
    window.URL.revokeObjectURL = revokeSpy as any;

    lastAnchor = null;
    const realCreate = document.createElement.bind(document);
    jest.spyOn(document, 'createElement').mockImplementation((tag: string) => {
      const el = realCreate(tag) as HTMLElement;
      if (tag === 'a') lastAnchor = el as HTMLAnchorElement;
      return el as any;
    });

    clickSpy = jest
      .spyOn(HTMLAnchorElement.prototype, 'click')
      .mockImplementation(() => {});
  });

  afterEach(() => {
    global.Blob = OriginalBlob;
    window.URL.createObjectURL = originalCreate;
    window.URL.revokeObjectURL = originalRevoke;
    jest.restoreAllMocks();
  });

  const baseRow: SecurityRow = {
    id: 1,
    catalogKey: 'CK-1',
    name: 'Catálogo 1',
    status: 1,
    totalAssigned: 5,
  } as SecurityRow;

  it('genera URL, dispara click y limpia el blob', () => {
    exportSecurityRowsAsCsv([baseRow], 'Reporte Seguridad');

    expect(createSpy).toHaveBeenCalledTimes(1);
    expect(clickSpy).toHaveBeenCalledTimes(1);
    expect(revokeSpy).toHaveBeenCalledWith('blob:mock-url');
  });

  it('usa "Activo" cuando status=1 y "Inactivo" en caso contrario', () => {
    exportSecurityRowsAsCsv(
      [
        { ...baseRow, id: 1, status: 1 } as SecurityRow,
        { ...baseRow, id: 2, status: 0 } as SecurityRow,
      ],
      'r',
    );

    expect(capturedCsv).toContain('"Activo"');
    expect(capturedCsv).toContain('"Inactivo"');
    expect(capturedCsv.split('\n')[0]).toBe('id,clave,nombre,estatus,totalAsignados');
  });

  it('escapa comillas dobles duplicándolas', () => {
    exportSecurityRowsAsCsv(
      [{ ...baseRow, name: 'Nombre con "comillas"' } as SecurityRow],
      'r',
    );

    expect(capturedCsv).toContain('"Nombre con ""comillas"""');
  });

  it('produce filename con título normalizado y extensión .csv', () => {
    exportSecurityRowsAsCsv([baseRow], 'Reporte Con Espacios/Y*Carac');

    expect(lastAnchor).not.toBeNull();
    const download = lastAnchor!.download;
    expect(download).toMatch(/\.csv$/);
    expect(download).not.toContain(' ');
    expect(download).not.toContain('/');
    expect(download).not.toContain('*');
    expect(download.startsWith('reporte-con-espaciosycarac-')).toBe(true);
  });

  it('soporta valores nulos sin romper', () => {
    exportSecurityRowsAsCsv(
      [{ ...baseRow, name: null as any } as SecurityRow],
      'r',
    );

    expect(capturedCsv).toContain('""');
  });
});
