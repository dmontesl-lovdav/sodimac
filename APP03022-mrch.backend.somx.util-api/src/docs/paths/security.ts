const searchParameters = [
    { name: 'startDate', in: 'query', required: true, schema: { type: 'string', format: 'date' } },
    { name: 'endDate', in: 'query', required: true, schema: { type: 'string', format: 'date' } },
    { name: 'entityId', in: 'query', schema: { type: 'string' } },
    { name: 'entityName', in: 'query', schema: { type: 'string' } },
    { name: 'status', in: 'query', schema: { type: 'integer', enum: [0, 1] } },
    {
        name: 'langId',
        in: 'query',
        required: false,
        schema: { type: 'integer', default: 1 },
        description: 'Idioma para resolver descripcion en shared_catalogs.dictionary_lang',
    },
];

const pathIdParameter = {
    name: 'id',
    in: 'path',
    required: true,
    schema: { type: 'integer' },
};

const langIdParameter = {
    name: 'langId',
    in: 'query',
    required: false,
    schema: { type: 'integer', default: 1 },
    description: 'Idioma para resolver descripcion en shared_catalogs.dictionary_lang',
};

const pathUserKeyParameter = {
    name: 'userKey',
    in: 'path',
    required: true,
    schema: { type: 'string' },
    description:
        'Identificador de usuario (core_security.user_data): preferred_username, sub, email o user_data_id como texto',
};

