/**
 * @jest-environment jsdom
 */
import { describe, it, expect, beforeEach, afterEach, jest } from "@jest/globals";
import {
  isValidXml,
  isValidPdf,
  saveFiltersToLocalStorage,
  getFiltersFromLocalStorage,
  buildFiscalSpaUrl,
  exportToCSV,
  downloadXML,
  downloadPDF,
  fetchCatalog,
  fetchSystemParameters,
  fetchCatalogDetails,
} from "../utils";

// ---------------------------------------------------------------------------
// Setup global mocks de DOM
// ---------------------------------------------------------------------------

const mockCreateObjectURL = jest.fn(() => "blob:mock-url");
const mockRevokeObjectURL = jest.fn();

beforeEach(() => {
  Object.defineProperty(URL, "createObjectURL", {
    value: mockCreateObjectURL,
    writable: true,
  });
  Object.defineProperty(URL, "revokeObjectURL", {
    value: mockRevokeObjectURL,
    writable: true,
  });
  mockCreateObjectURL.mockClear();
  mockRevokeObjectURL.mockClear();
  localStorage.clear();
});

// ---------------------------------------------------------------------------
// isValidXml
// ---------------------------------------------------------------------------

describe("isValidXml", () => {
  it("acepta archivo .xml con MIME application/xml", () => {
    const f = new File(["<root/>"], "documento.xml", { type: "application/xml" });
    expect(isValidXml(f)).toBe(true);
  });

  it("acepta archivo .xml con MIME text/xml", () => {
    const f = new File(["<root/>"], "archivo.xml", { type: "text/xml" });
    expect(isValidXml(f)).toBe(true);
  });

  it("rechaza archivo .pdf aunque el MIME sea correcto", () => {
    const f = new File([""], "archivo.pdf", { type: "application/xml" });
    expect(isValidXml(f)).toBe(false);
  });

  it("rechaza archivo con extensión .xml pero MIME incorrecto", () => {
    const f = new File([""], "archivo.xml", { type: "text/plain" });
    expect(isValidXml(f)).toBe(false);
  });

  it("rechaza archivo sin extensión y sin MIME", () => {
    const f = new File([""], "archivo", { type: "" });
    expect(isValidXml(f)).toBe(false);
  });

  it("extensión .XML en mayúsculas es aceptada (toLowerCase)", () => {
    const f = new File(["<root/>"], "DOCUMENTO.XML", { type: "application/xml" });
    expect(isValidXml(f)).toBe(true);
  });
});

// ---------------------------------------------------------------------------
// isValidPdf
// ---------------------------------------------------------------------------

describe("isValidPdf", () => {
  it("acepta archivo .pdf con MIME application/pdf", () => {
    const f = new File(["%PDF-1.4"], "factura.pdf", { type: "application/pdf" });
    expect(isValidPdf(f)).toBe(true);
  });

  it("rechaza archivo .xml aunque tenga nombre correcto", () => {
    const f = new File([""], "factura.xml", { type: "application/pdf" });
    expect(isValidPdf(f)).toBe(false);
  });

  it("rechaza archivo .pdf con MIME incorrecto", () => {
    const f = new File([""], "factura.pdf", { type: "application/octet-stream" });
    expect(isValidPdf(f)).toBe(false);
  });

  it("extensión .PDF en mayúsculas es aceptada (toLowerCase)", () => {
    const f = new File(["%PDF-1.4"], "FACTURA.PDF", { type: "application/pdf" });
    expect(isValidPdf(f)).toBe(true);
  });
});

// ---------------------------------------------------------------------------
// saveFiltersToLocalStorage / getFiltersFromLocalStorage
// ---------------------------------------------------------------------------

