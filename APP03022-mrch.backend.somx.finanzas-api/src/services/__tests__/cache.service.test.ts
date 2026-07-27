import { getCache, setCache } from "../cache.service.js";

describe("cache.service", () => {
    beforeEach(() => {
        jest.restoreAllMocks();
    });

    it("returns null when the key does not exist", () => {
        expect(getCache("missing-key")).toBeNull();
    });

    it("stores and returns a cached value", () => {
        jest.spyOn(Date, "now").mockReturnValue(1_000);

        setCache("user", { id: 10, name: "Mary" }, 5_000);

        expect(getCache<{ id: number; name: string }>("user")).toEqual({
            id: 10,
            name: "Mary",
        });
    });

    it("returns null and removes an expired value", () => {
        const nowSpy = jest.spyOn(Date, "now");

        nowSpy.mockReturnValue(1_000);
        setCache("expired-key", "cached-value", 100);

        nowSpy.mockReturnValue(1_101);

        expect(getCache("expired-key")).toBeNull();

        nowSpy.mockReturnValue(1_050);

        // Si no se hubiera eliminado, todavía devolvería el valor.
        expect(getCache("expired-key")).toBeNull();
    });

    it("keeps the value while the expiry time has not passed", () => {
        const nowSpy = jest.spyOn(Date, "now");

        nowSpy.mockReturnValue(1_000);
        setCache("active-key", 123, 100);

        nowSpy.mockReturnValue(1_100);

        expect(getCache<number>("active-key")).toBe(123);
    });
});