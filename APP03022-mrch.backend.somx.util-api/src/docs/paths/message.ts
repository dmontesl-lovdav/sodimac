// src/docs/paths/message.ts

export const messagePaths = {
    '/messages': {
        get: {
            tags: ['Messages'],
            summary: 'Listar mensajes',
            description: 'Obtiene la lista de mensajes con filtros opcionales',
            parameters: [
                { name: 'messageCode', in: 'query', schema: { type: 'string' }, description: 'Filtrar por codigo' },
                { name: 'idMessageType', in: 'query', schema: { type: 'integer' }, description: 'Filtrar por tipo' }
            ],
            responses: {
                200: {
                    description: 'Lista de mensajes',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/MessageListResponse' }
                        }
                    }
                }
            }
        },
        post: {
            tags: ['Messages'],
            summary: 'Crear mensaje',
            description: 'Crea un nuevo mensaje',
            requestBody: {
                required: true,
                content: {
                    'application/json': {
                        schema: { $ref: '#/components/schemas/CreateMessageRequest' }
                    }
                }
            },
            responses: {
                201: {
                    description: 'Mensaje creado exitosamente',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/MessageResponse' }
                        }
                    }
                },
                400: { description: 'Datos invalidos' },
                409: { description: 'Ya existe un mensaje con ese codigo' }
            }
        }
    },
    '/messages/code/{code}': {
        get: {
            tags: ['Messages'],
            summary: 'Obtener mensaje por codigo',
            parameters: [
                { name: 'code', in: 'path', required: true, schema: { type: 'string' }, description: 'Codigo del mensaje' }
            ],
            responses: {
                200: {
                    description: 'Mensaje encontrado',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/MessageResponse' }
                        }
                    }
                },
                404: { description: 'Mensaje no encontrado' }
            }
        }
    },
    '/messages/{id}': {
        get: {
            tags: ['Messages'],
            summary: 'Obtener mensaje por ID',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, description: 'ID del mensaje' }
            ],
            responses: {
                200: {
                    description: 'Mensaje encontrado',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/MessageResponse' }
                        }
                    }
                },
                404: { description: 'Mensaje no encontrado' }
            }
        },
        patch: {
            tags: ['Messages'],
            summary: 'Actualizar mensaje',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, description: 'ID del mensaje' }
            ],
            requestBody: {
                required: true,
                content: {
                    'application/json': {
                        schema: { $ref: '#/components/schemas/UpdateMessageRequest' }
                    }
                }
            },
            responses: {
                200: {
                    description: 'Mensaje actualizado',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/MessageResponse' }
                        }
                    }
                },
                404: { description: 'Mensaje no encontrado' },
                409: { description: 'Ya existe un mensaje con ese codigo' }
            }
        },
        delete: {
            tags: ['Messages'],
            summary: 'Eliminar mensaje',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, description: 'ID del mensaje' }
            ],
            responses: {
                200: { description: 'Mensaje eliminado exitosamente' },
                404: { description: 'Mensaje no encontrado' }
            }
        }
    }
};
