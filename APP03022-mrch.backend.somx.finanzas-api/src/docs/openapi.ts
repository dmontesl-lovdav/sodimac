import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

type AnyObj = Record<string, unknown>;
type Tag = { name: string; description?: string };

function isPlainObject(value: unknown): value is Record<string, unknown> {
    return !!value && typeof value === 'object' && !Array.isArray(value);
}

async function importAllFrom(dir: string): Promise<AnyObj[]> {
    const absolutePath = path.join(__dirname, dir);

    if (!fs.existsSync(absolutePath)) {
        return [];
    }

    const files = fs
        .readdirSync(absolutePath)
        .filter((file) => file.endsWith('.ts') && !file.endsWith('.d.ts'));

    const modules: AnyObj[] = [];

    for (const file of files) {
        console.log('Loading OpenAPI file:', file);

        const moduleUrl = pathToFileURL(
            path.join(absolutePath, file),
        ).href;

        const loadedModule = await import(moduleUrl);
        modules.push(loadedModule as AnyObj);
    }

    return modules;
}

function mergeModuleExports(
    modules: AnyObj[],
    exportSuffix: 'Schemas' | 'Paths',
    target: AnyObj,
    tags: Tag[],
): void {
    for (const module of modules) {
        mergeSingleModule(
            module,
            exportSuffix,
            target,
            tags,
        );
    }
}

function mergeSingleModule(
    module: AnyObj,
    exportSuffix: 'Schemas' | 'Paths',
    target: AnyObj,
    tags: Tag[],
): void {
    for (const [key, value] of Object.entries(module)) {
        mergeExportValue(
            key,
            value,
            exportSuffix,
            target,
            tags,
        );
    }
}

function mergeExportValue(
    key: string,
    value: unknown,
    exportSuffix: 'Schemas' | 'Paths',
    target: AnyObj,
    tags: Tag[],
): void {
    if (key.endsWith(exportSuffix) && isPlainObject(value)) {
        Object.assign(target, value);
    }

    if (key === 'tags' && Array.isArray(value)) {
        tags.push(...(value as Tag[]));
    }
}

function createInfo(): AnyObj {
    return {
        title: process.env.SERVICE_TITLE ?? 'Finanzas API',
        version: process.env.npm_package_version ?? '1.0.0',
        description: 'Express + TypeScript API (OAS3)',
    };
}

function createServers(): AnyObj[] {
    return [
        {
            url: '/api',
            description: 'Base URL (paths incluyen /api si así los defines)',
        },
    ];
}

function createSecuritySchemes(): AnyObj {
    return {
        bearerAuth: {
            type: 'http',
            scheme: 'bearer',
            bearerFormat: 'JWT',
            description: 'Bearer <token>',
        },
    };
}

function createSchemas(schemas: AnyObj): AnyObj {
    return {
        Error: {
            type: 'object',
            properties: {
                error: {
                    type: 'string',
                },
            },
            example: {
                error: 'Something went wrong',
            },
        },
        ...schemas,
    };
}

function createOpenApiSpec(
    schemas: AnyObj,
    paths: AnyObj,
    tags: Tag[],
): AnyObj {
    return {
        openapi: '3.0.3',
        info: createInfo(),
        servers: createServers(),
        tags,
        paths,
        components: {
            securitySchemes: createSecuritySchemes(),
            schemas: createSchemas(schemas),
        },
        security: [],
    };
}

/**
 * Recorre todos los módulos en:
 *  - src/docs/components/*.ts   -> objetos ...Schemas
 *  - src/docs/paths/*.ts        -> objetos ...Paths
 * y arma una OAS3 válida.
 */
export async function buildOpenAPISpec(): Promise<AnyObj> {
    const [componentsModules, pathsModules] = await Promise.all([
        importAllFrom('./components'),
        importAllFrom('./paths'),
    ]);

    const schemas: AnyObj = {};
    const paths: AnyObj = {};
    const tags: Tag[] = [];

    mergeModuleExports(
        componentsModules,
        'Schemas',
        schemas,
        tags,
    );

    mergeModuleExports(
        pathsModules,
        'Paths',
        paths,
        tags,
    );

    return createOpenApiSpec(
        schemas,
        paths,
        tags,
    );
}