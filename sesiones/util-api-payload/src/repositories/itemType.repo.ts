// src/repositories/itemType.repo.ts
import { getDataSource } from '../config/typeorm-datasource.js';
import { CatItemType } from '../entities/CatItemType.entity.js';

export const getItemTypeRepository = () => getDataSource().getRepository(CatItemType);
