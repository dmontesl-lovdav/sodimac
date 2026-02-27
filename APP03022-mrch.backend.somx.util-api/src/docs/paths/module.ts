// src/docs/paths/module.ts

export const modulePaths = {
    '/modules': {
        get: {
            tags: ['Modules'],
            summary: 'Listar modulos',
            description: 'Obtiene la lista de modulos con filtros opcionales',
            parameters: [
                { name: 'name', in: 'query', schema: { type: 'string' }, description: 'Filtrar por nombre' }
            ],
            responses: {
                200: {
                    description: 'Lista de modulos',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ModuleListResponse' }
                        }
                    }
                }
            }
        },
        post: {
            tags: ['Modules'],
            summary: 'Crear modulo',
            description: 'Crea un nuevo modulo',
            requestBody: {
                required: true,
                content: {
                    'application/json': {
                        schema: { $ref: '#/components/schemas/CreateModuleRequest' }
                    }
                }
            },
            responses: {
                201: {
                    description: 'Modulo creado exitosamente',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ModuleResponse' }
                        }
                    }
                },
                400: { description: 'Datos invalidos' }
            }
        }
    },
    '/modules/{id}': {
        get: {
            tags: ['Modules'],
            summary: 'Obtener modulo por ID',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, description: 'ID del modulo' }
            ],
            responses: {
                200: {
                    description: 'Modulo encontrado',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ModuleResponse' }
                        }
                    }
                },
                404: { description: 'Modulo no encontrado' }
            }
        },
        patch: {
            tags: ['Modules'],
            summary: 'Actualizar modulo',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, description: 'ID del modulo' }
            ],
            requestBody: {
                required: true,
                content: {
                    'application/json': {
                        schema: { $ref: '#/components/schemas/UpdateModuleRequest' }
                    }
                }
            },
            responses: {
                200: {
                    description: 'Modulo actualizado',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ModuleResponse' }
                        }
                    }
                },
                404: { description: 'Modulo no encontrado' }
            }
        },
        delete: {
            tags: ['Modules'],
            summary: 'Eliminar modulo',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, description: 'ID del modulo' }
            ],
            responses: {
                200: { description: 'Modulo eliminado exitosamente' },
                404: { description: 'Modulo no encontrado' }
            }
        }
    }
};
