// src/docs/paths/applicationMsg.ts

export const applicationMsgPaths = {
    '/application-messages': {
        get: {
            tags: ['ApplicationMessages'],
            summary: 'Listar mensajes por aplicativo',
            description: 'Obtiene la lista de relaciones mensaje-aplicativo',
            parameters: [
                { name: 'idApplication', in: 'query', schema: { type: 'integer' }, description: 'Filtrar por aplicativo' },
                { name: 'idMessage', in: 'query', schema: { type: 'integer' }, description: 'Filtrar por mensaje' }
            ],
            responses: {
                200: {
                    description: 'Lista de relaciones mensaje-aplicativo',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ApplicationMsgListResponse' }
                        }
                    }
                }
            }
        },
        post: {
            tags: ['ApplicationMessages'],
            summary: 'Crear relacion mensaje-aplicativo',
            description: 'Asocia un mensaje con un aplicativo',
            requestBody: {
                required: true,
                content: {
                    'application/json': {
                        schema: { $ref: '#/components/schemas/CreateApplicationMsgRequest' }
                    }
                }
            },
            responses: {
                201: {
                    description: 'Relacion creada exitosamente',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ApplicationMsgResponse' }
                        }
                    }
                },
                400: { description: 'Datos invalidos' },
                409: { description: 'Ya existe esta relacion mensaje-aplicativo' }
            }
        }
    },
    '/application-messages/{id}': {
        get: {
            tags: ['ApplicationMessages'],
            summary: 'Obtener relacion por ID',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, description: 'ID de la relacion' }
            ],
            responses: {
                200: {
                    description: 'Relacion encontrada',
                    content: {
                        'application/json': {
                            schema: { $ref: '#/components/schemas/ApplicationMsgResponse' }
                        }
                    }
                },
                404: { description: 'Relacion no encontrada' }
            }
        },
        delete: {
            tags: ['ApplicationMessages'],
            summary: 'Eliminar relacion',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' }, description: 'ID de la relacion' }
            ],
            responses: {
                200: { description: 'Relacion eliminada exitosamente' },
                404: { description: 'Relacion no encontrada' }
            }
        }
    }
};
