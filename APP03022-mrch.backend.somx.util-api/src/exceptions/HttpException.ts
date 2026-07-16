export class HttpException extends Error {
    public readonly status: number;
    public readonly code?: string;

    constructor(status: number, message: string, code?: string) {
        super(message);
        this.name = 'HttpException';
        this.status = status;
        if (code !== undefined) this.code = code;

        if (Error.captureStackTrace) {
            Error.captureStackTrace(this, HttpException);
        }
    }
}

export default HttpException;
