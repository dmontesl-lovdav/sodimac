/**
 * @jest-environment jsdom
 */
import { describe, it, expect } from "@jest/globals";

jest.mock("@/utils/getUserIdFromStore", () => ({ getUserIdFromStore: () => "1" }));
jest.mock("react-router-dom", () => ({
  useLocation: () => ({ search: "" }),
}));
jest.mock("@/shared/components/ui/decorator/SimpleDecorator", () => ({
  decorate: (_b: any, _p: any, node: any) => node,
}));
jest.mock("@/shared/components/ui", () => ({
  GenericButton: () => null,
  GenericModal: () => null,
}));
jest.mock("@shared/security", () => ({
  APP_EVENT: { CREDIT_NOTES: { PUBLISH: {} } },
  PermissionGate: ({ children }: any) => children,
}));
jest.mock("@/shared/components/ui/progress", () => ({
  GenericLinearProgress: () => null,
}));
jest.mock("@/hooks/TraceFolioProvider", () => ({
  TraceFolioProvider: ({ children }: any) => children,
  useTraceFolio: () => ({
    traceId: "t",
    addLog: () => undefined,
    headerActions: null,
    noTraceWarning: null,
    traceLoading: false,
  }),
}));
jest.mock("@/utils/utils", () => ({
  fetchSystemParameters: async () => null,
  formatLocalDateStr: () => "",
  getErrorMessage: () => "err",
  buildFiscalSpaUrl: () => "/",
  fetchProvidersAsCatalog: async () => [],
}));
jest.mock("../api/CreditNotePublishClient", () => ({
  createCreditNotePublishClient: () => ({}),
}));
jest.mock("../parts/publishQuery", () => ({
  parsePublishQuery: () => ({}),
  isCommercialDiscountFlow: () => false,
}));
jest.mock("../parts/parseValidatedXml", () => ({ parseValidatedXml: () => null }));
jest.mock("../utils/resolveXmlValidationCommand", () => ({
  resolveXmlValidationCommand: () => "",
}));
jest.mock("../parts/buildPublishFormData", () => ({ buildPublishFormData: () => new FormData() }));
jest.mock("../parts/useRelatedInvoice", () => ({
  useRelatedInvoice: () => ({ invoice: null, loading: false }),
}));
jest.mock("../parts/RelatedInvoiceGrid", () => ({ __esModule: true, default: () => null }));
jest.mock("../parts/CreditNoteSummary", () => ({ __esModule: true, default: () => null }));
jest.mock("../parts/PublishResultNotice", () => ({ __esModule: true, default: () => null }));
jest.mock("../parts/publishResult", () => ({
  buildFinishModal: () => null,
  isPublishSuccessful: () => true,
}));

import {
  checkSystemParameterValue,
  getXmlFileError,
  getPdfFileError,
  resolveLoadingMessage,
} from "../PublishCreditNote";

describe("checkSystemParameterValue", () => {
  it("retorna deshabilitado cuando no hay parámetros", () => {
    expect(checkSystemParameterValue(null, 11)).toEqual({ value: "", isEnabled: false });
  });

  it("detecta parámetro habilitado con status '1'", () => {
    const params = [
      { idParameter: 11, value: "1", status: "1" },
      { idParameter: 12, value: "0", status: "0" },
    ] as any;
    expect(checkSystemParameterValue(params, 11)).toEqual({ value: "1", isEnabled: true });
    expect(checkSystemParameterValue(params, 12).isEnabled).toBe(false);
  });

  it("retorna vacío cuando el id no existe", () => {
    expect(
      checkSystemParameterValue([{ idParameter: 1, value: "x", status: "1" }] as any, 99)
    ).toEqual({
      value: "",
      isEnabled: false,
    });
  });
});

describe("getXmlFileError", () => {
  it("valida tamaño", () => {
    const file = { name: "a.xml", size: 10 } as File;
    expect(getXmlFileError(file, 5, 1)).toContain("no debe exceder");
  });

  it("valida extensión .xml", () => {
    const file = { name: "a.pdf", size: 1 } as File;
    expect(getXmlFileError(file, 100, 1)).toContain("xml válido");
  });

  it("acepta XML válido", () => {
    const file = { name: "nota.XML", size: 1 } as File;
    expect(getXmlFileError(file, 100, 1)).toBeNull();
  });
});

describe("getPdfFileError", () => {
  it("valida tamaño", () => {
    const file = { name: "a.pdf", size: 10 } as File;
    expect(getPdfFileError(file, 5, 1)).toContain("no debe exceder");
  });

  it("valida extensión .pdf", () => {
    const file = { name: "a.xml", size: 1 } as File;
    expect(getPdfFileError(file, 100, 1)).toContain("pdf válido");
  });

  it("acepta PDF válido", () => {
    const file = { name: "factura.PDF", size: 1 } as File;
    expect(getPdfFileError(file, 100, 1)).toBeNull();
  });
});

describe("resolveLoadingMessage", () => {
  it("prioriza uploading > validating > invoice > default", () => {
    expect(resolveLoadingMessage(true, true, true)).toContain("Procesando");
    expect(resolveLoadingMessage(false, true, true)).toContain("Validando");
    expect(resolveLoadingMessage(false, false, true)).toContain("factura");
    expect(resolveLoadingMessage(false, false, false)).toContain("información");
  });
});
