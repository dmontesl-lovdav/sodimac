import type { BreadcrumbItem } from './Breadcrumb';

/** Home del módulo Finanzas (coincide con la ruta en `App.tsx`). */
export const FINANCE_HOME_PATH = '/finanzas';

/** Página de inicio Finanzas. "Inicio" lo inyecta `Breadcrumb` vía FBC_HOME. */
export const breadcrumbFinanceHomePage: BreadcrumbItem[] = [
  { label: 'Finanzas' },
];

/** Prefijo estándar para el resto de pantallas. "Inicio" lo inyecta `Breadcrumb` vía FBC_HOME. */
export const breadcrumbFinancePrefix: BreadcrumbItem[] = [
  { label: 'Finanzas', to: FINANCE_HOME_PATH },
];

export function withFinanceBreadcrumb(suffix: BreadcrumbItem[]): BreadcrumbItem[] {
  return [...breadcrumbFinancePrefix, ...suffix];
}
