const userIdHeader = {
    name: 'X-User-Id',
    in: 'header',
    schema: { type: 'string', default: 'system' }
};

export const supplierPaths = {
    '/suppliers': {
        get: {
            tags: ['Proveedores'],
            summary: 'Obtiene todos los proveedores',
            parameters: [{ name: 'status', in: 'query', schema: { type: 'integer' } }],
            responses: {
                200: {
                    description: 'Lista de proveedores',
                    content: {
                        'application/json': {
                            schema: { type: 'array', items: { $ref: '#/components/schemas/SupplierDto' } }
                        }
                    }
                }
            }
        },
        post: {
            tags: ['Proveedores'],
            summary: 'Crea un nuevo proveedor',
            parameters: [userIdHeader],
            requestBody: {
                required: true,
                content: { 'application/json': { schema: { $ref: '#/components/schemas/SupplierCreateDto' } } }
            },
            responses: {
                201: {
                    description: 'Proveedor creado',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SupplierDto' } } }
                }
            }
        }
    },
    '/suppliers/{id}': {
        get: {
            tags: ['Proveedores'],
            summary: 'Obtiene un proveedor por ID',
            parameters: [{ name: 'id', in: 'path', required: true, schema: { type: 'integer' } }],
            responses: {
                200: {
                    description: 'Proveedor encontrado',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SupplierDto' } } }
                },
                404: { description: 'No encontrado' }
            }
        },
        put: {
            tags: ['Proveedores'],
            summary: 'Actualiza un proveedor',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' } },
                userIdHeader
            ],
            requestBody: {
                required: true,
                content: { 'application/json': { schema: { $ref: '#/components/schemas/SupplierUpdateDto' } } }
            },
            responses: {
                200: {
                    description: 'Actualizado',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SupplierDto' } } }
                }
            }
        },
        delete: {
            tags: ['Proveedores'],
            summary: 'Elimina (desactiva) un proveedor',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' } },
                userIdHeader
            ],
            responses: {
                204: { description: 'Eliminado' },
                404: { description: 'No encontrado' }
            }
        }
    },
    '/suppliers/number/{supplierNumber}': {
        get: {
            tags: ['Proveedores'],
            summary: 'Obtiene un proveedor por número',
            parameters: [{ name: 'supplierNumber', in: 'path', required: true, schema: { type: 'string' } }],
            responses: {
                200: {
                    description: 'Proveedor encontrado',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SupplierDto' } } }
                },
                404: { description: 'No encontrado' }
            }
        }
    },
    '/suppliers/rfc/{rfc}': {
        get: {
            tags: ['Proveedores'],
            summary: 'Obtiene un proveedor por RFC',
            parameters: [{ name: 'rfc', in: 'path', required: true, schema: { type: 'string' } }],
            responses: {
                200: {
                    description: 'Proveedor encontrado',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SupplierDto' } } }
                },
                404: { description: 'No encontrado' }
            }
        }
    },
    '/suppliers/types': {
        get: {
            tags: ['Proveedores'],
            summary: 'Obtiene todos los tipos de proveedor',
            responses: {
                200: {
                    description: 'Lista',
                    content: {
                        'application/json': {
                            schema: { type: 'array', items: { $ref: '#/components/schemas/SupplierTypeDto' } }
                        }
                    }
                }
            }
        }
    },
    '/suppliers/payment-conditions': {
        get: {
            tags: ['Proveedores'],
            summary: 'Obtiene todas las condiciones de pago',
            responses: {
                200: {
                    description: 'Lista',
                    content: {
                        'application/json': {
                            schema: { type: 'array', items: { $ref: '#/components/schemas/PaymentConditionDto' } }
                        }
                    }
                }
            }
        }
    },
    '/suppliers/filter': {
        get: {
            tags: ['Proveedores'],
            summary: 'STM-831: Filtra proveedores por tipo y estado de bloqueo',
            parameters: [
                { name: 'tipoProveedor', in: 'query', required: true, schema: { type: 'integer' } },
                { name: 'estatusBloqueo', in: 'query', required: true, schema: { type: 'integer' } }
            ],
            responses: {
                200: {
                    description: 'Lista filtrada',
                    content: {
                        'application/json': {
                            schema: { type: 'array', items: { $ref: '#/components/schemas/SupplierFilterDto' } }
                        }
                    }
                }
            }
        }
    },
    '/supplier-blocks': {
        get: {
            tags: ['Bloqueos de Proveedores'],
            summary: 'Obtiene todos los bloqueos',
            parameters: [{ name: 'status', in: 'query', schema: { type: 'integer' } }],
            responses: {
                200: {
                    description: 'Lista',
                    content: {
                        'application/json': {
                            schema: { type: 'array', items: { $ref: '#/components/schemas/SupplierBlockDto' } }
                        }
                    }
                }
            }
        },
        post: {
            tags: ['Bloqueos de Proveedores'],
            summary: 'Crea un nuevo bloqueo',
            parameters: [userIdHeader],
            requestBody: {
                required: true,
                content: { 'application/json': { schema: { $ref: '#/components/schemas/SupplierBlockCreateDto' } } }
            },
            responses: {
                201: {
                    description: 'Bloqueo creado',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SupplierBlockResponseDto' } } }
                }
            }
        }
    },
    '/supplier-blocks/{id}': {
        get: {
            tags: ['Bloqueos de Proveedores'],
            summary: 'Obtiene un bloqueo por ID',
            parameters: [{ name: 'id', in: 'path', required: true, schema: { type: 'integer' } }],
            responses: {
                200: {
                    description: 'Bloqueo encontrado',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SupplierBlockDto' } } }
                },
                404: { description: 'No encontrado' }
            }
        },
        put: {
            tags: ['Bloqueos de Proveedores'],
            summary: 'Actualiza un bloqueo',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' } },
                userIdHeader
            ],
            requestBody: {
                required: true,
                content: { 'application/json': { schema: { $ref: '#/components/schemas/SupplierBlockUpdateDto' } } }
            },
            responses: {
                200: {
                    description: 'Actualizado',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SupplierBlockResponseDto' } } }
                }
            }
        },
        delete: {
            tags: ['Bloqueos de Proveedores'],
            summary: 'Elimina (desactiva) un bloqueo',
            parameters: [
                { name: 'id', in: 'path', required: true, schema: { type: 'integer' } },
                userIdHeader
            ],
            responses: { 204: { description: 'Eliminado' }, 404: { description: 'No encontrado' } }
        }
    },
    '/supplier-blocks/supplier/{supplierNumber}': {
        get: {
            tags: ['Bloqueos de Proveedores'],
            summary: 'Bloqueos del proveedor',
            parameters: [{ name: 'supplierNumber', in: 'path', required: true, schema: { type: 'string' } }],
            responses: {
                200: {
                    description: 'Lista',
                    content: {
                        'application/json': {
                            schema: { type: 'array', items: { $ref: '#/components/schemas/SupplierBlockDto' } }
                        }
                    }
                }
            }
        }
    },
    '/supplier-blocks/supplier/{supplierNumber}/active': {
        get: {
            tags: ['Bloqueos de Proveedores'],
            summary: 'Bloqueos activos vigentes del proveedor',
            parameters: [{ name: 'supplierNumber', in: 'path', required: true, schema: { type: 'string' } }],
            responses: {
                200: {
                    description: 'Lista',
                    content: {
                        'application/json': {
                            schema: { type: 'array', items: { $ref: '#/components/schemas/SupplierBlockDto' } }
                        }
                    }
                }
            }
        }
    },
    '/supplier-blocks/supplier/{supplierNumber}/is-blocked': {
        get: {
            tags: ['Bloqueos de Proveedores'],
            summary: '¿El proveedor está bloqueado?',
            parameters: [{ name: 'supplierNumber', in: 'path', required: true, schema: { type: 'string' } }],
            responses: {
                200: {
                    description: 'Estado',
                    content: {
                        'application/json': {
                            schema: {
                                type: 'object',
                                properties: {
                                    supplierNumber: { type: 'string' },
                                    blocked: { type: 'boolean' }
                                }
                            }
                        }
                    }
                }
            }
        }
    },
    '/supplier-blocks/supplier/{supplierNumber}/at-date': {
        get: {
            tags: ['Bloqueos de Proveedores'],
            summary: 'Bloqueos del proveedor a una fecha específica',
            parameters: [
                { name: 'supplierNumber', in: 'path', required: true, schema: { type: 'string' } },
                { name: 'date', in: 'query', required: true, schema: { type: 'string', format: 'date' } }
            ],
            responses: {
                200: {
                    description: 'Lista',
                    content: {
                        'application/json': {
                            schema: { type: 'array', items: { $ref: '#/components/schemas/SupplierBlockDto' } }
                        }
                    }
                }
            }
        }
    }
};

