import 'reflect-metadata';
import dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';
import { DataSource } from 'typeorm';
import type { DataSourceOptions } from 'typeorm';
import * as svcAxios from "@/services/axios.service.js";

// Load local .env with priority over system variables
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

dotenv.config({
    path: path.resolve(__dirname, '../../.env'),
    override: true,
});

// Importar todas las entidades explícitamente
import { OriginCatalog } from '../entities/OriginCatalog.entity.js';
import { StatusCatalog } from '../entities/StatusCatalog.entity.js';
import { PacCatalog } from '../entities/PacCatalog.entity.js';
import { VersionCatalog } from '../entities/VersionCatalog.entity.js';
import { PurchaseOrder } from '../entities/PurchaseOrder.entity.js';
import { Rebate } from '../entities/Rebate.entity.js';
import { SapDocument } from '../entities/SapDocument.entity.js';
import { Reception } from '../entities/Reception.entity.js';
import { ReceptionSku } from '../entities/ReceptionSku.entity.js';
import { ShippingGuide } from '../entities/ShippingGuide.entity.js';
import { ShippingGuideDocument } from '../entities/ShippingGuideDocument.entity.js';
import { ShippingGuidePurchaseOrder } from '../entities/ShippingGuidePurchaseOrder.entity.js';
import { SupplierBlock } from '../entities/SupplierBlock.entity.js';
import { FiscalPayment } from '../entities/FiscalPayment.entity.js';
import { AccountsPayable } from '../entities/AccountsPayable.entity.js';
import { Healthcheck } from '../entities/Healthcheck.entity.js'
import { VendorBlock } from '../entities/VendorBlock.entity.js';
import { StampedRebate } from '../entities/StampedRebate.entity.js';
import { FinanzasPayment } from '../entities/FinanzasPayment.entities.js';
import { Addendum } from '../entities/tenant_fiscal.addendum.entity.js';
import { Invoice } from '../entities/tenant_fiscal.invoice.entity.js';
import { AddendumManual } from '../entities/AddendumManual.entity.js';

import { ThreeWayMatch } from '../entities/ThreeWayMatch.entity.js';
import { TwmEjecucion } from '../entities/TwmEjecucion.entity.js';
import { TwmLogs } from '../entities/TwmLogs.entity.js';
import { TwmCifrasControl } from '../entities/TwmCifrasControl.entity.js';
import { AccountStatement } from '../entities/AccountStatement.entity.js';
import { FinanzasPaymentHeader } from '../entities/FinanzasPaymentHeader.entity.js';
import { MigoDocument } from '../entities/MigoDocument.entity.js';
import { MigoDocumentReception } from '../entities/MigoDocumentReception.entity.js';
import { TransactionId } from '../entities/TransactionId.entity.js';
import { TransactionIdErrorLog } from '../entities/TransactionIdErrorLog.entity.js';
import { SharedCatalogHeader } from '../entities/SharedCatalogHeader.entity.js';
import { SharedCatalogDetail } from '../entities/SharedCatalogDetail.entity.js';
import { SharedSupplier } from '../entities/SharedSupplier.entity.js';
import { SharedSupplierType } from '../entities/SharedSupplierType.entity.js';
import { SharedCatalogDictionaryLang } from '../entities/SharedCatalogDictionaryLang.entity.js';

// Registrar entidades explícitamente
const ENTITIES = [
    OriginCatalog,
    StatusCatalog,
    PacCatalog,
    VersionCatalog,
    PurchaseOrder,
    Rebate,
    SapDocument,
    Reception,
    ReceptionSku,
    ShippingGuide,
    ShippingGuideDocument,
    ShippingGuidePurchaseOrder,
    SupplierBlock,
    FiscalPayment,
    AccountsPayable,
    Healthcheck,
    VendorBlock,
    StampedRebate,
    FinanzasPayment,
    Addendum,
    Invoice,
    ThreeWayMatch,
    TwmEjecucion,
    TwmLogs,
    TwmCifrasControl,
    AccountStatement,
    FinanzasPaymentHeader,
    AddendumManual,
    MigoDocument,
    MigoDocumentReception,
    TransactionId,
    TransactionIdErrorLog,
    SharedCatalogHeader,
    SharedCatalogDetail,
    SharedSupplier,
    SharedSupplierType,
    SharedCatalogDictionaryLang,
];

const dbHost = process.env.DB_HOST;
const dbPort = Number(process.env.DB_PORT);
const dbUser = process.env.DB_USER;
const dbPass = process.env.DB_PASS;
const dbName = process.env.DB_NAME;
const dbSchema = process.env.DB_SCHEMA || 'tenant_finance';
const dbSSL = String(process.env.DB_SSL).trim().toLowerCase() === "true";

if (!dbHost || !dbPort || !dbUser || !dbPass || !dbName) {
    throw new Error(
        'Missing required environment variables: DB_HOST, DB_PORT, DB_USER, DB_PASS, DB_NAME'
    );
}

const options: DataSourceOptions = {
    type: 'postgres',
    host: dbHost,
    port: dbPort,
    username: dbUser,
    password: dbPass,
    database: dbName,
    schema: dbSchema,
    entities: ENTITIES,
    logging: true,
    synchronize: false,
    ...(dbSSL && { ssl: { rejectUnauthorized: false } }),
};

console.log('[DATASOURCE] Creating new DataSource instance');
console.log('[DATASOURCE] Entity classes count:', ENTITIES.length);
try {
    console.log('[DATASOURCE] Entity names:', ENTITIES.map(e => e?.name || 'UNNAMED').join(', '));
} catch (err) {
    console.error('[DATASOURCE] Error getting entity names:', err);
}

export const datasource = new DataSource(options);
console.log('[DATASOURCE] DataSource created with', datasource.options.entities?.length, 'entities');

let initializedDataSource: DataSource | null = null;

export async function initDataSource() {
    console.log('[INIT] DataSource isInitialized:', datasource.isInitialized);
    console.log('[INIT] DataSource entity metadata count BEFORE init:', datasource.entityMetadatas?.length || 0);

    if (!datasource.isInitialized) {
        console.log('[DB CONNECT]', {
            host: dbHost,
            port: dbPort,
            user: dbUser,
            db: dbName,
            schema: dbSchema,
            ssl: dbSSL,
        });

        await datasource.initialize();

        console.log('[INIT] DataSource entity metadata count AFTER init:', datasource.entityMetadatas?.length || 0);
    }

    initializedDataSource = datasource;
    console.log('[INIT] initializedDataSource set with', initializedDataSource.entityMetadatas?.length, 'entity metadatas');

    return datasource;
}

export function getDataSource(): DataSource {
    console.log(
        '[getDataSource] Called. initializedDataSource:',
        !!initializedDataSource,
        'isInitialized:',
        initializedDataSource?.isInitialized
    );

    if (!initializedDataSource || !initializedDataSource.isInitialized) {
        throw new Error('DataSource not initialized. Call initDataSource() first.');
    }

    console.log('[getDataSource] Returning DataSource with', initializedDataSource.entityMetadatas?.length, 'entity metadatas');
    return initializedDataSource;
}