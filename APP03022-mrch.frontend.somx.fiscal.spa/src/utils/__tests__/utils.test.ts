import { describe, it, expect, beforeEach, afterEach } from "@jest/globals";
import {
  formatAmount,
  startOfLocalDay,
  endOfLocalDay,
  formatLocalDateStr,
  parseLocalDateStr,
  isDateRangeOverSixMonths,
  toNumber,
  toCurrency,
  mapCatalogResponseToFilterOptions,
  insertThousandsSeparators,
  stripTrailingSlashes,
  fetchCatalogAsSelectableOptions,
  getXmlFileNameFromRow,
  formatBytes,
  getErrorMessage,
} from "../utils";

// ---------------------------------------------------------------------------
// formatAmount
// ---------------------------------------------------------------------------

describe("formatAmount", () => {
  it("formatea número positivo con dos decimales y separador de miles", () => {
    expect(formatAmount(1234567.89)).toBe("$1,234,567.89");
  });

  it("insertThousandsSeparators agrega comas", () => {
    expect(insertThousandsSeparators("1234567")).toBe("1,234,567");
    expect(insertThousandsSeparators("-500")).toBe("-500");
  });

  it("stripTrailingSlashes elimina solo barras finales", () => {
    expect(stripTrailingSlashes("https://a.com///")).toBe("https://a.com");
    expect(stripTrailingSlashes("https://a.com")).toBe("https://a.com");
  });

  it("formatea cero correctamente", () => {
    expect(formatAmount(0)).toBe("$0.00");
  });

  it("formatea número negativo", () => {
    expect(formatAmount(-500)).toBe("$-500.00");
  });

  it("devuelve $0.00 para NaN", () => {
    expect(formatAmount(NaN)).toBe("$0.00");
  });

  it("convierte string numérico a monto", () => {
    expect(formatAmount("1500" as any)).toBe("$1,500.00");
  });
});

// ---------------------------------------------------------------------------
// toNumber
// ---------------------------------------------------------------------------

describe("toNumber", () => {
  it("devuelve el número si es válido", () => {
    expect(toNumber(42)).toBe(42);
  });

  it("devuelve el default para null", () => {
    expect(toNumber(null)).toBe(0);
  });

  it("devuelve el default para undefined", () => {
    expect(toNumber(undefined)).toBe(0);
  });

  it("convierte string numérico", () => {
    expect(toNumber("3.14")).toBe(3.14);
  });

  it("devuelve el default para string no numérico", () => {
    expect(toNumber("abc", -1)).toBe(-1);
  });

  it("devuelve el default para Infinity", () => {
    expect(toNumber(Infinity, 99)).toBe(99);
  });

  it("devuelve el default para NaN", () => {
    expect(toNumber(NaN, 5)).toBe(5);
  });
});

// ---------------------------------------------------------------------------
// toCurrency
// ---------------------------------------------------------------------------

describe("toCurrency", () => {
  it("formatea número válido como moneda", () => {
    expect(toCurrency(1000)).toBe("$1,000.00");
  });

  it("devuelve default para null", () => {
    expect(toCurrency(null)).toBe("$0.00");
  });

  it("devuelve default personalizado para valor inválido", () => {
    expect(toCurrency("nope", "N/A")).toBe("N/A");
  });

  it("formatea cero", () => {
    expect(toCurrency(0)).toBe("$0.00");
  });

  it("convierte string numérico a moneda", () => {
    expect(toCurrency("250.5")).toBe("$250.50");
  });
});

// ---------------------------------------------------------------------------
// startOfLocalDay / endOfLocalDay
// ---------------------------------------------------------------------------

describe("startOfLocalDay", () => {
  it("devuelve medianoche (00:00:00.000) del día dado", () => {
    const d = new Date(2024, 5, 15, 14, 30, 45, 500);
    const start = startOfLocalDay(d);
    expect(start.getHours()).toBe(0);
    expect(start.getMinutes()).toBe(0);
    expect(start.getSeconds()).toBe(0);
    expect(start.getMilliseconds()).toBe(0);
  });

  it("conserva el mismo año/mes/día", () => {
    const d = new Date(2024, 5, 15, 14, 30);
    const start = startOfLocalDay(d);
    expect(start.getFullYear()).toBe(2024);
    expect(start.getMonth()).toBe(5);
    expect(start.getDate()).toBe(15);
  });
});

