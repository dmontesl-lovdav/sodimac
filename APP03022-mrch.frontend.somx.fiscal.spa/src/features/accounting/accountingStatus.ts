/**
 * CatEstatusFactura / CatEstatusNotaCredito (portal FBC).
 * Activo cuando el documento ya está en el flujo SAP/contable.
 * Cancelada (20) nunca habilita la acción.
 *
 */
export const INVOICE_STATUS_VER_CONTABILIDAD = [
  1,2,3,4,5,6,7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22,
];

export const CREDIT_NOTE_STATUS_VER_CONTABILIDAD = [
  1,2,3,4,5,6,7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19,
];

export function canViewAccounting(
  status: number | null | undefined,
  allowed: readonly number[]
): boolean {
  if (status == null) return false;
  if (status === 20) return false;
  return allowed.includes(status);
}
