export interface ExceptionWrapper {
    error: string;
    code: number;
    details: string;
}

export function mapCodeToErrorType(code: number): string {
    switch (code) {
        case 400:
            return 'ValidationError';
        case 401:
            return 'UnauthorizedError';
        case 403:
            return 'ForbiddenError';
        case 404:
            return 'NotFoundError';
        case 409:
            return 'ConflictError';
        case 500:
        default:
            return 'InternalServerError';
    }
}

export function buildWrapper(code: number, details: string, error?: string): ExceptionWrapper {
    return {
        error: error ?? mapCodeToErrorType(code),
        code,
        details
    };
}

