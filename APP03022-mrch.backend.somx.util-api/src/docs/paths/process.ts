// src/docs/paths/process.ts

export const processPaths = {
    '/processes': {
        get: {
            tags: ['Processes'],
            summary: 'Listar procesos',
            description: 'Obtiene la lista de procesos con filtros opcionales',
            parameters: [
                { name: 'name', in: 'query', schema: { type: 'string' }, description: 'Filtrar por nombre' }
            ],
            responses: {
                200: {
                    description: 'Lista de procesos',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ProcessListResponse' }
                        }
                    }
                }
            }
        },
        post: {
            tags: ['Processes'],
            summary: 'Crear proceso',
            description: 'Crea un nuevo proceso',
            requestBody: {
                required: true,
                content: {
                    'application/json': {
                        schema: { $ref: '#/components/schemas/CreateProcessRequest' }
                    }
                }
            },
            responses: {
                201: {
                    description: 'Proceso creado exitosamente',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ProcessResponse' }
                        }
                    }
                },
                400: { description: 'Datos invalidos' }
            }
        }
    },
    '/processes/{id}': {
        get: {
            tags: ['Processes'],
            summary: 'Obtener proceso por ID',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, description: 'ID del proceso' }
            ],
            responses: {
                200: {
                    description: 'Proceso encontrado',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ProcessResponse' }
                        }
                    }
                },
                404: { description: 'Proceso no encontrado' }
            }
        },
        patch: {
            tags: ['Processes'],
            summary: 'Actualizar proceso',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, description: 'ID del proceso' }
            ],
            requestBody: {
                required: true,
                content: {
                    'application/json': {
                        schema: { $ref: '#/components/schemas/UpdateProcessRequest' }
                    }
                }
            },
            responses: {
                200: {
                    description: 'Proceso actualizado',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ProcessResponse' }
                        }
                    }
                },
                404: { description: 'Proceso no encontrado' }
            }
        },
        delete: {
            tags: ['Processes'],
            summary: 'Eliminar proceso',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, description: 'ID del proceso' }
            ],
            responses: {
                200: { description: 'Proceso eliminado exitosamente' },
                404: { description: 'Proceso no encontrado' }
            }
        }
    }
};
