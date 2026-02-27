// src/docs/components/applicationMsg.ts

export const tags = [
    { name: 'ApplicationMessages', description: 'Gestion de mensajes por aplicativo' }
];

export const applicationMsgSchemas = {
    ApplicationMsg: {
        type: 'object',
        properties: {
            idApplicationMsg: { type: 'integer', description: 'ID unico de la relacion' },
            idMessage: { type: 'integer', description: 'ID del mensaje' },
            idApplication: { type: 'integer', description: 'ID del aplicativo' },
            createdBy: { type: 'integer', nullable: true, description: 'ID del usuario que creo el registro' },
            createdAt: { type: 'string', format: 'date-time', description: 'Fecha de creacion' }
        },
        required: ['idApplicationMsg', 'idMessage', 'idApplication']
    },
    CreateApplicationMsgRequest: {
        type: 'object',
        properties: {
            idMessage: { type: 'integer', description: 'ID del mensaje' },
            idApplication: { type: 'integer', description: 'ID del aplicativo' }
        },
        required: ['idMessage', 'idApplication']
    },
    ApplicationMsgListResponse: {
        type: 'object',
        properties: {
            success: { type: 'boolean' },
            data: {
                type: 'array',
                items: { $ref: '#/components/schemas/ApplicationMsg' }
            },
            count: { type: 'integer' }
        }
    },
    ApplicationMsgResponse: {
        type: 'object',
        properties: {
            success: { type: 'boolean' },
            data: { $ref: '#/components/schemas/ApplicationMsg' }
        }
    }
};
