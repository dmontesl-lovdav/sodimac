export const statusTrainPaths = {
    '/status-train/validate': {
        get: {
            tags: ['Tren de Estatus'],
            summary: 'Valida si una transición de estatus está permitida',
            parameters: [
                { name: 'optionId', in: 'query', required: true, schema: { type: 'integer' } },
                { name: 'sourceStatus', in: 'query', required: true, schema: { type: 'integer' } },
                { name: 'targetStatus', in: 'query', required: true, schema: { type: 'integer' } }
            ],
            responses: {
                200: {
                    description: 'Resultado de la validación',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/StatusTrainValidationResponse' } } }
                },
                400: {
                    description: 'Transición no válida',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/StatusTrainValidationResponse' } } }
                }
            }
        }
    },
    '/status-train/allowed-destinations': {
        get: {
            tags: ['Tren de Estatus'],
            summary: 'Obtiene los estatus destino permitidos desde un estatus origen',
            parameters: [
                { name: 'optionId', in: 'query', required: true, schema: { type: 'integer' } },
                { name: 'sourceStatus', in: 'query', required: true, schema: { type: 'integer' } }
            ],
            responses: {
                200: {
                    description: 'Lista de destinos permitidos',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/StatusTrainListResponse' } } }
                },
                400: {
                    description: 'Estatus origen no catalogado',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/StatusTrainListResponse' } } }
                }
            }
        }
    },
    '/status-train': {
        post: {
            tags: ['Tren de Estatus'],
            summary: 'Crea una nueva regla de transición',
            requestBody: {
                required: true,
                content: { 'application/json': { schema: { $ref: '#/components/schemas/StatusTrainCreateDto' } } }
            },
            responses: {
                201: {
                    description: 'Regla creada',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/StatusTrainDto' } } }
                }
            }
        }
    },
    '/status-train/{id}': {
        get: {
            tags: ['Tren de Estatus'],
            summary: 'Obtiene una regla por ID',
            parameters: [{ name: 'id', in: 'path', required: true, schema: { type: 'integer' } }],
            responses: {
                200: {
                    description: 'Regla encontrada',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/StatusTrainDto' } } }
                },
                404: { description: 'Regla no encontrada' }
            }
        },
        put: {
            tags: ['Tren de Estatus'],
            summary: 'Actualiza una regla',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' } },
                { name: 'optionId', in: 'query', required: true, schema: { type: 'integer' } }
            ],
            requestBody: {
                required: true,
                content: { 'application/json': { schema: { $ref: '#/components/schemas/StatusTrainUpdateDto' } } }
            },
            responses: {
                200: {
                    description: 'Regla actualizada',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/StatusTrainDto' } } }
                },
                404: { description: 'Regla no encontrada' }
            }
        },
        delete: {
            tags: ['Tren de Estatus'],
            summary: 'Elimina una regla',
            parameters: [{ name: 'id', in: 'path', required: true, schema: { type: 'integer' } }],
            responses: {
                204: { description: 'Regla eliminada' },
                404: { description: 'Regla no encontrada' }
            }
        }
    }
};

