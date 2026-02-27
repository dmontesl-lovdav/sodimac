// src/repositories/process.repo.ts
import { getDataSource } from '../config/typeorm-datasource.js';
import { CatProcess } from '../entities/CatProcess.entity.js';

export const getProcessRepository = () => getDataSource().getRepository(CatProcess);
