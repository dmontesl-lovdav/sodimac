import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import type { OpenAPIV3 } from 'openapi-types';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

type AnyObj = Record<string, unknown>;

/** Convierte OAS3 → Swagger 2.0 “suficiente” para integraciones legadas */
function toSwagger2(oas3: OpenAPIV3.Document, endpointsHost?: string): AnyObj {
    const swagger: AnyObj = {
        swagger: '2.0',
        info: oas3.info,
        schemes: ['https', 'http'],
        basePath: '/',
        paths: {},
        definitions: {},
        securityDefinitions: {},
        produces: ['application/json'],
        consumes: ['application/json'],
    };

    if (endpointsHost) {
        (swagger as AnyObj)['host'] = endpointsHost;
        (swagger as AnyObj)['x-google-endpoints'] = [{ name: endpointsHost }];
    }

    if (Array.isArray(oas3.servers) && oas3.servers.length > 0) {
        const u = oas3.servers[0]?.url ?? '/';
        if (typeof u === 'string' && u.startsWith('/')) (swagger as AnyObj)['basePath'] = u;
    }

    const sec = (oas3.components as AnyObj | undefined)?.['securitySchemes'] as AnyObj | undefined;
    if (sec) {
        for (const [k, v] of Object.entries(sec)) {
            const s = v as AnyObj & { type?: string; scheme?: string; description?: string; name?: string; in?: string };
            if (s.type === 'http' && s.scheme === 'bearer') {
                (swagger as AnyObj)['securityDefinitions'] = {
                    ...(swagger as AnyObj)['securityDefinitions'] as AnyObj,
                    [k]: {
                        type: 'apiKey',
                        name: 'Authorization',
                        in: 'header',
                        description: s.description ?? 'Bearer <token>',
                    },
                };
            } else if (s.type === 'apiKey') {
                (swagger as AnyObj)['securityDefinitions'] = {
                    ...(swagger as AnyObj)['securityDefinitions'] as AnyObj,
                    [k]: {
                        type: 'apiKey',
                        name: s.name,
                        in: s.in,
                        description: s.description,
                    },
                };
            } else {
                (swagger as AnyObj)['securityDefinitions'] = {
                    ...(swagger as AnyObj)['securityDefinitions'] as AnyObj,
                    [k]: s,
                };
            }
        }
    }

    const schemas = (oas3.components as AnyObj | undefined)?.['schemas'] as AnyObj | undefined;
    (swagger as AnyObj)['definitions'] = schemas ?? {};

    const outPaths = (swagger as AnyObj)['paths'] as AnyObj;
    for (const [p, pathItem] of Object.entries(oas3.paths ?? {})) {
        const pi = pathItem as OpenAPIV3.PathItemObject;
        outPaths[p] = {};

        const operations = ['get', 'put', 'post', 'delete', 'options', 'head', 'patch', 'trace'] as const;

        const pathParams =
            (pi.parameters as unknown as AnyObj[] | undefined)?.map(cleanParameter) ?? [];

        for (const m of operations) {
            const op = (pi as unknown as Record<string, unknown>)[m];
            if (!op) continue;

            const o = op as OpenAPIV3.OperationObject;
            const v2op: AnyObj = {
                tags: o.tags,
                summary: o.summary,
                description: o.description,
                operationId: o.operationId,
                produces: ['application/json'],
                consumes: ['application/json'],
                parameters: [...pathParams],
                responses: {},
            };

            if (o.security) (v2op as AnyObj)['security'] = o.security;

            if (o.requestBody) {
                const rb = o.requestBody as OpenAPIV3.RequestBodyObject;
                const firstCt = rb.content ? Object.keys(rb.content)[0] : undefined;
                const content =
                    rb.content?.['application/json'] ??
                    (firstCt ? rb.content?.[firstCt as keyof typeof rb.content] : undefined);
                const schema = (content as OpenAPIV3.MediaTypeObject | undefined)?.schema ?? { type: 'object' };
                (v2op.parameters as unknown as AnyObj[]).push({
                    in: 'body',
                    name: 'body',
                    required: !!rb.required,
                    schema,
                });
                if (firstCt) (v2op as AnyObj)['consumes'] = [firstCt];
            }

            const params = (o.parameters as unknown as AnyObj[] | undefined) ?? [];
            for (const prm of params) (v2op.parameters as unknown as AnyObj[]).push(cleanParameter(prm));

            const v2resps = (v2op as AnyObj)['responses'] as AnyObj;
            for (const [code, resp] of Object.entries(o.responses ?? {})) {
                const r = resp as OpenAPIV3.ResponseObject;
                const firstCt = r.content ? Object.keys(r.content)[0] : undefined;
                const c =
                    r.content?.['application/json'] ??
                    (firstCt ? r.content?.[firstCt as keyof typeof r.content] : undefined);
                v2resps[code] = { description: r.description ?? '', schema: (c as AnyObj | undefined)?.['schema'] };
                if (firstCt) (v2op as AnyObj)['produces'] = [firstCt];
            }

            (outPaths[p] as AnyObj)[m] = v2op;
        }
    }

    return swagger;
}

function cleanParameter(prm: AnyObj): AnyObj {
    const hasIn = typeof prm?.['in'] === 'string';
    const hasName = typeof prm?.['name'] === 'string';
    if (hasIn && hasName && prm['in'] !== 'body') {
        const schema = prm['schema'] as AnyObj | undefined;
        const out: AnyObj = {
            name: prm['name'],
            in: prm['in'] === 'cookie' ? 'header' : prm['in'],
            required: (prm['required'] as boolean | undefined) ?? false,
            description: prm['description'],
            type: (schema?.['type'] as string | undefined) || (prm['type'] as string | undefined) || 'string',
        };
        if (schema?.['format']) (out as AnyObj)['format'] = schema['format'];
        return out;
    }
    return prm;
}

function writeJsonFile(filePath: string, data: unknown) {
    fs.mkdirSync(path.dirname(filePath), { recursive: true });
    fs.writeFileSync(filePath, JSON.stringify(data, null, 2), 'utf-8');
}

async function main() {
    const root = path.join(__dirname, '..');

    const mod = await import(pathToFileURL(path.join(root, 'src', 'docs', 'openapi.ts')).href);
    if (!('buildOpenAPISpec' in mod)) throw new Error('No se encontró buildOpenAPISpec');

    const oas3 = await (mod as { buildOpenAPISpec: () => Promise<OpenAPIV3.Document> }).buildOpenAPISpec();
    const host = process.env.ENDPOINTS_HOST;
    const swagger2 = toSwagger2(oas3, host);

    const outputs = [
        {
            openapi: path.join(root, 'src', 'docs', 'openapi.json'),
            swagger: path.join(root, 'src', 'docs', 'swagger.json'),
        },
        {
            openapi: path.join(root, 'dist', 'docs', 'openapi.json'),
            swagger: path.join(root, 'dist', 'docs', 'swagger.json'),
        },
    ];

    for (const output of outputs) {
        writeJsonFile(output.openapi, oas3);
        writeJsonFile(output.swagger, swagger2);
    }

    console.log('✅ Generados:');
    for (const output of outputs) {
        console.log(' -', path.relative(root, output.openapi));
        console.log(' -', path.relative(root, output.swagger));
    }
}

main().catch((e) => {
    console.error('Failed generating specs:', e);
    process.exit(1);
});