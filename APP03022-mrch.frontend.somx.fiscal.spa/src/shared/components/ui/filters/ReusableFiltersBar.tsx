import { ReactElement, useState, useEffect, useCallback, useRef } from "react";
import {
  GenericButton,
  GenericInputSearch,
  GenericSelectSearchable,
} from "@shared/components/ui";
import { GenericDateRangePicker } from "@shared/components/ui/date";
import type { DateRange } from "@shared/components/ui/date";
import type { ChangeEvent } from "react";
import GenericModal from "@shared/components/ui/modal/GenericModal";
import { PermissionGate } from "@shared/security";
import "./ReusableFiltersBar.css";
import { readFiscalListFilters } from "@/shared/session/fiscalListSession";
import {
  fetchProvidersAsCatalog,
  fiscalFilterTodayDateRange,
  formatLocalDateStr,
  isDateRangeOverSixMonths,
  parseLocalDateStr,
  startOfLocalDay,
} from "@/utils/utils";

type SelectableOption<T> = {
  label: string;
  value: T;
};

export interface FilterField {
  key: string;
  label: string;
  type: "text" | "select" | "dateRange" | "selectFloating" | "providerSelect";
  placeholder?: string;
  options?: SelectableOption<string | number>[];
  required?: boolean;
  widthClass?: string;
  containerClassName?: string;
}

export function normalizeProviderFilterValue(value: unknown): string {
  const raw = value == null ? "" : String(value).trim();
  return raw === "" || raw === " " ? "" : raw;
}

export function normalizeListboxFilterValue(value: unknown): string {
  return normalizeProviderFilterValue(value);
}

export function normalizeFiltersForSubmit<F extends Record<string, any>>(
  filters: F,
  fields: FilterField[]
): F {
  const next = { ...filters } as F;
  for (const field of fields) {
    if (field.type === "providerSelect") {
      (next as Record<string, unknown>)[field.key] = normalizeProviderFilterValue(
        next[field.key]
      );
      continue;
    }
    if (field.type !== "selectFloating") continue;

    const normalized = normalizeListboxFilterValue(next[field.key]);
    if (field.key === "estatus") {
      (next as Record<string, unknown>)[field.key] =
        normalized === "" ? undefined : Number(normalized);
      continue;
    }
    (next as Record<string, unknown>)[field.key] = normalized;
  }
  return next;
}

export function selectFilterValue(value: unknown): string {
  if (value == null || value === "") return "";
  if (String(value) === " ") return " ";
  return String(value);
}

export function mapSelectOptions(
  options: SelectableOption<string | number>[] = []
): { value: string; label: string }[] {
  return options.map((opt) => ({
    value: String(opt.value),
    label: String(opt.label),
  }));
}

export function resolveFieldWrapperClass(field: FilterField): string {
  if (field.type === "dateRange") return "rc-field-dates";
  if (field.containerClassName) return field.containerClassName;
  if (
    field.type === "selectFloating" &&
    (field.key === "estatus" || field.key === "status")
  ) {
    return "rc-filter-status-wrap";
  }
  if (
    field.type === "select" &&
    (field.key === "estatus" || field.key === "status")
  ) {
    return "rc-filter-status-wrap";
  }
  return "";
}

export function resolveSelectPlaceholder(field: FilterField): string {
  if (field.type === "providerSelect") {
    return field.placeholder ?? "Nombre Proveedor";
  }
  return field.placeholder ?? field.label;
}

export function resolveTextPlaceholder(field: FilterField): string {
  return field.placeholder ?? field.label;
}

export function resolveDatePlaceholder(field: FilterField): string {
  if (field.placeholder) return field.placeholder;
  if (field.key === "fechaRecepcion") return "Fecha de recepción";
  if (field.key === "fechaPago") return "Fecha de pago";
  if (field.key === "fechaEmision") return "Fecha de emisión";
  return field.label ?? "Fecha desde – hasta";
}

