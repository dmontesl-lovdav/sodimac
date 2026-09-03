import type { RebateFilters } from "../interfaces";
import {
  FINANCE_LIST_KEYS,
  saveFinanceListFilters,
} from "@/shared/hooks";

/** Snapshot one-shot al ir a Fiscal a publicar NC. No lo limpia el reset de listado. */
export const DISCOUNT_FISCAL_RESTORE_KEY = "finz:discounts:fiscalNcSearch";

export const DISCOUNT_RESTORE_SEARCH_PARAM = "restoreSearch";

export function saveDiscountSearchRestore(filters: RebateFilters): void {
  try {
    localStorage.setItem(
      DISCOUNT_FISCAL_RESTORE_KEY,
      JSON.stringify(filters)
    );
    saveFinanceListFilters(FINANCE_LIST_KEYS.discounts.filters, filters);
  } catch {
    /* ignore */
  }
}

export function readDiscountSearchRestore(): RebateFilters | null {
  try {
    const raw = localStorage.getItem(DISCOUNT_FISCAL_RESTORE_KEY);
    if (!raw) return null;
    return JSON.parse(raw) as RebateFilters;
  } catch {
    return null;
  }
}

export function clearDiscountSearchRestore(): void {
  try {
    localStorage.removeItem(DISCOUNT_FISCAL_RESTORE_KEY);
  } catch {
    /* ignore */
  }
}

/** Copia el snapshot a sessionStorage del listado para hidratar filtros y refetch. */
export function hydrateDiscountSearchRestoreIntoSession(): void {
  const snapshot = readDiscountSearchRestore();
  if (!snapshot) return;
  saveFinanceListFilters(FINANCE_LIST_KEYS.discounts.filters, snapshot);
}