describe("saveFiltersToLocalStorage / getFiltersFromLocalStorage", () => {
  const KEY = "test:filters";

  it("guarda y recupera un objeto de filtros", () => {
    const filters = { status: "1", startDate: "2024-01-01" };
    saveFiltersToLocalStorage(KEY, filters);
    const result = getFiltersFromLocalStorage<typeof filters>(KEY);
    expect(result).toEqual(filters);
  });

  it("devuelve null cuando la clave no existe", () => {
    expect(getFiltersFromLocalStorage("clave:inexistente")).toBeNull();
  });

  it("sobrescribe filtros guardados previamente", () => {
    saveFiltersToLocalStorage(KEY, { status: "1" });
    saveFiltersToLocalStorage(KEY, { status: "2", extra: true });
    const result = getFiltersFromLocalStorage<{ status: string; extra?: boolean }>(KEY);
    expect(result?.status).toBe("2");
    expect(result?.extra).toBe(true);
  });

  it("guarda correctamente valores con tipos variados", () => {
    const filters = { page: 3, active: false, tags: ["a", "b"] };
    saveFiltersToLocalStorage(KEY, filters);
    const result = getFiltersFromLocalStorage<typeof filters>(KEY);
    expect(result).toEqual(filters);
  });

  it("devuelve null cuando el valor almacenado no es JSON válido", () => {
    localStorage.setItem(KEY, "{invalid json");
    expect(getFiltersFromLocalStorage(KEY)).toBeNull();
  });
});

// ---------------------------------------------------------------------------
// buildFiscalSpaUrl (con variables de entorno)
// ---------------------------------------------------------------------------

describe("buildFiscalSpaUrl", () => {
  const OLD_ENV = process.env;

  beforeEach(() => {
    process.env = { ...OLD_ENV };
  });

  afterEach(() => {
    process.env = OLD_ENV;
  });

  it("construye URL con base que incluye # y FBC_HOME definido", () => {
    process.env.FBC_HOME = "https://app.ejemplo.com/";
    const url = buildFiscalSpaUrl("facturas");
    expect(url).toContain("facturas");
    expect(url).toContain("#");
    expect(url).toContain("https://app.ejemplo.com/");
  });

  it("elimina la barra inicial de la ruta", () => {
    process.env.FBC_HOME = "https://app.ejemplo.com/";
    const url = buildFiscalSpaUrl("/notas-credito");
    expect(url).toContain("notas-credito");
    expect(url).not.toContain("//notas-credito");
  });

  it("agrega query string cuando se pasan URLSearchParams", () => {
    process.env.FBC_HOME = "https://app.ejemplo.com/";
    const params = new URLSearchParams({ uuid: "abc-123" });
    const url = buildFiscalSpaUrl("facturas", params);
    expect(url).toContain("uuid=abc-123");
  });

  it("no duplica separador cuando FBC_HOME termina sin barra", () => {
    process.env.FBC_HOME = "https://app.ejemplo.com";
    const url = buildFiscalSpaUrl("facturas");
    expect(url).not.toContain("//facturas");
  });

  it("FBC_HOME vacío produce URL relativa con hash", () => {
    process.env.FBC_HOME = "";
    const url = buildFiscalSpaUrl("facturas");
    expect(url).toContain("facturas");
    expect(url).toContain("#");
  });
});

// ---------------------------------------------------------------------------
// exportToCSV (mock DOM)
// ---------------------------------------------------------------------------

describe("exportToCSV", () => {
  it("llama a URL.createObjectURL para crear el blob", () => {
    exportToCSV(["Columna A", "Columna B"], [["val1", "val2"]], "reporte");
    expect(mockCreateObjectURL).toHaveBeenCalledTimes(1);
  });

  it("agrega y luego remueve el enlace del DOM", () => {
    const appendSpy = jest.spyOn(document.body, "appendChild");
    const removeSpy = jest.spyOn(document.body, "removeChild");
    exportToCSV(["Col"], [["dato"]], "archivo");
    expect(appendSpy).toHaveBeenCalledTimes(1);
    expect(removeSpy).toHaveBeenCalledTimes(1);
    appendSpy.mockRestore();
    removeSpy.mockRestore();
  });

  it("no lanza excepción con arrays vacíos", () => {
    expect(() => exportToCSV([], [], "vacio")).not.toThrow();
  });
});

// ---------------------------------------------------------------------------
// downloadXML (mock DOM)
// ---------------------------------------------------------------------------

