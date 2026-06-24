
export const APP_KEYS = {
    SUPPLIERS_CATALOG:   'APL001', 
    CATALOGS_CATALOG:    'APL002', 
    RECEPTIONS:          'APL003',
    CARTA_PORTE:         'APL004',
    DISCOUNTS:           'APL005', 
    ACCOUNT_STATEMENT:   'APL006',
    THREE_WAY_MATCH:     'APL007',
    MIGO:                'APL008',
    INVOICES:            'APL009', 
    CREDIT_NOTES:        'APL010', 
    PAYMENT_COMPLEMENTS: 'APL011', 
    PARAMETERS:          'APL012',
    AUDIT_LOG:           'APL013', 
    PROFILE_ADMIN:       'APL014', 
    ROLES_ADMIN:         'APL015', 
    PERMISSIONS_ADMIN:   'APL016', 
} as const;

export type AppKey = (typeof APP_KEYS)[keyof typeof APP_KEYS];
