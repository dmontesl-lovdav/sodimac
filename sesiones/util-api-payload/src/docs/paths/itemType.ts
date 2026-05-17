// src/docs/paths/itemType.ts

export const itemTypePaths = {
    '/item-types': {
        get: {
            tags: ['ItemTypes'],
            summary: 'Listar tipos de elemento',
            description: 'Obtiene la lista de tipos de elemento con filtros opcionales',
            parameters: [
                { name: 'name', in: 'query', schema: { type: 'string' }, description: 'Filtrar por nombre' }
            ],
            responses: {
                200: {
                    description: 'Lista de tipos de elemento',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ItemTypeListResponse' }
                        }
                    }
                }
            }
        },
        post: {
            tags: ['ItemTypes'],
            summary: 'Crear tipo de elemento',
            description: 'Crea un nuevo tipo de elemento',
            requestBody: {
                required: true,
                content: {
                    'application/json': {
                        schema: { $ref: '#/components/schemas/CreateItemTypeRequest' }
                    }
                }
            },
            responses: {
                201: {
                    description: 'Tipo de elemento creado exitosamente',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ItemTypeResponse' }
                        }
                    }
                },
                400: { description: 'Datos invalidos' }
            }
        }
    },
    '/item-types/{id}': {
        get: {
            tags: ['ItemTypes'],
            summary: 'Obtener tipo de elemento por ID',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, description: 'ID del tipo de elemento' }
            ],
            responses: {
                200: {
                    description: 'Tipo de elemento encontrado',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ItemTypeResponse' }
                        }
                    }
                },
                404: { description: 'Tipo de elemento no encontrado' }
            }
        },
        patch: {
            tags: ['ItemTypes'],
            summary: 'Actualizar tipo de elemento',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, description: 'ID del tipo de elemento' }
            ],
            requestBody: {
                required: true,
                content: {
                    'application/json': {
                        schema: { $ref: '#/components/schemas/UpdateItemTypeRequest' }
                    }
                }
            },
            responses: {
                200: {
                    description: 'Tipo de elemento actualizado',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ItemTypeResponse' }
                        }
                    }
                },
                404: { description: 'Tipo de elemento no encontrado' }
            }
        },
        delete: {
            tags: ['ItemTypes'],
            summary: 'Eliminar tipo de elemento',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, description: 'ID del tipo de elemento' }
            ],
            responses: {
                200: { description: 'Tipo de elemento eliminado exitosamente' },
                404: { description: 'Tipo de elemento no encontrado' }
            }
        }
    }
};