describe("downloadXML", () => {
  it("no hace nada cuando xmlContent es null", () => {
    downloadXML(null);
    expect(mockCreateObjectURL).not.toHaveBeenCalled();
  });

  it("no hace nada cuando xmlContent es string vacío", () => {
    downloadXML("   ");
    expect(mockCreateObjectURL).not.toHaveBeenCalled();
  });

  it("llama a URL.createObjectURL con contenido válido", () => {
    downloadXML("<root><data/></root>", "exportacion");
    expect(mockCreateObjectURL).toHaveBeenCalledTimes(1);
  });

  it("agrega .xml al nombre si no lo tiene", () => {
    const appendSpy = jest.spyOn(document.body, "appendChild");
    downloadXML("<root/>", "sin-extension");
    const anchor = appendSpy.mock.calls[0]?.[0] as HTMLAnchorElement;
    expect(anchor?.download).toBe("sin-extension.xml");
    appendSpy.mockRestore();
  });

  it("respeta el nombre cuando ya termina en .xml", () => {
    const appendSpy = jest.spyOn(document.body, "appendChild");
    downloadXML("<root/>", "ya-tiene.xml");
    const anchor = appendSpy.mock.calls[0]?.[0] as HTMLAnchorElement;
    expect(anchor?.download).toBe("ya-tiene.xml");
    appendSpy.mockRestore();
  });
});

// ---------------------------------------------------------------------------
// downloadPDF (mock DOM)
// ---------------------------------------------------------------------------

describe("downloadPDF", () => {
  it("no hace nada cuando la URL es vacía", () => {
    const appendSpy = jest.spyOn(document.body, "appendChild");
    downloadPDF("");
    expect(appendSpy).not.toHaveBeenCalled();
    appendSpy.mockRestore();
  });

  it("no hace nada cuando la URL es solo espacios", () => {
    const appendSpy = jest.spyOn(document.body, "appendChild");
    downloadPDF("   ");
    expect(appendSpy).not.toHaveBeenCalled();
    appendSpy.mockRestore();
  });

  it("agrega enlace al DOM con la URL proporcionada", () => {
    const appendSpy = jest.spyOn(document.body, "appendChild");
    downloadPDF("https://servidor.com/archivo.pdf", "mi-factura");
    const anchor = appendSpy.mock.calls[0]?.[0] as HTMLAnchorElement;
    expect(anchor?.href).toContain("https://servidor.com/archivo.pdf");
    appendSpy.mockRestore();
  });

  it("agrega .pdf al nombre de archivo si no lo tiene", () => {
    const appendSpy = jest.spyOn(document.body, "appendChild");
    downloadPDF("https://servidor.com/archivo.pdf", "sin-extension");
    const anchor = appendSpy.mock.calls[0]?.[0] as HTMLAnchorElement;
    expect(anchor?.download).toBe("sin-extension.pdf");
    appendSpy.mockRestore();
  });

  it("respeta el nombre cuando ya termina en .pdf", () => {
    const appendSpy = jest.spyOn(document.body, "appendChild");
    downloadPDF("https://servidor.com/x.pdf", "con-extension.pdf");
    const anchor = appendSpy.mock.calls[0]?.[0] as HTMLAnchorElement;
    expect(anchor?.download).toBe("con-extension.pdf");
    appendSpy.mockRestore();
  });
});

// ---------------------------------------------------------------------------
// fetchCatalog (datos hardcodeados, sin fetch real)
// ---------------------------------------------------------------------------

describe("fetchCatalog", () => {
  it("devuelve un objeto no nulo para un catálogo conocido", async () => {
    const result = await fetchCatalog("CatEstatusFactura");
    expect(result).not.toBeNull();
  });

  it("el objeto devuelto tiene el code igual al nombre del catálogo", async () => {
    const result = await fetchCatalog("CatEstatusFactura");
    expect(result?.code).toBe("CatEstatusFactura");
  });

  it("el objeto devuelto tiene un array details", async () => {
    const result = await fetchCatalog("CatEstatusFactura");
    expect(Array.isArray(result?.details)).toBe(true);
    expect((result?.details ?? []).length).toBeGreaterThan(0);
  });

  it("el catálogo CatEstatusNotaCredito tiene details", async () => {
    const result = await fetchCatalog("CatEstatusNotaCredito");
    expect(result?.details.length).toBeGreaterThan(0);
  });

  it("devuelve module 'General' y catalogType 'Simple'", async () => {
    const result = await fetchCatalog("CatMsgExitoso");
    expect(result?.module).toBe("General");
    expect(result?.catalogType).toBe("Simple");
  });
});

