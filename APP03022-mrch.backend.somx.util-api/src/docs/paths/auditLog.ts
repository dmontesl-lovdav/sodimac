export const auditLogPaths = {
    '/audit-logs/applications': {
        get: {
            tags: ['Audit Logs'],
            summary: 'Listar aplicativos registrados en bitacora',
            description:
                'Obtiene los service_name distintos registrados en core_audit.activity_logs. Opcionalmente permite filtrar por modulo.',
            parameters: [
                {
                    name: 'modulo',
                    in: 'query',
                    required: false,
                    schema: {
                        type: 'string',
                    },
                    example: 'API_FINANZAS',
                    description:
                        'Modulo de la bitacora utilizado para filtrar los aplicativos, por ejemplo API_FINANZAS, API_FISCAL o API_UTIL',
                },
            ],
            responses: {
                200: {
                    description: 'Lista de aplicativos registrados en la bitacora',
                    content: {
                        'application/json': {
                            schema: {
                                $ref: '#/components/schemas/AuditLogApplicationsResponse',
                            },
                        },
                    },
                },
                500: {
                    description: 'Error interno del servidor',
                },
            },
        },
    },
};