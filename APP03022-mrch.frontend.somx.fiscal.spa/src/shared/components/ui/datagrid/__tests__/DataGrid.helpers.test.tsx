/**
 * @jest-environment jsdom
 */
import { jest, describe, it, expect, beforeEach } from "@jest/globals";

jest.mock("@/utils/utils", () => ({
  exportToCSV: jest.fn(),
  getStandardFilename: (r: any) => r?.folio ?? "file",
}));

jest.mock("@shared/security", () => ({
  useSecurityContext: () => ({ hasEvent: () => true }),
}));

jest.mock("@/shared/components/ui/table/DataTable", () => ({
  __esModule: true,
  default: () => null,
}));

jest.mock("@/shared/components/ui", () => ({
  GenericButton: () => null,
  GenericModal: () => null,
}));

jest.mock("@/shared/components/ui/datagrid/hooks/usePaginatedData", () => ({
  usePaginatedData: () => ({
    rows: [],
    loading: false,
    page: 0,
    size: 10,
    totalPages: 0,
    totalItems: 0,
    setPage: jest.fn(),
    setSize: jest.fn(),
    error: null,
  }),
  parseFetchError: (err: any) => ({ message: String(err?.message ?? err) }),
}));

import { exportToCSV } from "@/utils/utils";
import {
  exportDataGridToCsv,
  createOnFilter,
  parseFiscalXmlError,
  headerToString,
  getCellValue,
  downloadDataGridXml,
  resolveCsvExportRows,
  extractPaginatedContent,
} from "../DataGrid";

describe("headerToString", () => {
  it("retorna el string cuando header es string", () => {
    expect(headerToString("Folio")).toBe("Folio");
  });

  it("retorna fallback cuando header no es string", () => {
    expect(headerToString({} as any, "FB")).toBe("FB");
  });
});

describe("getCellValue", () => {
  const row = { id: 1, name: "A" };

  it("prioriza exportAccessor", () => {
    expect(
      getCellValue(
        {
          header: "H",
          exportAccessor: (r) => r.name,
          accessor: () => "ignored",
        },
        row
      )
    ).toBe("A");
  });

  it("usa accessor si no hay exportAccessor", () => {
    expect(getCellValue({ header: "H", accessor: (r) => r.id }, row)).toBe(1);
  });

  it("usa render si no hay accessors", () => {
    expect(getCellValue({ header: "H", render: () => "cell" }, row)).toBe("cell");
  });

  it("retorna vacío sin accessors ni render", () => {
    expect(getCellValue({ header: "H" }, row)).toBe("");
  });
});

describe("resolveCsvExportRows", () => {
  it("retorna seleccionadas cuando hay selección", async () => {
    const selected = [{ id: 1 }];
    const result = await resolveCsvExportRows({
      selectedRows: selected,
      currentRows: [{ id: 1 }, { id: 2 }],
      totalItems: 20,
      fetchFn: jest.fn(),
      filters: {},
      pageSize: 10,
    });
    expect(result).toBe(selected);
  });

  it("retorna página actual cuando ya tiene todos los items", async () => {
    const rows = [{ id: 1 }, { id: 2 }];
    const fetchFn = jest.fn();
    const result = await resolveCsvExportRows({
      selectedRows: [],
      currentRows: rows,
      totalItems: 2,
      fetchFn,
      filters: { a: 1 },
      pageSize: 10,
    });
    expect(result).toEqual(rows);
    expect(fetchFn).not.toHaveBeenCalled();
  });

  it("reconsulta todas las páginas cuando no hay selección", async () => {
    const fetchFn = jest
      .fn()
      .mockResolvedValueOnce({ content: [{ id: 1 }, { id: 2 }], totalElements: 3 })
      .mockResolvedValueOnce({ data: { content: [{ id: 3 }] } });

    const result = await resolveCsvExportRows({
      selectedRows: [],
      currentRows: [{ id: 1 }, { id: 2 }],
      totalItems: 3,
      fetchFn: fetchFn as any,
      filters: { status: "1" },
      pageSize: 2,
    });

    expect(fetchFn).toHaveBeenCalledTimes(2);
    expect(fetchFn).toHaveBeenNthCalledWith(1, { status: "1", page: 0, size: 2 });
    expect(fetchFn).toHaveBeenNthCalledWith(2, { status: "1", page: 1, size: 2 });
    expect(result).toEqual([{ id: 1 }, { id: 2 }, { id: 3 }]);
  });
});