export function resolveDateFilterKeys(fieldKey: string): {
  startKey: string;
  endKey: string;
} {
  if (fieldKey === "fecha") {
    return { startKey: "fechaInicio", endKey: "fechaFinal" };
  }
  if (fieldKey === "fechaRecepcion") {
    return { startKey: "fechaInicioRecepcion", endKey: "fechaFinalRecepcion" };
  }
  if (fieldKey === "fechaPago") {
    return { startKey: "fechaPagoInicio", endKey: "fechaPagoFin" };
  }
  if (fieldKey === "fechaEmision") {
    return { startKey: "fechaEmisionInicio", endKey: "fechaEmisionFin" };
  }
  return { startKey: `${fieldKey}Inicio`, endKey: `${fieldKey}Fin` };
}

export function buildDateRangeFromFilterValues(
  filters: Record<string, unknown>,
  fieldKey: string
): DateRange {
  const { startKey, endKey } = resolveDateFilterKeys(fieldKey);
  const start = parseLocalDateStr(filters[startKey]);
  const end = parseLocalDateStr(filters[endKey]);
  if (start && end) return [start, end];
  return fiscalFilterTodayDateRange();
}

export function applyDefaultDateFilters<F extends Record<string, any>>(
  filters: F,
  fields: FilterField[]
): { filters: F; ranges: Record<string, DateRange> } {
  const next = { ...filters } as F;
  const ranges: Record<string, DateRange> = {};
  const [todayStart, todayEnd] = fiscalFilterTodayDateRange();
  const todayStartStr = formatLocalDateStr(todayStart);
  const todayEndStr = formatLocalDateStr(todayEnd);

  for (const field of fields) {
    if (field.type !== "dateRange") continue;
    const { startKey, endKey } = resolveDateFilterKeys(field.key);
    const start = parseLocalDateStr((next as Record<string, unknown>)[startKey]);
    const end = parseLocalDateStr((next as Record<string, unknown>)[endKey]);

    if (start && end) {
      ranges[field.key] = [start, end];
      (next as Record<string, unknown>)[startKey] = formatLocalDateStr(
        startOfLocalDay(start)
      );
      (next as Record<string, unknown>)[endKey] = formatLocalDateStr(
        startOfLocalDay(end)
      );
    } else {
      ranges[field.key] = [todayStart, todayEnd];
      (next as Record<string, unknown>)[startKey] = todayStartStr;
      (next as Record<string, unknown>)[endKey] = todayEndStr;
    }
  }

  return { filters: next, ranges };
}

export function hydrateFilterState<F extends Record<string, any>>(
  initialFilters: F,
  fields: FilterField[],
  options?: {
    restoreSavedFilters?: boolean;
    sessionFiltersKey?: string;
  }
): { filters: F; ranges: Record<string, DateRange> } {
  let effectiveFilters: F = { ...initialFilters };
  if (options?.restoreSavedFilters && options.sessionFiltersKey) {
    const savedFilters = readFiscalListFilters<F>(options.sessionFiltersKey);
    if (savedFilters) {
      effectiveFilters = { ...effectiveFilters, ...savedFilters };
    }
  }
  return applyDefaultDateFilters(effectiveFilters, fields);
}

interface Props<F extends Record<string, any>> {
  fields: FilterField[];
  initialFilters: F;
  /** Base al pulsar Limpiar; si no se define, se usa `initialFilters`. */
  resetFiltersOnClear?: F;
  onSearch: (filters: F) => void;
  onFiltersChange?: (filters: F) => void;
  onClear?: (filters: F) => void;
  sessionFiltersKey?: string;
  restoreSavedFilters?: boolean;
  validateFilters?: (filters: F) => string | null;
  onHydrated?: (filters: F) => void;
  searchAppEvent?: { app: string; event: string };
  clearAppEvent?: { app: string; event: string };
}

