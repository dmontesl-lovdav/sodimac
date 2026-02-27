// src/docs/components/itemType.ts

export const tags = [
    { name: 'ItemTypes', description: 'Gestion de tipos de elemento' }
];

export const itemTypeSchemas = {
    CatItemType: {
        type: 'object',
        properties: {
            idItemType: { type: 'integer', description: 'ID unico del tipo de elemento' },
            name: { type: 'string', maxLength: 100, description: 'Nombre del tipo de elemento' },
            description: { type: 'string', maxLength: 250, nullable: true, description: 'Descripcion del tipo de elemento' },
            createdBy: { type: 'integer', nullable: true, description: 'ID del usuario que creo el registro' },
            createdAt: { type: 'string', format: 'date-time', description: 'Fecha de creacion' },
            updatedBy: { type: 'integer', nullable: true, description: 'ID del usuario que actualizo' },
            updatedAt: { type: 'string', format: 'date-time', nullable: true, description: 'Fecha de actualizacion' }
        },
        required: ['idItemType', 'name']
    },
    CreateItemTypeRequest: {
        type: 'object',
        properties: {
            name: { type: 'string', maxLength: 100, description: 'Nombre del tipo de elemento' },
            description: { type: 'string', maxLength: 250, description: 'Descripcion del tipo de elemento' }
        },
        required: ['name']
    },
    UpdateItemTypeRequest: {
        type: 'object',
        properties: {
            name: { type: 'string', maxLength: 100 },
            description: { type: 'string', maxLength: 250 }
        }
    },
    ItemTypeListResponse: {
        type: 'object',
        properties: {
            success: { type: 'boolean' },
            data: {
                type: 'array',
                items: { $ref: '#/components/schemas/CatItemType' }
            },
            count: { type: 'integer' }
        }
    },
    ItemTypeResponse: {
        type: 'object',
        properties: {
            success: { type: 'boolean' },
            data: { $ref: '#/components/schemas/CatItemType' }
        }
    }
};