describe("extractPaginatedContent", () => {
  it("soporta content directo y envuelto en data", () => {
    expect(extractPaginatedContent({ content: [1] })).toEqual([1]);
    expect(extractPaginatedContent({ data: { content: [2] } })).toEqual([2]);
    expect(extractPaginatedContent({})).toEqual([]);
  });
});

describe("exportDataGridToCsv", () => {
  beforeEach(() => {
    (exportToCSV as jest.Mock).mockClear();
  });

  it("exporta con exportHeader y valores nulos como vacío", () => {
    exportDataGridToCsv(
      [
        { header: "Nombre", exportHeader: "NAME", exportAccessor: (r: any) => r.v },
        { header: "X", accessor: () => null },
      ],
      [{ v: "ok" }, { v: null }],
      "demo"
    );
    expect(exportToCSV).toHaveBeenCalledWith(
      ["NAME", "X"],
      [
        ["ok", ""],
        ["", ""],
      ],
      "demo"
    );
  });
});

describe("createOnFilter", () => {
  it("carga filas en éxito", async () => {
    const setLoading = jest.fn();
    const setRows = jest.fn();
    const onFilter = createOnFilter({
      fetchFn: async () => ({ content: [{ id: 1 }] }),
      setLoading,
      setRows,
    });
    await onFilter({} as any);
    expect(setLoading).toHaveBeenCalledWith(true);
    expect(setRows).toHaveBeenCalledWith([{ id: 1 }]);
    expect(setLoading).toHaveBeenLastCalledWith(false);
  });

  it("limpia filas y notifica error en fallo", async () => {
    const setLoading = jest.fn();
    const setRows = jest.fn();
    const onError = jest.fn();
    const onFilter = createOnFilter({
      fetchFn: async () => {
        throw new Error("boom");
      },
      setLoading,
      setRows,
      onError,
    });
    await onFilter({} as any);
    expect(onError).toHaveBeenCalled();
    expect(setRows).toHaveBeenCalledWith([]);
    expect(setLoading).toHaveBeenLastCalledWith(false);
  });
});

describe("parseFiscalXmlError", () => {
  it("arma mensaje con code, message y additionalInfo", () => {
    const xml = `<FiscalErrorResponse>
      <errorCode>ERR1</errorCode>
      <message>Sin PDF</message>
      <additionalInfo>detalle</additionalInfo>
    </FiscalErrorResponse>`;
    expect(parseFiscalXmlError(xml)).toBe("[ERR1] — Sin PDF — detalle");
  });

  it("retorna fallback cuando no hay message", () => {
    expect(parseFiscalXmlError("<root/>")).toContain("No hay PDF disponible");
  });
});

describe("downloadDataGridXml", () => {
  beforeEach(() => {
    Object.defineProperty(URL, "createObjectURL", {
      value: jest.fn(() => "blob:x"),
      writable: true,
    });
    Object.defineProperty(URL, "revokeObjectURL", {
      value: jest.fn(),
      writable: true,
    });
  });

  it("no descarga cuando el contenido está vacío", () => {
    const warn = jest.spyOn(console, "warn").mockImplementation(() => {});
    downloadDataGridXml("   ");
    expect(warn).toHaveBeenCalled();
    warn.mockRestore();
  });

  it("crea enlace de descarga con extensión .xml", () => {
    const append = jest.spyOn(document.body, "appendChild");
    downloadDataGridXml("<xml/>", "nota");
    expect(URL.createObjectURL).toHaveBeenCalled();
    expect(append).toHaveBeenCalled();
    append.mockRestore();
  });
});
