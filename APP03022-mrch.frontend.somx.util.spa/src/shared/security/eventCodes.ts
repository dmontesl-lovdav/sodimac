import { APP_KEYS, type AppKey } from './appCodes';

export const EVENT_KEYS = {
    COMMON: {
        DOWNLOAD_CSV:        'EVT002', 
        SEARCH:              'EVT0008', 
        CLEAR_FILTERS:       'EVT001', 
        EDIT:                'EVT004', 
        DOWNLOAD_CSV_DETAIL: 'EVT006', 
    },
    RECEPTIONS: {
        DOWNLOAD_CSV:        'EVT002',
        SEARCH:              'EVT0008',
        CLEAR_FILTERS:       'EVT001',
        VIEW_DETAIL:         'EVT003',
        EDIT_RECEPTION:      'EVT004',
        LINK_INVOICE:        'EVT005', 
        LINK_CREDIT_NOTE:    'EVT007', 
        DOWNLOAD_CSV_DETAIL: 'EVT006',
    },
    SUPPLIERS_CATALOG: {
        DOWNLOAD_CSV:        'EVT002',
        SEARCH:              'EVT0008',
        CLEAR_FILTERS:       'EVT001',
    },
    CATALOGS_CATALOG: {
        DOWNLOAD_CSV:        'EVT002',
        SEARCH:              'EVT0008',
        CLEAR_FILTERS:       'EVT001',
        VIEW_DETAIL:         'EVT003',
        EDIT:                'EVT004',
        NEW_ELEMENT:                 'EVT0127',
        CHANGE_ELEMENT_STATUS:       'EVT0128',
        VIEW_CONVERSION:             'EVT0129',
        NEW_CONVERSION:              'EVT0130',
        CHANGE_PRINCIPAL_CONVERSION: 'EVT0131',
        DELETE:                      'EVT0132',
        DELETE_SELECTED:             'EVT0133',
    },
} as const;


export const APP_EVENT = {
    SUPPLIERS_CATALOG: {
        DOWNLOAD_CSV:  { app: APP_KEYS.SUPPLIERS_CATALOG, event: EVENT_KEYS.SUPPLIERS_CATALOG.DOWNLOAD_CSV, label: 'Exportar CSV' },
        SEARCH:        { app: APP_KEYS.SUPPLIERS_CATALOG, event: EVENT_KEYS.SUPPLIERS_CATALOG.SEARCH, label: 'Buscar' },
        CLEAR_FILTERS: { app: APP_KEYS.SUPPLIERS_CATALOG, event: EVENT_KEYS.SUPPLIERS_CATALOG.CLEAR_FILTERS, label: 'Limpiar' },
    },
    CATALOGS_CATALOG: {
        DOWNLOAD_CSV:  { app: APP_KEYS.CATALOGS_CATALOG, event: EVENT_KEYS.CATALOGS_CATALOG.DOWNLOAD_CSV, label: 'Exportar CSV' },
        SEARCH:        { app: APP_KEYS.CATALOGS_CATALOG, event: EVENT_KEYS.CATALOGS_CATALOG.SEARCH, label: 'Buscar' },
        CLEAR_FILTERS: { app: APP_KEYS.CATALOGS_CATALOG, event: EVENT_KEYS.CATALOGS_CATALOG.CLEAR_FILTERS, label: 'Limpiar' },
        VIEW_DETAIL:   { app: APP_KEYS.CATALOGS_CATALOG, event: EVENT_KEYS.CATALOGS_CATALOG.VIEW_DETAIL, label: 'Ver' },
        EDIT:          { app: APP_KEYS.CATALOGS_CATALOG, event: EVENT_KEYS.CATALOGS_CATALOG.EDIT, label: 'Editar' },
        NEW_ELEMENT:                 { app: APP_KEYS.CATALOGS_CATALOG, event: EVENT_KEYS.CATALOGS_CATALOG.NEW_ELEMENT, label: 'Nuevo Elemento' },
        CHANGE_ELEMENT_STATUS:       { app: APP_KEYS.CATALOGS_CATALOG, event: EVENT_KEYS.CATALOGS_CATALOG.CHANGE_ELEMENT_STATUS, label: 'Cambiar estatus del elemento' },
        VIEW_CONVERSION:             { app: APP_KEYS.CATALOGS_CATALOG, event: EVENT_KEYS.CATALOGS_CATALOG.VIEW_CONVERSION, label: 'Ver Conversión' },
        NEW_CONVERSION:              { app: APP_KEYS.CATALOGS_CATALOG, event: EVENT_KEYS.CATALOGS_CATALOG.NEW_CONVERSION, label: 'Nueva Conversión' },
        CHANGE_PRINCIPAL_CONVERSION: { app: APP_KEYS.CATALOGS_CATALOG, event: EVENT_KEYS.CATALOGS_CATALOG.CHANGE_PRINCIPAL_CONVERSION, label: 'Cambiar conversión principal' },
        DELETE:                      { app: APP_KEYS.CATALOGS_CATALOG, event: EVENT_KEYS.CATALOGS_CATALOG.DELETE, label: 'Borrar' },
        DELETE_SELECTED:             { app: APP_KEYS.CATALOGS_CATALOG, event: EVENT_KEYS.CATALOGS_CATALOG.DELETE_SELECTED, label: 'Borrar Seleccionados' },
    },
    RECEPTIONS: {
        DOWNLOAD_CSV:        { app: APP_KEYS.RECEPTIONS, event: EVENT_KEYS.RECEPTIONS.DOWNLOAD_CSV, label: 'Exportar CSV' },
        DOWNLOAD_CSV_DETAIL: { app: APP_KEYS.RECEPTIONS, event: EVENT_KEYS.RECEPTIONS.DOWNLOAD_CSV_DETAIL, label: 'Descargar CSV Detalle' },
        SEARCH:              { app: APP_KEYS.RECEPTIONS, event: EVENT_KEYS.RECEPTIONS.SEARCH, label: 'Buscar' },
        CLEAR_FILTERS:       { app: APP_KEYS.RECEPTIONS, event: EVENT_KEYS.RECEPTIONS.CLEAR_FILTERS, label: 'Limpiar' },
        VIEW_DETAIL:         { app: APP_KEYS.RECEPTIONS, event: EVENT_KEYS.RECEPTIONS.VIEW_DETAIL, label: 'Ver detalle' },
        EDIT_RECEPTION:      { app: APP_KEYS.RECEPTIONS, event: EVENT_KEYS.RECEPTIONS.EDIT_RECEPTION, label: 'Editar recepción' },
        LINK_INVOICE:        { app: APP_KEYS.RECEPTIONS, event: EVENT_KEYS.RECEPTIONS.LINK_INVOICE, label: 'Relacionar factura' },
        LINK_CREDIT_NOTE:    { app: APP_KEYS.RECEPTIONS, event: EVENT_KEYS.RECEPTIONS.LINK_CREDIT_NOTE, label: 'Relacionar nota crédito' },
    },
} as const satisfies Record<string, Record<string, { app: AppKey; event: string; label: string }>>;

export type AppEvent = { app: AppKey; event: string; label?: string };
