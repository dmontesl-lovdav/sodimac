const userIdHeader = {
    name: 'X-User-Id',
    in: 'header',
    schema: { type: 'string', default: 'system' }
};

export const conversionPaths = {
    '/conversions': {
        get: {
            tags: ['Conversiones'],
            summary: 'Buscar conversiones con filtros y paginación',
            parameters: [
                { name: 'idElementoOrigen', in: 'query', schema: { type: 'integer' } },
                { name: 'idElemento', in: 'query', schema: { type: 'integer' } },
                { name: 'elemento', in: 'query', schema: { type: 'string' } },
                { name: 'valorElemento', in: 'query', schema: { type: 'string' } },
                { name: 'catalogoOrigen', in: 'query', schema: { type: 'string' } },
                { name: 'estatus', in: 'query', schema: { type: 'integer' } },
                { name: 'page', in: 'query', schema: { type: 'integer', default: 1 } },
                { name: 'pageSize', in: 'query', schema: { type: 'integer', default: 10 } },
                { name: 'sortBy', in: 'query', schema: { type: 'string', default: 'createdAt' } },
                { name: 'sortDir', in: 'query', schema: { type: 'string', enum: ['asc', 'desc'], default: 'desc' } }
            ],
            responses: {
                200: {
                    description: 'Resultados',
                    content: {
                        'application/json': { schema: { $ref: '#/components/schemas/ConversionPageResponse' } }
                    }
                }
            }
        },
        post: {
            tags: ['Conversiones'],
            summary: 'Crear conversión',
            parameters: [userIdHeader],
            requestBody: {
                required: true,
                content: { 'application/json': { schema: { $ref: '#/components/schemas/ConversionCreateDto' } } }
            },
            responses: {
                201: {
                    description: 'Conversión creada',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/ConversionDto' } } }
                }
            }
        },
        delete: {
            tags: ['Conversiones'],
            summary: 'Eliminar múltiples conversiones',
            parameters: [userIdHeader],
            requestBody: {
                content: {
                    'application/json': {
                        schema: {
                            type: 'object',
                            properties: { ids: { type: 'array', items: { type: 'integer' } } }
                        }
                    }
                }
            },
            responses: { 204: { description: 'Eliminadas' } }
        }
    },
    '/conversions/{id}': {
        get: {
            tags: ['Conversiones'],
            summary: 'Consultar conversión por ID',
            parameters: [{ name: 'id', in: 'path', required: true, schema: { type: 'integer' } }],
            responses: {
                200: {
                    description: 'Conversión',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/ConversionDto' } } }
                },
                404: { description: 'No encontrada' }
            }
        },
        put: {
            tags: ['Conversiones'],
            summary: 'Editar conversión',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' } },
                userIdHeader
            ],
            requestBody: {
                required: true,
                content: { 'application/json': { schema: { $ref: '#/components/schemas/ConversionUpdateDto' } } }
            },
            responses: {
                200: {
                    description: 'Actualizada',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/ConversionDto' } } }
                }
            }
        },
        delete: {
            tags: ['Conversiones'],
            summary: 'Eliminar conversión individual',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' } },
                userIdHeader
            ],
            responses: { 204: { description: 'Eliminada' } }
        }
    },
    '/conversions/{id}/principal': {
        patch: {
            tags: ['Conversiones'],
            summary: 'Cambiar conversión principal',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' } },
                userIdHeader
            ],
            requestBody: {
                content: {
                    'application/json': {
                        schema: {
                            type: 'object',
                            properties: { esPrincipal: { type: 'boolean' } }
                        }
                    }
                }
            },
            responses: {
                200: {
                    description: 'Actualizada',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/ConversionDto' } } }
                }
            }
        }
    }
};

