export class GenericException extends Error {
    public readonly code: number;

    constructor(code: number, description: string) {
        super(description);
        this.name = 'GenericException';
        this.code = code;

        if (Error.captureStackTrace) {
            Error.captureStackTrace(this, GenericException);
        }
    }
}

export default GenericException;

