// src/docs/components/message.ts

export const tags = [
    { name: 'Messages', description: 'Gestion de mensajes del sistema' }
];

export const messageSchemas = {
    CatMessage: {
        type: 'object',
        properties: {
            idMessage: { type: 'integer', description: 'ID unico del mensaje' },
            messageCode: { type: 'string', maxLength: 50, description: 'Codigo unico del mensaje' },
            idMessageType: { type: 'integer', description: 'Tipo de mensaje' },
            description: { type: 'string', maxLength: 500, nullable: true, description: 'Descripcion del mensaje' },
            createdBy: { type: 'integer', nullable: true, description: 'ID del usuario que creo el registro' },
            createdAt: { type: 'string', format: 'date-time', description: 'Fecha de creacion' },
            updatedBy: { type: 'integer', nullable: true, description: 'ID del usuario que actualizo' },
            updatedAt: { type: 'string', format: 'date-time', nullable: true, description: 'Fecha de actualizacion' }
        },
        required: ['idMessage', 'messageCode', 'idMessageType']
    },
    CreateMessageRequest: {
        type: 'object',
        properties: {
            messageCode: { type: 'string', maxLength: 50, description: 'Codigo unico del mensaje' },
            idMessageType: { type: 'integer', description: 'Tipo de mensaje' },
            description: { type: 'string', maxLength: 500, description: 'Descripcion del mensaje' }
        },
        required: ['messageCode', 'idMessageType']
    },
    UpdateMessageRequest: {
        type: 'object',
        properties: {
            messageCode: { type: 'string', maxLength: 50 },
            idMessageType: { type: 'integer' },
            description: { type: 'string', maxLength: 500 }
        }
    },
    MessageListResponse: {
        type: 'object',
        properties: {
            success: { type: 'boolean' },
            data: {
                type: 'array',
                items: { $ref: '#/components/schemas/CatMessage' }
            },
            count: { type: 'integer' }
        }
    },
    MessageResponse: {
        type: 'object',
        properties: {
            success: { type: 'boolean' },
            data: { $ref: '#/components/schemas/CatMessage' }
        }
    }
};
