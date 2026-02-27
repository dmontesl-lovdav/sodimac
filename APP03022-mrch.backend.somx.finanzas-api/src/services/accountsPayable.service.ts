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

    //if (q.status !== undefined) filter.status = q.status;
    if (q.vendorNumber !== undefined) filter.vendorNumber = q.vendorNumber;

    //if (q.from && q.to) filter.orderDate = Between(q.from, q.to);
    //else if (q.from) filter.orderDate = MoreThanOrEqual(q.from);
    //else if (q.to) filter.orderDate = LessThanOrEqual(q.to);

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

    if (dto.deliveryDate !== undefined && dto.deliveryDate !== null) {
        //data.deliveryDate = dto.deliveryDate;
    }
    if (dto.terms !== undefined) {
        //data.terms = dto.terms;
    }
    if (dto.createdBy !== undefined) {
        //data.createdBy = dto.createdBy;
    }

    return r.createOne(data);
}

export async function update(id: string, dto: UpdateAccountsPayableDto) {
    const patch: Partial<AccountsPayable> = {
        updatedAt: new Date(),
    };

    // if (dto.orderNumber !== undefined) patch.orderNumber = dto.orderNumber;
    // if (dto.vendorNumber !== undefined) patch.vendorNumber = dto.vendorNumber;
    // if (dto.sourceId !== undefined) patch.sourceId = dto.sourceId;
    // if (dto.totalAmount !== undefined) patch.totalAmount = dto.totalAmount;
    // if (dto.currency !== undefined) patch.currency = dto.currency;
    // if (dto.status !== undefined) patch.status = dto.status;
    // if (dto.orderDate !== undefined) patch.orderDate = dto.orderDate;

    if (dto.deliveryDate !== undefined && dto.deliveryDate !== null) {
        //patch.deliveryDate = dto.deliveryDate; // no asignar undefined
    }
    if (dto.terms !== undefined) {
        //patch.terms = dto.terms; // no usar ?? undefined
    }
    if (dto.createdBy !== undefined) {
        //patch.createdBy = dto.createdBy;
    }
    if (dto.updatedBy !== undefined) {
        patch.updatedBy = dto.updatedBy;
    }

    return r.updateOne(id, patch);
}

export async function remove(id: string) {
    await r.deleteOne(id);
}
