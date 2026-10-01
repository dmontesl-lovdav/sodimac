export const tags = [
    {
        name: 'Audit Logs',
        description: 'Consulta de bitacora de actividades',
    },
];

export const auditLogSchemas = {
    AuditLogApplicationsResponse: {
        type: 'array',
        description:
            'Listado de service_name distintos registrados en core_audit.activity_logs',
        items: {
            type: 'string',
        },
        example: [
            'CartaPorte',
            'FinanzasPayment',
            'PurchaseOrders',
            'ShippingGuide',
        ],
    },
};