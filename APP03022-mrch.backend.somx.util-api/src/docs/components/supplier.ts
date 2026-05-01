export const supplierSchemas = {
    SupplierTypeDto: {
        type: 'object',
        properties: {
            id: { type: 'integer' },
            code: { type: 'string' },
            description: { type: 'string' }
        }
    },
    PaymentConditionDto: {
        type: 'object',
        properties: {
            id: { type: 'integer' },
            conditionName: { type: 'string' },
            days: { type: 'integer' }
        }
    },
    SupplierDto: {
        type: 'object',
        properties: {
            id: { type: 'integer' },
            supplierNumber: { type: 'string' },
            rfc: { type: 'string' },
            businessName: { type: 'string' },
            supplierType: { $ref: '#/components/schemas/SupplierTypeDto', nullable: true },
            logo: { type: 'string', nullable: true },
            paymentCondition: { $ref: '#/components/schemas/PaymentConditionDto', nullable: true },
            status: { type: 'integer' }
        }
    },
    SupplierCreateDto: {
        type: 'object',
        required: ['supplierNumber', 'rfc', 'businessName'],
        properties: {
            supplierNumber: { type: 'string', example: 'PROV001' },
            rfc: { type: 'string', example: 'ABC123456789' },
            businessName: { type: 'string' },
            supplierTypeId: { type: 'integer', nullable: true },
            logo: { type: 'string', nullable: true },
            paymentConditionId: { type: 'integer', nullable: true }
        }
    },
    SupplierUpdateDto: {
        type: 'object',
        properties: {
            rfc: { type: 'string', nullable: true },
            businessName: { type: 'string', nullable: true },
            supplierTypeId: { type: 'integer', nullable: true },
            logo: { type: 'string', nullable: true },
            paymentConditionId: { type: 'integer', nullable: true },
            status: { type: 'integer', nullable: true }
        }
    },
    SupplierBlockInfoDto: {
        type: 'object',
        properties: {
            validFrom: { type: 'string', format: 'date' },
            validTo: { type: 'string', format: 'date' },
            blockReason: { type: 'string', nullable: true }
        }
    },
    SupplierFilterDto: {
        type: 'object',
        properties: {
            id: { type: 'integer' },
            supplierNumber: { type: 'string' },
            rfc: { type: 'string' },
            businessName: { type: 'string' },
            supplierType: { $ref: '#/components/schemas/SupplierTypeDto', nullable: true },
            paymentCondition: { $ref: '#/components/schemas/PaymentConditionDto', nullable: true },
            status: { type: 'integer' },
            blocked: { type: 'boolean' },
            blockInfo: { $ref: '#/components/schemas/SupplierBlockInfoDto', nullable: true }
        }
    },
    SupplierBlockDto: {
        type: 'object',
        properties: {
            id: { type: 'integer' },
            supplierNumber: { type: 'string' },
            validFrom: { type: 'string', format: 'date' },
            validTo: { type: 'string', format: 'date' },
            blockReason: { type: 'string', nullable: true },
            status: { type: 'integer' },
            currentlyBlocked: { type: 'boolean' },
            createdAt: { type: 'string', format: 'date-time', nullable: true },
            createdBy: { type: 'string', nullable: true },
            updatedAt: { type: 'string', format: 'date-time', nullable: true },
            updatedBy: { type: 'string', nullable: true }
        }
    },
    SupplierBlockResponseDto: {
        type: 'object',
        properties: {
            message: { type: 'string' },
            data: { $ref: '#/components/schemas/SupplierBlockDto' }
        }
    },
    SupplierBlockCreateDto: {
        type: 'object',
        required: ['supplierNumber', 'validFrom', 'validTo'],
        properties: {
            supplierNumber: { type: 'string' },
            validFrom: { type: 'string', format: 'date' },
            validTo: { type: 'string', format: 'date' },
            blockReason: { type: 'string', nullable: true }
        }
    },
    SupplierBlockUpdateDto: {
        type: 'object',
        properties: {
            validFrom: { type: 'string', format: 'date', nullable: true },
            validTo: { type: 'string', format: 'date', nullable: true },
            blockReason: { type: 'string', nullable: true },
            status: { type: 'integer', nullable: true }
        }
    },
    ConversionDto: {
        type: 'object',
        properties: {
            idConversion: { type: 'integer' },
            idElementoOrigen: { type: 'integer', nullable: true },
            elementoOrigen: { type: 'string', nullable: true },
            valorElementoOrigen: { type: 'string', nullable: true },
            estatusElementoOrigen: { type: 'string', nullable: true },
            catalogoElementoOrigen: { type: 'string', nullable: true },
            idElemento: { type: 'integer', nullable: true },
            elemento: { type: 'string', nullable: true },
            valor: { type: 'string', nullable: true },
            catalogoOrigen: { type: 'string', nullable: true },
            fechaInicioVigencia: { type: 'string', format: 'date', nullable: true },
            fechaFinVigencia: { type: 'string', format: 'date', nullable: true },
            estatus: { type: 'string', nullable: true },
            esPrincipal: { type: 'boolean', nullable: true },
            idUsuarioRegistro: { type: 'string', nullable: true },
            fechaRegistro: { type: 'string', format: 'date-time', nullable: true },
            idUsuarioActualizacion: { type: 'string', nullable: true },
            fechaActualizacion: { type: 'string', format: 'date-time', nullable: true }
        }
    },
    ConversionCreateDto: {
        type: 'object',
        required: ['sourceElementId', 'targetElementId'],
        properties: {
            sourceElementId: { type: 'integer' },
            targetElementId: { type: 'integer' },
            validFrom: { type: 'string', format: 'date', nullable: true },
            validTo: { type: 'string', format: 'date', nullable: true },
            isPrincipal: { type: 'boolean', nullable: true }
        }
    },
    ConversionUpdateDto: {
        type: 'object',
        required: ['targetElementId'],
        properties: {
            targetElementId: { type: 'integer' },
            validFrom: { type: 'string', format: 'date', nullable: true },
            validTo: { type: 'string', format: 'date', nullable: true },
            status: { type: 'integer', nullable: true }
        }
    },
    ConversionPageResponse: {
        type: 'object',
        properties: {
            items: { type: 'array', items: { $ref: '#/components/schemas/ConversionDto' } },
            page: { type: 'integer' },
            pageSize: { type: 'integer' },
            total: { type: 'integer' },
            totalPages: { type: 'integer' }
        }
    },
    StatusTrainDto: {
        type: 'object',
        properties: {
            id: { type: 'integer' },
            optionId: { type: 'integer' },
            sourceStatus: { type: 'integer' },
            targetStatus: { type: 'integer' },
            createdBy: { type: 'integer', nullable: true },
            createdAt: { type: 'string', format: 'date-time', nullable: true },
            updatedBy: { type: 'integer', nullable: true },
            updatedAt: { type: 'string', format: 'date-time', nullable: true }
        }
    },
    StatusTrainCreateDto: {
        type: 'object',
        required: ['optionId', 'sourceStatus', 'targetStatus', 'createdBy'],
        properties: {
            optionId: { type: 'integer' },
            sourceStatus: { type: 'integer' },
            targetStatus: { type: 'integer' },
            createdBy: { type: 'integer' }
        }
    },
    StatusTrainUpdateDto: {
        type: 'object',
        required: ['sourceStatus', 'targetStatus', 'updatedBy'],
        properties: {
            sourceStatus: { type: 'integer' },
            targetStatus: { type: 'integer' },
            updatedBy: { type: 'integer' }
        }
    },
    StatusTrainValidationResponse: {
        type: 'object',
        properties: {
            success: { type: 'boolean' },
            valid: { type: 'boolean' },
            code: { type: 'string', nullable: true },
            message: { type: 'string', nullable: true },
            data: { $ref: '#/components/schemas/StatusTrainDto' }
        }
    },
    StatusTrainListResponse: {
        type: 'object',
        properties: {
            success: { type: 'boolean' },
            code: { type: 'string', nullable: true },
            message: { type: 'string', nullable: true },
            count: { type: 'integer', nullable: true },
            data: { type: 'array', items: { $ref: '#/components/schemas/StatusTrainDto' } }
        }
    }
};

