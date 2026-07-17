import { describe, it, expect, jest, beforeEach } from "@jest/globals";

const mockRequest = jest.fn();
const mockRequestBinary = jest.fn();

jest.mock("@/services/ApiClient", () => ({
  createApiClient: () => ({
    request: (...args: unknown[]) => mockRequest(...args),
    requestBinary: (...args: unknown[]) => mockRequestBinary(...args),
  }),
}));

import { shippingGuideService } from "../ShippingGuideClient";

describe("shippingGuideService", () => {
  beforeEach(() => {
    mockRequest.mockReset();
    mockRequestBinary.mockReset();
  });

  it("get parsea content anidado", async () => {
    mockRequest.mockResolvedValue({
      data: { content: [{ shippingGuideId: "1", guideNumber: "G1" }] },
    });
    const rows = await shippingGuideService.get({
      guideNumber: "G1",
      vendorNumber: 1,
      sourceId: 2,
      truckPlate: "ABC",
      trailerPlate: "T1",
      deliveryType: "X",
      status: 1,
      from: "2026-01-01",
      to: "2026-01-31",
    } as any);
    expect(rows).toHaveLength(1);
    expect(rows[0].guideNumber).toBe("G1");
  });

  it("get parsea array directo", async () => {
    mockRequest.mockResolvedValue([{ shippingGuideId: "2" }]);
    const rows = await shippingGuideService.get({} as any);
    expect(rows).toHaveLength(1);
  });

  it("get binary llama requestBinary", async () => {
    mockRequestBinary.mockResolvedValue(undefined);
    const rows = await shippingGuideService.get({} as any, true);
    expect(mockRequestBinary).toHaveBeenCalled();
    expect(rows).toEqual([]);
  });

  it("getDetail retorna data", async () => {
    mockRequest.mockResolvedValue({
      success: true,
      data: { shippingGuideId: "abc" },
    });
    const detail = await shippingGuideService.getDetail("abc");
    expect(detail.shippingGuideId).toBe("abc");
  });

  it("getDetail lanza si success false", async () => {
    mockRequest.mockResolvedValue({ success: false, message: "fail" });
    await expect(shippingGuideService.getDetail("x")).rejects.toThrow("fail");
  });

  it("cancel y updateStatus delegan en api", async () => {
    mockRequest.mockResolvedValue(undefined);
    await shippingGuideService.cancel({ shippingGuideIds: ["1"] } as any);
    await shippingGuideService.updateStatus({ shippingGuideId: "1" } as any);
    expect(mockRequest).toHaveBeenCalledTimes(2);
  });
});
