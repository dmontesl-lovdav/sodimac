function pad2(n: number): string {
    return String(n).padStart(2, "0");
}

export function parseDisplayDate(
    value: string | Date | null | undefined
): Date | null {
    if (value == null || value === "") return null;
    if (value instanceof Date) {
        return Number.isNaN(value.getTime()) ? null : value;
    }

    const s = String(value).trim();
    if (!s) return null;

    const slash = s.match(/^(\d{1,2})[/-](\d{1,2})[/-](\d{4})/);
    if (slash) {
        return new Date(Number(slash[3]), Number(slash[2]) - 1, Number(slash[1]));
    }

    const isoDate = s.match(/^(\d{4})-(\d{2})-(\d{2})/);
    if (isoDate) {
        const hasTime = s.includes("T") || /\d{2}:\d{2}/.test(s);
        if (!hasTime) {
            return new Date(
                Number(isoDate[1]),
                Number(isoDate[2]) - 1,
                Number(isoDate[3])
            );
        }
    }

    const d = new Date(s);
    return Number.isNaN(d.getTime()) ? null : d;
}

export function formatDate(
    date: string | Date | null | undefined,
    includeHour: boolean = false
): string {
    if (date == null || date === "") return "-";
    const d = parseDisplayDate(date);
    if (!d) {
        const raw = String(date).trim();
        return raw || "-";
    }

    const base = `${pad2(d.getDate())}/${pad2(d.getMonth() + 1)}/${d.getFullYear()}`;
    if (!includeHour) return base;
    return `${base} ${pad2(d.getHours())}:${pad2(d.getMinutes())}`;
}

export function formatDateTime(
    date: string | Date | null | undefined,
    options?: { seconds?: boolean }
): string {
    if (date == null || date === "") return "-";
    const d = parseDisplayDate(date);
    if (!d) return String(date).trim() || "-";

    const base = `${pad2(d.getDate())}/${pad2(d.getMonth() + 1)}/${d.getFullYear()}`;
    const h = pad2(d.getHours());
    const m = pad2(d.getMinutes());
    if (options?.seconds) {
        return `${base} ${h}:${m}:${pad2(d.getSeconds())}`;
    }
    return `${base} ${h}:${m}`;
}
