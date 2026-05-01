const langParam = {
    name: 'lang',
    in: 'query',
    schema: { type: 'integer', enum: [1, 2, 3], default: 1 },
    description: 'ID del idioma (1=ES, 2=EN, 3=PT). Por defecto: 1'
};

const errorContent = {
    'application/json': { schema: { $ref: '#/components/schemas/ExceptionWrapper' } }
};

export const catalogPaths = {
    '/catalog': {
        get: {
            tags: ['Catalogos'],
            summary: 'Obtiene todos los catálogos disponibles',
            parameters: [langParam],
            responses: {
                200: {
                    description: 'Lista de catálogos',
                    content: {
                        'application/json': {
                            schema: { type: 'array', items: { $ref: '#/components/schemas/CatalogHeaderDto' } }
                        }
                    }
                },
                500: { description: 'Error interno', content: errorContent }
            }
        }
    },
    '/catalog/module/{module}': {
        get: {
            tags: ['Catalogos'],
            summary: 'Obtiene catálogos por módulo',
            parameters: [
                { name: 'module', in: 'path', required: true, schema: { type: 'string' } },
                langParam
            ],
            responses: {
                200: {
                    description: 'Lista de catálogos del módulo',
                    content: {
                        'application/json': {
                            schema: { type: 'array', items: { $ref: '#/components/schemas/CatalogHeaderDto' } }
                        }
                    }
                }
            }
        }
    },
    '/catalog/{code}': {
        get: {
            tags: ['Catalogos'],
            summary: 'Obtiene un catálogo por su código, incluyendo detalles',
            parameters: [
                { name: 'code', in: 'path', required: true, schema: { type: 'string' } },
                langParam
            ],
            responses: {
                200: {
                    description: 'Catálogo con detalles',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/CatalogHeaderDto' } } }
                },
                404: { description: 'Catálogo no encontrado', content: errorContent }
            }
        }
    },
    '/catalog/{code}/details': {
        get: {
            tags: ['Catalogos'],
            summary: 'Obtiene solo los detalles de un catálogo',
            description:
                'Permite filtrar por dependencia de otro catálogo usando el parámetro dependsOn. ' +
                'Formato: dependsOn=CATALOG_CODE:EXTERNAL_KEY (ej: dependsOn=REGIMEN_FISCAL:605).',
            parameters: [
                { name: 'code', in: 'path', required: true, schema: { type: 'string' } },
                langParam,
                {
                    name: 'dependsOn',
                    in: 'query',
                    schema: { type: 'string' },
                    description: 'Filtro de dependencia (CATALOG_CODE:EXTERNAL_KEY)'
                }
            ],
            responses: {
                200: {
                    description: 'Lista de detalles del catálogo',
                    content: {
                        'application/json': {
                            schema: { type: 'array', items: { $ref: '#/components/schemas/CatalogDetailDto' } }
                        }
                    }
                },
                400: { description: 'Formato de dependsOn inválido', content: errorContent }
            }
        }
    },
    '/catalog/{code}/details/{key}': {
        get: {
            tags: ['Catalogos'],
            summary: 'Obtiene un detalle específico por código de catálogo y clave',
            parameters: [
                { name: 'code', in: 'path', required: true, schema: { type: 'string' } },
                { name: 'key', in: 'path', required: true, schema: { type: 'string' } },
                langParam
            ],
            responses: {
                200: {
                    description: 'Detalle del catálogo',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/CatalogDetailDto' } } }
                },
                404: { description: 'Detalle no encontrado', content: errorContent }
            }
        }
    },
    '/catalog/message/{key}': {
        get: {
            tags: ['Catalogos'],
            summary: 'Obtiene un mensaje/detalle directamente por su clave única',
            parameters: [
                { name: 'key', in: 'path', required: true, schema: { type: 'string' } },
                langParam
            ],
            responses: {
                200: {
                    description: 'Mensaje encontrado',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/CatalogDetailDto' } } }
                },
                404: { description: 'Mensaje no encontrado', content: errorContent }
            }
        }
    },
    '/catalog/message/{key}/format': {
        get: {
            tags: ['Catalogos'],
            summary: 'Obtiene un mensaje con placeholders sustituidos por parámetros',
            description:
                "Busca el mensaje por clave y sustituye los placeholders {0}, {1}, etc. con los parámetros proporcionados.",
            parameters: [
                { name: 'key', in: 'path', required: true, schema: { type: 'string' } },
                langParam,
                {
                    name: 'params',
                    in: 'query',
                    schema: { type: 'array', items: { type: 'string' } },
                    style: 'form',
                    explode: true,
                    description: 'Parámetros para sustituir placeholders'
                }
            ],
            responses: {
                200: {
                    description: 'Mensaje formateado',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/CatalogDetailDto' } } }
                },
                400: { description: 'Cantidad de parámetros incorrecta', content: errorContent },
                404: { description: 'Mensaje no encontrado', content: errorContent }
            }
        }
    },
    '/catalog/id/{id}': {
        get: {
            tags: ['Catalogos'],
            summary: 'Obtiene un catálogo por su ID',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' } },
                langParam
            ],
            responses: {
                200: {
                    description: 'Catálogo con detalles',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/CatalogHeaderDto' } } }
                },
                404: { description: 'Catálogo no encontrado', content: errorContent }
            }
        }
    },
    '/catalog/prefix/{prefix}': {
        get: {
            tags: ['Catalogos'],
            summary: 'Obtiene un catálogo por su prefijo',
            parameters: [
                { name: 'prefix', in: 'path', required: true, schema: { type: 'string' } },
                langParam
            ],
            responses: {
                200: {
                    description: 'Catálogo con detalles',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/CatalogHeaderDto' } } }
                },
                404: { description: 'Catálogo no encontrado', content: errorContent }
            }
        }
    }
};

