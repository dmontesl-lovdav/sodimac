// src/docs/paths/parameter.ts

export const parameterPaths = {
    '/parameters': {
        get: {
            tags: ['Parameters'],
            summary: 'Listar parametros',
            description: 'Obtiene la lista de parametros con filtros opcionales',
            parameters: [
                { name: 'idModule', in: 'query', schema: { type: 'integer' }, description: 'Filtrar por modulo' },
                { name: 'idType', in: 'query', schema: { type: 'integer' }, description: 'Filtrar por tipo' },
                { name: 'name', in: 'query', schema: { type: 'string' }, description: 'Filtrar por nombre' },
                { name: 'status', in: 'query', schema: { type: 'integer', enum: [0, 1] }, description: 'Filtrar por estado' }
            ],
            responses: {
                200: {
                    description: 'Lista de parametros',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ParameterListResponse' }
                        }
                    }
                }
            }
        },
        post: {
            tags: ['Parameters'],
            summary: 'Crear parametro',
            description: 'Crea un nuevo parametro de configuracion',
            requestBody: {
                required: true,
                content: {
                    'application/json': {
                        schema: { $ref: '#/components/schemas/CreateParameterRequest' }
                    }
                }
            },
            responses: {
                201: {
                    description: 'Parametro creado exitosamente',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ParameterResponse' }
                        }
                    }
                },
                400: { description: 'Datos invalidos' },
                409: { description: 'Parametro ya existe con ese nombre y version' }
            }
        }
    },
    '/parameters/{id}': {
        get: {
            tags: ['Parameters'],
            summary: 'Obtener parametro por ID',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, description: 'ID del parametro' }
            ],
            responses: {
                200: {
                    description: 'Parametro encontrado',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ParameterResponse' }
                        }
                    }
                },
                404: { description: 'Parametro no encontrado' }
            }
        },
        patch: {
            tags: ['Parameters'],
            summary: 'Actualizar parametro',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, description: 'ID del parametro' }
            ],
            requestBody: {
                required: true,
                content: {
                    'application/json': {
                        schema: { $ref: '#/components/schemas/UpdateParameterRequest' }
                    }
                }
            },
            responses: {
                200: {
                    description: 'Parametro actualizado',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ParameterResponse' }
                        }
                    }
                },
                404: { description: 'Parametro no encontrado' },
                409: { description: 'Conflicto: nombre y version ya existen' }
            }
        },
        delete: {
            tags: ['Parameters'],
            summary: 'Eliminar parametro',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, description: 'ID del parametro' }
            ],
            responses: {
                200: { description: 'Parametro eliminado exitosamente' },
                404: { description: 'Parametro no encontrado' }
            }
        }
    }
};
