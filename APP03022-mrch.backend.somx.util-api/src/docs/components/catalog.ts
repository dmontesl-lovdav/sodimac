export const catalogSchemas = {
    CatalogDetailDto: {
        type: 'object',
        properties: {
            key: { type: 'string', example: 'EFA001' },
            internalStatus: { type: 'integer', example: 1, nullable: true },
            externalKey: { type: 'string', example: 'G01', nullable: true },
            value: { type: 'string', example: '0.160000', nullable: true },
            description: { type: 'string', example: 'Factura creada exitosamente', nullable: true },
            color: { type: 'string', example: '#28a745', nullable: true },
            sortOrder: { type: 'integer', example: 1, nullable: true },
            validFrom: { type: 'string', format: 'date', nullable: true },
            validTo: { type: 'string', format: 'date', nullable: true },
            attributes: { type: 'object', nullable: true }
        }
    },
    CatalogHeaderDto: {
        type: 'object',
        properties: {
            code: { type: 'string', example: 'CatEstatusFactura' },
            prefix: { type: 'string', example: 'EFA' },
            name: { type: 'string', example: 'Estatus de Factura' },
            description: { type: 'string', nullable: true },
            module: { type: 'string', nullable: true },
            catalogType: { type: 'string', example: 'SIMPLE' },
            details: {
                type: 'array',
                items: { $ref: '#/components/schemas/CatalogDetailDto' }
            }
        }
    },
    CatalogSimpleDto: {
        type: 'object',
        properties: {
            id: { type: 'integer', example: 1 },
            code: { type: 'string', example: 'CAT_MODULOS' },
            name: { type: 'string', example: 'Catálogo de Módulos' },
            description: { type: 'string', nullable: true },
            catalogType: { type: 'string', example: 'PRIMARIO' },
            status: { type: 'integer', example: 1 }
        }
    },
    CatalogResponseDto: {
        type: 'object',
        properties: {
            id: { type: 'integer' },
            code: { type: 'string' },
            prefix: { type: 'string' },
            name: { type: 'string' },
            description: { type: 'string', nullable: true },
            module: { type: 'string', nullable: true },
            catalogType: { type: 'string' },
            status: { type: 'integer' },
            createdBy: { type: 'string', nullable: true },
            createdAt: { type: 'string', format: 'date-time', nullable: true },
            updatedBy: { type: 'string', nullable: true },
            updatedAt: { type: 'string', format: 'date-time', nullable: true },
            elementCount: { type: 'integer' }
        }
    },
    CatalogCreateDto: {
        type: 'object',
        required: ['name', 'catalogType'],
        properties: {
            code: { type: 'string', example: 'CAT_PAISES' },
            prefix: { type: 'string', example: 'PAI' },
            name: { type: 'string', example: 'Catálogo de Países' },
            description: { type: 'string', nullable: true },
            catalogType: { type: 'string', example: 'PRIMARIO' },
            module: { type: 'string', example: 'general' }
        }
    },
    CatalogUpdateDto: {
        type: 'object',
        properties: {
            name: { type: 'string' },
            description: { type: 'string', nullable: true },
            catalogType: { type: 'string' },
            status: { type: 'integer' },
            module: { type: 'string' }
        }
    },
    CatalogElementDto: {
        type: 'object',
        properties: {
            id: { type: 'integer' },
            catalogId: { type: 'integer', nullable: true },
            catalogCode: { type: 'string', nullable: true },
            element: { type: 'string', nullable: true },
            value: { type: 'string', nullable: true },
            key: { type: 'string', nullable: true },
            validFrom: { type: 'string', format: 'date', nullable: true },
            validTo: { type: 'string', format: 'date', nullable: true },
            status: { type: 'integer', nullable: true },
            statusDescription: { type: 'string', nullable: true },
            parentCatalogId: { type: 'integer', nullable: true },
            parentCatalogName: { type: 'string', nullable: true },
            parentElementId: { type: 'integer', nullable: true },
            parentElementName: { type: 'string', nullable: true },
            createdBy: { type: 'string', nullable: true },
            createdAt: { type: 'string', format: 'date-time', nullable: true },
            updatedBy: { type: 'string', nullable: true },
            updatedAt: { type: 'string', format: 'date-time', nullable: true },
            externalKey: { type: 'string', nullable: true },
            sortOrder: { type: 'integer', nullable: true },
            attributes: { type: 'object', nullable: true }
        }
    },
    CatalogElementCreateDto: {
        type: 'object',
        required: ['element', 'validFrom'],
        properties: {
            element: { type: 'string', example: 'Motivo K' },
            value: { type: 'string', nullable: true },
            validFrom: { type: 'string', format: 'date' },
            validTo: { type: 'string', format: 'date', nullable: true },
            parentCatalogId: { type: 'integer', nullable: true },
            parentElementId: { type: 'integer', nullable: true },
            externalKey: { type: 'string', nullable: true },
            sortOrder: { type: 'integer', nullable: true },
            attributes: { type: 'object', nullable: true }
        }
    },
    CatalogElementUpdateDto: {
        type: 'object',
        properties: {
            element: { type: 'string', nullable: true },
            value: { type: 'string', nullable: true },
            validFrom: { type: 'string', format: 'date', nullable: true },
            validTo: { type: 'string', format: 'date', nullable: true },
            parentCatalogId: { type: 'integer', nullable: true },
            parentElementId: { type: 'integer', nullable: true },
            externalKey: { type: 'string', nullable: true },
            sortOrder: { type: 'integer', nullable: true },
            attributes: { type: 'object', nullable: true },
            status: { type: 'integer', nullable: true }
        }
    },
    CatalogElementStatusDto: {
        type: 'object',
        required: ['status'],
        properties: {
            status: { type: 'integer', example: 1 }
        }
    },
    CatalogElementPageResponse: {
        type: 'object',
        properties: {
            items: { type: 'array', items: { $ref: '#/components/schemas/CatalogElementDto' } },
            page: { type: 'integer' },
            pageSize: { type: 'integer' },
            total: { type: 'integer' },
            totalPages: { type: 'integer' },
            hasNext: { type: 'boolean' },
            hasPrevious: { type: 'boolean' }
        }
    },
    CatalogPageResponse: {
        type: 'object',
        properties: {
            items: { type: 'array', items: { $ref: '#/components/schemas/CatalogResponseDto' } },
            page: { type: 'integer' },
            pageSize: { type: 'integer' },
            total: { type: 'integer' },
            totalPages: { type: 'integer' },
            hasNext: { type: 'boolean' },
            hasPrevious: { type: 'boolean' }
        }
    },
    LayoutValidationError: {
        type: 'object',
        properties: {
            row: { type: 'integer' },
            cell: { type: 'string' },
            column: { type: 'string' },
            message: { type: 'string' }
        }
    },
    LayoutValidationResponse: {
        type: 'object',
        properties: {
            isValid: { type: 'boolean' },
            errorCount: { type: 'integer' },
            errors: { type: 'array', items: { $ref: '#/components/schemas/LayoutValidationError' } },
            reportAvailable: { type: 'boolean' },
            reportId: { type: 'string', nullable: true },
            rowsProcessed: { type: 'integer' }
        }
    }
};

