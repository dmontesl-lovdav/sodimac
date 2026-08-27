interface BreadcrumbItem {
    label: string;
    to?: string;
    state?: unknown;
    external?: boolean;
    onClick?: () => void;
}

const HOME_PATH = (typeof process !== 'undefined' && process.env?.FBC_HOME) || '/';

export const UTIL_HOME_PATH = '/util';

export const breadcrumbFinanceHomePage: BreadcrumbItem[] = [
    { label: 'Inicio', to: HOME_PATH, external: true },
    { label: 'Herramientas y Utilerías' },
];

export const breadcrumbFinancePrefix: BreadcrumbItem[] = [
    { label: 'Inicio', to: HOME_PATH, external: true },
    { label: 'Herramientas y Utilerías', to: UTIL_HOME_PATH },
];

export function withFinanceBreadcrumb(suffix: BreadcrumbItem[]): BreadcrumbItem[] {
    return [...breadcrumbFinancePrefix, ...suffix];
}
