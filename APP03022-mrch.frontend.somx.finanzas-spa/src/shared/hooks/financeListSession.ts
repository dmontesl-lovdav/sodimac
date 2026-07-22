import { useEffect, useRef } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { endOfLocalDay, startOfLocalDay } from "@/utils/utils";

export type FinanceListSessionKeys = {
    /** Identificador del módulo (p. ej. `shippingGuides`). */
    moduleKey: string;
    /** Ruta HashRouter del listado (p. ej. `/finanzas/guias`). */
    listPath: string;
    /** Persistencia de criterios del formulario (solo al pulsar Buscar). */
    filters: string;
    /** Resultados del grid guardados en sesión (se borran al entrar). */
    grid?: string;
    /** Claves antiguas (localStorage / sessionStorage) a eliminar al salir. */
    legacy?: readonly string[];
};

const RETURN_FLAG_PREFIX = "finz:list:return:";
const PENDING_RESET_PREFIX = "finz:list:pendingReset:";

export function normalizeFinancePath(path: string): string {
    return path.replace(/\/$/, "") ?? "/";
}

/** Rutas hijas del listado (detalle, factura, estatus, etc.). */
export function isFinanceListDetailPath(
    pathname: string,
    listPath: string
): boolean {
    const norm = normalizeFinancePath(pathname);
    const base = normalizeFinancePath(listPath);
    return norm.length > base.length && norm.startsWith(`${base}/`);
}

export function markFinanceListReturnFromDetail(moduleKey: string): void {
    try {
        sessionStorage.setItem(`${RETURN_FLAG_PREFIX}${moduleKey}`, "1");
    } catch {
        /* ignore */
    }
}

export function consumeFinanceListReturnFromDetail(moduleKey: string): boolean {
    const key = `${RETURN_FLAG_PREFIX}${moduleKey}`;
    try {
        const v = sessionStorage.getItem(key);
        if (v) {
            sessionStorage.removeItem(key);
            return true;
        }
    } catch {
        /* ignore */
    }
    return false;
}

export function clearFinanceListReturnFromDetail(moduleKey: string): void {
    try {
        sessionStorage.removeItem(`${RETURN_FLAG_PREFIX}${moduleKey}`);
    } catch {
        /* ignore */
    }
}

export function readFinanceListFilters<T>(filtersKey: string): T | null {
    try {
        const raw = sessionStorage.getItem(filtersKey);
        if (!raw) return null;
        return JSON.parse(raw) as T;
    } catch {
        return null;
    }
}

export function saveFinanceListFilters(
    filtersKey: string,
    filters: unknown
): void {
    try {
        sessionStorage.setItem(filtersKey, JSON.stringify(filters));
    } catch {
        /* ignore */
    }
}

export function markPendingFinanceListReset(moduleKey: string): void {
    try {
        sessionStorage.setItem(`${PENDING_RESET_PREFIX}${moduleKey}`, "1");
    } catch {
        /* ignore */
    }
}

/** Consumido por la barra de filtros tras `?reset=true` (la URL ya se limpió en el contenedor). */
export function consumePendingFinanceListReset(moduleKey: string): boolean {
    const key = `${PENDING_RESET_PREFIX}${moduleKey}`;
    try {
        if (sessionStorage.getItem(key)) {
            sessionStorage.removeItem(key);
            return true;
        }
    } catch {
        /* ignore */
    }
    return false;
}

export function removeFinanceListFilters(filtersKey: string): void {
    try {
        sessionStorage.removeItem(filtersKey);
    } catch {
        /* ignore */
    }
}

export function clearFinanceListSession(keys: FinanceListSessionKeys): void {
    const all = [
        keys.filters,
        keys.grid,
        ...(keys.legacy ?? []),
    ].filter(Boolean) as string[];

    for (const key of all) {
        try {
            sessionStorage.removeItem(key);
            localStorage.removeItem(key);
        } catch {
            /* ignore */
        }
    }
    clearFinanceListReturnFromDetail(keys.moduleKey);
}

