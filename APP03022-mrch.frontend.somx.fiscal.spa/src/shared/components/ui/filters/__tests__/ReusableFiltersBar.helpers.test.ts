/**
 * @jest-environment jsdom
 */
import { describe, it, expect } from "@jest/globals";

jest.mock("@shared/components/ui", () => ({
  GenericButton: () => null,
  GenericInputSearch: () => null,
  GenericSelectSearchable: () => null,
}));
jest.mock("@shared/components/ui/date", () => ({
  GenericDateRangePicker: () => null,
}));
jest.mock("@shared/components/ui/modal/GenericModal", () => ({
  __esModule: true,
  default: () => null,
}));
jest.mock("@shared/security", () => ({
  PermissionGate: ({ children }: any) => children,
}));
jest.mock("@/shared/session/fiscalListSession", () => ({
  readFiscalListFilters: () => null,
}));

import {
  normalizeProviderFilterValue,
  normalizeListboxFilterValue,
  normalizeFiltersForSubmit,
  selectFilterValue,
  mapSelectOptions,
  resolveFieldWrapperClass,
  resolveSelectPlaceholder,
  resolveTextPlaceholder,
  resolveDatePlaceholder,
  resolveDateFilterKeys,
  buildDateRangeFromFilterValues,
  applyDefaultDateFilters,
  hydrateFilterState,
  isValidUuid,
  normalizeUuidFilterValue,
  validateUuidFilterField,
  type FilterField,
} from "../ReusableFiltersBar";

describe("normalizeProviderFilterValue", () => {
  it("normaliza null, vacío y espacio a ''", () => {
    expect(normalizeProviderFilterValue(null)).toBe("");
    expect(normalizeProviderFilterValue("")).toBe("");
    expect(normalizeProviderFilterValue(" ")).toBe("");
  });

  it("conserva valores útiles", () => {
    expect(normalizeProviderFilterValue("ABC")).toBe("ABC");
  });
});

describe("normalizeListboxFilterValue", () => {
  it("delega a normalizeProviderFilterValue", () => {
    expect(normalizeListboxFilterValue(" ")).toBe("");
  });
});

describe("normalizeFiltersForSubmit", () => {
  it("normaliza providerSelect y estatus selectFloating a número", () => {
    const fields: FilterField[] = [
      { key: "idProveedor", label: "P", type: "providerSelect" },
      { key: "estatus", label: "E", type: "selectFloating" },
      { key: "otro", label: "O", type: "selectFloating" },
    ];
    const result = normalizeFiltersForSubmit(
      { idProveedor: " ", estatus: "3", otro: "X" },
      fields
    );
    expect(result.idProveedor).toBe("");
    expect(result.estatus).toBe(3);
    expect(result.otro).toBe("X");
  });

  it("estatus vacío queda undefined", () => {
    const fields: FilterField[] = [
      { key: "estatus", label: "E", type: "selectFloating" },
    ];
    expect(normalizeFiltersForSubmit({ estatus: " " }, fields).estatus).toBeUndefined();
  });

  it("recorta campos uuid", () => {
    const fields: FilterField[] = [
      { key: "uuid", label: "UUID", type: "uuid" },
    ];
    const result = normalizeFiltersForSubmit(
      { uuid: "  550e8400-e29b-41d4-a716-446655440000  " },
      fields
    );
    expect(result.uuid).toBe("550e8400-e29b-41d4-a716-446655440000");
  });
});

describe("uuid helpers", () => {
  it("isValidUuid acepta UUID canónico e ignora mayúsculas", () => {
    expect(isValidUuid("550e8400-e29b-41d4-a716-446655440000")).toBe(true);
    expect(isValidUuid("550E8400-E29B-41D4-A716-446655440000")).toBe(true);
  });

  it("isValidUuid rechaza vacío e inválidos", () => {
    expect(isValidUuid("")).toBe(false);
    expect(isValidUuid("   ")).toBe(false);
    expect(isValidUuid("abc-123")).toBe(false);
    expect(isValidUuid("550e8400e29b41d4a716446655440000")).toBe(false);
  });

  it("normalizeUuidFilterValue recorta y limpia", () => {
    expect(normalizeUuidFilterValue("  x  ")).toBe("x");
    expect(normalizeUuidFilterValue(" ")).toBe("");
  });

  it("validateUuidFilterField permite vacío si no es required", () => {
    const field: FilterField = { key: "uuid", label: "UUID", type: "uuid" };
    expect(validateUuidFilterField(field, "")).toBeNull();
    expect(validateUuidFilterField(field, "abc")).toBe("UUID no es un UUID válido");
    expect(
      validateUuidFilterField(field, "550e8400-e29b-41d4-a716-446655440000")
    ).toBeNull();
  });

  it("validateUuidFilterField exige valor si required", () => {
    const field: FilterField = {
      key: "uuid",
      label: "UUID",
      type: "uuid",
      required: true,
    };
    expect(validateUuidFilterField(field, "")).toBe("UUID es obligatorio");
  });
});