describe("endOfLocalDay", () => {
  it("devuelve 23:59:59.999 del día dado", () => {
    const d = new Date(2024, 5, 15, 3, 0, 0);
    const end = endOfLocalDay(d);
    expect(end.getHours()).toBe(23);
    expect(end.getMinutes()).toBe(59);
    expect(end.getSeconds()).toBe(59);
    expect(end.getMilliseconds()).toBe(999);
  });

  it("conserva el mismo año/mes/día", () => {
    const d = new Date(2024, 5, 15);
    const end = endOfLocalDay(d);
    expect(end.getFullYear()).toBe(2024);
    expect(end.getMonth()).toBe(5);
    expect(end.getDate()).toBe(15);
  });

  it("startOfLocalDay es anterior a endOfLocalDay para el mismo día", () => {
    const d = new Date(2024, 5, 15);
    expect(startOfLocalDay(d).getTime()).toBeLessThan(endOfLocalDay(d).getTime());
  });
});

// ---------------------------------------------------------------------------
// formatLocalDateStr
// ---------------------------------------------------------------------------

describe("formatLocalDateStr", () => {
  it("formatea fecha en YYYY-MM-DD", () => {
    const d = new Date(2024, 0, 5);
    expect(formatLocalDateStr(d)).toBe("2024-01-05");
  });

  it("agrega cero a mes y día de un dígito", () => {
    const d = new Date(2024, 8, 3);
    expect(formatLocalDateStr(d)).toBe("2024-09-03");
  });

  it("formatea correctamente diciembre (mes 11)", () => {
    const d = new Date(2023, 11, 31);
    expect(formatLocalDateStr(d)).toBe("2023-12-31");
  });
});

// ---------------------------------------------------------------------------
// parseLocalDateStr
// ---------------------------------------------------------------------------

describe("parseLocalDateStr", () => {
  it("parsea YYYY-MM-DD correctamente", () => {
    const d = parseLocalDateStr("2024-06-15");
    expect(d).not.toBeNull();
    expect(d!.getFullYear()).toBe(2024);
    expect(d!.getMonth()).toBe(5);
    expect(d!.getDate()).toBe(15);
  });

  it("devuelve null para string vacío", () => {
    expect(parseLocalDateStr("")).toBeNull();
  });

  it("devuelve null para null", () => {
    expect(parseLocalDateStr(null)).toBeNull();
  });

  it("devuelve null para fecha inválida", () => {
    expect(parseLocalDateStr("not-a-date")).toBeNull();
  });

  it("devuelve null para número", () => {
    expect(parseLocalDateStr(12345)).toBeNull();
  });

  it("roundtrip: formatLocalDateStr → parseLocalDateStr preserva el día", () => {
    const original = new Date(2024, 3, 20);
    const str = formatLocalDateStr(original);
    const parsed = parseLocalDateStr(str);
    expect(parsed?.getDate()).toBe(original.getDate());
    expect(parsed?.getMonth()).toBe(original.getMonth());
    expect(parsed?.getFullYear()).toBe(original.getFullYear());
  });
});

// ---------------------------------------------------------------------------
// isDateRangeOverSixMonths
// ---------------------------------------------------------------------------

describe("isDateRangeOverSixMonths", () => {
  it("rango de exactamente 6 meses → false", () => {
    const start = new Date(2024, 0, 1);
    const end = new Date(2024, 6, 1);
    expect(isDateRangeOverSixMonths(start, end)).toBe(false);
  });

  it("rango de 7 meses → true", () => {
    const start = new Date(2024, 0, 1);
    const end = new Date(2024, 7, 1);
    expect(isDateRangeOverSixMonths(start, end)).toBe(true);
  });

  it("rango de 5 meses → false", () => {
    const start = new Date(2024, 0, 1);
    const end = new Date(2024, 5, 1);
    expect(isDateRangeOverSixMonths(start, end)).toBe(false);
  });

  it("rango de 1 año → true", () => {
    const start = new Date(2023, 0, 1);
    const end = new Date(2024, 0, 1);
    expect(isDateRangeOverSixMonths(start, end)).toBe(true);
  });

  it("mismo día → false", () => {
    const d = new Date(2024, 5, 1);
    expect(isDateRangeOverSixMonths(d, d)).toBe(false);
  });

  it("cruza cambio de año dentro de 6 meses → false", () => {
    const start = new Date(2023, 9, 1);
    const end = new Date(2024, 3, 1);
    expect(isDateRangeOverSixMonths(start, end)).toBe(false);
  });
});

// ---------------------------------------------------------------------------
// mapCatalogResponseToFilterOptions
// ---------------------------------------------------------------------------

