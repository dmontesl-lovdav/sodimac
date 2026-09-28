import express from 'express';
import { request as httpRequest, type Server } from 'node:http';
import type { AddressInfo } from 'node:net';
import { once } from 'node:events';
import { datasource } from '@/config/typeorm-datasource.js';
import * as auditRepo from '@/repositories/auditLog.repo.js';
import { activityLogger, globalErrorHandler, logActivity, logBeforeMethod, runWithTrace } from '../../middlewares/logger.js';
import { errorHandler } from '../../middlewares/errorHandler.js';

jest.mock('@/config/typeorm-datasource.js', () => ({ datasource: { query: jest.fn() } }));

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

describe('Auditoria HTTP global', () => {
    const query = datasource.query as jest.Mock;
    const inserts = () => query.mock.calls.filter(([sql]) => String(sql).includes('INSERT INTO core_audit.activity_logs'))
        .map(([, values]) => values as any[]);
    let server: Server;
    let url: string;

    beforeEach(async () => {
        delete process.env.UTIL_AUDIT_ENABLED;
        query.mockReset().mockResolvedValue([{ activity_logs_uuid: 'stored' }]);
        jest.spyOn(console, 'error').mockImplementation(() => {});
        const app = express();
        app.use(activityLogger('UtilApi'));
        app.use(express.json());
        app.get('/api/items', (_req, res) => res.json({ success: true, data: [1, 2] }));
        app.post('/api/items', (req, res) => res.json(req.body));
        app.get('/api/business', (_req, res) => res.json({ success: false, code: 'BUS001' }));
        app.get('/api/forbidden', (_req, res) => res.status(403).json({ success: false }));
        app.get('/api/boom', () => { throw new Error('test error'); });
        app.get('/api/duplicate', activityLogger('Nested'), logBeforeMethod('list'), (_req, res) => res.json({ success: true }));
        app.get('/api/delay/:id', async (req, res) => {
            await new Promise(resolve => setTimeout(resolve, req.params.id === 'one' ? 20 : 5));
            res.json({ message: req.params.id });
        });
        app.get('/api/download', (_req, res) => {
            res.type('application/octet-stream');
            res.write(Buffer.from([0, 1, 2]));
            res.end(Buffer.from([255, 4]));
        });
        app.get('/api/abort', (_req, res) => { res.write('partial'); });
        app.use(globalErrorHandler());
        app.use(errorHandler);
        server = app.listen(0, '127.0.0.1');
        await once(server, 'listening');
        url = `http://127.0.0.1:${(server.address() as AddressInfo).port}`;
    });

    afterEach(async () => {
        delete process.env.UTIL_AUDIT_ENABLED;
        server.closeAllConnections();
        await new Promise<void>(resolve => server.close(() => resolve()));
        jest.restoreAllMocks();
    });

    test('registra inicio/cierre con módulo, duración y TraceFrontId', async () => {
        const front = '74319eec-9720-41e9-9f2c-d26b8a8508aa';
        const response = await fetch(`${url}/api/items`, { headers: { TraceFrontId: front } });
        expect(response.status).toBe(200);
        const rows = inserts();
        expect(rows).toHaveLength(2);
        expect(rows.map(row => row[7])).toEqual(['START_GET', 'END_GET']);
        expect(rows[0]![0]).toBe(response.headers.get('X-Trace-Id'));
        expect(rows[1]![0]).toBe(rows[0]![0]);
        expect(rows[1]![1]).toBe(front);
        expect(rows[1]![2]).toBeGreaterThan(0);
        expect(rows[1]![4]).toBe('API_UTIL');
        expect(rows[1]![5]).toBe('UtilApi:items');
        expect(rows[1]![12]).toBe('INFO');
    });

    test('clasifica HTTP 403 y conserva su respuesta', async () => {
        const response = await fetch(`${url}/api/forbidden`);
        expect(response.status).toBe(403);
        expect(inserts().at(-1)![12]).toBe('ERROR');
        expect(inserts().at(-1)![13]).toBe('403');
    });

    test('clasifica error funcional aunque HTTP sea 200', async () => {
        const response = await fetch(`${url}/api/business`);
        expect(response.status).toBe(200);
        expect(inserts().at(-1)![3]).toBe(true);
        expect(inserts().at(-1)![13]).toBe('BUS001');
    });

    test('captura JSON inválido antes del controlador y mantiene HTTP 400', async () => {
        const response = await fetch(`${url}/api/items`, {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: '{bad',
        });
        expect(response.status).toBe(400);
        expect(inserts().map(row => row[7])).toEqual(['START_POST', 'ERROR_HANDLER', 'END_POST']);
        expect(inserts().at(-1)![12]).toBe('ERROR');
    });

    test('captura excepción sin cambiar el manejador existente', async () => {
        const response = await fetch(`${url}/api/boom`);
        expect(response.status).toBe(500);
        const rows = inserts();
        expect(rows.map(row => row[7])).toEqual(['START_GET', 'ERROR_HANDLER', 'END_GET']);
        expect(rows[1]![16]).toContain('exceptions.test.ts');
        expect(rows[1]![16]).not.toContain('test error');
    });

    test('no consume el body ni guarda credenciales de la petición o respuesta', async () => {
        const body = { success: true, password: 'private-value', token: 'secret-value', value: 42 };
        const response = await fetch(`${url}/api/items?token=secret-query`, {
            method: 'POST', headers: { 'Content-Type': 'application/json', Authorization: 'Bearer private-header' },
            body: JSON.stringify(body),
        });
        expect(await response.json()).toEqual(body);
        const saved = JSON.stringify(inserts());
        for (const secret of ['private-value', 'secret-value', 'secret-query', 'private-header']) expect(saved).not.toContain(secret);
    });

    test('evita doble instrumentación y conserva INIT_METHOD explícito', async () => {
        await fetch(`${url}/api/duplicate`);
        expect(inserts().map(row => row[7])).toEqual(['START_GET', 'INIT_METHOD', 'END_GET']);
        expect(inserts().at(-1)![6]).toBe('list');
        expect(new Set(inserts().map(row => row[0])).size).toBe(1);
    });

    test('mantiene aisladas las trazas de peticiones simultáneas', async () => {
        const [one, two] = await Promise.all([fetch(`${url}/api/delay/one`), fetch(`${url}/api/delay/two`)]);
        expect(one.headers.get('X-Trace-Id')).not.toBe(two.headers.get('X-Trace-Id'));
        for (const response of [one, two]) {
            const rows = inserts().filter(row => row[0] === response.headers.get('X-Trace-Id'));
            expect(rows).toHaveLength(2);
            expect(rows[0]![11].endpoint).toBe(rows[1]![11].endpoint);
        }
    });

    test('preserva descarga binaria por streaming', async () => {
        const response = await fetch(`${url}/api/download`);
        expect(Buffer.from(await response.arrayBuffer())).toEqual(Buffer.from([0, 1, 2, 255, 4]));
        expect(inserts().at(-1)![12]).toBe('INFO');
    });

    test('registra una desconexión como REQUEST_ABORTED una sola vez', async () => {
        await new Promise<void>((resolve, reject) => {
            const req = httpRequest(`${url}/api/abort`, res => {
                res.once('data', () => { req.destroy(); resolve(); });
            });
            req.on('error', reject);
            req.end();
        });
        for (let i = 0; i < 50 && inserts().length < 2; i++) await new Promise(resolve => setTimeout(resolve, 5));
        expect(inserts().map(row => row[7])).toEqual(['START_GET', 'REQUEST_ABORTED']);
        expect(inserts().at(-1)![12]).toBe('ERROR');
    });

    test('un fallo del INSERT no altera la respuesta funcional', async () => {
        query.mockRejectedValue(new Error('database unavailable'));
        const response = await fetch(`${url}/api/items`);
        expect(response.status).toBe(200);
        expect(console.error).toHaveBeenCalledWith('UTIL_AUDIT_WRITE_FAILED', expect.any(Object));
    });

    test('permite desactivar auditoría sin afectar las rutas', async () => {
        process.env.UTIL_AUDIT_ENABLED = 'false';
        const response = await fetch(`${url}/api/items`);
        expect(response.status).toBe(200);
        expect(inserts()).toHaveLength(0);
    });

    test('preserva TraceFrontId inválido como detalle sin insertarlo en columna UUID', async () => {
        await fetch(`${url}/api/items`, { headers: { TraceFrontId: 'external-reference' } });
        expect(inserts()[0]![1]).toBeNull();
        expect(inserts()[0]![11].trace_front_id_original).toBe('external-reference');
    });

    test('mantiene compatibilidad de createOne para consumidores existentes', async () => {
        const result = await auditRepo.createOne({ trace_id: 'trace', service_name: 'service', modulo: 'OTHER', paso: 'CREATE', tipo_evento: 'INFO' });
        expect(result).toEqual({ activity_logs_uuid: 'stored' });
        expect(inserts()[0]![1]).toBeNull();
        expect(inserts()[0]![2]).toBe(0);
    });

    test('mantiene eventos internos y metadatos opcionales', async () => {
        await new Promise<void>(resolve => runWithTrace('trace', 'Internal', 'SAVE', undefined, () => {
            void logActivity(true, 'FAILED', 'detail', { password: 'hidden' }, 12, { codigoError: 'ERR001', log: 'technical' }).then(resolve);
        }));
        expect(inserts()[0]![13]).toBe('ERR001');
        expect(inserts()[0]![16]).toBe('technical');
        expect(inserts()[0]![11].password).toBe('[REDACTED]');
    });
});