export function clearFinanceListGridOnly(keys: FinanceListSessionKeys): void {
    if (keys.grid) {
        try {
            sessionStorage.removeItem(keys.grid);
        } catch {
            /* ignore */
        }
    }
}

/** `?reset=true` en la URL (menú Finanzas): limpiar filtros guardados. */
function isResetParamTrue(params: URLSearchParams): boolean {
    const v = params.get("reset")?.trim().toLowerCase();
    return v === "true" || v === "1";
}

export function isFinanceListUrlReset(
    searchParams: URLSearchParams
): boolean {
    if (isResetParamTrue(searchParams)) return true;
    if (typeof window === "undefined") return false;
    const hash = window.location.hash ?? "";
    const q = hash.indexOf("?");
    if (q === -1) return false;
    return isResetParamTrue(new URLSearchParams(hash.slice(q)));
}

export function stripFinanceListResetParam(
    searchParams: URLSearchParams
): URLSearchParams {
    const next = new URLSearchParams(searchParams);
    next.delete("reset");
    return next;
}

/**
 * Al montar el listado: limpia grid; si no se vuelve desde detalle interno, borra filtros.
 * @returns `true` si debe hidratarse el formulario desde sesión.
 */
export function prepareFinanceListScreen(
    keys: FinanceListSessionKeys,
    options?: { resetFromUrl?: boolean }
): boolean {
    clearFinanceListGridOnly(keys);
    if (options?.resetFromUrl) {
        clearFinanceListSession(keys);
        markPendingFinanceListReset(keys.moduleKey);
        return false;
    }
    const returning = consumeFinanceListReturnFromDetail(keys.moduleKey);
    if (!returning) {
        clearFinanceListSession(keys);
        return false;
    }
    return true;
}

/** Rango [inicio, fin] del día actual (hora local). */
export function financeListTodayDateRange(): [Date, Date] {
    const t = new Date();
    return [startOfLocalDay(t), endOfLocalDay(t)];
}

/**
 * Fecha calendario local `YYYY-MM-DD` para sessionStorage.
 * Evita desfase de un día al usar `toISOString()` o `new Date("YYYY-MM-DD")` (UTC).
 */
export function formatFinanceListLocalDate(d: Date): string {
    const x = startOfLocalDay(d);
    const y = x.getFullYear();
    const m = String(x.getMonth() + 1).padStart(2, "0");
    const day = String(x.getDate()).padStart(2, "0");
    return `${y}-${m}-${day}`;
}

/**
 * Restaura un día calendario guardado (`YYYY-MM-DD`, ISO u otro valor parseable).
 * Siempre devuelve inicio del día en hora local.
 */
export function parseFinanceListLocalDate(value: unknown): Date | null {
    if (value == null || value === "") return null;
    const s = String(value).trim();

    const ymd = /^(\d{4})-(\d{2})-(\d{2})/.exec(s);
    if (ymd) {
        const y = Number(ymd[1]);
        const mo = Number(ymd[2]) - 1;
        const day = Number(ymd[3]);
        const local = new Date(y, mo, day);
        if (!Number.isNaN(local.getTime())) return startOfLocalDay(local);
    }

    const parsed = new Date(s);
    if (Number.isNaN(parsed.getTime())) return null;
    return startOfLocalDay(parsed);
}

/** Rango para DateRangePicker: inicio y fin como día calendario local. */
export function parseFinanceListDateRange(
    startValue: unknown,
    endValue: unknown
): [Date | null, Date | null] {
    return [
        parseFinanceListLocalDate(startValue),
        parseFinanceListLocalDate(endValue),
    ];
}

/**
 * Tras pulsar Volver en pantalla hija: relanza la búsqueda con filtros guardados en sesión.
 * `returningFromDetail` proviene de `useFinanceListScreenSession`.
 */
export function useFinanceListRefetchOnReturn<T>(
    keys: FinanceListSessionKeys,
    returningFromDetail: boolean,
    refetch: (filters: T) => void | Promise<void>
): void {
    const refetchRef = useRef(refetch);
    refetchRef.current = refetch;
    const firedRef = useRef(false);

    useEffect(() => {
        if (!returningFromDetail || firedRef.current) return;
        const saved = readFinanceListFilters<T>(keys.filters);
        if (!saved) return;
        firedRef.current = true;
        refetchRef.current(saved);
    }, [returningFromDetail, keys.filters]);
}

