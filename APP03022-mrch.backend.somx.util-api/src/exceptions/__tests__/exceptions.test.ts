import { HttpException } from '../HttpException.js';
import { GenericException } from '../GenericException.js';
import { ConflictException } from '../ConflictException.js';

describe('HttpException', () => {
    it('sets status and message', () => {
        const error = new HttpException(404, 'Not found');

        expect(error).toBeInstanceOf(Error);
        expect(error.name).toBe('HttpException');
        expect(error.status).toBe(404);
        expect(error.message).toBe('Not found');
        expect(error.code).toBeUndefined();
    });

    it('sets the optional code when provided', () => {
        const error = new HttpException(409, 'Conflict', 'DUPLICATED');

        expect(error.status).toBe(409);
        expect(error.code).toBe('DUPLICATED');
    });
});

describe('GenericException', () => {
    it('sets code and description', () => {
        const error = new GenericException(500, 'Unexpected error');

        expect(error).toBeInstanceOf(Error);
        expect(error.name).toBe('GenericException');
        expect(error.code).toBe(500);
        expect(error.message).toBe('Unexpected error');
    });
});

describe('ConflictException', () => {
    it('uses the default error type', () => {
        const error = new ConflictException('Already exists');

        expect(error).toBeInstanceOf(Error);
        expect(error.name).toBe('ConflictException');
        expect(error.message).toBe('Already exists');
        expect(error.errorType).toBe('ConflictError');
    });

    it('accepts a custom error type', () => {
        const error = new ConflictException('Bad state', 'StateError');

        expect(error.errorType).toBe('StateError');
    });
});
