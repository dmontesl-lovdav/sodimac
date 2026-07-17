import { describe, it, expect } from "@jest/globals";
import { parseFilenameFromContentDisposition } from "../ApiClient";

describe("parseFilenameFromContentDisposition", () => {
  it("retorna null sin header", () => {
    expect(parseFilenameFromContentDisposition(null)).toBeNull();
    expect(parseFilenameFromContentDisposition(undefined)).toBeNull();
  });

  it("parsea filename ASCII", () => {
    expect(
      parseFilenameFromContentDisposition('attachment; filename="reporte.csv"')
    ).toBe("reporte.csv");
  });

  it("parsea filename* UTF-8", () => {
    expect(
      parseFilenameFromContentDisposition(
        "attachment; filename*=UTF-8''datos%20export.csv"
      )
    ).toBe("datos export.csv");
  });
});
