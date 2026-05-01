export class ConflictException extends Error {
    public readonly errorType: string;

    constructor(message: string, errorType: string = 'ConflictError') {
        super(message);
        this.name = 'ConflictException';
        this.errorType = errorType;

        if (Error.captureStackTrace) {
            Error.captureStackTrace(this, ConflictException);
        }
    }
}

export default ConflictException;

