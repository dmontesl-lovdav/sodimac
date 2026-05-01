const errorContent = {
    'application/json': { schema: { $ref: '#/components/schemas/ExceptionWrapper' } }
};

const userIdHeader = {
    name: 'X-User-Id',
    in: 'header',
    schema: { type: 'string', default: 'system' },
    description: 'ID del usuario que realiza la operacion'
};

export const catalogosPaths = {
    '/catalogos': {
        get: {
            tags: ['Gestión de Catálogos'],
            summary: 'Listar catálogos con filtros y paginación',
            parameters: [
                { name: 'id', in: 'query', schema: { type: 'integer' } },
                { name: 'nombre', in: 'query', schema: { type: 'string' } },
                { name: 'descripcion', in: 'query', schema: { type: 'string' } },
                { name: 'tipo', in: 'query', schema: { type: 'string', enum: ['PRIMARIO', 'SECUNDARIO'] } },
                { name: 'code', in: 'query', schema: { type: 'string' } },
                { name: 'prefix', in: 'query', schema: { type: 'string' } },
                { name: 'estatus', in: 'query', schema: { type: 'integer' } },
                { name: 'page', in: 'query', schema: { type: 'integer', default: 1 } },
                { name: 'pageSize', in: 'query', schema: { type: 'integer', default: 10 } },
                { name: 'sortBy', in: 'query', schema: { type: 'string', default: 'createdAt' } },
                { name: 'sortDir', in: 'query', schema: { type: 'string', enum: ['asc', 'desc'], default: 'desc' } }
            ],
            responses: {
                200: {
                    description: 'Lista de catálogos',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/CatalogPageResponse' } } }
                }
            }
        },
        post: {
            tags: ['Gestión de Catálogos'],
            summary: 'Crear un nuevo catálogo',
            parameters: [userIdHeader],
            requestBody: {
                required: true,
                content: { 'application/json': { schema: { $ref: '#/components/schemas/CatalogCreateDto' } } }
            },
            responses: {
                201: {
                    description: 'Catálogo creado',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/CatalogResponseDto' } } }
                },
                400: { description: 'Datos inválidos o duplicado', content: errorContent },
                409: { description: 'Código o prefijo duplicado', content: errorContent }
            }
        }
    },
    '/catalogos/{id}': {
        get: {
            tags: ['Gestión de Catálogos'],
            summary: 'Obtener catálogo por ID',
            parameters: [{ name: 'id', in: 'path', required: true, schema: { type: 'integer' } }],
            responses: {
                200: {
                    description: 'Catálogo encontrado',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/CatalogResponseDto' } } }
                },
                404: { description: 'Catálogo no encontrado', content: errorContent }
            }
        },
        put: {
            tags: ['Gestión de Catálogos'],
            summary: 'Actualizar un catálogo',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' } },
                userIdHeader
            ],
            requestBody: {
                required: true,
                content: { 'application/json': { schema: { $ref: '#/components/schemas/CatalogUpdateDto' } } }
            },
            responses: {
                200: {
                    description: 'Catálogo actualizado',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/CatalogResponseDto' } } }
                },
                404: { description: 'Catálogo no encontrado', content: errorContent }
            }
        }
    },
    '/catalogos/primarios': {
        get: {
            tags: ['Elementos de Catálogo'],
            summary: 'Listar catálogos primarios activos',
            responses: {
                200: {
                    description: 'Lista de catálogos primarios',
                    content: {
                        'application/json': {
                            schema: { type: 'array', items: { $ref: '#/components/schemas/CatalogSimpleDto' } }
                        }
                    }
                }
            }
        }
    },
    '/catalogos/{catalogId}/detalle': {
        get: {
            tags: ['Elementos de Catálogo'],
            summary: 'Obtener detalle de un catálogo',
            parameters: [{ name: 'catalogId', in: 'path', required: true, schema: { type: 'integer' } }],
            responses: {
                200: {
                    description: 'Catálogo encontrado',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/CatalogSimpleDto' } } }
                },
                404: { description: 'Catálogo no encontrado', content: errorContent }
            }
        }
    },
    '/catalogos/{catalogId}/elementos': {
        get: {
            tags: ['Elementos de Catálogo'],
            summary: 'Consultar elementos de un catálogo',
            parameters: [
                { name: 'catalogId', in: 'path', required: true, schema: { type: 'integer' } },
                { name: 'idElemento', in: 'query', schema: { type: 'integer' } },
                { name: 'elemento', in: 'query', schema: { type: 'string' } },
                { name: 'valor', in: 'query', schema: { type: 'string' } },
                { name: 'idCatalogoPadre', in: 'query', schema: { type: 'integer' } },
                { name: 'idElementoPadre', in: 'query', schema: { type: 'integer' } },
                { name: 'clave', in: 'query', schema: { type: 'string' } },
                { name: 'estatus', in: 'query', schema: { type: 'integer' } },
                { name: 'page', in: 'query', schema: { type: 'integer', default: 1 } },
                { name: 'pageSize', in: 'query', schema: { type: 'integer', default: 10 } },
                { name: 'sortBy', in: 'query', schema: { type: 'string', default: 'createdAt' } },
                { name: 'sortDir', in: 'query', schema: { type: 'string', enum: ['asc', 'desc'], default: 'desc' } }
            ],
            responses: {
                200: {
                    description: 'Lista de elementos',
                    content: {
                        'application/json': { schema: { $ref: '#/components/schemas/CatalogElementPageResponse' } }
                    }
                },
                404: { description: 'Catálogo no encontrado', content: errorContent }
            }
        },
        post: {
            tags: ['Elementos de Catálogo'],
            summary: 'Crear un elemento',
            parameters: [
                { name: 'catalogId', in: 'path', required: true, schema: { type: 'integer' } },
                userIdHeader
            ],
            requestBody: {
                required: true,
                content: { 'application/json': { schema: { $ref: '#/components/schemas/CatalogElementCreateDto' } } }
            },
            responses: {
                201: {
                    description: 'Elemento creado',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/CatalogElementDto' } } }
                },
                400: { description: 'Datos inválidos', content: errorContent },
                404: { description: 'Catálogo no encontrado', content: errorContent }
            }
        }
    },
    '/catalogos/{catalogId}/elementos/activos': {
        get: {
            tags: ['Elementos de Catálogo'],
            summary: 'Listar elementos activos de un catálogo',
            parameters: [{ name: 'catalogId', in: 'path', required: true, schema: { type: 'integer' } }],
            responses: {
                200: {
                    description: 'Lista de elementos activos',
                    content: {
                        'application/json': {
                            schema: { type: 'array', items: { $ref: '#/components/schemas/CatalogElementDto' } }
                        }
                    }
                }
            }
        }
    },
    '/catalogos/{catalogId}/elementos/{elementId}': {
        get: {
            tags: ['Elementos de Catálogo'],
            summary: 'Obtener detalle de un elemento',
            parameters: [
                { name: 'catalogId', in: 'path', required: true, schema: { type: 'integer' } },
                { name: 'elementId', in: 'path', required: true, schema: { type: 'integer' } }
            ],
            responses: {
                200: {
                    description: 'Elemento encontrado',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/CatalogElementDto' } } }
                },
                404: { description: 'Elemento no encontrado', content: errorContent }
            }
        },
        put: {
            tags: ['Elementos de Catálogo'],
            summary: 'Actualizar un elemento',
            parameters: [
                { name: 'catalogId', in: 'path', required: true, schema: { type: 'integer' } },
                { name: 'elementId', in: 'path', required: true, schema: { type: 'integer' } },
                userIdHeader
            ],
            requestBody: {
                required: true,
                content: { 'application/json': { schema: { $ref: '#/components/schemas/CatalogElementUpdateDto' } } }
            },
            responses: {
                200: {
                    description: 'Elemento actualizado',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/CatalogElementDto' } } }
                },
                404: { description: 'Elemento no encontrado', content: errorContent }
            }
        }
    },
    '/catalogos/elementos/{elementId}/estatus': {
        patch: {
            tags: ['Elementos de Catálogo'],
            summary: 'Cambiar estatus de un elemento',
            parameters: [
                { name: 'elementId', in: 'path', required: true, schema: { type: 'integer' } },
                userIdHeader
            ],
            requestBody: {
                required: true,
                content: { 'application/json': { schema: { $ref: '#/components/schemas/CatalogElementStatusDto' } } }
            },
            responses: {
                200: {
                    description: 'Estatus actualizado',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/CatalogElementDto' } } }
                },
                404: { description: 'Elemento no encontrado', content: errorContent }
            }
        }
    },
    '/catalogos/validate-layout': {
        post: {
            tags: ['Elementos de Catálogo'],
            summary: 'Validar archivo XLSX de layout',
            requestBody: {
                required: true,
                content: {
                    'multipart/form-data': {
                        schema: {
                            type: 'object',
                            required: ['file', 'tipoCatalogoSeleccionado', 'nombreCatalogo'],
                            properties: {
                                file: { type: 'string', format: 'binary' },
                                tipoCatalogoSeleccionado: { type: 'string', example: 'PRIMARIO' },
                                nombreCatalogo: { type: 'string', example: 'Catálogo Prueba' }
                            }
                        }
                    }
                }
            },
            responses: {
                200: {
                    description: 'Resultado de la validación',
                    content: {
                        'application/json': { schema: { $ref: '#/components/schemas/LayoutValidationResponse' } }
                    }
                },
                400: { description: 'Archivo inválido' }
            }
        }
    },
    '/validation-reports/{reportId}': {
        get: {
            tags: ['Elementos de Catálogo'],
            summary: 'Descargar reporte de errores de validación',
            parameters: [{ name: 'reportId', in: 'path', required: true, schema: { type: 'string' } }],
            responses: {
                200: {
                    description: 'Reporte de errores',
                    content: { 'text/plain': { schema: { type: 'string' } } }
                },
                404: { description: 'Reporte no encontrado' }
            }
        }
    }
};

