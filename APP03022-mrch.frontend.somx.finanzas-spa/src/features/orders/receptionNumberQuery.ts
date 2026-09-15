export function receptionNumberContainsQuery(
  receptionNumber: unknown,
  query?: string
): boolean {
  const q = String(query ?? "").trim().toLowerCase();
  if (!q) return true;
  return String(receptionNumber ?? "").toLowerCase().includes(q);
}

export function filterByReceptionQuery<T extends { receptionNumber?: unknown }>(
  receptions: T[],
  q?: string
): T[] {
  const t = q?.trim();
  if (!t) return receptions;
  return receptions.filter((r) => receptionNumberContainsQuery(r.receptionNumber, t));
}
