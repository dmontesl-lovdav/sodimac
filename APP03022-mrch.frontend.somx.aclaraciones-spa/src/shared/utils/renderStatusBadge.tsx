type RequestStatusId = 10 | 20 | 30 | 40;
type BadgeColor = 'warning' | 'info' | 'success' | 'error' | 'primary';

const STATUS_MAP: Record<RequestStatusId, { label: string; color: BadgeColor }> = {
    10: { label: 'Sin atender', color: 'warning' },
    20: { label: 'En atención', color: 'info' },
    30: { label: 'Resuelto', color: 'success' },
    40: { label: 'Cancelado', color: 'error' },
};

const COLOR_MAP: Record<BadgeColor, string> = {
    warning: 'bg-yellow-100 text-yellow-800',
    info: 'bg-blue-100 text-blue-800',
    success: 'bg-green-100 text-green-800',
    error: 'bg-red-100 text-red-800',
    primary: 'bg-gray-100 text-gray-800',
};

export const renderStatusBadge = (
    id: RequestStatusId | undefined | null,
    testId = ''
) => {
    const { label, color } =
        (id !== null && id !== undefined && STATUS_MAP[id]) || {
            label: 'Sin info',
            color: 'primary' as BadgeColor,
        };

    const classes =
        'inline-block rounded px-2 py-0.5 text-xs font-medium ' + COLOR_MAP[color];

    return (
        <span className={classes} data-testid={testId}>
            {label}
        </span>
    );
};