/**
 * Pantallas de listado: resetea filtros al salir del módulo; los conserva al ir a detalle.
 * Al volver (`returningFromDetail === true`), usar `useFinanceListRefetchOnReturn` para recargar el grid.
 */
export function useFinanceListScreenSession(keys: FinanceListSessionKeys): boolean {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const location = useLocation();
    const initializedRef = useRef(false);
    const restoreFiltersRef = useRef(false);

    if (!initializedRef.current) {
        initializedRef.current = true;
        const resetFromUrl = isFinanceListUrlReset(searchParams);
        restoreFiltersRef.current = prepareFinanceListScreen(keys, {
            resetFromUrl,
        });
        if (resetFromUrl) {
            const next = stripFinanceListResetParam(searchParams);
            const q = next.toString();
            navigate(
                {
                    pathname: location.pathname,
                    search: q ? `?${q}` : "",
                },
                { replace: true }
            );
        }
    }

    return restoreFiltersRef.current;
}

/**
 * En barras de filtros: aplica valores por defecto cuando el menú entró con `?reset=true`.
 * El contenedor marca un flag en sesión antes de quitar el parámetro de la URL.
 */
export function useFinanceListDefaultsOnUrlReset(
    moduleKey: string,
    applyDefaults: () => void
): void {
    const appliedRef = useRef(false);

    useEffect(() => {
        if (appliedRef.current) return;
        if (!consumePendingFinanceListReset(moduleKey)) return;
        appliedRef.current = true;
        applyDefaults();
    }, [moduleKey, applyDefaults]);
}

/** Pantallas de detalle / acción interna: marcar que al volver se restauran filtros y se refetch el listado. */
export function useFinanceListReturnFromDetail(
    moduleKey: string,
    listPath: string
): void {
    useEffect(() => {
        markFinanceListReturnFromDetail(moduleKey);
        return () => {
            const path = normalizeFinancePath(
                typeof window !== "undefined"
                    ? window.location.pathname ?? ""
                    : ""
            );
            const base = normalizeFinancePath(listPath);
            if (path === base || isFinanceListDetailPath(path, listPath)) {
                return;
            }
            clearFinanceListReturnFromDetail(moduleKey);
        };
    }, [moduleKey, listPath]);
}

/** Claves por módulo de Finanzas. */
export const FINANCE_LIST_KEYS = {
    receptions: {
        moduleKey: "receptions",
        listPath: "/finanzas/recepciones",
        filters: "finz:list:receptions:filters",
        grid: "finz_recepciones_ultima_busqueda",
        legacy: ["receptions_filters"],
    },
    discounts: {
        moduleKey: "discounts",
        listPath: "/finanzas/descuentos-comerciales",
        filters: "finz:list:discounts:filters",
        legacy: ["rebates_filters"],
    },
    shippingGuides: {
        moduleKey: "shippingGuides",
        listPath: "/finanzas/guias",
        filters: "finz:list:shippingGuides:filters",
        legacy: ["shippingGuides:lastFilters"],
    },
    payments: {
        moduleKey: "payments",
        listPath: "/finanzas/pagos",
        filters: "finz:list:payments:filters",
    },
    accountStatement: {
        moduleKey: "accountStatement",
        listPath: "/finanzas/estado-cuenta",
        filters: "finz:list:accountStatement:filters",
    },
    migo: {
        moduleKey: "migo",
        listPath: "/finanzas/migo",
        filters: "finz:list:migo:filters",
    },
    threeWayMatch: {
        moduleKey: "threeWayMatch",
        listPath: "/finanzas/three-way-match",
        filters: "finz:list:threeWayMatch:filters",
    },
    rebatesLegacy: {
        moduleKey: "rebatesLegacy",
        listPath: "/finanzas/rebates",
        filters: "finz:list:rebates-legacy:filters",
    },
} as const;
