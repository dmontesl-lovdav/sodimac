export class AppError extends Error {
    code: string;
    status: number;
    details?: unknown;

    constructor(code: string, message: string, status = 400, details?: unknown) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = details;
    }
}

export const assert = (cond: unknown, code: string, message: string, status = 400) => {
    if (!cond) throw new AppError(code, message, status);
};

export const toHttp = (err: unknown) => {
    if (err instanceof AppError) {
        return { status: err.status, body: { code: err.code, message: err.message, details: err.details ?? null } };
    }
    return { status: 500, body: { code: 'INTERNAL', message: 'Unexpected error.' } };
};
