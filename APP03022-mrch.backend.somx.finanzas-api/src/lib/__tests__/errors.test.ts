import { AppError, assert, toHttp } from "../errors.js";

describe("errors", () => {
    describe("AppError", () => {
        it("creates an error with the provided values", () => {
            const details = { field: "uuid" };

            const error = new AppError(
                "INVALID_DATA",
                "Invalid data",
                422,
                details,
            );

            expect(error).toBeInstanceOf(Error);
            expect(error.message).toBe("Invalid data");
            expect(error.code).toBe("INVALID_DATA");
            expect(error.status).toBe(422);
            expect(error.details).toEqual(details);
        });

        it("uses status 400 by default", () => {
            const error = new AppError("BAD_REQUEST", "Bad request");

            expect(error.status).toBe(400);
            expect(error.details).toBeUndefined();
        });
    });

    describe("assert", () => {
        it("does not throw when the condition is truthy", () => {
            expect(() =>
                assert(true, "INVALID", "Invalid value"),
            ).not.toThrow();
        });

        it("throws AppError when the condition is falsy", () => {
            expect(() =>
                assert(false, "INVALID", "Invalid value", 422),
            ).toThrow(
                expect.objectContaining({
                    code: "INVALID",
                    message: "Invalid value",
                    status: 422,
                }),
            );
        });
    });

    describe("toHttp", () => {
        it("converts AppError to an HTTP response", () => {
            const error = new AppError(
                "NOT_FOUND",
                "Resource not found",
                404,
                { id: 10 },
            );

            expect(toHttp(error)).toEqual({
                status: 404,
                body: {
                    code: "NOT_FOUND",
                    message: "Resource not found",
                    details: { id: 10 },
                },
            });
        });

        it("returns null details when AppError has no details", () => {
            const error = new AppError("BAD_REQUEST", "Bad request");

            expect(toHttp(error)).toEqual({
                status: 400,
                body: {
                    code: "BAD_REQUEST",
                    message: "Bad request",
                    details: null,
                },
            });
        });

        it("converts unknown errors to an internal error", () => {
            expect(toHttp(new Error("Database failed"))).toEqual({
                status: 500,
                body: {
                    code: "INTERNAL",
                    message: "Unexpected error.",
                },
            });
        });
    });
});