// src/docs/openapi.ts
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

type AnyObj = Record<string, unknown>;
type Tag = { name: string; description?: string };

function isPlainObject(v: unknown): v is Record<string, unknown> {
    return !!v && typeof v === 'object' && !Array.isArray(v);
}

async function importAllFrom(dir: string): Promise<AnyObj[]> {
    const abs = path.join(__dirname, dir);
    if (!fs.existsSync(abs)) return [];
    const files = fs.readdirSync(abs).filter(f => f.endsWith('.ts'));
    const mods: AnyObj[] = [];
    for (const f of files) {
        const url = pathToFileURL(path.join(abs, f)).href;
        // import dinámico de cada archivo
        // eslint rule no-await-in-loop no aplica si el objetivo es secuencial
        // y aquí necesitamos preservar el orden de merge; mantener await dentro del loop
        // pero sin deshabilitar reglas globalmente
        const m = await import(url);
        mods.push(m as AnyObj);
    }
    return mods;
}

/**
 * Recorre todos los módulos en:
 *  - src/docs/components/*.ts   -> objetos ...Schemas
 *  - src/docs/paths/*.ts        -> objetos ...Paths
 * y arma una OAS3 válida.
 */
export async function buildOpenAPISpec() {
    const componentsMods = await importAllFrom('./components');
    const pathsMods = await importAllFrom('./paths');

    const schemas: AnyObj = {};
    const paths: AnyObj = {};
    const tags: Tag[] = [{ name: 'Health', description: 'Service health checks' }];

    // merge de componentes (cualquier export que termine en "Schemas")
    for (const m of componentsMods) {
        for (const [k, v] of Object.entries(m)) {
            if (k.endsWith('Schemas') && isPlainObject(v)) Object.assign(schemas, v);
            if (k === 'tags' && Array.isArray(v)) tags.push(...(v as Tag[]));
        }
    }

    // merge de paths (cualquier export que termine en "Paths")
    for (const m of pathsMods) {
        for (const [k, v] of Object.entries(m)) {
            if (k.endsWith('Paths') && isPlainObject(v)) Object.assign(paths, v);
            if (k === 'tags' && Array.isArray(v)) tags.push(...(v as Tag[]));
        }
    }

    const spec = {
        openapi: '3.0.3',
        info: {
            title: process.env.SERVICE_TITLE ?? 'Finanzas API',
            version: process.env.npm_package_version ?? '1.0.0',
            description: 'Express + TypeScript API (OAS3)',
        },
        servers: [{ url: '/api', description: 'Base URL (paths incluyen /api si así los defines)' }],
        tags,
        paths,
        components: {
            securitySchemes: {
                bearerAuth: {
                    type: 'http',
                    scheme: 'bearer',
                    bearerFormat: 'JWT',
                    description: 'Bearer <token>',
                },
            },
            schemas: {
                // esquema de error base + lo encontrado en /components
                Error: {
                    type: 'object',
                    properties: { error: { type: 'string' } },
                    example: { error: 'Something went wrong' },
                },
                ...schemas,
            },
        },
        security: [], // opcional: puedes poner [{ bearerAuth: [] }]
    };

    return spec;
}
