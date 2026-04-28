export const tags = [
    { name: 'Security', description: 'Administracion de perfiles, roles, permisos y atributos' },
];

export const securitySchemas = {
    SecuritySummaryRow: {
        type: 'object',
        properties: {
            id: { type: 'integer' },
            catalogKey: {
                type: 'string',
                description: 'Clave logica catalog_detail.key (ej. PERM_PAGO_APROBAR)',
            },
            name: { type: 'string' },
            description: { type: 'string' },
            status: { type: 'integer', enum: [0, 1] },
            totalAssigned: { type: 'integer' },
            updatedAt: { type: 'string', format: 'date-time', nullable: true },
        },
    },
    SecuritySearchResponse: {
        type: 'object',
        properties: {
            items: {
                type: 'array',
                items: { $ref: '#/components/schemas/SecuritySummaryRow' },
            },
            warningCode: { type: 'string', nullable: true },
            warningMessage: { type: 'string', nullable: true },
        },
    },
    AssignableItem: {
        type: 'object',
        properties: {
            id: { type: 'integer' },
            title: { type: 'string' },
            subtitle: { type: 'string', nullable: true },
            tags: {
                type: 'array',
                nullable: true,
                items: { type: 'string' },
            },
        },
    },
    SecurityAssignmentResponse: {
        type: 'object',
        properties: {
            available: {
                type: 'array',
                items: { $ref: '#/components/schemas/AssignableItem' },
            },
            assigned: {
                type: 'array',
                items: { $ref: '#/components/schemas/AssignableItem' },
            },
        },
    },
    SaveAssignmentRequest: {
        type: 'object',
        required: ['selectedIds'],
        properties: {
            selectedIds: {
                type: 'array',
                items: { type: 'integer' },
            },
        },
    },
    UserAttribute: {
        type: 'object',
        description: 'Detalle de atributos asignados. Cada item representa un registro activo de core_security.user_attribute.',
        properties: {
            id: { type: 'integer', description: 'user_attribute_id (identificador unico por fila)' },
            userId: { type: 'integer', description: 'user_data_id' },
            name: { type: 'string' },
            attributeTypeId: { type: 'integer' },
            attributeTypeName: { type: 'string' },
            attributeValueId: {
                type: 'integer',
                nullable: true,
                description: 'catalog_detail.id del valor concreto (ej. TPR001)',
            },
            attributeValueName: { type: 'string', nullable: true },
            attributeValueKey: { type: 'string', nullable: true },
            status: { type: 'integer', enum: [0, 1] },
            createdBy: { type: 'string' },
            createdAt: { type: 'string', format: 'date-time' },
            updatedBy: { type: 'string', nullable: true },
            updatedAt: { type: 'string', format: 'date-time', nullable: true },
        },
    },
    UserAttributeListResponse: {
        type: 'object',
        properties: {
            items: {
                type: 'array',
                items: { $ref: '#/components/schemas/UserAttribute' },
            },
            total: { type: 'integer' },
        },
    },
    CreateUserAttributeRequest: {
        type: 'object',
        required: ['attributeTypeId', 'attributeValueId'],
        properties: {
            attributeTypeId: { type: 'integer' },
            attributeValueId: {
                type: 'integer',
                description: 'catalog_detail.id del valor concreto del atributo (ej. TPR001)',
            },
        },
    },
    AttributeTypeOption: {
        type: 'object',
        properties: {
            id: { type: 'integer' },
            name: { type: 'string' },
        },
    },
    AttributeValueOption: {
        type: 'object',
        properties: {
            id: { type: 'integer' },
            catalogKey: { type: 'string' },
            name: { type: 'string' },
        },
    },
    SecurityUserRef: {
        type: 'object',
        description: 'Usuario en core_security.user_data',
        properties: {
            id: { type: 'integer', description: 'user_data_id' },
            sub: { type: 'string' },
            preferredUsername: { type: 'string', nullable: true },
            givenName: { type: 'string', nullable: true },
            familyName: { type: 'string', nullable: true },
            email: { type: 'string', nullable: true },
            status: { type: 'integer', enum: [0, 1] },
        },
    },
    SecurityCatalogRef: {
        type: 'object',
        description: 'Referencia a shared_catalogs.catalog_detail',
        properties: {
            id: { type: 'integer' },
            catalogKey: { type: 'string', description: 'Columna catalog_detail.key' },
            dictId: { type: 'integer' },
            label: { type: 'string', description: 'COALESCE(value, key) para presentacion' },
            status: { type: 'integer', enum: [0, 1] },
        },
    },
    SecurityUserDetailAttribute: {
        type: 'object',
        properties: {
            idUserAttribute: { type: 'integer' },
            attributeType: { $ref: '#/components/schemas/SecurityCatalogRef' },
            attributeValue: {
                allOf: [{ $ref: '#/components/schemas/SecurityCatalogRef' }],
                nullable: true,
            },
            status: { type: 'integer', enum: [0, 1] },
        },
    },
    SecurityApplicationModuleProcessRow: {
        type: 'object',
        properties: {
            idModuleProcess: { type: 'integer' },
            module: { $ref: '#/components/schemas/SecurityCatalogRef' },
            process: { $ref: '#/components/schemas/SecurityCatalogRef' },
            profile: { $ref: '#/components/schemas/SecurityCatalogRef' },
        },
    },
    SecurityPermissionViaRole: {
        type: 'object',
        properties: {
            permission: { $ref: '#/components/schemas/SecurityCatalogRef' },
            role: { $ref: '#/components/schemas/SecurityCatalogRef' },
        },
    },
    SecurityProviderViaRole: {
        type: 'object',
        properties: {
            provider: { $ref: '#/components/schemas/SecurityCatalogRef' },
            role: { $ref: '#/components/schemas/SecurityCatalogRef' },
        },
    },
    SecurityUserDetailsResponse: {
        type: 'object',
        description: 'Contexto de acceso funcional consolidado para frontend (AUTH-010)',
        properties: {
            version: { type: 'string', example: 'v1' },
            usuario: {
                type: 'object',
                properties: {
                    clave: { type: 'string' },
                },
            },
            perfiles: {
                type: 'array',
                items: {
                    type: 'object',
                    properties: {
                        clave: { type: 'string' },
                        nombre: { type: 'string' },
                    },
                },
            },
            aplicativos: {
                type: 'array',
                items: { $ref: '#/components/schemas/SecurityUserDetailApplication' },
            },
            roles: {
                type: 'array',
                items: {
                    type: 'object',
                    properties: {
                        clave: { type: 'string' },
                        nombre: { type: 'string' },
                    },
                },
            },
            permisos: {
                type: 'array',
                items: {
                    type: 'object',
                    properties: {
                        clave: { type: 'string' },
                        nombre: { type: 'string' },
                        rol: {
                            type: 'object',
                            properties: {
                                clave: { type: 'string' },
                                nombre: { type: 'string' },
                            },
                        },
                    },
                },
            },
            proveedores: {
                type: 'array',
                items: {
                    type: 'object',
                    properties: {
                        clave: { type: 'string' },
                        nombre: { type: 'string' },
                        rol: {
                            type: 'object',
                            properties: {
                                clave: { type: 'string' },
                                nombre: { type: 'string' },
                            },
                        },
                    },
                },
            },
            atributos: {
                type: 'array',
                items: {
                    type: 'object',
                    properties: {
                        tipo: {
                            type: 'object',
                            properties: {
                                clave: { type: 'string' },
                                nombre: { type: 'string' },
                            },
                        },
                        valor: {
                            nullable: true,
                            type: 'object',
                            properties: {
                                clave: { type: 'string' },
                                nombre: { type: 'string' },
                            },
                        },
                    },
                },
            },
        },
    },
    SecurityUserDetailApplication: {
        type: 'object',
        properties: {
            clave: { type: 'string' },
            nombre: { type: 'string' },
            eventos: {
                type: 'array',
                items: {
                    type: 'object',
                    properties: {
                        clave: { type: 'string' },
                        nombre: { type: 'string' },
                    },
                },
            },
        },
    },
    SecurityUserDetailsApiEnvelope: {
        type: 'object',
        properties: {
            success: { type: 'boolean' },
            data: { $ref: '#/components/schemas/SecurityUserDetailsResponse' },
        },
    },
};
