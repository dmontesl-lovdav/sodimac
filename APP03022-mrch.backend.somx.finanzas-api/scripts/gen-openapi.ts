import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import type { OpenAPIV3 } from 'openapi-types';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

type AnyObj = Record<string, unknown>;

const HTTP_METHODS = [
    'get',
    'put',
    'post',
    'delete',
    'options',
    'head',
    'patch',
    'trace',
] as const;

function createSwaggerBase(oas3: OpenAPIV3.Document): AnyObj {
    return {
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
}

function applyEndpointsHost(swagger: AnyObj, endpointsHost?: string): void {
    if (!endpointsHost) return;

    swagger['host'] = endpointsHost;
    swagger['x-google-endpoints'] = [{ name: endpointsHost }];
}

function applyBasePath(swagger: AnyObj, oas3: OpenAPIV3.Document): void {
    if (!Array.isArray(oas3.servers) || oas3.servers.length === 0) return;

    const url = oas3.servers[0]?.url ?? '/';
    if (typeof url === 'string' && url.startsWith('/')) {
        swagger['basePath'] = url;
    }
}

function toSwaggerSecurityScheme(value: unknown): AnyObj {
    const scheme = value as AnyObj & {
        type?: string;
        scheme?: string;
        description?: string;
        name?: string;
        in?: string;
    };

    if (scheme.type === 'http' && scheme.scheme === 'bearer') {
        return {
            type: 'apiKey',
            name: 'Authorization',
            in: 'header',
            description: scheme.description ?? 'Bearer <token>',
        };
    }

    if (scheme.type === 'apiKey') {
        return {
            type: 'apiKey',
            name: scheme.name,
            in: scheme.in,
            description: scheme.description,
        };
    }

    return scheme;
}

function applySecurityDefinitions(
    swagger: AnyObj,
    oas3: OpenAPIV3.Document,
): void {
    const securitySchemes = (
        oas3.components as AnyObj | undefined
    )?.['securitySchemes'] as AnyObj | undefined;

    if (!securitySchemes) return;

    const definitions = Object.fromEntries(
        Object.entries(securitySchemes).map(([key, value]) => [
            key,
            toSwaggerSecurityScheme(value),
        ]),
    );

    swagger['securityDefinitions'] = definitions;
}

function applyDefinitions(swagger: AnyObj, oas3: OpenAPIV3.Document): void {
    const schemas = (
        oas3.components as AnyObj | undefined
    )?.['schemas'] as AnyObj | undefined;

    swagger['definitions'] = schemas ?? {};
}

function getFirstContentType(
    content?: Record<string, OpenAPIV3.MediaTypeObject>,
): string | undefined {
    return content ? Object.keys(content)[0] : undefined;
}

function getPreferredContent(
    content?: Record<string, OpenAPIV3.MediaTypeObject>,
): OpenAPIV3.MediaTypeObject | undefined {
    if (!content) return undefined;

    const firstContentType = getFirstContentType(content);

    return (
        content['application/json'] ??
        (firstContentType ? content[firstContentType] : undefined)
    );
}

function applyRequestBody(
    v2Operation: AnyObj,
    requestBody?: OpenAPIV3.ReferenceObject | OpenAPIV3.RequestBodyObject,
): void {
    if (!requestBody || !('content' in requestBody)) return;

    const firstContentType = getFirstContentType(requestBody.content);
    const content = getPreferredContent(requestBody.content);
    const schema = content?.schema ?? { type: 'object' };
    const parameters = v2Operation['parameters'] as AnyObj[];

    parameters.push({
        in: 'body',
        name: 'body',
        required: requestBody.required ?? false,
        schema,
    });

    if (firstContentType) {
        v2Operation['consumes'] = [firstContentType];
    }
}

function applyOperationParameters(
    v2Operation: AnyObj,
    parameters?: Array<OpenAPIV3.ReferenceObject | OpenAPIV3.ParameterObject>,
): void {
    if (!parameters) return;

    const target = v2Operation['parameters'] as AnyObj[];
    target.push(
        ...parameters.map((parameter) =>
            cleanParameter(parameter as unknown as AnyObj),
        ),
    );
}

function toSwaggerResponse(
    response: OpenAPIV3.ReferenceObject | OpenAPIV3.ResponseObject,
): {
    response: AnyObj;
    contentType?: string;
} {
    if (!('description' in response)) {
        return {
            response: {
                description: '',
            },
        };
    }

    const contentType = getFirstContentType(response.content);
    const content = getPreferredContent(response.content);

    return {
        response: {
            description: response.description ?? '',
            schema: content?.schema,
        },
        contentType,
    };
}

function applyResponses(
    v2Operation: AnyObj,
    responses: OpenAPIV3.ResponsesObject,
): void {
    const v2Responses = v2Operation['responses'] as AnyObj;

    for (const [code, response] of Object.entries(responses)) {
        const converted = toSwaggerResponse(response);
        v2Responses[code] = converted.response;

        if (converted.contentType) {
            v2Operation['produces'] = [converted.contentType];
        }
    }
}

function createSwaggerOperation(
    operation: OpenAPIV3.OperationObject,
    pathParameters: AnyObj[],
): AnyObj {
    const v2Operation: AnyObj = {
        tags: operation.tags,
        summary: operation.summary,
        description: operation.description,
        operationId: operation.operationId,
        produces: ['application/json'],
        consumes: ['application/json'],
        parameters: [...pathParameters],
        responses: {},
    };

    if (operation.security) {
        v2Operation['security'] = operation.security;
    }

    applyRequestBody(v2Operation, operation.requestBody);
    applyOperationParameters(v2Operation, operation.parameters);
    applyResponses(v2Operation, operation.responses);

    return v2Operation;
}

function convertPathItem(
    pathItem: OpenAPIV3.PathItemObject,
): AnyObj {
    const output: AnyObj = {};
    const pathParameters = (
        pathItem.parameters as unknown as AnyObj[] | undefined
    )?.map(cleanParameter) ?? [];

    for (const method of HTTP_METHODS) {
        const operation = (
            pathItem as unknown as Record<string, unknown>
        )[method] as OpenAPIV3.OperationObject | undefined;

        if (!operation) continue;

        output[method] = createSwaggerOperation(
            operation,
            pathParameters,
        );
    }

    return output;
}

function applyPaths(swagger: AnyObj, oas3: OpenAPIV3.Document): void {
    const outputPaths = swagger['paths'] as AnyObj;

    for (const [route, pathItem] of Object.entries(oas3.paths ?? {})) {
        if (!pathItem) continue;

        outputPaths[route] = convertPathItem(
            pathItem as OpenAPIV3.PathItemObject,
        );
    }
}

/** Convierte OAS3 → Swagger 2.0 “suficiente” para integraciones legadas */
function toSwagger2(
    oas3: OpenAPIV3.Document,
    endpointsHost?: string,
): AnyObj {
    const swagger = createSwaggerBase(oas3);

    applyEndpointsHost(swagger, endpointsHost);
    applyBasePath(swagger, oas3);
    applySecurityDefinitions(swagger, oas3);
    applyDefinitions(swagger, oas3);
    applyPaths(swagger, oas3);

    return swagger;
}

function cleanParameter(prm: AnyObj): AnyObj {
    const hasIn = typeof prm['in'] === 'string';
    const hasName = typeof prm['name'] === 'string';

    if (!hasIn || !hasName || prm['in'] === 'body') {
        return prm;
    }

    const schema = prm['schema'] as AnyObj | undefined;
    const output: AnyObj = {
        name: prm['name'],
        in: prm['in'] === 'cookie' ? 'header' : prm['in'],
        required: (prm['required'] as boolean | undefined) ?? false,
        description: prm['description'],
        type:
            (schema?.['type'] as string | undefined) ||
            (prm['type'] as string | undefined) ||
            'string',
    };

    if (schema?.['format']) {
        output['format'] = schema['format'];
    }

    return output;
}

function writeJsonFile(filePath: string, data: unknown): void {
    fs.mkdirSync(path.dirname(filePath), { recursive: true });
    fs.writeFileSync(
        filePath,
        JSON.stringify(data, null, 2),
        'utf-8',
    );
}

async function loadOpenApiDocument(
    root: string,
): Promise<OpenAPIV3.Document> {
    const modulePath = path.join(
        root,
        'src',
        'docs',
        'openapi.ts',
    );

    const mod = await import(pathToFileURL(modulePath).href);

    if (!('buildOpenAPISpec' in mod)) {
        throw new Error('No se encontró buildOpenAPISpec');
    }

    return (
        mod as {
            buildOpenAPISpec: () => Promise<OpenAPIV3.Document>;
        }
    ).buildOpenAPISpec();
}

function getOutputPaths(root: string) {
    return [
        {
            openapi: path.join(
                root,
                'src',
                'docs',
                'openapi.json',
            ),
            swagger: path.join(
                root,
                'src',
                'docs',
                'swagger.json',
            ),
        },
        {
            openapi: path.join(
                root,
                'dist',
                'docs',
                'openapi.json',
            ),
            swagger: path.join(
                root,
                'dist',
                'docs',
                'swagger.json',
            ),
        },
    ];
}

function writeGeneratedSpecs(
    root: string,
    oas3: OpenAPIV3.Document,
    swagger2: AnyObj,
): void {
    const outputs = getOutputPaths(root);

    for (const output of outputs) {
        writeJsonFile(output.openapi, oas3);
        writeJsonFile(output.swagger, swagger2);
    }
}

function logGeneratedSpecs(root: string): void {
    console.log('✅ Generados:');

    for (const output of getOutputPaths(root)) {
        console.log(' -', path.relative(root, output.openapi));
        console.log(' -', path.relative(root, output.swagger));
    }
}

async function main(): Promise<void> {
    const root = path.join(__dirname, '..');
    const oas3 = await loadOpenApiDocument(root);
    const swagger2 = toSwagger2(
        oas3,
        process.env.ENDPOINTS_HOST,
    );

    writeGeneratedSpecs(root, oas3, swagger2);
    logGeneratedSpecs(root);
}

main().catch((error: unknown) => {
    console.error('Failed generating specs:', error);
    process.exit(1);
});