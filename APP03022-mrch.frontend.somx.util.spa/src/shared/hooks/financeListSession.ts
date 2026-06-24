import { useEffect, useRef } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";

function startOfLocalDay(d: Date): Date {
    const x = new Date(d);
    x.setHours(0, 0, 0, 0);
    return x;
}

function endOfLocalDay(d: Date): Date {
    const x = new Date(d);
    x.setHours(23, 59, 59, 999);
    return x;
}

export type FinanceListSessionKeys = {
    moduleKey: string;
    listPath: string;
    filters: string;
    grid?: string;
    legacy?: readonly string[];
};

const RETURN_FLAG_PREFIX = "finz:list:return:";
const PENDING_RESET_PREFIX = "finz:list:pendingReset:";

export function normalizeFinancePath(path: string): string {
    return path.replace(/\/$/, "") || "/";
}

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

function isResetParamTrue(params: URLSearchParams): boolean {
    const v = params.get("reset")?.trim().toLowerCase();
    return v === "true" || v === "1";
}

export function isFinanceListUrlReset(
    searchParams: URLSearchParams
): boolean {
    if (isResetParamTrue(searchParams)) return true;
    if (typeof window === "undefined") return false;
    const hash = window.location.hash || "";
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

export function financeListTodayDateRange(): [Date, Date] {
    const t = new Date();
    return [startOfLocalDay(t), endOfLocalDay(t)];
}

export function formatFinanceListLocalDate(d: Date): string {
    const x = startOfLocalDay(d);
    const y = x.getFullYear();
    const m = String(x.getMonth() + 1).padStart(2, "0");
    const day = String(x.getDate()).padStart(2, "0");
    return `${y}-${m}-${day}`;
}

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

export function parseFinanceListDateRange(
    startValue: unknown,
    endValue: unknown
): [Date | null, Date | null] {
    return [
        parseFinanceListLocalDate(startValue),
        parseFinanceListLocalDate(endValue),
    ];
}

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
        void refetchRef.current(saved);
    }, [returningFromDetail, keys.filters]);
}

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

export function useFinanceListReturnFromDetail(
    moduleKey: string,
    listPath: string
): void {
    useEffect(() => {
        markFinanceListReturnFromDetail(moduleKey);
        return () => {
            const path = normalizeFinancePath(
                typeof window !== "undefined"
                    ? window.location.pathname || ""
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

export const FINANCE_LIST_KEYS = {
    auditLogs: {
        moduleKey: "auditLogs",
        listPath: "/util/auditoria/bitacora-actividades",
        filters: "finz:list:auditLogs:filters",
    },
} as const;
