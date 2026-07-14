import {
    beforeEach,
    describe,
    expect,
    it,
    jest,
} from "@jest/globals";

import type {
    Request,
    Response,
    NextFunction,
} from "express";

const serviceMocks = {
    list: jest.fn<(...args: unknown[]) => Promise<unknown>>(),
    get: jest.fn<(...args: unknown[]) => Promise<unknown>>(),
    create: jest.fn<(...args: unknown[]) => Promise<unknown>>(),
    update: jest.fn<(...args: unknown[]) => Promise<unknown>>(),
    remove: jest.fn<(...args: unknown[]) => Promise<unknown>>(),
};

const schemaMocks = {
    listQueryParse: jest.fn<(input: unknown) => unknown>(),
    idParamParse: jest.fn<(input: unknown) => unknown>(),
    createParse: jest.fn<(input: unknown) => unknown>(),
    updateParse: jest.fn<(input: unknown) => unknown>(),
};

jest.unstable_mockModule(
    "@/services/accountsPayable.service.js",
    () => serviceMocks,
);

jest.unstable_mockModule(
    "@/schemas/accountsPayable.schema.js",
    () => ({
        ListAccountsQuerySchema: {
            parse: schemaMocks.listQueryParse,
        },
        IdParamSchema: {
            parse: schemaMocks.idParamParse,
        },
        CreateAccountsPayableSchema: {
            parse: schemaMocks.createParse,
        },
        UpdateAccountsPayableSchema: {
            parse: schemaMocks.updateParse,
        },
    }),
);

const controller = await import(
    "../accountsPayable.controller.js"
);

const { HttpError } = await import(
    "../../utils/HttpError.js"
);

function createRequest(
    values: Partial<
        Pick<Request, "params" | "query" | "body">
    > = {},
): Request {
    return {
        params: {},
        query: {},
        body: {},
        ...values,
    } as unknown as Request;
}

function createResponse() {
    const status = jest.fn();
    const json = jest.fn();
    const end = jest.fn();

    const response = {
        status,
        json,
        end,
    } as unknown as Response;

    status.mockReturnValue(response);
    json.mockReturnValue(response);
    end.mockReturnValue(response);

    return {
        response,
        status,
        json,
        end,
    };
}

function createNext() {
    const nextMock = jest.fn();

    return {
        next: nextMock as unknown as NextFunction,
        nextMock,
    };
}