describe("mapCatalogResponseToFilterOptions", () => {
  it("array plano de filas con internalStatus y description", () => {
    const data = [
      { internalStatus: "1", description: "Activo" },
      { internalStatus: "2", description: "Inactivo" },
    ];
    const result = mapCatalogResponseToFilterOptions(data);
    expect(result).not.toBeNull();
    expect(result![0]).toEqual({ label: "Todos los tipos", value: " " });
    expect(result![1]).toEqual({ label: "Activo", value: "1" });
    expect(result![2]).toEqual({ label: "Inactivo", value: "2" });
  });

  it("objeto con propiedad details", () => {
    const data = {
      details: [{ internalStatus: "10", description: "Pendiente" }],
    };
    const result = mapCatalogResponseToFilterOptions(data);
    expect(result).not.toBeNull();
    expect(result![1]).toEqual({ label: "Pendiente", value: "10" });
  });

  it("objeto con propiedad content", () => {
    const data = {
      content: [{ id: "5", description: "En proceso" }],
    };
    const result = mapCatalogResponseToFilterOptions(data);
    expect(result).not.toBeNull();
    expect(result![1]).toEqual({ label: "En proceso", value: "5" });
  });

  it("objeto con propiedad items", () => {
    const data = {
      items: [{ value: "A", description: "Tipo A" }],
    };
    const result = mapCatalogResponseToFilterOptions(data);
    expect(result).not.toBeNull();
    expect(result![1]).toEqual({ label: "Tipo A", value: "A" });
  });

  it("array vacío → null", () => {
    expect(mapCatalogResponseToFilterOptions([])).toBeNull();
  });

  it("null → null", () => {
    expect(mapCatalogResponseToFilterOptions(null)).toBeNull();
  });

  it("string → null", () => {
    expect(mapCatalogResponseToFilterOptions("texto")).toBeNull();
  });

  it("filtra filas con value vacío", () => {
    const data = [
      { internalStatus: "", description: "" },
      { internalStatus: "1", description: "Válido" },
    ];
    const result = mapCatalogResponseToFilterOptions(data);
    expect(result).not.toBeNull();
    expect(result!.length).toBe(2);
  });
});

// ---------------------------------------------------------------------------
// fetchCatalogAsSelectableOptions
// ---------------------------------------------------------------------------

describe("fetchCatalogAsSelectableOptions", () => {
  const ROWS = [
    { description: "Disponible", value: "1", key: "A" },
    { description: "En proceso", value: "2", key: "B" },
    { description: "Borrado", value: "3", key: "C" },
  ];

  it("incluye la opción 'Todos' al inicio con value ' '", () => {
    const result = fetchCatalogAsSelectableOptions(ROWS);
    expect(result[0]).toEqual({ label: "Todos", value: " " });
  });

  it("mapea description → label y value → value", () => {
    const result = fetchCatalogAsSelectableOptions(ROWS);
    expect(result[1]).toEqual({ label: "Disponible", value: "1" });
    expect(result[2]).toEqual({ label: "En proceso", value: "2" });
  });

  it("filtra filas cuya description incluye 'borra'", () => {
    const result = fetchCatalogAsSelectableOptions(ROWS);
    const labels = result.map((r) => r.label);
    expect(labels).not.toContain("Borrado");
  });

  it("filtra filas con value vacío", () => {
    const data = [
      { description: "Sin valor", value: "" },
      { description: "Con valor", value: "1" },
    ];
    const result = fetchCatalogAsSelectableOptions(data);
    const labels = result.map((r) => r.label);
    expect(labels).not.toContain("Sin valor");
  });

  it("acepta etiqueta personalizada para la opción inicial", () => {
    const result = fetchCatalogAsSelectableOptions(ROWS, "Seleccionar");
    expect(result[0].label).toBe("Seleccionar");
  });

  it("devuelve al menos la opción 'Todos' cuando los datos están vacíos", () => {
    const result = fetchCatalogAsSelectableOptions([]);
    expect(result).toHaveLength(1);
    expect(result[0].value).toBe(" ");
  });

  it("acepta objeto con propiedad details", () => {
    const data = { details: [{ description: "Activo", value: "10" }] };
    const result = fetchCatalogAsSelectableOptions(data);
    expect(result[1]).toEqual({ label: "Activo", value: "10" });
  });

  it("usa lista vacía cuando el objeto no tiene details array", () => {
    const result = fetchCatalogAsSelectableOptions({ foo: 1 });
    expect(result).toEqual([{ label: "Todos", value: " " }]);
  });

  it("usa lista vacía para null", () => {
    const result = fetchCatalogAsSelectableOptions(null);
    expect(result).toEqual([{ label: "Todos", value: " " }]);
  });

  it("ordena las opciones numéricamente por value", () => {
    const data = [
      { description: "Tres", value: "3" },
      { description: "Uno", value: "1" },
      { description: "Dos", value: "2" },
    ];
    const result = fetchCatalogAsSelectableOptions(data);
    expect(result[1].label).toBe("Uno");
    expect(result[2].label).toBe("Dos");
    expect(result[3].label).toBe("Tres");
  });
});

