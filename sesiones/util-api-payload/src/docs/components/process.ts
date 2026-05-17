// src/docs/components/process.ts

export const tags = [
    { name: 'Processes', description: 'Gestion de procesos del sistema' }
];

export const processSchemas = {
    CatProcess: {
        type: 'object',
        properties: {
            idProcess: { type: 'integer', description: 'ID unico del proceso' },
            name: { type: 'string', maxLength: 100, description: 'Nombre del proceso' },
            description: { type: 'string', maxLength: 250, nullable: true, description: 'Descripcion del proceso' },
            createdBy: { type: 'integer', nullable: true, description: 'ID del usuario que creo el registro' },
            createdAt: { type: 'string', format: 'date-time', description: 'Fecha de creacion' },
            updatedBy: { type: 'integer', nullable: true, description: 'ID del usuario que actualizo' },
            updatedAt: { type: 'string', format: 'date-time', nullable: true, description: 'Fecha de actualizacion' }
        },
        required: ['idProcess', 'name']
    },
    CreateProcessRequest: {
        type: 'object',
        properties: {
            name: { type: 'string', maxLength: 100, description: 'Nombre del proceso' },
            description: { type: 'string', maxLength: 250, description: 'Descripcion del proceso' }
        },
        required: ['name']
    },
    UpdateProcessRequest: {
        type: 'object',
        properties: {
            name: { type: 'string', maxLength: 100 },
            description: { type: 'string', maxLength: 250 }
        }
    },
    ProcessListResponse: {
        type: 'object',
        properties: {
            success: { type: 'boolean' },
            data: {
                type: 'array',
                items: { $ref: '#/components/schemas/CatProcess' }
            },
            count: { type: 'integer' }
        }
    },
    ProcessResponse: {
        type: 'object',
        properties: {
            success: { type: 'boolean' },
            data: { $ref: '#/components/schemas/CatProcess' }
        }
    }
};