export default function ReusableFiltersBar<F extends Record<string, any>>({
  fields,
  initialFilters,
  resetFiltersOnClear,
  onSearch,
  onFiltersChange,
  onClear,
  sessionFiltersKey,
  restoreSavedFilters = false,
  validateFilters,
  onHydrated,
  searchAppEvent,
  clearAppEvent,
}: Props<F>): ReactElement {
  const [filterState] = useState(() =>
    hydrateFilterState(initialFilters, fields, {
      restoreSavedFilters,
      sessionFiltersKey,
    })
  );
  const [filters, setFilters] = useState<F>(filterState.filters);
  const [error, setError] = useState<string>("");
  const [dateRanges, setDateRanges] = useState<Record<string, DateRange>>(
    filterState.ranges
  );
  const [providerOptions, setProviderOptions] = useState<SelectableOption<string>[]>([]);
  const hasHydratedRef = useRef(false);
  const onHydratedRef = useRef(onHydrated);
  onHydratedRef.current = onHydrated;
  const needsProviderCatalog = fields.some((field) => field.type === "providerSelect");
  const today = useRef(startOfLocalDay(new Date())).current;

  useEffect(() => {
    if (!needsProviderCatalog) return;
    (async () => {
      const list = await fetchProvidersAsCatalog("supplierNumber");
      if (list) {
        setProviderOptions(
          list.map((item) => ({
            label: String(item.label),
            value: String(item.value),
          }))
        );
      }
    })();
  }, [needsProviderCatalog]);

  useEffect(() => {
    if (hasHydratedRef.current) return;
    hasHydratedRef.current = true;
    onHydratedRef.current?.(filterState.filters);
  }, [filterState.filters]);

  const handleFieldChange = useCallback(<K extends keyof F>(key: K) => {
    return (event: ChangeEvent<HTMLInputElement | HTMLSelectElement> | { target: { value: string } }) => {
      setFilters(prev => ({ ...prev, [key]: event.target.value as F[K] }));
      setError("");
    };
  }, []);

  const handleDateRangeChange = useCallback((fieldKey: string) => {
    return (dates: DateRange) => {
      const [start, end] = dates;
      setDateRanges((prev) => ({ ...prev, [fieldKey]: dates }));

      const { startKey, endKey } = resolveDateFilterKeys(fieldKey);
      if (!start || !end) {
        setFilters((prev) => ({
          ...prev,
          [startKey]: "",
          [endKey]: "",
        }));
        setError("");
        return;
      }

      setFilters((prev) => ({
        ...prev,
        [startKey]: formatLocalDateStr(startOfLocalDay(start)),
        [endKey]: formatLocalDateStr(startOfLocalDay(end)),
      }));
      setError("");
    };
  }, []);

  const handleClear = useCallback(() => {
    setError("");
    const base = resetFiltersOnClear ?? initialFilters;
    const cleared = applyDefaultDateFilters({ ...base } as F, fields);
    setFilters(cleared.filters);
    setDateRanges(cleared.ranges);
    onFiltersChange?.(cleared.filters);
    onClear?.(cleared.filters);
  }, [fields, initialFilters, resetFiltersOnClear, onFiltersChange, onClear]);

  const validateDateRangeField = useCallback(
    (
      field: FilterField,
      range: DateRange,
      normalizedFilters: F
    ): string | null => {
      const [d1, d2] = range;
      const { startKey, endKey } = resolveDateFilterKeys(field.key);

      if (field.required && (!d1 || !d2)) {
        return `${field.label} es obligatorio`;
      }
      if (!d1 || !d2) return null;

      const start = startOfLocalDay(d1);
      const end = startOfLocalDay(d2);

      if (end > today) {
        return "La fecha fin no puede ser posterior a la fecha actual.";
      }
      if (start > end) {
        return `La fecha de inicio no puede ser mayor a la fecha final en ${field.label}`;
      }
      if (isDateRangeOverSixMonths(start, end)) {
        return "El rango máximo permitido es 6 meses.";
      }

      (normalizedFilters as Record<string, unknown>)[startKey] =
        formatLocalDateStr(start);
      (normalizedFilters as Record<string, unknown>)[endKey] =
        formatLocalDateStr(end);
      return null;
    },
    [today]
  );

  const handleSubmit = useCallback(() => {
    setError("");
    const normalizedFilters = normalizeFiltersForSubmit(filters, fields);

    if (validateFilters) {
      const validationError = validateFilters(normalizedFilters);
      if (validationError) {
        setError(validationError);
        return;
      }
    }

    for (const field of fields) {
      if (field.type === "dateRange") {
        const range =
          dateRanges[field.key] ??
          buildDateRangeFromFilterValues(normalizedFilters as Record<string, unknown>, field.key);
        const dateError = validateDateRangeField(field, range, normalizedFilters);
        if (dateError) {
          setError(dateError);
          return;
        }
        continue;
      }

      if (!field.required) continue;
      const value = normalizedFilters[field.key];
      if (!value || (typeof value === "string" && value.trim() === "")) {
        setError(`${field.label} es obligatorio`);
        return;
      }
    }

    setFilters(normalizedFilters);
    setDateRanges((prev) => {
      const next = { ...prev };
      for (const field of fields) {
        if (field.type !== "dateRange") continue;
        const { startKey, endKey } = resolveDateFilterKeys(field.key);
        const start = parseLocalDateStr(
          (normalizedFilters as Record<string, unknown>)[startKey]
        );
        const end = parseLocalDateStr(
          (normalizedFilters as Record<string, unknown>)[endKey]
        );
        if (start && end) next[field.key] = [start, end];
      }
      return next;
    });
    onSearch(normalizedFilters);
  }, [
    filters,
    fields,
    validateFilters,
    onSearch,
    dateRanges,
    validateDateRangeField,
  ]);

  return (
    <div className="rc-filters">
      <div className="rc-row finz-filter-row">
        {fields.map((field) => {
          const wrapperClass = resolveFieldWrapperClass(field);

          if (field.type === "text") {
            return (
              <GenericInputSearch
                key={field.key}
                value={String(filters[field.key] ?? "")}
                onChange={handleFieldChange(field.key)}
                placeholder={resolveTextPlaceholder(field)}
                className="generic-input rc-filter-input"
              />
            );
          }

          if (
            field.type === "select" ||
            field.type === "selectFloating" ||
            field.type === "providerSelect"
          ) {
            const options =
              field.type === "providerSelect"
                ? providerOptions
                : mapSelectOptions(field.options);
            const select = (
              <GenericSelectSearchable
                value={selectFilterValue(filters[field.key])}
                onChange={handleFieldChange(field.key)}
                options={options}
                placeholder={resolveSelectPlaceholder(field)}
              />
            );

            if (wrapperClass) {
              return (
                <div key={field.key} className={wrapperClass}>
                  {select}
                </div>
              );
            }

            return <div key={field.key}>{select}</div>;
          }

          if (field.type === "dateRange") {
            const range =
              dateRanges[field.key] ??
              buildDateRangeFromFilterValues(
                filters as Record<string, unknown>,
                field.key
              );

            return (
              <div key={field.key} className={wrapperClass}>
                <GenericDateRangePicker
                  value={range}
                  onChange={handleDateRangeChange(field.key)}
                  placeholder={resolveDatePlaceholder(field)}
                />
              </div>
            );
          }

          return null;
        })}
        <div className="finz-filter-actions">
          {searchAppEvent ? (
            <PermissionGate appEvent={searchAppEvent}>
              <GenericButton variant="outlineFill" onClick={handleSubmit}>
                Buscar
              </GenericButton>
            </PermissionGate>
          ) : (
            <GenericButton variant="outlineFill" onClick={handleSubmit}>
              Buscar
            </GenericButton>
          )}
          {clearAppEvent ? (
            <PermissionGate appEvent={clearAppEvent}>
              <GenericButton variant="outlineFill" onClick={handleClear}>
                Limpiar
              </GenericButton>
            </PermissionGate>
          ) : (
            <GenericButton variant="outlineFill" onClick={handleClear}>
              Limpiar
            </GenericButton>
          )}
        </div>
      </div>
      <GenericModal
        visible={!!error}
        variant="alert"
        severity="warning"
        title="Advertencia"
        message={error}
        buttonText="Aceptar"
        onClose={() => setError("")}
        onConfirm={() => setError("")}
      />
    </div>
  );
}
