// src/repositories/message.repo.ts
import { getDataSource } from '../config/typeorm-datasource.js';
import { CatMessage } from '../entities/CatMessage.entity.js';

export const getMessageRepository = () => getDataSource().getRepository(CatMessage);
