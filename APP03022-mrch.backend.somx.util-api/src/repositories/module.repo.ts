// src/repositories/module.repo.ts
import { getDataSource } from '../config/typeorm-datasource.js';
import { CatModule } from '../entities/CatModule.entity.js';

export const getModuleRepository = () => getDataSource().getRepository(CatModule);
