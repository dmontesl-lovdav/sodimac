import { ReactElement, useState, useEffect, useCallback, useRef } from "react";
import { GenericButton, GenericInput, GenericSelect, GenericSelectFloating } from "@shared/components/ui";
import { GenericDateRangePicker } from "@shared/components/ui/date";
import type { ChangeEvent } from "react";
import { ErrorMessage } from "@shared/components/ui";
import { getFiltersFromLocalStorage, saveFiltersToLocalStorage } from "@/utils/utils";

type SelectableOption<T> = {
  label: string;
  value: T;
};

export interface FilterField {
  key: string;
  label: string;
  type: "text" | "select" | "dateRange" | "selectFloating";
  placeholder?: string;
  options?: SelectableOption<string | number>[];
  required?: boolean;
  width?: string;
}

interface Props<F extends Record<string, any>> {
  fields: FilterField[];
  initialFilters: F;
  onSearch: (filters: F) => void;
  storageKey?: string;
  validateFilters?: (filters: F) => string | null;
  /** Se invoca una vez al montar con los filtros efectivos (guardados o initial). Usar para no hacer fetch hasta tener los filtros definitivos. */
  onHydrated?: (filters: F) => void;
}

export default function ReusableFiltersBar<F extends Record<string, any>>({
  fields,
  initialFilters,
  onSearch,
  storageKey,
  validateFilters,
  onHydrated,
}: Props<F>): ReactElement {
  const [filters, setFilters] = useState<F>(initialFilters);
  const [error, setError] = useState<string>("");
  const [dateRanges, setDateRanges] = useState<Record<string, Date[]>>({});
  const hasRestoredFromStorage = useRef(false);
  const hasHydratedRef = useRef(false);

  useEffect(() => {
    let effectiveFilters: F = initialFilters;
    if (storageKey) {
      const savedFilters = getFiltersFromLocalStorage<F>(storageKey);
      if (savedFilters) {
        effectiveFilters = savedFilters;
        setFilters(savedFilters);
        // Restaurar rangos de fechas
        fields.forEach(field => {
          if (field.type === "dateRange") {
            let startKey = `${field.key}Inicio`;
            let endKey = `${field.key}Fin`;
            
            if (field.key === "fecha") {
              startKey = "fechaInicio";
              endKey = "fechaFinal";
            } else if (field.key === "fechaRecepcion") {
              startKey = "fechaInicioRecepcion";
              endKey = "fechaFinalRecepcion";
            } else if (field.key === "fechaPago") {
              startKey = "fechaPagoInicio";
              endKey = "fechaPagoFin";
            } else if (field.key === "fechaEmision") {
              startKey = "fechaEmisionInicio";
              endKey = "fechaEmisionFin";
            }
            
            const start = savedFilters[startKey];
            const end = savedFilters[endKey];
            if (start && end) {
              setDateRanges(prev => ({
                ...prev,
                [field.key]: [new Date(start), new Date(end)],
              }));
            }
          }
        });
        if (!hasRestoredFromStorage.current) {
          hasRestoredFromStorage.current = true;
          onSearch(savedFilters);
        }
      }
    }
    if (!hasHydratedRef.current) {
      hasHydratedRef.current = true;
      onHydrated?.(effectiveFilters);
    }
  }, [storageKey, fields, onSearch, initialFilters, onHydrated]);

  const handleFieldChange = useCallback(<K extends keyof F>(key: K) => {
    return (event: ChangeEvent<HTMLInputElement | HTMLSelectElement> | { target: { value: string } }) => {
      setFilters(prev => ({ ...prev, [key]: event.target.value as F[K] }));
      setError("");
    };
  }, []);

  const handleDateRangeChange = useCallback((fieldKey: string) => {
    return (dates: (Date | null)[]) => {
      setDateRanges(prev => ({ ...prev, [fieldKey]: dates.filter(Boolean) as Date[] }));
      // Mapear campos de fecha comunes
      let startKey = `${fieldKey}Inicio`;
      let endKey = `${fieldKey}Fin`;
      
      // Casos especiales
      if (fieldKey === "fecha") {
        startKey = "fechaInicio";
        endKey = "fechaFinal";
      } else if (fieldKey === "fechaRecepcion") {
        startKey = "fechaInicioRecepcion";
        endKey = "fechaFinalRecepcion";
      } else if (fieldKey === "fechaPago") {
        startKey = "fechaPagoInicio";
        endKey = "fechaPagoFin";
      } else if (fieldKey === "fechaEmision") {
        startKey = "fechaEmisionInicio";
        endKey = "fechaEmisionFin";
      } else if (fieldKey.includes("fecha")) {
        startKey = `${fieldKey}Inicio`;
        endKey = `${fieldKey}Fin`;
      }
      
      setFilters(prev => ({
        ...prev,
        [startKey]: dates?.[0]?.toISOString().split('T')[0] ?? prev[startKey],
        [endKey]: dates?.[1]?.toISOString().split('T')[0] ?? prev[endKey],
      }));
      setError("");
    };
  }, []);

  const handleClear = useCallback(() => {
    setFilters([] as unknown as F);
    setDateRanges({});
    setError("");
    if (storageKey) {
      saveFiltersToLocalStorage(storageKey, initialFilters);
    }
    onSearch(initialFilters);
  }, [initialFilters, storageKey, onSearch]);

  const handleSubmit = useCallback(() => {
    setError("");
    
    // Validación personalizada
    if (validateFilters) {
      const validationError = validateFilters(filters);
      if (validationError) {
        setError(validationError);
        return;
      }
    }

    // Validación de campos requeridos
    const requiredFields = fields.filter(f => f.required);
    for (const field of requiredFields) {
          if (field.type === "dateRange") {
            let startKey = `${field.key}Inicio`;
            let endKey = `${field.key}Fin`;
            
            if (field.key === "fecha") {
              startKey = "fechaInicio";
              endKey = "fechaFinal";
            } else if (field.key === "fechaRecepcion") {
              startKey = "fechaInicioRecepcion";
              endKey = "fechaFinalRecepcion";
            } else if (field.key === "fechaPago") {
              startKey = "fechaPagoInicio";
              endKey = "fechaPagoFin";
            } else if (field.key === "fechaEmision") {
              startKey = "fechaEmisionInicio";
              endKey = "fechaEmisionFin";
            }
            
            if (!filters[startKey] || !filters[endKey]) {
              setError(`${field.label} es obligatorio`);
              return;
            }
            const start = new Date(filters[startKey]);
            const end = new Date(filters[endKey]);
            if (start > end) {
              setError(`La fecha de inicio no puede ser mayor a la fecha final en ${field.label}`);
              return;
            }
          } else {
        const value = filters[field.key];
        if (!value || (typeof value === "string" && value.trim() === "")) {
          setError(`${field.label} es obligatorio`);
          return;
        }
      }
    }

    if (storageKey) {
      saveFiltersToLocalStorage(storageKey, filters);
    }
    onSearch(filters);
  }, [filters, fields, validateFilters, onSearch, storageKey]);

  return (
    <div>
      
      <div className="fiscal-flex fiscal-flex-wrap">
        {fields.map((field) => {
          if (field.type === "text") {
            return (
              <div key={field.key} className={field.width === "450px" ? "fiscal-w-450" : field.width === "650px" ? "fiscal-w-650" : "fiscal-w-auto"}>
                <GenericInput
                  label={field.label}
                  value={String(filters[field.key] || "")}
                  onChange={handleFieldChange(field.key)}
                  placeholder={field.placeholder}
                  required={field.required}
                />
              </div>
            );
          }

          if (field.type === "select") {
            return (
              <div key={field.key} className={field.width ? (field.width === "450px" ? "fiscal-w-450" : "fiscal-w-650") : "fiscal-w-auto"}>
                <GenericSelect
                  value={String(filters[field.key] || "")}
                  onChange={handleFieldChange(field.key)}
                  placeholder={field.placeholder || "Seleccione una opción"}
                  options={field.options || []}
                />
              </div>
            );
          }

          if (field.type === "selectFloating") {
            return (
              <div key={field.key} className={field.width === "650px" ? "fiscal-w-650" : "fiscal-w-450"}>
                <GenericSelectFloating
                  label={field.label}
                  value={String(filters[field.key] || "")}
                  onChange={handleFieldChange(field.key)}
                  options={field.options || []}
                  placeholder={field.placeholder || "Seleccione una opción"}
                />
              </div>
            );
          }

          if (field.type === "dateRange") {
            let startKey = `${field.key}Inicio`;
            let endKey = `${field.key}Fin`;
            
            if (field.key === "fecha") {
              startKey = "fechaInicio";
              endKey = "fechaFinal";
            } else if (field.key === "fechaRecepcion") {
              startKey = "fechaInicioRecepcion";
              endKey = "fechaFinalRecepcion";
            } else if (field.key === "fechaPago") {
              startKey = "fechaPagoInicio";
              endKey = "fechaPagoFin";
            } else if (field.key === "fechaEmision") {
              startKey = "fechaEmisionInicio";
              endKey = "fechaEmisionFin";
            }
            
            return (
              <div key={field.key}>
                <GenericDateRangePicker
                  value={dateRanges[field.key]?.length === 2 ? (dateRanges[field.key] as [Date | null, Date | null]) : [
                    filters[startKey] ? new Date(filters[startKey]) : null,
                    filters[endKey] ? new Date(filters[endKey]) : null,
                  ]}
                  onChange={handleDateRangeChange(field.key)}
                  placeholder={field.placeholder || "Rango de fechas"}
                />
              </div>
            );
          }

          return null;
        })}

        
      <div className="fiscal-flex fiscal-gap-sm fiscal-justify-end">
        <GenericButton variant="outline" onClick={handleClear}>
          Limpiar filtros
        </GenericButton>
        <GenericButton variant="outline" onClick={handleSubmit}>
          Buscar
        </GenericButton>
      </div>
      </div>
      {error && (
        <div className="fiscal-mt-2">
          <ErrorMessage message={error} />
        </div>
      )}
    </div>
  );
}
