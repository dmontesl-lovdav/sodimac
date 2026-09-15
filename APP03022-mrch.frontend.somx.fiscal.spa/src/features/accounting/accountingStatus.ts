export const INVOICE_STATUS_VER_CONTABILIDAD = [
  9, 15, 16, 17, 19,
];

export const CREDIT_NOTE_STATUS_VER_CONTABILIDAD = [
  9, 15, 16, 17, 19,
];

export function canViewAccounting(
  status: number | null | undefined,
  allowed: readonly number[]
): boolean {
  if (status == null) return false;
  if (status === 20) return false;
  return allowed.includes(status);
}
