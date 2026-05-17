import { datasource, getDataSource } from "@/config/typeorm-datasource.js";
import { type CreateActivityLogDto } from "@/schemas/activityLog.schema.js";
import { ActivityLogs } from "@/entities/ActivityLogs.entity.js";
import { type FindOptionsWhere } from "typeorm";
import { logger } from "@/utils/logger.js";

export const repo = () => getDataSource().getRepository(ActivityLogs);

export async function SaveLogActivity(dto: CreateActivityLogDto) {
    const details = dto.details ?? {};

    const query = `
      INSERT INTO core_audit.activity_logs (trace_id, trace_front_id, duration_ms, is_error, modulo, service_name, action, message, message_detail, user_id, timestamp, details)
      VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12::jsonb)
    `;

    const values = [dto.traceId, dto.traceFrontId, dto.durationms, dto.isError, dto.modulo, dto.serviceName, dto.action, dto.message, dto.messageDetail, dto.userId, dto.timestamp, JSON.stringify(details)];
    await datasource.query(query, values);
}

export async function findAllPaginated(filter: FindOptionsWhere<ActivityLogs>, pageSize: number, pageNumber: number) {
    const skip = (pageNumber - 1) * pageSize;
    const [result, total] = await repo().findAndCount({ where: filter, take: pageSize, skip: skip, order: { timestamp: "ASC" } });
    logger.info({ count: result.length }, "ActivityLogs List");
    return [result, total, result.length];
}
