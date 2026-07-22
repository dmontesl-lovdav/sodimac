import { useEffect, useRef } from "react";

export type FiscalListSessionKeys = {
  moduleKey: string;
  listPath: string;
  filters: string;
  legacy?: readonly string[];
  /** Rutas de detalle que no son hijas de `listPath` (p. ej. complemento). */
  detailPathPrefixes?: readonly string[];
};

const RETURN_FLAG_PREFIX = "fiscal:list:return:";

export function normalizeFiscalPath(path: string): string {
  return path.replace(/\/$/, "") ?? "/";
}

export function isFiscalListDetailPath(
  pathname: string,
  listPath: string,
  detailPathPrefixes?: readonly string[]
): boolean {
  const norm = normalizeFiscalPath(pathname);
  const base = normalizeFiscalPath(listPath);
  if (norm.length > base.length && norm.startsWith(`${base}/`)) {
    return true;
  }
  if (!detailPathPrefixes?.length) return false;
  return detailPathPrefixes.some((prefix) => {
    const p = normalizeFiscalPath(prefix);
    return norm === p || norm.startsWith(`${p}/`);
  });
}

export function markFiscalListReturnFromDetail(moduleKey: string): void {
  try {
    sessionStorage.setItem(`${RETURN_FLAG_PREFIX}${moduleKey}`, "1");
  } catch {
    /* ignore */
  }
}

export function consumeFiscalListReturnFromDetail(moduleKey: string): boolean {
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

export function clearFiscalListReturnFromDetail(moduleKey: string): void {
  try {
    sessionStorage.removeItem(`${RETURN_FLAG_PREFIX}${moduleKey}`);
  } catch {
    /* ignore */
  }
}

export function readFiscalListFilters<T>(filtersKey: string): T | null {
  try {
    const raw = sessionStorage.getItem(filtersKey);
    if (!raw) return null;
    return JSON.parse(raw) as T;
  } catch {
    return null;
  }
}

export function saveFiscalListFilters(
  filtersKey: string,
  filters: unknown
): void {
  try {
    sessionStorage.setItem(filtersKey, JSON.stringify(filters));
  } catch {
    /* ignore */
  }
}

export function clearFiscalListSession(keys: FiscalListSessionKeys): void {
  const all = [keys.filters, ...(keys.legacy ?? [])].filter(Boolean) as string[];
  for (const key of all) {
    try {
      sessionStorage.removeItem(key);
      localStorage.removeItem(key);
    } catch {
      /* ignore */
    }
  }
  clearFiscalListReturnFromDetail(keys.moduleKey);
}

/**
 * Al montar el listado: si no se vuelve desde detalle, borra filtros guardados.
 * @returns `true` si debe restaurarse el formulario desde sesión.
 */
export function prepareFiscalListScreen(keys: FiscalListSessionKeys): boolean {
  const returning = consumeFiscalListReturnFromDetail(keys.moduleKey);
  if (!returning) {
    clearFiscalListSession(keys);
    return false;
  }
  return true;
}

export function useFiscalListRefetchOnReturn<T>(
  keys: FiscalListSessionKeys,
  returningFromDetail: boolean,
  refetch: (filters: T) => void | Promise<void>
): void {
  const refetchRef = useRef(refetch);
  refetchRef.current = refetch;
  const firedRef = useRef(false);

  useEffect(() => {
    if (!returningFromDetail || firedRef.current) return;
    const saved = readFiscalListFilters<T>(keys.filters);
    if (!saved) return;
    firedRef.current = true;
        refetchRef.current(saved);
  }, [returningFromDetail, keys.filters]);
}

export function useFiscalListScreenSession(
  keys: FiscalListSessionKeys
): boolean {
  const initializedRef = useRef(false);
  const restoreFiltersRef = useRef(false);

  if (!initializedRef.current) {
    initializedRef.current = true;
    restoreFiltersRef.current = prepareFiscalListScreen(keys);
  }

  return restoreFiltersRef.current;
}

/** En pantallas hijas: al volver al listado se restauran filtros y se refetch. */
export function useFiscalListReturnFromDetail(
  keys: FiscalListSessionKeys
): void {
  useEffect(() => {
    markFiscalListReturnFromDetail(keys.moduleKey);
    return () => {
      const path = normalizeFiscalPath(
        typeof window !== "undefined" ? window.location.pathname ?? "" : ""
      );
      const base = normalizeFiscalPath(keys.listPath);
      if (
        path === base ||
        isFiscalListDetailPath(path, keys.listPath, keys.detailPathPrefixes)
      ) {
        return;
      }
      clearFiscalListReturnFromDetail(keys.moduleKey);
    };
  }, [keys.moduleKey, keys.listPath, keys.detailPathPrefixes]);
}

export const FISCAL_LIST_KEYS = {
  invoices: {
    moduleKey: "invoices",
    listPath: "/fiscal/facturas",
    filters: "fiscal:list:invoices:filters",
    legacy: ["invoiceFilters"],
  },
  creditNotes: {
    moduleKey: "creditNotes",
    listPath: "/fiscal/notas-credito",
    filters: "fiscal:list:creditNotes:filters",
    legacy: ["creditNoteFilters"],
  },
  complementPayments: {
    moduleKey: "complementPayments",
    listPath: "/fiscal/consulta-complemento-pago",
    filters: "fiscal:list:complementPayments:filters",
    legacy: ["complementPaymentFilters"],
    detailPathPrefixes: ["/fiscal/complemento"],
  },
} as const;
