import {
    buildReceptionNumberIlikePattern,
    receptionNumberContainsQuery,
} from "../receptionNumberFilter.js";

describe("receptionNumberContainsQuery", () => {
    it("hace match parcial sobre el número de recepción", () => {
        expect(receptionNumberContainsQuery("77", "7")).toBe(true);
        expect(receptionNumberContainsQuery("87", "7")).toBe(true);
        expect(receptionNumberContainsQuery("275", "7")).toBe(true);
        expect(receptionNumberContainsQuery("7", "7")).toBe(true);
        expect(receptionNumberContainsQuery("1", "7")).toBe(false);
        expect(receptionNumberContainsQuery("854951", "7")).toBe(false);
    });

    it("no usa el uuid interno: solo el número", () => {
        expect(
            receptionNumberContainsQuery("1", "7")
        ).toBe(false);
    });
});

describe("buildReceptionNumberIlikePattern", () => {
    it("escapa comodines de LIKE", () => {
        expect(buildReceptionNumberIlikePattern("7%")).toBe("%7\\%%");
        expect(buildReceptionNumberIlikePattern("7")).toBe("%7%");
    });
});