describe("accountsPayable.controller", () => {
    beforeEach(() => {
        jest.resetAllMocks();
    });

    describe("list", () => {
        it("returns the accounts payable records", async () => {
            const rawQuery = {
                page: "1",
                limit: "20",
            };

            const parsedQuery = {
                page: 1,
                limit: 20,
            };

            const rows = [
                {
                    id: 1,
                    supplier: "Supplier 1",
                },
            ];

            schemaMocks.listQueryParse.mockReturnValue(
                parsedQuery,
            );

            serviceMocks.list.mockResolvedValue(rows);

            const request = createRequest({
                query: rawQuery,
            });

            const {
                response,
                status,
                json,
            } = createResponse();

            const {
                next,
                nextMock,
            } = createNext();

            await controller.list(
                request,
                response,
                next,
            );

            expect(
                schemaMocks.listQueryParse,
            ).toHaveBeenCalledWith(rawQuery);

            expect(
                serviceMocks.list,
            ).toHaveBeenCalledWith(parsedQuery);

            expect(status).toHaveBeenCalledWith(200);
            expect(json).toHaveBeenCalledWith(rows);
            expect(nextMock).not.toHaveBeenCalled();
        });

        it("forwards an HttpError when no records are found", async () => {
            const rawQuery = {
                page: "1",
                limit: "20",
            };

            const parsedQuery = {
                page: 1,
                limit: 20,
            };

            schemaMocks.listQueryParse.mockReturnValue(
                parsedQuery,
            );

            serviceMocks.list.mockResolvedValue([]);

            const request = createRequest({
                query: rawQuery,
            });

            const {
                response,
                json,
            } = createResponse();

            const {
                next,
                nextMock,
            } = createNext();

            await controller.list(
                request,
                response,
                next,
            );

            expect(
                schemaMocks.listQueryParse,
            ).toHaveBeenCalledWith(rawQuery);

            expect(
                serviceMocks.list,
            ).toHaveBeenCalledWith(parsedQuery);

            expect(nextMock).toHaveBeenCalledTimes(1);

            const forwardedError =
                nextMock.mock.calls[0]?.[0];

            expect(forwardedError).toBeInstanceOf(HttpError);

            expect(forwardedError).toMatchObject({
                message: "No records found for that filter",
            });

            expect(json).not.toHaveBeenCalled();
        });

        it("forwards query validation errors", async () => {
            const validationError = new Error(
                "Invalid query",
            );

            schemaMocks.listQueryParse.mockImplementation(
                () => {
                    throw validationError;
                },
            );

            const request = createRequest({
                query: {
                    page: "invalid",
                },
            });

            const { response } = createResponse();

            const {
                next,
                nextMock,
            } = createNext();

            await controller.list(
                request,
                response,
                next,
            );

            expect(nextMock).toHaveBeenCalledWith(
                validationError,
            );

            expect(
                serviceMocks.list,
            ).not.toHaveBeenCalled();
        });

        it("forwards service errors", async () => {
            const serviceError = new Error(
                "Database error",
            );

            const rawQuery = {
                page: "1",
                limit: "20",
            };

            const parsedQuery = {
                page: 1,
                limit: 20,
            };

            schemaMocks.listQueryParse.mockReturnValue(
                parsedQuery,
            );

            serviceMocks.list.mockRejectedValue(
                serviceError,
            );

            const request = createRequest({
                query: rawQuery,
            });

            const { response } = createResponse();

            const {
                next,
                nextMock,
            } = createNext();

            await controller.list(
                request,
                response,
                next,
            );

            expect(nextMock).toHaveBeenCalledWith(
                serviceError,
            );
        });
    });

    describe("getById", () => {
        it("returns an account payable record", async () => {
            const params = {
                id: "10",
            };

            const parsedParams = {
                id: 10,
            };

            const row = {
                id: 10,
                supplier: "Supplier 10",
            };

            schemaMocks.idParamParse.mockReturnValue(
                parsedParams,
            );

            serviceMocks.get.mockResolvedValue(row);

            const request = createRequest({
                params,
            });

            const {
                response,
                status,
                json,
            } = createResponse();

            const {
                next,
                nextMock,
            } = createNext();

            await controller.getById(
                request,
                response,
                next,
            );

            expect(
                schemaMocks.idParamParse,
            ).toHaveBeenCalledWith(params);

            expect(
                serviceMocks.get,
            ).toHaveBeenCalledWith(10);

            expect(status).toHaveBeenCalledWith(200);
            expect(json).toHaveBeenCalledWith(row);
            expect(nextMock).not.toHaveBeenCalled();
        });

        it("returns 404 when the record does not exist", async () => {
            schemaMocks.idParamParse.mockReturnValue({
                id: 10,
            });

            serviceMocks.get.mockResolvedValue(null);

            const request = createRequest({
                params: {
                    id: "10",
                },
            });

            const {
                response,
                status,
                json,
            } = createResponse();

            const {
                next,
                nextMock,
            } = createNext();

            await controller.getById(
                request,
                response,
                next,
            );

            expect(status).toHaveBeenCalledWith(404);

            expect(json).toHaveBeenCalledWith({
                message: "Not found",
            });

            expect(nextMock).not.toHaveBeenCalled();
        });

        it("forwards parameter validation errors", async () => {
            const validationError = new Error(
                "Invalid id",
            );

            schemaMocks.idParamParse.mockImplementation(
                () => {
                    throw validationError;
                },
            );

            const request = createRequest({
                params: {
                    id: "invalid",
                },
            });

            const { response } = createResponse();

            const {
                next,
                nextMock,
            } = createNext();

            await controller.getById(
                request,
                response,
                next,
            );

            expect(nextMock).toHaveBeenCalledWith(
                validationError,
            );

            expect(
                serviceMocks.get,
            ).not.toHaveBeenCalled();
        });

        it("forwards service errors", async () => {
            const serviceError = new Error(
                "Database error",
            );

            schemaMocks.idParamParse.mockReturnValue({
                id: 10,
            });

            serviceMocks.get.mockRejectedValue(
                serviceError,
            );

            const request = createRequest({
                params: {
                    id: "10",
                },
            });

            const { response } = createResponse();

            const {
                next,
                nextMock,
            } = createNext();

            await controller.getById(
                request,
                response,
                next,
            );

            expect(nextMock).toHaveBeenCalledWith(
                serviceError,
            );
        });
    });

    describe("create", () => {
        it("creates an account payable record", async () => {
            const body = {
                supplierId: 20,
                amount: 1500,
            };

            const createdRecord = {
                id: 1,
                ...body,
            };

            schemaMocks.createParse.mockReturnValue(body);

            serviceMocks.create.mockResolvedValue(
                createdRecord,
            );

            const request = createRequest({
                body,
            });

            const {
                response,
                status,
                json,
            } = createResponse();

            const {
                next,
                nextMock,
            } = createNext();

            await controller.create(
                request,
                response,
                next,
            );

            expect(
                schemaMocks.createParse,
            ).toHaveBeenCalledWith(body);

            expect(
                serviceMocks.create,
            ).toHaveBeenCalledWith(body);

            expect(status).toHaveBeenCalledWith(201);

            expect(json).toHaveBeenCalledWith(
                createdRecord,
            );

            expect(nextMock).not.toHaveBeenCalled();
        });

        it("forwards body validation errors", async () => {
            const validationError = new Error(
                "Invalid body",
            );

            schemaMocks.createParse.mockImplementation(
                () => {
                    throw validationError;
                },
            );

            const request = createRequest({
                body: {},
            });

            const { response } = createResponse();

            const {
                next,
                nextMock,
            } = createNext();

            await controller.create(
                request,
                response,
                next,
            );

            expect(nextMock).toHaveBeenCalledWith(
                validationError,
            );

            expect(
                serviceMocks.create,
            ).not.toHaveBeenCalled();
        });

        it("forwards service errors", async () => {
            const serviceError = new Error(
                "Create failed",
            );

            const body = {
                supplierId: 20,
                amount: 1500,
            };

            schemaMocks.createParse.mockReturnValue(body);

            serviceMocks.create.mockRejectedValue(
                serviceError,
            );

            const request = createRequest({
                body,
            });

            const { response } = createResponse();

            const {
                next,
                nextMock,
            } = createNext();

            await controller.create(
                request,
                response,
                next,
            );

            expect(nextMock).toHaveBeenCalledWith(
                serviceError,
            );
        });
    });

    describe("update", () => {
        it("updates an account payable record", async () => {
            const params = {
                id: "15",
            };

            const body = {
                amount: 2500,
            };

            const updatedRecord = {
                id: 15,
                amount: 2500,
            };

            schemaMocks.idParamParse.mockReturnValue({
                id: 15,
            });

            schemaMocks.updateParse.mockReturnValue(body);

            serviceMocks.update.mockResolvedValue(
                updatedRecord,
            );

            const request = createRequest({
                params,
                body,
            });

            const {
                response,
                status,
                json,
            } = createResponse();

            const {
                next,
                nextMock,
            } = createNext();

            await controller.update(
                request,
                response,
                next,
            );

            expect(
                schemaMocks.idParamParse,
            ).toHaveBeenCalledWith(params);

            expect(
                schemaMocks.updateParse,
            ).toHaveBeenCalledWith(body);

            expect(
                serviceMocks.update,
            ).toHaveBeenCalledWith(15, body);

            expect(status).toHaveBeenCalledWith(200);

            expect(json).toHaveBeenCalledWith(
                updatedRecord,
            );

            expect(nextMock).not.toHaveBeenCalled();
        });

        it("returns 404 when the record to update does not exist", async () => {
            const body = {
                amount: 2500,
            };

            schemaMocks.idParamParse.mockReturnValue({
                id: 15,
            });

            schemaMocks.updateParse.mockReturnValue(body);

            serviceMocks.update.mockResolvedValue(null);

            const request = createRequest({
                params: {
                    id: "15",
                },
                body,
            });

            const {
                response,
                status,
                json,
            } = createResponse();

            const {
                next,
                nextMock,
            } = createNext();

            await controller.update(
                request,
                response,
                next,
            );

            expect(status).toHaveBeenCalledWith(404);

            expect(json).toHaveBeenCalledWith({
                message: "Not found",
            });

            expect(nextMock).not.toHaveBeenCalled();
        });

        it("forwards parameter validation errors", async () => {
            const validationError = new Error(
                "Invalid id",
            );

            schemaMocks.idParamParse.mockImplementation(
                () => {
                    throw validationError;
                },
            );

            const request = createRequest({
                params: {
                    id: "invalid",
                },
                body: {
                    amount: 2500,
                },
            });

            const { response } = createResponse();

            const {
                next,
                nextMock,
            } = createNext();

            await controller.update(
                request,
                response,
                next,
            );

            expect(nextMock).toHaveBeenCalledWith(
                validationError,
            );

            expect(
                schemaMocks.updateParse,
            ).not.toHaveBeenCalled();

            expect(
                serviceMocks.update,
            ).not.toHaveBeenCalled();
        });

        it("forwards body validation errors", async () => {
            const validationError = new Error(
                "Invalid body",
            );

            schemaMocks.idParamParse.mockReturnValue({
                id: 15,
            });

            schemaMocks.updateParse.mockImplementation(
                () => {
                    throw validationError;
                },
            );

            const request = createRequest({
                params: {
                    id: "15",
                },
                body: {},
            });

            const { response } = createResponse();

            const {
                next,
                nextMock,
            } = createNext();

            await controller.update(
                request,
                response,
                next,
            );

            expect(nextMock).toHaveBeenCalledWith(
                validationError,
            );

            expect(
                serviceMocks.update,
            ).not.toHaveBeenCalled();
        });

        it("forwards service errors", async () => {
            const serviceError = new Error(
                "Update failed",
            );

            const body = {
                amount: 2500,
            };

            schemaMocks.idParamParse.mockReturnValue({
                id: 15,
            });

            schemaMocks.updateParse.mockReturnValue(body);

            serviceMocks.update.mockRejectedValue(
                serviceError,
            );

            const request = createRequest({
                params: {
                    id: "15",
                },
                body,
            });

            const { response } = createResponse();

            const {
                next,
                nextMock,
            } = createNext();

            await controller.update(
                request,
                response,
                next,
            );

            expect(nextMock).toHaveBeenCalledWith(
                serviceError,
            );
        });
    });

    describe("remove", () => {
        it("removes an account payable record", async () => {
            const params = {
                id: "25",
            };

            schemaMocks.idParamParse.mockReturnValue({
                id: 25,
            });

            serviceMocks.remove.mockResolvedValue(
                undefined,
            );

            const request = createRequest({
                params,
            });

            const {
                response,
                status,
                end,
            } = createResponse();

            const {
                next,
                nextMock,
            } = createNext();

            await controller.remove(
                request,
                response,
                next,
            );

            expect(
                schemaMocks.idParamParse,
            ).toHaveBeenCalledWith(params);

            expect(
                serviceMocks.remove,
            ).toHaveBeenCalledWith(25);

            expect(status).toHaveBeenCalledWith(204);
            expect(end).toHaveBeenCalledTimes(1);
            expect(nextMock).not.toHaveBeenCalled();
        });

        it("forwards parameter validation errors", async () => {
            const validationError = new Error(
                "Invalid id",
            );

            schemaMocks.idParamParse.mockImplementation(
                () => {
                    throw validationError;
                },
            );

            const request = createRequest({
                params: {
                    id: "invalid",
                },
            });

            const { response } = createResponse();

            const {
                next,
                nextMock,
            } = createNext();

            await controller.remove(
                request,
                response,
                next,
            );

            expect(nextMock).toHaveBeenCalledWith(
                validationError,
            );

            expect(
                serviceMocks.remove,
            ).not.toHaveBeenCalled();
        });

        it("forwards delete errors", async () => {
            const serviceError = new Error(
                "Delete failed",
            );

            schemaMocks.idParamParse.mockReturnValue({
                id: 25,
            });

            serviceMocks.remove.mockRejectedValue(
                serviceError,
            );

            const request = createRequest({
                params: {
                    id: "25",
                },
            });

            const { response } = createResponse();

            const {
                next,
                nextMock,
            } = createNext();

            await controller.remove(
                request,
                response,
                next,
            );

            expect(nextMock).toHaveBeenCalledTimes(1);

            expect(nextMock).toHaveBeenCalledWith(
                serviceError,
            );
        });
    });
});