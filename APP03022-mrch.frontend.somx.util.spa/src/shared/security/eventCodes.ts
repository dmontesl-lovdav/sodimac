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
    },
} as const;

export type EventKey = string;

export const APP_EVENT = {
    SUPPLIERS_CATALOG: {
        DOWNLOAD_CSV:  { app: APP_KEYS.SUPPLIERS_CATALOG, event: EVENT_KEYS.SUPPLIERS_CATALOG.DOWNLOAD_CSV },
        SEARCH:        { app: APP_KEYS.SUPPLIERS_CATALOG, event: EVENT_KEYS.SUPPLIERS_CATALOG.SEARCH },
        CLEAR_FILTERS: { app: APP_KEYS.SUPPLIERS_CATALOG, event: EVENT_KEYS.SUPPLIERS_CATALOG.CLEAR_FILTERS },
    },
    CATALOGS_CATALOG: {
        DOWNLOAD_CSV:  { app: APP_KEYS.CATALOGS_CATALOG, event: EVENT_KEYS.CATALOGS_CATALOG.DOWNLOAD_CSV },
        SEARCH:        { app: APP_KEYS.CATALOGS_CATALOG, event: EVENT_KEYS.CATALOGS_CATALOG.SEARCH },
        CLEAR_FILTERS: { app: APP_KEYS.CATALOGS_CATALOG, event: EVENT_KEYS.CATALOGS_CATALOG.CLEAR_FILTERS },
        VIEW_DETAIL:   { app: APP_KEYS.CATALOGS_CATALOG, event: EVENT_KEYS.CATALOGS_CATALOG.VIEW_DETAIL },
        EDIT:          { app: APP_KEYS.CATALOGS_CATALOG, event: EVENT_KEYS.CATALOGS_CATALOG.EDIT },
    },
    RECEPTIONS: {
        DOWNLOAD_CSV:        { app: APP_KEYS.RECEPTIONS, event: EVENT_KEYS.RECEPTIONS.DOWNLOAD_CSV },
        DOWNLOAD_CSV_DETAIL: { app: APP_KEYS.RECEPTIONS, event: EVENT_KEYS.RECEPTIONS.DOWNLOAD_CSV_DETAIL },
        SEARCH:              { app: APP_KEYS.RECEPTIONS, event: EVENT_KEYS.RECEPTIONS.SEARCH },
        CLEAR_FILTERS:       { app: APP_KEYS.RECEPTIONS, event: EVENT_KEYS.RECEPTIONS.CLEAR_FILTERS },
        VIEW_DETAIL:         { app: APP_KEYS.RECEPTIONS, event: EVENT_KEYS.RECEPTIONS.VIEW_DETAIL },
        EDIT_RECEPTION:      { app: APP_KEYS.RECEPTIONS, event: EVENT_KEYS.RECEPTIONS.EDIT_RECEPTION },
        LINK_INVOICE:        { app: APP_KEYS.RECEPTIONS, event: EVENT_KEYS.RECEPTIONS.LINK_INVOICE },
        LINK_CREDIT_NOTE:    { app: APP_KEYS.RECEPTIONS, event: EVENT_KEYS.RECEPTIONS.LINK_CREDIT_NOTE },
    },
} as const satisfies Record<string, Record<string, { app: AppKey; event: EventKey }>>;

export type AppEvent = { app: AppKey; event: EventKey };
