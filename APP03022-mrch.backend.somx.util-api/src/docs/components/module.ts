// src/docs/components/module.ts

export const tags = [
    { name: 'Modules', description: 'Gestion de modulos del sistema' }
];

export const moduleSchemas = {
    CatModule: {
        type: 'object',
        properties: {
            idModule: { type: 'integer', description: 'ID unico del modulo' },
            name: { type: 'string', maxLength: 100, description: 'Nombre del modulo' },
            description: { type: 'string', maxLength: 250, nullable: true, description: 'Descripcion del modulo' },
            createdBy: { type: 'integer', nullable: true, description: 'ID del usuario que creo el registro' },
            createdAt: { type: 'string', format: 'date-time', description: 'Fecha de creacion' },
            updatedBy: { type: 'integer', nullable: true, description: 'ID del usuario que actualizo' },
            updatedAt: { type: 'string', format: 'date-time', nullable: true, description: 'Fecha de actualizacion' }
        },
        required: ['idModule', 'name']
    },
    CreateModuleRequest: {
        type: 'object',
        properties: {
            name: { type: 'string', maxLength: 100, description: 'Nombre del modulo' },
            description: { type: 'string', maxLength: 250, description: 'Descripcion del modulo' }
        },
        required: ['name']
    },
    UpdateModuleRequest: {
        type: 'object',
        properties: {
            name: { type: 'string', maxLength: 100 },
            description: { type: 'string', maxLength: 250 }
        }
    },
    ModuleListResponse: {
        type: 'object',
        properties: {
            success: { type: 'boolean' },
            data: {
                type: 'array',
                items: { $ref: '#/components/schemas/CatModule' }
            },
            count: { type: 'integer' }
        }
    },
    ModuleResponse: {
        type: 'object',
        properties: {
            success: { type: 'boolean' },
            data: { $ref: '#/components/schemas/CatModule' }
        }
    }
};
