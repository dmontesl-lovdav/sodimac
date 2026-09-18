import type { PaymentFiltersValues } from "../components/FiltersBar";
import {
  FINANCE_LIST_KEYS,
  saveFinanceListFilters,
} from "@/shared/hooks";

/** Snapshot one-shot al ir a Fiscal a publicar complemento. No lo limpia el reset de listado. */
export const PAYMENT_FISCAL_RESTORE_KEY = "finz:payments:fiscalComplementSearch";

export const PAYMENT_RESTORE_SEARCH_PARAM = "restoreSearch";

export function savePaymentSearchRestore(filters: PaymentFiltersValues): void {
  try {
    localStorage.setItem(
      PAYMENT_FISCAL_RESTORE_KEY,
      JSON.stringify(filters)
    );
    saveFinanceListFilters(FINANCE_LIST_KEYS.payments.filters, filters);
  } catch {
    /* ignore */
  }
}

export function readPaymentSearchRestore(): PaymentFiltersValues | null {
  try {
    const raw = localStorage.getItem(PAYMENT_FISCAL_RESTORE_KEY);
    if (!raw) return null;
    return JSON.parse(raw) as PaymentFiltersValues;
  } catch {
    return null;
  }
}

export function clearPaymentSearchRestore(): void {
  try {
    localStorage.removeItem(PAYMENT_FISCAL_RESTORE_KEY);
  } catch {
    /* ignore */
  }
}

/** Copia el snapshot a sessionStorage del listado para hidratar filtros y refetch. */
export function hydratePaymentSearchRestoreIntoSession(): void {
  const snapshot = readPaymentSearchRestore();
  if (!snapshot) return;
  saveFinanceListFilters(FINANCE_LIST_KEYS.payments.filters, snapshot);
}
