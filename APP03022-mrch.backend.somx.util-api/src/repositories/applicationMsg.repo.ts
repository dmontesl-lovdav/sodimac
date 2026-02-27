// src/repositories/applicationMsg.repo.ts
import { getDataSource } from '../config/typeorm-datasource.js';
import { ApplicationMsg } from '../entities/ApplicationMsg.entity.js';

export const getApplicationMsgRepository = () => getDataSource().getRepository(ApplicationMsg);
