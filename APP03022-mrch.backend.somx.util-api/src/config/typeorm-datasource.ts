import 'reflect-metadata';
import dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';
import { DataSource } from 'typeorm';
import type { DataSourceOptions } from 'typeorm';
import { initializeTransactionalContext, patchTypeORMRepositoryWithBaseRepository } from 'typeorm-transactional-cls-hooked';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

dotenv.config({
    path: path.resolve(__dirname, '../../.env'),
    override: true,
});

import { CatParameter } from '../entities/CatParameter.entity.js';
import { CatModule } from '../entities/CatModule.entity.js';
import { CatMessage } from '../entities/CatMessage.entity.js';
import { ApplicationMsg } from '../entities/ApplicationMsg.entity.js';
import { CatProcess } from '../entities/CatProcess.entity.js';
import { CatItemType } from '../entities/CatItemType.entity.js';
import { CatItem } from '../entities/CatItem.entity.js';
import { ActivityLogs } from '@/entities/ActivityLogs.entity.js';

import {
    ModuleProcess,
    ProfileModule,
    ProfileModuleProcess,
    ProfileUser,
    RolePermission,
    RoleProvider,
    RoleUser,
    UserAttribute,
    UserData,
} from '../entities/SecurityRelations.entity.js';

import { CatalogHeader } from '../entities/CatalogHeader.entity.js';
import { CatalogDetail } from '../entities/CatalogDetail.entity.js';
import { CatalogDetailRelation } from '../entities/CatalogDetailRelation.entity.js';
import { CatalogConversion } from '../entities/CatalogConversion.entity.js';
import { DictionaryLang } from '../entities/DictionaryLang.entity.js';
import { PaymentCondition } from '../entities/PaymentCondition.entity.js';
import { StatusTrain } from '../entities/StatusTrain.entity.js';
import { Supplier } from '../entities/Supplier.entity.js';
import { SupplierBlock } from '../entities/SupplierBlock.entity.js';
import { SupplierType } from '../entities/SupplierType.entity.js';

const ENTITIES = [
    CatParameter,
    CatModule,
    CatMessage,
    ApplicationMsg,
    CatProcess,
    CatItemType,
    CatItem,
    ActivityLogs,
    UserData,
    ModuleProcess,
    ProfileModule,
    ProfileUser,
    ProfileModuleProcess,
    RoleUser,
    RolePermission,
    RoleProvider,
    UserAttribute,
    CatalogHeader,
    CatalogDetail,
    CatalogDetailRelation,
    CatalogConversion,
    DictionaryLang,
    PaymentCondition,
    StatusTrain,
    Supplier,
    SupplierBlock,
    SupplierType
];

const dbHost = process.env.DB_HOST;
const dbPort = Number(process.env.DB_PORT);
const dbUser = process.env.DB_USER;
const dbPass = process.env.DB_PASS;
const dbName = process.env.DB_NAME;
const dbSchema = process.env.DB_SCHEMA || 'core_utils';
const dbSSL = process.env.DB_SSL === 'true';

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
    logging: process.env.NODE_ENV === 'development',
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

        initializeTransactionalContext();
        patchTypeORMRepositoryWithBaseRepository();

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

    if (!initializedDataSource?.isInitialized) {
        throw new Error('DataSource not initialized. Call initDataSource() first.');
    }

    console.log('[getDataSource] Returning DataSource with', initializedDataSource.entityMetadatas?.length, 'entity metadatas');
    return initializedDataSource;
}
