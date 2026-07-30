import { APP_KEYS, type AppKey } from './appCodes';

export const EVENT_KEYS = {
    COMMON: {
        CLEAR_FILTERS:       'EVT001',
        DOWNLOAD_CSV:        'EVT002',
        VIEW_DETAIL:         'EVT003',
        EDIT:                'EVT004',
        LINK_INVOICE:        'EVT005',
        DOWNLOAD_CSV_DETAIL: 'EVT006',
        LINK_CREDIT_NOTE:    'EVT007',
        SEARCH:              'EVT0008',
        DOWNLOAD_XLS:        'EVT009',
        DOWNLOAD_XML:        'EVT010',
        CANCEL:              'EVT011',
        UPDATE_STATUS:       'EVT012',
        DOWNLOAD_PDF:        'EVT013',
        CONFIRM_REVIEW:      'EVT014',
        REQUEST_REVIEW:      'EVT015',
        PUBLISH:             'EVT016',
        AUTHORIZE:           'EVT017',
        REJECT:              'EVT018',
    },
} as const;

export const APP_EVENT = {
    INVOICES: {
        SEARCH:              { app: APP_KEYS.INVOICES, event: EVENT_KEYS.COMMON.SEARCH, label: 'Buscar' },
        CLEAR_FILTERS:       { app: APP_KEYS.INVOICES, event: EVENT_KEYS.COMMON.CLEAR_FILTERS, label: 'Limpiar' },
        DOWNLOAD_CSV:        { app: APP_KEYS.INVOICES, event: EVENT_KEYS.COMMON.DOWNLOAD_CSV, label: 'Exportar CSV' },
        VIEW_DETAIL:         { app: APP_KEYS.INVOICES, event: EVENT_KEYS.COMMON.VIEW_DETAIL },
        DOWNLOAD_XML:        { app: APP_KEYS.INVOICES, event: EVENT_KEYS.COMMON.DOWNLOAD_XML, label: 'Ver XML' },
        DOWNLOAD_PDF:        { app: APP_KEYS.INVOICES, event: EVENT_KEYS.COMMON.DOWNLOAD_PDF, label: 'Ver PDF' },
        CANCEL:              { app: APP_KEYS.INVOICES, event: EVENT_KEYS.COMMON.CANCEL, label: 'Cancelar Factura' },
        UPDATE_STATUS:       { app: APP_KEYS.INVOICES, event: EVENT_KEYS.COMMON.UPDATE_STATUS, label: 'Reproceso Contable' },
        LINK_CREDIT_NOTE:    { app: APP_KEYS.INVOICES, event: EVENT_KEYS.COMMON.LINK_CREDIT_NOTE, label: 'Ver Nota Crédito' },
    },
    CREDIT_NOTES: {
        SEARCH:              { app: APP_KEYS.CREDIT_NOTES, event: EVENT_KEYS.COMMON.SEARCH, label: 'Buscar' },
        CLEAR_FILTERS:       { app: APP_KEYS.CREDIT_NOTES, event: EVENT_KEYS.COMMON.CLEAR_FILTERS, label: 'Limpiar' },
        DOWNLOAD_CSV:        { app: APP_KEYS.CREDIT_NOTES, event: EVENT_KEYS.COMMON.DOWNLOAD_CSV, label: 'Exportar CSV' },
        VIEW_DETAIL:         { app: APP_KEYS.CREDIT_NOTES, event: EVENT_KEYS.COMMON.VIEW_DETAIL },
        DOWNLOAD_XML:        { app: APP_KEYS.CREDIT_NOTES, event: EVENT_KEYS.COMMON.DOWNLOAD_XML, label: 'Ver XML' },
        DOWNLOAD_PDF:        { app: APP_KEYS.CREDIT_NOTES, event: EVENT_KEYS.COMMON.DOWNLOAD_PDF, label: 'Ver PDF' },
        CANCEL:              { app: APP_KEYS.CREDIT_NOTES, event: EVENT_KEYS.COMMON.CANCEL, label: 'Cancelar Nota Crédito' },
        PUBLISH:             { app: APP_KEYS.CREDIT_NOTES, event: EVENT_KEYS.COMMON.PUBLISH, label: 'Agregar Nota de Crédito' },
        LINK_INVOICE:        { app: APP_KEYS.CREDIT_NOTES, event: EVENT_KEYS.COMMON.LINK_INVOICE, label: 'Ver Factura' },
    },
    PAYMENT_COMPLEMENTS: {
        SEARCH:              { app: APP_KEYS.PAYMENT_COMPLEMENTS, event: EVENT_KEYS.COMMON.SEARCH, label: 'Buscar' },
        CLEAR_FILTERS:       { app: APP_KEYS.PAYMENT_COMPLEMENTS, event: EVENT_KEYS.COMMON.CLEAR_FILTERS, label: 'Limpiar' },
        DOWNLOAD_CSV:        { app: APP_KEYS.PAYMENT_COMPLEMENTS, event: EVENT_KEYS.COMMON.DOWNLOAD_CSV },
        DOWNLOAD_CSV_DETAIL: { app: APP_KEYS.PAYMENT_COMPLEMENTS, event: EVENT_KEYS.COMMON.DOWNLOAD_CSV_DETAIL },
        VIEW_DETAIL:         { app: APP_KEYS.PAYMENT_COMPLEMENTS, event: EVENT_KEYS.COMMON.VIEW_DETAIL },
        DOWNLOAD_XML:        { app: APP_KEYS.PAYMENT_COMPLEMENTS, event: EVENT_KEYS.COMMON.DOWNLOAD_XML },
        DOWNLOAD_PDF:        { app: APP_KEYS.PAYMENT_COMPLEMENTS, event: EVENT_KEYS.COMMON.DOWNLOAD_PDF },
        PUBLISH:             { app: APP_KEYS.PAYMENT_COMPLEMENTS, event: EVENT_KEYS.COMMON.PUBLISH },
    },
} as const satisfies Record<string, Record<string, { app: AppKey; event: string; label?: string }>>;

export type AppEvent = { app: AppKey; event: string; label?: string };
