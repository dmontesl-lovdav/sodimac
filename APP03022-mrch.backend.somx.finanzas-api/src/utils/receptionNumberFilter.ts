/**
 * Filtro de número de recepción (contiene, sin comodines SQL del usuario).
 */
export function escapeIlikePattern(raw: string): string {
    return raw.replace(/\\/g, "\\\\").replace(/%/g, "\\%").replace(/_/g, "\\_");
}

export function receptionNumberContainsQuery(
    receptionNumber: unknown,
    query: string | null | undefined
): boolean {
    const q = String(query ?? "").trim().toLowerCase();
    if (!q) return true;
    return String(receptionNumber ?? "").toLowerCase().includes(q);
}

export function buildReceptionNumberIlikePattern(query: string): string {
    return `%${escapeIlikePattern(query.trim())}%`;
}
