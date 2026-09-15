/**
 * @jest-environment node
 */
import { filterByReceptionQuery, receptionNumberContainsQuery } from "../receptionNumberQuery";

describe("filterByReceptionQuery", () => {
  it("deja recepciones cuyo número contiene el texto (7 → 7, 77, 87, 275)", () => {
    const rows = [
      { receptionNumber: "1", receptionId: "aaaaaaa7-1111-4111-8111-111111111111" },
      { receptionNumber: "7", receptionId: "bbbbbbbb-2222-4222-8222-222222222222" },
      { receptionNumber: "8", receptionId: "ccccccc7-3333-4333-8333-333333333333" },
      { receptionNumber: "77", receptionId: "dddddddd-4444-4444-8444-444444444444" },
      { receptionNumber: "854951", receptionId: "eeeeeee7-5555-4555-8555-555555555555" },
      { receptionNumber: "87", receptionId: "ffffffff-6666-4666-8666-666666666666" },
      { receptionNumber: "275", receptionId: "00000000-7777-4777-8777-777777777777" },
    ];

    const filtered = filterByReceptionQuery(rows, "7").map((r) => r.receptionNumber);
    expect(filtered).toEqual(["7", "77", "87", "275"]);
  });

  it("no filtra cuando el query está vacío", () => {
    expect(receptionNumberContainsQuery("1", "")).toBe(true);
  });
});