// ---------------------------------------------------------------------------
// fetchSystemParameters (mock global.fetch)
// ---------------------------------------------------------------------------

describe("fetchSystemParameters", () => {
  const OLD_ENV = process.env;

  beforeEach(() => {
    process.env = { ...OLD_ENV };
  });

  afterEach(() => {
    process.env = OLD_ENV;
    delete (global as any).fetch;
  });

  it("devuelve null cuando CATALOGS_API_URL no está configurado", async () => {
    delete process.env.CATALOGS_API_URL;
    const result = await fetchSystemParameters();
    expect(result).toBeNull();
  });

  it("devuelve null cuando CATALOGS_API_URL está vacío", async () => {
    process.env.CATALOGS_API_URL = "";
    const result = await fetchSystemParameters();
    expect(result).toBeNull();
  });

  it("devuelve los datos cuando el fetch es exitoso", async () => {
    process.env.CATALOGS_API_URL = "https://api.ejemplo.com";
    const mockData = { success: true, data: [], total: 0, page: 1, limit: 10, totalPages: 1 };
    (global as any).fetch = jest.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockData),
    });
    const result = await fetchSystemParameters();
    expect(result).toEqual(mockData);
  });

  it("devuelve null cuando la respuesta no es ok", async () => {
    process.env.CATALOGS_API_URL = "https://api.ejemplo.com";
    (global as any).fetch = jest.fn().mockResolvedValue({ ok: false });
    const result = await fetchSystemParameters();
    expect(result).toBeNull();
  });

  it("devuelve null cuando el fetch lanza una excepción", async () => {
    process.env.CATALOGS_API_URL = "https://api.ejemplo.com";
    (global as any).fetch = jest.fn().mockRejectedValue(new Error("Network error"));
    const result = await fetchSystemParameters();
    expect(result).toBeNull();
  });
});

// ---------------------------------------------------------------------------
// fetchCatalogDetails (mock global.fetch)
// ---------------------------------------------------------------------------

describe("fetchCatalogDetails", () => {
  const OLD_ENV = process.env;

  beforeEach(() => {
    process.env = { ...OLD_ENV };
  });

  afterEach(() => {
    process.env = OLD_ENV;
    delete (global as any).fetch;
  });

  it("cae a los datos locales cuando CATALOGS_API_URL no está configurado", async () => {
    delete process.env.CATALOGS_API_URL;
    const result = await fetchCatalogDetails("CatEstatusFactura");
    expect(result).not.toBeNull();
  });

  it("devuelve null cuando el catálogo no existe localmente y no hay API configurada", async () => {
    delete process.env.CATALOGS_API_URL;
    const result = await fetchCatalogDetails("CatalogoInexistente");
    expect(result).toBeNull();
  });

  it("usa el fetch cuando CATALOGS_API_URL está configurado y la respuesta es ok", async () => {
    process.env.CATALOGS_API_URL = "https://api.ejemplo.com";
    const mockData = [{ key: "E1", description: "Estado 1" }];
    (global as any).fetch = jest.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockData),
    });
    const result = await fetchCatalogDetails("CatEstatusFactura");
    expect(result).toEqual(mockData);
  });

  it("cae a datos locales cuando el fetch falla con respuesta no-ok", async () => {
    process.env.CATALOGS_API_URL = "https://api.ejemplo.com";
    (global as any).fetch = jest.fn().mockResolvedValue({ ok: false });
    const result = await fetchCatalogDetails("CatEstatusFactura");
    expect(result).not.toBeNull();
  });
});