export const securityPaths = {
    '/security/profile-users': {
        get: {
            tags: ['Security'],
            summary: 'Consultar perfil usuario',
            parameters: searchParameters,
            responses: {
                200: {
                    description: 'Consulta de perfiles con total de usuarios asignados',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SecuritySearchResponse' } } },
                },
            },
        },
    },
    '/security/profiles/{id}/users': {
        get: {
            tags: ['Security'],
            summary: 'Obtener asignacion de usuarios por perfil',
            parameters: [pathIdParameter, langIdParameter],
            responses: {
                200: {
                    description: 'Usuarios disponibles y asignados',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SecurityAssignmentResponse' } } },
                },
            },
        },
        put: {
            tags: ['Security'],
            summary: 'Guardar asignacion de usuarios por perfil',
            parameters: [pathIdParameter, langIdParameter],
            requestBody: {
                required: true,
                content: { 'application/json': { schema: { $ref: '#/components/schemas/SaveAssignmentRequest' } } },
            },
            responses: {
                200: { description: 'Relacion actualizada correctamente' },
            },
        },
    },
    '/security/role-users': {
        get: {
            tags: ['Security'],
            summary: 'Consultar rol usuario',
            parameters: searchParameters,
            responses: {
                200: {
                    description: 'Consulta de roles con total de usuarios asignados',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SecuritySearchResponse' } } },
                },
            },
        },
    },
    '/security/roles/{id}/users': {
        get: {
            tags: ['Security'],
            summary: 'Obtener asignacion de usuarios por rol',
            parameters: [pathIdParameter],
            responses: {
                200: {
                    description: 'Usuarios disponibles y asignados por rol',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SecurityAssignmentResponse' } } },
                },
            },
        },
        put: {
            tags: ['Security'],
            summary: 'Guardar asignacion de usuarios por rol',
            parameters: [pathIdParameter],
            requestBody: {
                required: true,
                content: { 'application/json': { schema: { $ref: '#/components/schemas/SaveAssignmentRequest' } } },
            },
            responses: {
                200: { description: 'Relacion actualizada correctamente' },
            },
        },
    },
    '/security/role-permissions': {
        get: {
            tags: ['Security'],
            summary: 'Consultar rol permiso',
            parameters: searchParameters,
            responses: {
                200: {
                    description: 'Consulta de roles con total de permisos asignados',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SecuritySearchResponse' } } },
                },
            },
        },
    },
    '/security/roles/{id}/permissions': {
        get: {
            tags: ['Security'],
            summary: 'Obtener asignacion de permisos por rol',
            parameters: [pathIdParameter],
            responses: {
                200: {
                    description: 'Permisos disponibles y asignados por rol',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SecurityAssignmentResponse' } } },
                },
            },
        },
        put: {
            tags: ['Security'],
            summary: 'Guardar asignacion de permisos por rol',
            parameters: [pathIdParameter],
            requestBody: {
                required: true,
                content: { 'application/json': { schema: { $ref: '#/components/schemas/SaveAssignmentRequest' } } },
            },
            responses: {
                200: { description: 'Relacion actualizada correctamente' },
            },
        },
    },
    '/security/user-attributes': {
        get: {
            tags: ['Security'],
            summary: 'Consultar usuario atributo',
            parameters: searchParameters,
            responses: {
                200: {
                    description: 'Consulta de usuarios con total de atributos asignados',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SecuritySearchResponse' } } },
                },
            },
        },
    },
    '/security/users/{id}/attributes': {
        get: {
            tags: ['Security'],
            summary: 'Listar atributos de un usuario',
            parameters: [
                pathIdParameter,
                langIdParameter,
                { name: 'page', in: 'query', schema: { type: 'integer' } },
                { name: 'limit', in: 'query', schema: { type: 'integer' } },
            ],
            responses: {
                200: {
                    description: 'Atributos del usuario',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/UserAttributeListResponse' } } },
                },
            },
        },
        post: {
            tags: ['Security'],
            summary: 'Asignar atributo y valor a un usuario',
            parameters: [pathIdParameter, langIdParameter],
            requestBody: {
                required: true,
                content: { 'application/json': { schema: { $ref: '#/components/schemas/CreateUserAttributeRequest' } } },
            },
            responses: {
                201: { description: 'Atributo creado correctamente' },
            },
        },
    },
    '/security/users/{id}/attributes/{attributeId}': {
        delete: {
            tags: ['Security'],
            summary: 'Eliminar atributo de un usuario',
            parameters: [
                pathIdParameter,
                {
                    name: 'attributeId',
                    in: 'path',
                    required: true,
                    description: 'user_attribute_id del registro a eliminar',
                    schema: { type: 'integer' },
                },
            ],
            responses: {
                200: { description: 'Atributo eliminado correctamente' },
            },
        },
    },
    '/security/catalogs/attribute-types': {
        get: {
            tags: ['Security'],
            summary: 'Listar tipos de atributo activos',
            responses: {
                200: {
                    description: 'Catalogo de tipos de atributo',
                    content: {
                        'application/json': {
                            schema: {
                                type: 'array',
                                items: { $ref: '#/components/schemas/AttributeTypeOption' },
                            },
                        },
                    },
                },
            },
        },
    },
    '/security/catalogs/attribute-values': {
        get: {
            tags: ['Security'],
            summary: 'Listar valores de atributo por tipo',
            parameters: [
                {
                    name: 'attributeTypeId',
                    in: 'query',
                    required: true,
                    schema: { type: 'integer' },
                },
                langIdParameter,
            ],
            responses: {
                200: {
                    description: 'Catalogo de valores para el tipo de atributo',
                    content: {
                        'application/json': {
                            schema: {
                                type: 'object',
                                properties: {
                                    success: { type: 'boolean' },
                                    data: {
                                        type: 'object',
                                        properties: {
                                            items: {
                                                type: 'array',
                                                items: { $ref: '#/components/schemas/AttributeValueOption' },
                                            },
                                            warningCode: { type: 'string', nullable: true },
                                            warningMessage: { type: 'string', nullable: true },
                                        },
                                    },
                                },
                            },
                        },
                    },
                },
            },
        },
    },
    '/security/profile-modules': {
        get: {
            tags: ['Security'],
            summary: 'Consultar perfil aplicativo (perfil–modulo)',
            parameters: searchParameters,
            responses: {
                200: {
                    description: 'Perfiles con total de modulos asignados',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SecuritySearchResponse' } } },
                },
            },
        },
    },
    '/security/profiles/{id}/modules': {
        get: {
            tags: ['Security'],
            summary: 'Asignacion modulos por perfil',
            parameters: [pathIdParameter, langIdParameter],
            responses: {
                200: {
                    description: 'Modulos disponibles y asignados',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SecurityAssignmentResponse' } } },
                },
            },
        },
        put: {
            tags: ['Security'],
            summary: 'Guardar asignacion modulos por perfil',
            parameters: [pathIdParameter],
            requestBody: {
                required: true,
                content: { 'application/json': { schema: { $ref: '#/components/schemas/SaveAssignmentRequest' } } },
            },
            responses: { 200: { description: 'Actualizado' } },
        },
    },
    '/security/profile-module-processes': {
        get: {
            tags: ['Security'],
            summary: 'Consultar perfil evento (perfil–aplicativo evento)',
            parameters: searchParameters,
            responses: {
                200: {
                    description: 'Perfiles con total de pares modulo-proceso asignados',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SecuritySearchResponse' } } },
                },
            },
        },
    },
    '/security/profiles/{id}/module-processes': {
        get: {
            tags: ['Security'],
            summary: 'Asignacion aplicativo-evento por perfil',
            parameters: [pathIdParameter],
            responses: {
                200: {
                    description: 'Pares disponibles y asignados',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SecurityAssignmentResponse' } } },
                },
            },
        },
        put: {
            tags: ['Security'],
            summary: 'Guardar asignacion aplicativo-evento por perfil',
            parameters: [pathIdParameter],
            requestBody: {
                required: true,
                content: { 'application/json': { schema: { $ref: '#/components/schemas/SaveAssignmentRequest' } } },
            },
            responses: { 200: { description: 'Actualizado' } },
        },
    },
    '/security/application-events': {
        get: {
            tags: ['Security'],
            summary: 'Consultar aplicativo–evento (modulo con procesos)',
            parameters: searchParameters,
            responses: {
                200: {
                    description: 'Modulos con total de procesos vinculados',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SecuritySearchResponse' } } },
                },
            },
        },
    },
    '/security/user-details/{userKey}': {
        get: {
            tags: ['Security'],
            summary: 'Contexto de acceso versionado por usuario y perfil',
            parameters: [
                pathUserKeyParameter,
                {
                    name: 'idPerfil',
                    in: 'query',
                    required: false,
                    schema: { type: 'integer' },
                    description: 'Id del perfil a validar contra el usuario (opcional)',
                },
                langIdParameter,
            ],
            responses: {
                200: {
                    description: 'Contexto funcional consolidado (sin ids tecnicos)',
                    content: {
                        'application/json': { schema: { $ref: '#/components/schemas/SecurityUserDetailsApiEnvelope' } },
                    },
                },
                404: { description: 'Usuario no encontrado o inactivo' },
            },
        },
    },
    '/security/user-details/cache': {
        delete: {
            tags: ['Security'],
            summary: 'Invalidar cache de contexto de acceso',
            parameters: [
                {
                    name: 'userKey',
                    in: 'query',
                    required: false,
                    schema: { type: 'string' },
                    description: 'Si no se envia, limpia toda la cache',
                },
                {
                    name: 'idPerfil',
                    in: 'query',
                    required: false,
                    schema: { type: 'integer' },
                    description: 'Requerido cuando se envia userKey',
                },
                langIdParameter,
            ],
            responses: {
                200: { description: 'Cache invalidada' },
            },
        },
    },
    '/security/modules/{id}/processes': {
        get: {
            tags: ['Security'],
            summary: 'Procesos (eventos) por modulo aplicativo',
            parameters: [pathIdParameter, langIdParameter],
            responses: {
                200: {
                    description: 'Procesos disponibles y asignados al modulo',
                    content: { 'application/json': { schema: { $ref: '#/components/schemas/SecurityAssignmentResponse' } } },
                },
            },
        },
        put: {
            tags: ['Security'],
            summary: 'Guardar procesos vinculados al modulo',
            parameters: [pathIdParameter],
            requestBody: {
                required: true,
                content: { 'application/json': { schema: { $ref: '#/components/schemas/SaveAssignmentRequest' } } },
            },
            responses: { 200: { description: 'Actualizado' } },
        },
    },
};
