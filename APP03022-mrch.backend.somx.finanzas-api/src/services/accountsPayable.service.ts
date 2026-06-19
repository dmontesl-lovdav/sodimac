// src/services/accountsPayable.service.ts
import * as r from "@/repositories/accountsPayable.repo.js";
import type {
    ListAccountsQuery,
    CreateAccountsPayableDto,
    UpdateAccountsPayableDto,
} from "@/schemas/accountsPayable.schema.js";
import type { AccountsPayable } from "@/entities/AccountsPayable.entity.js";
import {
    Between,
    LessThanOrEqual,
    MoreThanOrEqual,
    FindOptionsWhere,
} from "typeorm";

export async function list(q: ListAccountsQuery) {
    const filter: FindOptionsWhere<AccountsPayable> = {};
    if (q.vendorNumber !== undefined) filter.vendorNumber = q.vendorNumber;
    return r.findAll(filter, q.limit ?? 100);
}

export async function get(id: string) {
    return r.findById(id);
}

export async function create(dto: CreateAccountsPayableDto) {
    const data: Partial<AccountsPayable> = {
        //orderNumber: dto.orderNumber,
        vendorNumber: dto.vendorNumber,
        //sourceId: dto.sourceId,
        //totalAmount: dto.totalAmount,
        currency: dto.currency,
        //status: dto.status,
        //orderDate: dto.orderDate,
        createdAt: new Date(),
        updatedAt: new Date(),
    };

    return r.createOne(data);
}

export async function update(id: string, dto: UpdateAccountsPayableDto) {
    const patch: Partial<AccountsPayable> = {
        updatedAt: new Date(),
    };

    if (dto.updatedBy !== undefined) {
        patch.updatedBy = dto.updatedBy;
    }

    return r.updateOne(id, patch);
}

export async function remove(id: string) {
    await r.deleteOne(id);
}
