import { parseDisplayDate, formatDate, formatDateTime } from './utils';

describe('parseDisplayDate', () => {
  it('devuelve null para valores vacíos o nulos', () => {
    expect(parseDisplayDate(null)).toBeNull();
    expect(parseDisplayDate(undefined)).toBeNull();
    expect(parseDisplayDate('')).toBeNull();
    expect(parseDisplayDate('   ')).toBeNull();
  });

  it('devuelve la misma Date si recibe un Date válido', () => {
    const d = new Date(2026, 5, 11);
    expect(parseDisplayDate(d)).toBe(d);
  });

  it('devuelve null para Date inválido', () => {
    expect(parseDisplayDate(new Date('not-a-date'))).toBeNull();
  });

  it('parsea formato dd/MM/yyyy', () => {
    const d = parseDisplayDate('11/06/2026') as Date;
    expect(d.getFullYear()).toBe(2026);
    expect(d.getMonth()).toBe(5); expect(d.getDate()).toBe(11);
  });

  it('parsea formato dd-MM-yyyy', () => {
    const d = parseDisplayDate('11-06-2026') as Date;
    expect(d.getFullYear()).toBe(2026);
    expect(d.getMonth()).toBe(5);
    expect(d.getDate()).toBe(11);
  });

  it('parsea formato ISO yyyy-MM-dd sin hora', () => {
    const d = parseDisplayDate('2026-06-11') as Date;
    expect(d.getFullYear()).toBe(2026);
    expect(d.getMonth()).toBe(5);
    expect(d.getDate()).toBe(11);
  });

  it('parsea ISO con hora usando new Date()', () => {
    const d = parseDisplayDate('2026-06-11T10:30:00Z') as Date;
    expect(d).toBeInstanceOf(Date);
    expect(Number.isNaN(d.getTime())).toBe(false);
  });

  it('devuelve null si new Date() falla', () => {
    expect(parseDisplayDate('texto-cualquiera')).toBeNull();
  });
});

describe('formatDate', () => {
  it('devuelve "-" para valores nulos o vacíos', () => {
    expect(formatDate(null)).toBe('-');
    expect(formatDate(undefined)).toBe('-');
    expect(formatDate('')).toBe('-');
  });

  it('formatea Date como dd/MM/yyyy', () => {
    expect(formatDate(new Date(2026, 5, 11))).toBe('11/06/2026');
  });

  it('formatea string ISO como dd/MM/yyyy', () => {
    expect(formatDate('2026-06-11')).toBe('11/06/2026');
  });

  it('incluye la hora cuando includeHour=true', () => {
    expect(formatDate(new Date(2026, 5, 11, 9, 5), true)).toBe('11/06/2026 09:05');
  });

  it('devuelve el string original si no se puede parsear', () => {
    expect(formatDate('texto-cualquiera')).toBe('texto-cualquiera');
  });
});

describe('formatDateTime', () => {
  it('devuelve "-" para valores nulos o vacíos', () => {
    expect(formatDateTime(null)).toBe('-');
    expect(formatDateTime('')).toBe('-');
  });

  it('formatea con hora por defecto (sin segundos)', () => {
    expect(formatDateTime(new Date(2026, 5, 11, 9, 5, 33))).toBe('11/06/2026 09:05');
  });

  it('incluye segundos cuando se solicita', () => {
    expect(formatDateTime(new Date(2026, 5, 11, 9, 5, 33), { seconds: true })).toBe(
      '11/06/2026 09:05:33',
    );
  });

  it('devuelve el string original si no se puede parsear', () => {
    expect(formatDateTime('basura')).toBe('basura');
  });
});
