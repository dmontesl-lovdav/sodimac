import { APP_KEYS, type AppKey } from './appCodes';

export const EVENT_KEYS = {
    COMMON: {
        DOWNLOAD_CSV:        'EVT002',
        SEARCH:              'EVT0008',
        CLEAR_FILTERS:       'EVT001',
        VIEW_DETAIL:         'EVT003',
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
} as const;

export type EventKey = string;

export const APP_EVENT = {
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
