// src/config/typeorm-datasource.ts
import 'dotenv/config';
import 'reflect-metadata';
import { DataSource } from 'typeorm';
import type { DataSourceOptions } from 'typeorm';
import { initializeTransactionalContext, patchTypeORMRepositoryWithBaseRepository } from "typeorm-transactional-cls-hooked";

// Importar entidades
import { CatParameter } from '../entities/CatParameter.entity.js';
import { CatModule } from '../entities/CatModule.entity.js';
import { CatMessage } from '../entities/CatMessage.entity.js';
import { ApplicationMsg } from '../entities/ApplicationMsg.entity.js';
import { CatProcess } from '../entities/CatProcess.entity.js';
import { CatItemType } from '../entities/CatItemType.entity.js';
import { CatItem } from '../entities/CatItem.entity.js';

// Registrar entidades
const ENTITIES = [
    CatParameter,
    CatModule,
    CatMessage,
    ApplicationMsg,
    CatProcess,
    CatItemType,
    CatItem
];

/**
 * Parsear DATASOURCE_URL (formato JDBC Spring Boot) a configuracion TypeORM
 * Ejemplo: jdbc:postgresql://host:port/database?currentSchema=schema&useSSL=true
 */
function parseJdbcUrl(jdbcUrl: string) {
    const match = jdbcUrl.match(/jdbc:postgresql:\/\/([^:]+):(\d+)\/([^?]+)(\?.*)?$/);
    if (!match) {
        throw new Error(`Invalid DATASOURCE_URL format: ${jdbcUrl}`);
    }

    const [, host, port, database, queryString] = match;

    // Parsear parametros JDBC
    const paramsString = (queryString?.substring(1) || '').replace(/\?/g, '&');
    const params = new URLSearchParams(paramsString);

    return {
        host,
        port: Number(port),
        database,
        schema: params.get('currentSchema') || 'core_utils',
        ssl: params.get('useSSL') === 'true'
    };
}

// Extraer configuracion de variables de entorno
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
    logging: process.env.NODE_ENV === 'development',
    synchronize: false, // Usar migraciones, no sincronizacion automatica
    ...(useSSL && { ssl: { rejectUnauthorized: false } })
};

console.log('[DATASOURCE] Creating DataSource instance');
console.log('[DATASOURCE] Entities:', ENTITIES.map(e => e?.name || 'UNNAMED').join(', '));

export const datasource = new DataSource(options);

let initializedDataSource: DataSource | null = null;

export async function initDataSource() {
    console.log('[INIT] DataSource isInitialized:', datasource.isInitialized);

    if (!datasource.isInitialized) {
        console.log('[DB CONNECT]', {
            host: dbConfig.host,
            port: dbConfig.port,
            user: datasourceUsername,
            db: dbConfig.database,
            schema: dbConfig.schema,
            ssl: useSSL
        });

        initializeTransactionalContext();
        patchTypeORMRepositoryWithBaseRepository();
        await datasource.initialize();

        console.log('[INIT] DataSource initialized with', datasource.entityMetadatas?.length || 0, 'entities');
    }

    initializedDataSource = datasource;
    return datasource;
}

export function getDataSource(): DataSource {
    if (!initializedDataSource || !initializedDataSource.isInitialized) {
        throw new Error('DataSource not initialized. Call initDataSource() first.');
    }
    return initializedDataSource;
}
