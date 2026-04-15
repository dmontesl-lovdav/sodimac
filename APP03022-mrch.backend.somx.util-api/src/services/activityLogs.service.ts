import * as activityLogsRepo from "@/repositories/activityLogs.repo.js";
import {
    type CreateActivityLogDto,
    type ListActivityLogQueryDto,
} from "@/schemas/activityLog.schema.js";
import { ResponseHandler } from '@/response/ResponseHandler.js';
import { StatusCodes } from 'http-status-codes';
import { Between, type FindOptionsWhere } from "typeorm";
import { ActivityLogs } from "@/entities/ActivityLogs.entity.js";
import { type ResponsePageableDTO } from '@/response/ResponseHandler.dto.js';
import { z } from "zod/v4";

export async function CreateLogActivity(dto: CreateActivityLogDto) {
    activityLogsRepo.SaveLogActivity(dto);
}

export async function ReadLogActivity(dto: ListActivityLogQueryDto) {
    const receptionDateAtEnd = dto.dateAtEnd;
    const parsedDateEnd = z.coerce.date().parse(receptionDateAtEnd);
    parsedDateEnd.setDate(parsedDateEnd.getDate() + 1);
    dto.dateAtEnd = parsedDateEnd;
    const criteria = buildCriteria(dto);

    const [result, total, _numberOfElements] = await activityLogsRepo.findAllPaginated(criteria, dto.pageSize, dto.pageNumber);

    const _totalItems = Number(total?.valueOf() == null ? 0 : Number(total?.valueOf()));
    let _totalPages = _totalItems / dto.pageSize;

    if (_totalPages - Math.trunc(_totalPages) > 0) {
        _totalPages = Math.trunc(_totalPages) + 1;
    } else {
        _totalPages = Math.trunc(_totalPages);
    }

    const responsePageableDTO: ResponsePageableDTO = {
        content: result,
        totalElements: _totalItems,
        numberOfElements: _numberOfElements?.valueOf() == null ? 0 : Number(_numberOfElements?.valueOf()),
        totalPages: _totalPages,
        pageNumber: dto.pageNumber,
        pageSize: dto.pageSize
    };

    const resp = ResponseHandler.responseBuilder("", responsePageableDTO, 0, StatusCodes.ACCEPTED, true, "");
    return resp;
}

function buildCriteria(criteria: ListActivityLogQueryDto): FindOptionsWhere<ActivityLogs> {
    const filter: FindOptionsWhere<ActivityLogs> = {};

    if (criteria.traceId) filter.traceid = criteria.traceId;
    if (criteria.modulo !== undefined) filter.modulo = criteria.modulo;
    if (criteria.serviceName !== undefined) filter.serviceName = criteria.serviceName;
    if (criteria.dateAtInitial && criteria.dateAtEnd) filter.timestamp = Between(criteria.dateAtInitial, criteria.dateAtEnd);

    return filter;
}
