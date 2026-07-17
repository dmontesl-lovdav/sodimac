import { describe, it, expect, jest } from "@jest/globals";
import { createThreeWayMatchService } from "../threeWayMatchService";

describe("createThreeWayMatchService", () => {
  it("search arma path con query", async () => {
    const request = jest.fn().mockResolvedValue({ ok: true });
    const api = { request, requestBinary: jest.fn() } as any;
    const svc = createThreeWayMatchService(api);
    await svc.searchThreeWayMatch({ a: 1, b: "", c: null });
    expect(request).toHaveBeenCalledWith("three-way-match?a=1", "get");
  });

  it("export csv/xlsx usan requestBinary", async () => {
    const requestBinary = jest.fn().mockResolvedValue(undefined);
    const api = { request: jest.fn(), requestBinary } as any;
    const svc = createThreeWayMatchService(api);
    await svc.exportThreeWayMatchCsv({ x: "1" });
    await svc.exportThreeWayMatchXlsx({});
    expect(requestBinary).toHaveBeenCalledTimes(2);
    expect(requestBinary.mock.calls[0][0]).toContain("export/csv");
    expect(requestBinary.mock.calls[1][0]).toBe("three-way-match/export/xlsx");
  });
});
