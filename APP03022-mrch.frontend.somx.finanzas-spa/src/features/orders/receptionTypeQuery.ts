export function normalizeReceptionTypeQueryValue(
    raw: string | number | null | undefined
): string | undefined {
    if (raw == null) return undefined;
    const s = String(raw).trim();
    if (!s) return undefined;
    return s;
}

function firstNonEmptyCatalogId(
    ...candidates: Array<string | number | null | undefined>
): string {
    for (const candidate of candidates) {
        if (candidate == null) continue;
        const s = String(candidate).trim();
        if (s) return s;
    }
    return "";
}

export function catalogDetailsToReceptionTypeOptions(
    data: unknown
): { label: string; value: string }[] {
    const raw = data as Record<string, unknown> | null | undefined;
    const rows: unknown[] = Array.isArray(data)
        ? data
        : Array.isArray(raw?.details)
          ? (raw.details as unknown[])
          : Array.isArray(raw?.content)
            ? (raw.content as unknown[])
            : [];

    const mapped = rows
        .map((rowUnknown) => {
            const row = rowUnknown as Record<string, unknown>;
            const value = firstNonEmptyCatalogId(
                row.value,
                row.internalStatus,
                row.internal_status,
                row.id
            );
            const label = String(
                row.description ?? row.value ?? row.externalKey ?? row.key ?? ""
            ).trim();
            return { label, value };
        })
        .filter(
            (row) =>
                row.label.length > 0 &&
                row.value.length > 0 &&
                !row.label.toLowerCase().includes("borrado")
        )
        .sort((a, b) => Number(a.value) - Number(b.value));

    return [{ label: "Todos los tipos", value: "" }, ...mapped];
}

export function filterByReceptionType<T extends { receptionTypeId?: string | number | null }>(
    receptions: T[],
    receptionTypeId?: string | number
): T[] {
    const qRaw = normalizeReceptionTypeQueryValue(receptionTypeId);
    if (!qRaw) {
        return receptions;
    }
    const q = Number(qRaw);
    if (!Number.isFinite(q)) {
        return receptions;
    }
    return receptions.filter((r) => Number(r.receptionTypeId) === q);
}