describe("selectFilterValue / mapSelectOptions", () => {
  it("preserva el espacio especial de 'Todos'", () => {
    expect(selectFilterValue(" ")).toBe(" ");
    expect(selectFilterValue(null)).toBe("");
    expect(selectFilterValue(2)).toBe("2");
  });

  it("mapea options a value/label string", () => {
    expect(mapSelectOptions([{ label: "A", value: 1 }])).toEqual([
      { value: "1", label: "A" },
    ]);
  });
});

describe("resolve* helpers", () => {
  it("resolveFieldWrapperClass", () => {
    expect(resolveFieldWrapperClass({ key: "f", label: "F", type: "dateRange" })).toBe(
      "rc-field-dates"
    );
    expect(
      resolveFieldWrapperClass({
        key: "x",
        label: "X",
        type: "text",
        containerClassName: "custom",
      })
    ).toBe("custom");
    expect(
      resolveFieldWrapperClass({ key: "estatus", label: "E", type: "selectFloating" })
    ).toBe("rc-filter-status-wrap");
    expect(
      resolveFieldWrapperClass({ key: "status", label: "E", type: "select" })
    ).toBe("rc-filter-status-wrap");
    expect(resolveFieldWrapperClass({ key: "serie", label: "S", type: "text" })).toBe("");
  });

  it("placeholders", () => {
    expect(
      resolveSelectPlaceholder({ key: "idProveedor", label: "Prov", type: "providerSelect" })
    ).toBe("Nombre Proveedor");
    expect(
      resolveSelectPlaceholder({
        key: "e",
        label: "Estado",
        type: "select",
        placeholder: "P",
      })
    ).toBe("P");
    expect(resolveTextPlaceholder({ key: "serie", label: "Serie", type: "text" })).toBe(
      "Serie"
    );
    expect(
      resolveDatePlaceholder({ key: "fechaRecepcion", label: "X", type: "dateRange" })
    ).toBe("Fecha de recepción");
    expect(
      resolveDatePlaceholder({ key: "fechaPago", label: "X", type: "dateRange" })
    ).toBe("Fecha de pago");
    expect(
      resolveDatePlaceholder({ key: "fechaEmision", label: "X", type: "dateRange" })
    ).toBe("Fecha de emisión");
    expect(resolveDatePlaceholder({ key: "otra", label: "Rango", type: "dateRange" })).toBe(
      "Rango"
    );
  });
});

describe("resolveDateFilterKeys", () => {
  it("resuelve claves conocidas y default", () => {
    expect(resolveDateFilterKeys("fecha")).toEqual({
      startKey: "fechaInicio",
      endKey: "fechaFinal",
    });
    expect(resolveDateFilterKeys("fechaRecepcion").startKey).toBe("fechaInicioRecepcion");
    expect(resolveDateFilterKeys("fechaPago").endKey).toBe("fechaPagoFin");
    expect(resolveDateFilterKeys("fechaEmision").startKey).toBe("fechaEmisionInicio");
    expect(resolveDateFilterKeys("custom")).toEqual({
      startKey: "customInicio",
      endKey: "customFin",
    });
  });
});

describe("date filter builders", () => {
  it("buildDateRangeFromFilterValues usa fechas válidas", () => {
    const range = buildDateRangeFromFilterValues(
      { fechaInicioRecepcion: "2024-01-01", fechaFinalRecepcion: "2024-01-31" },
      "fechaRecepcion"
    );
    expect(range[0].getFullYear()).toBe(2024);
    expect(range[1].getMonth()).toBe(0);
  });

  it("applyDefaultDateFilters completa con hoy si faltan fechas", () => {
    const fields: FilterField[] = [
      { key: "fechaRecepcion", label: "F", type: "dateRange" },
    ];
    const { filters, ranges } = applyDefaultDateFilters({}, fields);
    expect(filters.fechaInicioRecepcion).toBeTruthy();
    expect(filters.fechaFinalRecepcion).toBeTruthy();
    expect(ranges.fechaRecepcion).toHaveLength(2);
  });

  it("hydrateFilterState sin restore no lee sesión", () => {
    const fields: FilterField[] = [
      { key: "fechaRecepcion", label: "F", type: "dateRange" },
    ];
    const result = hydrateFilterState(
      { fechaInicioRecepcion: "2024-06-01", fechaFinalRecepcion: "2024-06-02" },
      fields
    );
    expect(result.filters.fechaInicioRecepcion).toBe("2024-06-01");
  });
});
