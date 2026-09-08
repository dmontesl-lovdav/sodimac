/**
 * @jest-environment jsdom
 */
import { jest, describe, it, expect } from "@jest/globals";

jest.mock("@/shared/components/ui/datagrid/DataGrid", () => ({
  __esModule: true,
  default: () => null,
}));
jest.mock("@shared/security", () => ({
  APP_EVENT: {
    CREDIT_NOTES: {
      VIEW_ACCOUNTING: { app: "APL010", event: "EVT003" },
      LINK_INVOICE: { app: "APL010", event: "EVT005" },
      UPDATE_STATUS: { app: "APL010", event: "EVT012" },
      CANCEL: { app: "APL010", event: "EVT011" },
      DOWNLOAD_CSV: { app: "APL010", event: "EVT002" },
      PUBLISH: { app: "APL010", event: "EVT016" },
      SEARCH: { app: "APL010", event: "EVT0008" },
      CLEAR_FILTERS: { app: "APL010", event: "EVT001" },
      DOWNLOAD_XML: { app: "APL010", event: "EVT010" },
      DOWNLOAD_PDF: { app: "APL010", event: "EVT013" },
    },
    INVOICES: {
      VIEW_ACCOUNTING: { app: "APL009", event: "EVT003" },
      LINK_CREDIT_NOTE: { app: "APL009", event: "EVT007" },
      UPDATE_STATUS: { app: "APL009", event: "EVT012" },
      CANCEL: { app: "APL009", event: "EVT011" },
      DOWNLOAD_CSV: { app: "APL009", event: "EVT002" },
      SEARCH: { app: "APL009", event: "EVT0008" },
      CLEAR_FILTERS: { app: "APL009", event: "EVT001" },
      DOWNLOAD_XML: { app: "APL009", event: "EVT010" },
      DOWNLOAD_PDF: { app: "APL009", event: "EVT013" },
    },
  },
  PermissionGate: ({ children }: any) => children,
  useSecurityContext: () => ({ hasEvent: () => true, can: () => true }),
}));
jest.mock("@/utils/utils", () => ({
  formatDate: () => "",
  formatAmount: () => "",
  fetchCatalogMessage: async () => "",
  getXmlFileNameFromRow: () => "x.xml",
  fetchCatalogDetails: async () => null,
  fetchCatalogAsSelectableOptions: () => [],
  getErrorMessage: () => "err",
  getStandardFilename: () => "f",
}));
jest.mock("@/shared/components/ui/decorator/SimpleDecorator", () => ({
  decorate: (_b: any, _p: any, node: any) => node,
}));
jest.mock("@/shared/components/ui/filters", () => ({
  ReusableFiltersBar: () => null,
}));
jest.mock("@/shared/components/ui/misc", () => ({
  Divider: () => null,
  Title: () => null,
  ExportCsvButton: () => null,
}));
jest.mock("@/shared/components/ui", () => ({
  GenericModal: () => null,
  GenericButton: () => null,
}));
jest.mock("@/shared/session/fiscalListSession", () => ({
  FISCAL_LIST_KEYS: {
    creditNotes: { filters: "cn", screen: "cn-s" },
    invoices: { filters: "inv", screen: "inv-s" },
  },
  saveFiscalListFilters: jest.fn(),
  useFiscalListRefetchOnReturn: jest.fn(),
  useFiscalListScreenSession: () => false,
}));
jest.mock("react-router-dom", () => ({
  useLocation: () => ({ search: "", pathname: "/" }),
  useNavigate: () => jest.fn(),
}));
jest.mock("../api/CreditsClient", () => ({
  createCreditsClient: () => ({
    getCreditNotes: jest.fn(),
    getXmlDocument: jest.fn(),
    cancelCreditNote: jest.fn(),
  }),
}));
jest.mock("../../invoice/api/InvoiceClient", () => ({
  createInvoicesClient: () => ({
    getInvoices: jest.fn(),
  }),
}));

import { areCreditNoteFiltersEmpty } from "../CreditsContainer";
import { EMPTY_CREDIT_NOTE } from "../interfaces";
import { hasReceptionDates, EMPTY_GRID_RESULT } from "../../invoice/InvoicesContainer";
import { EMPTY_INVOICE } from "../../invoice/interfaces";

describe("areCreditNoteFiltersEmpty", () => {
  it("es true cuando faltan fechas", () => {
    expect(areCreditNoteFiltersEmpty(EMPTY_CREDIT_NOTE)).toBe(true);
    expect(
      areCreditNoteFiltersEmpty({
        ...EMPTY_CREDIT_NOTE,
        fechaInicioRecepcion: "2024-01-01",
        fechaFinalRecepcion: "",
      })
    ).toBe(true);
  });

  it("es false con ambas fechas", () => {
    expect(
      areCreditNoteFiltersEmpty({
        ...EMPTY_CREDIT_NOTE,
        fechaInicioRecepcion: "2024-01-01",
        fechaFinalRecepcion: "2024-01-31",
      })
    ).toBe(false);
  });
});

describe("hasReceptionDates / EMPTY_GRID_RESULT", () => {
  it("valida fechas de recepción", () => {
    expect(hasReceptionDates(EMPTY_INVOICE)).toBe(false);
    expect(
      hasReceptionDates({
        ...EMPTY_INVOICE,
        fechaInicioRecepcion: "2024-01-01",
        fechaFinalRecepcion: "2024-01-02",
      })
    ).toBe(true);
  });

  it("EMPTY_GRID_RESULT está vacío", () => {
    expect(EMPTY_GRID_RESULT.content).toEqual([]);
    expect(EMPTY_GRID_RESULT.totalElements).toBe(0);
  });
});
