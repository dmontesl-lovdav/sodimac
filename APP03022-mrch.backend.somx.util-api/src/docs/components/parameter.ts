// src/docs/components/parameter.ts

export const tags = [
    { name: 'Parameters', description: 'Gestion de parametros de configuracion del sistema' }
];

export const parameterSchemas = {
    CatParameter: {
        type: 'object',
        properties: {
            idParameter: { type: 'integer', description: 'ID unico del parametro' },
            idModule: { type: 'integer', description: 'ID del modulo al que pertenece' },
            idType: { type: 'integer', description: 'ID del tipo de parametro' },
            name: { type: 'string', maxLength: 50, description: 'Nombre tecnico del parametro' },
            description: { type: 'string', maxLength: 250, nullable: true, description: 'Descripcion funcional' },
            value: { type: 'string', maxLength: 150, description: 'Valor del parametro' },
            version: { type: 'number', description: 'Version del parametro (ej. 1.0, 1.1)' },
            startDate: { type: 'string', format: 'date-time', description: 'Fecha de inicio de vigencia' },
            endDate: { type: 'string', format: 'date-time', nullable: true, description: 'Fecha fin de vigencia' },
            status: { type: 'integer', enum: [0, 1], description: '1=Activo, 0=Inactivo' },
            createdBy: { type: 'integer', nullable: true, description: 'ID del usuario que creo el registro' },
            createdAt: { type: 'string', format: 'date-time', description: 'Fecha de creacion' },
            updatedBy: { type: 'integer', nullable: true, description: 'ID del usuario que actualizo' },
            updatedAt: { type: 'string', format: 'date-time', nullable: true, description: 'Fecha de actualizacion' }
        },
        required: ['idParameter', 'idModule', 'idType', 'name', 'value', 'version', 'status']
    },
    CreateParameterRequest: {
        type: 'object',
        properties: {
            idModule: { type: 'integer', description: 'ID del modulo' },
            idType: { type: 'integer', description: 'ID del tipo de parametro' },
            name: { type: 'string', maxLength: 50, description: 'Nombre del parametro' },
            description: { type: 'string', maxLength: 250, description: 'Descripcion funcional' },
            value: { type: 'string', maxLength: 150, description: 'Valor del parametro' },
            version: { type: 'number', default: 1.0, description: 'Version (default: 1.0)' },
            startDate: { type: 'string', format: 'date-time', description: 'Inicio de vigencia' },
            endDate: { type: 'string', format: 'date-time', description: 'Fin de vigencia' },
            status: { type: 'integer', default: 1, description: '1=Activo, 0=Inactivo' }
        },
        required: ['idModule', 'idType', 'name', 'value']
    },
    UpdateParameterRequest: {
        type: 'object',
        properties: {
            idModule: { type: 'integer' },
            idType: { type: 'integer' },
            name: { type: 'string', maxLength: 50 },
            description: { type: 'string', maxLength: 250 },
            value: { type: 'string', maxLength: 150 },
            version: { type: 'number' },
            startDate: { type: 'string', format: 'date-time' },
            endDate: { type: 'string', format: 'date-time' },
            status: { type: 'integer', enum: [0, 1] }
        }
    },
    ParameterListResponse: {
        type: 'object',
        properties: {
            success: { type: 'boolean' },
            data: {
                type: 'array',
                items: { $ref: '#/components/schemas/CatParameter' }
            },
            count: { type: 'integer' }
        }
    },
    ParameterResponse: {
        type: 'object',
        properties: {
            success: { type: 'boolean' },
            data: { $ref: '#/components/schemas/CatParameter' }
        }
    }
};
