// src/docs/paths/item.ts

export const itemPaths = {
    '/items': {
        get: {
            tags: ['Items'],
            summary: 'Listar elementos',
            description: 'Obtiene la lista de elementos con filtros opcionales',
            parameters: [
                { name: 'name', in: 'query', schema: { type: 'string' }, description: 'Filtrar por nombre' }
            ],
            responses: {
                200: {
                    description: 'Lista de elementos',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ItemListResponse' }
                        }
                    }
                }
            }
        },
        post: {
            tags: ['Items'],
            summary: 'Crear elemento',
            description: 'Crea un nuevo elemento',
            requestBody: {
                required: true,
                content: {
                    'application/json': {
                        schema: { $ref: '#/components/schemas/CreateItemRequest' }
                    }
                }
            },
            responses: {
                201: {
                    description: 'Elemento creado exitosamente',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ItemResponse' }
                        }
                    }
                },
                400: { description: 'Datos invalidos' }
            }
        }
    },
    '/items/{id}': {
        get: {
            tags: ['Items'],
            summary: 'Obtener elemento por ID',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, description: 'ID del elemento' }
            ],
            responses: {
                200: {
                    description: 'Elemento encontrado',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ItemResponse' }
                        }
                    }
                },
                404: { description: 'Elemento no encontrado' }
            }
        },
        patch: {
            tags: ['Items'],
            summary: 'Actualizar elemento',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, description: 'ID del elemento' }
            ],
            requestBody: {
                required: true,
                content: {
                    'application/json': {
                        schema: { $ref: '#/components/schemas/UpdateItemRequest' }
                    }
                }
            },
            responses: {
                200: {
                    description: 'Elemento actualizado',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ItemResponse' }
                        }
                    }
                },
                404: { description: 'Elemento no encontrado' }
            }
        },
        delete: {
            tags: ['Items'],
            summary: 'Eliminar elemento',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, description: 'ID del elemento' }
            ],
            responses: {
                200: { description: 'Elemento eliminado exitosamente' },
                404: { description: 'Elemento no encontrado' }
            }
        }
    }
};
