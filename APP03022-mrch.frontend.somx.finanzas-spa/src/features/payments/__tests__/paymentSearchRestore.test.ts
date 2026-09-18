/**
 * @jest-environment node
 */
import { PAYMENT_RESTORE_SEARCH_PARAM } from "../utils/paymentSearchRestore";

describe("paymentSearchRestore", () => {
  it("usa restoreSearch como en descuentos comerciales", () => {
    expect(PAYMENT_RESTORE_SEARCH_PARAM).toBe("restoreSearch");
  });
});
