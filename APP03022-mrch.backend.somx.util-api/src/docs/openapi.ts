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
        const m = await import(url);
        mods.push(m as AnyObj);
    }
    return mods;
}

function collectTagsFromModule(mod: AnyObj, tags: Tag[]): void {
    const maybeTags = mod.tags;
    if (Array.isArray(maybeTags)) tags.push(...(maybeTags as Tag[]));
}

function mergeExportsBySuffix(
    mods: AnyObj[],
    suffix: string,
    target: AnyObj,
    tags: Tag[],
): void {
    for (const m of mods) {
        for (const [k, v] of Object.entries(m)) {
            if (k.endsWith(suffix) && isPlainObject(v)) Object.assign(target, v);
        }
        collectTagsFromModule(m, tags);
    }
}

function buildSpecSkeleton(paths: AnyObj, tags: Tag[], schemas: AnyObj) {
    return {
        openapi: '3.0.3',
        info: {
            title: process.env.SERVICE_TITLE ?? 'Utils API - Herramientas y Utilerias',
            version: process.env.npm_package_version ?? '1.0.0',
            description: 'API de catalogos y parametros del sistema',
        },
        servers: [{ url: '/api', description: 'Base URL' }],
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
                Error: {
                    type: 'object',
                    properties: { error: { type: 'string' } },
                    example: { error: 'Something went wrong' },
                },
                ...schemas,
            },
        },
        security: [],
    };
}

/**
 * Recorre todos los modulos en:
 *  - src/docs/components/*.ts   -> objetos ...Schemas
 *  - src/docs/paths/*.ts        -> objetos ...Paths
 * y arma una OAS3 valida.
 */
export async function buildOpenAPISpec() {
    const componentsMods = await importAllFrom('./components');
    const pathsMods = await importAllFrom('./paths');

    const schemas: AnyObj = {};
    const paths: AnyObj = {};
    const tags: Tag[] = [{ name: 'Health', description: 'Service health checks' }];

    mergeExportsBySuffix(componentsMods, 'Schemas', schemas, tags);
    mergeExportsBySuffix(pathsMods, 'Paths', paths, tags);

    return buildSpecSkeleton(paths, tags, schemas);
}
