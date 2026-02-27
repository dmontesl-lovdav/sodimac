import 'dotenv/config';
import 'reflect-metadata';
import { DataSource } from 'typeorm';
import type { DataSourceOptions } from 'typeorm';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// .ts en dev / .js en build
const ENTITIES = [join(__dirname, '../entities/**/*.{ts,js}')];

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
        schema: params.get('currentSchema') || 'core_audit',
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

const base: DataSourceOptions = {
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

};

console.log('[DATASOURCE] Creating new DataSource instance');

try {
    console.log('[DATASOURCE] Entity Route:', ENTITIES.map(e => e || 'UNNAMED').join(', '));
} catch (err) {
    console.error('[DATASOURCE] Error getting entity names:', err);
}

const options: DataSourceOptions = {
    ...base,
    ...(useSSL ? { ssl: { rejectUnauthorized: false } } : {})
};

export const datasource = new DataSource(options);
//console.log('[DATASOURCE] DataSource created with', datasource.options.entities?.length, 'entities');

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
    
    const entityMetadatas = datasource.entityMetadatas;
    const entityNames = entityMetadatas.map(meta => meta.name);
    console.log(entityNames);
    
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
