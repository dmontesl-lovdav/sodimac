// statusPill.ts

export type RequestStatusId = 10 | 20 | 30 | 40 | 50;

type PillSpec = { label: string; bg: string; fg: string };

const MAP: Record<RequestStatusId, PillSpec> = {
    10: { label: 'Sin atender', bg: '#fff4e5', fg: '#b76e00' },
    20: { label: 'En atención', bg: '#e3f4ff', fg: '#0078c8' },
    30: { label: 'Resuelto', bg: '#e7f5ea', fg: '#2e7d32' },
    40: { label: 'Cancelado', bg: '#fde8e8', fg: '#d32f2f' },
    50: { label: 'Rechazado', bg: '#fde8e8', fg: '#a32a2a' },
};


// Catalog(11) "clazz" → RequestStatusId
const CLAZZ_TO_STATUS: Record<number, RequestStatusId> = {
    23: 10, // To Do → Sin atender
    24: 20, // Doing → En atención
    25: 30, // Done → Resuelto
    26: 40, // Bloqueo → Cancelado
    52: 50, // Rejected → Rechazado
};

// Normalize any incoming value (statusId or clazz) into RequestStatusId
function normalizeToStatusId(
    value: RequestStatusId | number | null | undefined
): RequestStatusId | null {
    if (value === null || value === undefined) return null;

    // already a valid RequestStatusId
    if (MAP[value as RequestStatusId]) return value as RequestStatusId;

    // otherwise try mapping clazz → status
    const mapped = CLAZZ_TO_STATUS[value as number];
    return mapped ?? null;
}

export function buildStatusPill(
    value: RequestStatusId | number | null | undefined
): string {
    const normalized = normalizeToStatusId(value);

    const { label, bg, fg }: PillSpec =
        (normalized && MAP[normalized]) || {
            label: 'Sin info',
            bg: '#eeeeee',
            fg: '#424242',
        };

    return `<span style="
    display:inline-block;
    padding:0 6px;
    border-radius:4px;
    font-size:12px;
    line-height:18px;
    font-weight:600;
    background:${bg};
    color:${fg};
  ">${label}</span>`;
}

// Optional exports if you need them elsewhere
export { CLAZZ_TO_STATUS, MAP as STATUS_PILL_MAP, normalizeToStatusId };
