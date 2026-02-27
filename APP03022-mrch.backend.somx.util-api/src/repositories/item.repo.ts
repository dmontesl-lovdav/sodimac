// src/repositories/item.repo.ts
import { getDataSource } from '../config/typeorm-datasource.js';
import { CatItem } from '../entities/CatItem.entity.js';

export const getItemRepository = () => getDataSource().getRepository(CatItem);
