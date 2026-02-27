import * as r from "@/repositories/FinanzasPayment.repo.js";
import { ResponsePageableDTO } from '@/response/ResponseHandler.dto.js';
import { ResponseHandler } from '@/response/ResponseHandler.js';
import { StatusCodes, ReasonPhrases } from 'http-status-codes';
import { z } from "zod/v4";
import type {
    ListFinanzasPaymentQuery,
    CreateFinanzasPaymentDto,
    UpdateFinanzasPaymentDto
} from "@/schemas/finanzasPayment.schema.js";
import type { FinanzasPayment } from "@/entities/FinanzasPayment.entities.js";
import {
    Between,
    LessThanOrEqual,
    MoreThanOrEqual,
    FindOptionsWhere,
} from "typeorm";

export async function list(q: ListFinanzasPaymentQuery) {
    const filter: FindOptionsWhere<FinanzasPayment> = {};

    if (q.documentNumber !== undefined) filter.documentNumber = q.documentNumber;
    if (q.vendorNumber !== undefined) filter.vendorNumber = q.vendorNumber;
    if (q.finanzasPaymentUuid !== undefined) filter.finanzasPaymentUuid = q.finanzasPaymentUuid as string;
    if (q.sapDocument !== undefined) filter.sapDocument = q.sapDocument;
    if (q.paymentDate) filter.paymentDate = MoreThanOrEqual(q.paymentDate);

    const createdAtEndString = q.createdAtEnd;
    const parsedDateEnd = z.coerce.date().parse(createdAtEndString);
    parsedDateEnd.setDate(parsedDateEnd.getDate() + 1);

    if (q.createdAtInitial && parsedDateEnd) filter.createdAt = Between(q.createdAtInitial, parsedDateEnd);


    var [result, total, _numberOfElements] = await r.findAllPaginated(filter, q.pageSize, q.pageNumber);

        //   console.log('[DEBUG] result:', result?.valueOf() );
        //   console.log('[DEBUG] total:', total?.valueOf());
    const _totalItems = Number(total?.valueOf() == null ? 0 : Number(total?.valueOf()));
    var _totalPages = _totalItems/q.pageSize;

    if(_totalPages - Math.trunc(_totalPages) > 0){
        _totalPages = Math.trunc(_totalPages) + 1;
    } else {
        _totalPages = Math.trunc(_totalPages)
    }

    const responsePageableDTO: ResponsePageableDTO = {
        content: result,
        totalElements: _totalItems,
        numberOfElements: _numberOfElements?.valueOf() == null ? 0 : Number(_numberOfElements?.valueOf()),
        totalPages: _totalPages,
        pageNumber: q.pageNumber,
        pageSize: q.pageSize

    };

      return ResponseHandler.responseBuilder("",responsePageableDTO,0, StatusCodes.OK, true, "");
  
}

type GenderType = { result: FinanzasPayment[], total: number };    // Specified format

export async function get(FinanzasPaymentUuid: string) {
    return r.findById(FinanzasPaymentUuid);
}

export async function create(dto: CreateFinanzasPaymentDto) {
    const data: Partial<FinanzasPayment> = {
        company: dto.company,
        documentNumber: dto.documentNumber,
        documentReference: dto.documentReference,
        amount: dto.amount,
        vendorNumber: dto.vendorNumber,
        status: dto.status,
        documentType: dto.documentType,
        sapDocument: dto.sapDocument,
        paymentDate: dto.paymentDate,
        createdAt: new Date()
    };
    const entityCreated = await r.createOne(data);
    console.log('[DEBUG] entityCreated:', entityCreated);
    return ResponseHandler.responseBuilder("",entityCreated,0, StatusCodes.CREATED, true, "");
}

export async function update(dto: UpdateFinanzasPaymentDto) {
    const filter: FindOptionsWhere<FinanzasPayment> = {};

    if (dto.documentNumber !== undefined) filter.documentNumber = dto.documentNumber;
    if (dto.vendorNumber !== undefined) filter.vendorNumber = dto.vendorNumber;
    if (dto.documentType !== undefined) filter.documentType = dto.documentType;
    if (dto.sapDocument !== undefined) filter.sapDocument = dto.sapDocument;

    const entityUpdated = await r.updateStatus(filter, dto.status);
    var response = ResponseHandler.responseBuilder("",entityUpdated,0, StatusCodes.OK, true, "");
    if (!entityUpdated) response = ResponseHandler.responseBuilder("NOT FOUND",entityUpdated,0, StatusCodes.NOT_FOUND, false, "");
    return response

}