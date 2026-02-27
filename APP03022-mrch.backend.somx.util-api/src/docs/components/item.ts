// src/docs/components/item.ts

export const tags = [
    { name: 'Items', description: 'Gestion de elementos del sistema' }
];

export const itemSchemas = {
    CatItem: {
        type: 'object',
        properties: {
            idItem: { type: 'integer', description: 'ID unico del elemento' },
            name: { type: 'string', maxLength: 100, description: 'Nombre del elemento' },
            description: { type: 'string', maxLength: 250, nullable: true, description: 'Descripcion del elemento' },
            createdBy: { type: 'integer', nullable: true, description: 'ID del usuario que creo el registro' },
            createdAt: { type: 'string', format: 'date-time', description: 'Fecha de creacion' },
            updatedBy: { type: 'integer', nullable: true, description: 'ID del usuario que actualizo' },
            updatedAt: { type: 'string', format: 'date-time', nullable: true, description: 'Fecha de actualizacion' }
        },
        required: ['idItem', 'name']
    },
    CreateItemRequest: {
        type: 'object',
        properties: {
            name: { type: 'string', maxLength: 100, description: 'Nombre del elemento' },
            description: { type: 'string', maxLength: 250, description: 'Descripcion del elemento' }
        },
        required: ['name']
    },
    UpdateItemRequest: {
        type: 'object',
        properties: {
            name: { type: 'string', maxLength: 100 },
            description: { type: 'string', maxLength: 250 }
        }
    },
    ItemListResponse: {
        type: 'object',
        properties: {
            success: { type: 'boolean' },
            data: {
                type: 'array',
                items: { $ref: '#/components/schemas/CatItem' }
            },
            count: { type: 'integer' }
        }
    },
    ItemResponse: {
        type: 'object',
        properties: {
            success: { type: 'boolean' },
            data: { $ref: '#/components/schemas/CatItem' }
        }
    }
};