// ---------------------------------------------------------------------------
// getXmlFileNameFromRow
// ---------------------------------------------------------------------------

describe("getXmlFileNameFromRow", () => {
  it("usa fiscalUuid cuando está disponible", () => {
    expect(getXmlFileNameFromRow({ fiscalUuid: "aaa-111" })).toBe("aaa-111.xml");
  });

  it("cae a invoiceUuid cuando fiscalUuid está vacío", () => {
    expect(getXmlFileNameFromRow({ fiscalUuid: "", invoiceUuid: "bbb-222" })).toBe("bbb-222.xml");
  });

  it("cae a paymentsUuid cuando los anteriores están vacíos", () => {
    expect(
      getXmlFileNameFromRow({ fiscalUuid: null, invoiceUuid: null, paymentsUuid: "ccc-333" })
    ).toBe("ccc-333.xml");
  });

  it("devuelve 'documento.xml' cuando todos los uuids son nulos o vacíos", () => {
    expect(getXmlFileNameFromRow({ fiscalUuid: null, invoiceUuid: null, paymentsUuid: null })).toBe(
      "documento.xml"
    );
  });

  it("ignora uuids que son solo espacios", () => {
    expect(getXmlFileNameFromRow({ fiscalUuid: "  ", invoiceUuid: "  " })).toBe("documento.xml");
  });

  it("usa fiscalUuid sin importar que los demás tengan valor", () => {
    expect(
      getXmlFileNameFromRow({ fiscalUuid: "pri", invoiceUuid: "seg", paymentsUuid: "ter" })
    ).toBe("pri.xml");
  });
});

// ---------------------------------------------------------------------------
// formatBytes
// ---------------------------------------------------------------------------

describe("formatBytes", () => {
  it("0 bytes → '0 B'", () => {
    expect(formatBytes(0)).toBe("0 B");
  });

  it("1024 bytes → '1 KB'", () => {
    expect(formatBytes(1024)).toBe("1 KB");
  });

  it("1048576 bytes → '1 MB'", () => {
    expect(formatBytes(1048576)).toBe("1 MB");
  });

  it("500 bytes → 'X B' (sin punto decimal innecesario)", () => {
    const result = formatBytes(500);
    expect(result).toContain("B");
    expect(result).not.toContain("KB");
  });

  it("1572864 bytes → '1.5 MB'", () => {
    expect(formatBytes(1572864)).toBe("1.5 MB");
  });
});

// ---------------------------------------------------------------------------
// getErrorMessage
// ---------------------------------------------------------------------------

describe("getErrorMessage (fiscal spa)", () => {
  const FALLBACK = "Error por defecto";

  it("devuelve fallback cuando error es null", () => {
    expect(getErrorMessage(null, FALLBACK)).toBe(FALLBACK);
  });

  it("devuelve fallback cuando error es string", () => {
    expect(getErrorMessage("texto", FALLBACK)).toBe(FALLBACK);
  });

  it("devuelve fallback cuando error es número", () => {
    expect(getErrorMessage(42, FALLBACK)).toBe(FALLBACK);
  });

  it("extrae e.message cuando no hay response.data", () => {
    expect(getErrorMessage({ message: "Algo salió mal" }, FALLBACK)).toBe(
      "Algo salió mal"
    );
  });

  it("extrae response.data.message cuando está disponible", () => {
    const err = { response: { data: { message: "Error del servidor" } } };
    expect(getErrorMessage(err, FALLBACK)).toBe("Error del servidor");
  });

  it("combina code y message cuando ambos están presentes en response.data", () => {
    const err = { response: { data: { code: "ERR001", message: "No permitido" } } };
    expect(getErrorMessage(err, FALLBACK)).toBe("ERR001: No permitido");
  });

  it("usa response.data.message cuando solo hay message (sin code)", () => {
    const err = { response: { data: { message: "Solo mensaje" } } };
    expect(getErrorMessage(err, FALLBACK)).toBe("Solo mensaje");
  });

  it("devuelve fallback cuando el objeto no tiene campos reconocibles", () => {
    expect(getErrorMessage({ foo: "bar" }, FALLBACK)).toBe(FALLBACK);
  });
});
