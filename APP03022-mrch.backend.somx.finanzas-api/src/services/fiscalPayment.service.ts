import * as r from "@/repositories/fiscalPayment.repo.js";
import type {
    ListFiscalPaymentQuery,
    CreateFiscalPaymentDto,
    UpdateFiscalPaymentDto,
} from "@/schemas/fiscalPayment.schema.js";
import type { FiscalPayment } from "@/entities/FiscalPayment.entity.js";
import { Between, LessThanOrEqual, MoreThanOrEqual, FindOptionsWhere } from "typeorm";

export async function list(q: ListFiscalPaymentQuery) {
    const filter: FindOptionsWhere<FiscalPayment> = {};

    if (q.status !== undefined) filter.status = q.status;
    if (q.vendorNumber !== undefined) filter.vendorNumber = q.vendorNumber;
    if (q.company !== undefined) filter.company = q.company;
    if (q.documentType !== undefined) filter.documentType = q.documentType;

    if (q.from && q.to) filter.paymentDate = Between(q.from, q.to);
    else if (q.from) filter.paymentDate = MoreThanOrEqual(q.from);
    else if (q.to) filter.paymentDate = LessThanOrEqual(q.to);

    return r.findAll(filter, q.limit ?? 100);
}

export async function get(id: string) {
    return r.findById(id);
}

export async function getByPaymentNumber(paymentNumber: string) {
    return r.findByPaymentNumber(paymentNumber);
}

export async function create(dto: CreateFiscalPaymentDto) {
    const data = {
        ...dto,
        createdAt: new Date(),
        updatedAt: new Date(),
    };

    return r.createOne(data as any);
}

export async function update(id: string, dto: UpdateFiscalPaymentDto) {
    const patch = {
        ...dto,
        updatedAt: new Date(),
    };

    return r.updateOne(id, patch as any);
}

export async function remove(id: string) {
    await r.deleteOne(id);
}
