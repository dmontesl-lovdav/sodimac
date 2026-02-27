import 'reflect-metadata';
import 'dotenv/config';
import { DataSource } from 'typeorm';
import type { DataSourceOptions } from 'typeorm';


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
import { VendorBlock } from '../entities/VendorBlock.entity.js';
import { StampedRebate } from '../entities/StampedRebate.entity.js';
import { FinanzasPayment } from '../entities/FinanzasPayment.entities.js';
import { Addendum } from '../entities/tenant_fiscal.addendum.entity.js';
import { Invoice } from '../entities/tenant_fiscal.invoice.entity.js';


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
    VendorBlock,
    StampedRebate,
    FinanzasPayment,
    Addendum,
    Invoice,
];

// Parsear DATASOURCE_URL (formato JDBC Spring Boot) a configuración TypeORM
function parseJdbcUrl(jdbcUrl: string) {
    // Parsear: jdbc:postgresql://host:port/database?currentSchema=schema&useSSL=true
    // Nota: Los parámetros en JDBC usan ? para separar, pero también & internamente
    const match = jdbcUrl.match(/jdbc:postgresql:\/\/([^:]+):(\d+)\/([^?]+)(\?.*)?$/);
    if (!match) {
        throw new Error(`Invalid DATASOURCE_URL format: ${jdbcUrl}`);
    }

    const [, host, port, database, queryString] = match;

    // Parsear parámetros JDBC: pueden usar ? como delimitador en lugar de &
    const paramsString = (queryString?.substring(1) || '').replace(/\?/g, '&');
    const params = new URLSearchParams(paramsString);

    return {
        host,
        port: Number(port),
        database,
        schema: params.get('currentSchema') || 'tenant_finance',
        ssl: params.get('useSSL') === 'true'
    };
}

// Extraer configuración de variables de entorno DATASOURCE_*
const datasourceUrl = process.env.DATASOURCE_URL;
const datasourceUsername = process.env.DATASOURCE_USERNAME;
const datasourcePassword = process.env.DATASOURCE_PASSWORD;

if (!datasourceUrl || !datasourceUsername || !datasourcePassword) {
    throw new Error('Missing required environment variables: DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD');
}

const dbConfig = parseJdbcUrl(datasourceUrl);

const useSSL = dbConfig.ssl;

const options: DataSourceOptions = {
    type: 'postgres',
    host: dbConfig.host as string,
    port: dbConfig.port,
    username: datasourceUsername,
    password: datasourcePassword,
    database: dbConfig.database as string,
    schema: dbConfig.schema as string,
    entities: ENTITIES,
    logging: true,
    synchronize: false,
    ...(useSSL && { ssl: { rejectUnauthorized: false } })
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
            host: dbConfig.host,
            port: dbConfig.port,
            user: datasourceUsername,
            db: dbConfig.database,
            schema: dbConfig.schema,
            ssl: useSSL
        });
        await datasource.initialize();


        console.log('[INIT] DataSource entity metadata count AFTER init:', datasource.entityMetadatas?.length || 0);
    }

    initializedDataSource = datasource;
    console.log('[INIT] initializedDataSource set with', initializedDataSource.entityMetadatas?.length, 'entity metadatas');

    return datasource;
}

export function getDataSource(): DataSource {
    console.log('[getDataSource] Called. initializedDataSource:', !!initializedDataSource, 'isInitialized:', initializedDataSource?.isInitialized);
    if (!initializedDataSource || !initializedDataSource.isInitialized) {
        throw new Error('DataSource not initialized. Call initDataSource() first.');
    }
    console.log('[getDataSource] Returning DataSource with', initializedDataSource.entityMetadatas?.length, 'entity metadatas');
    return initializedDataSource;
}
