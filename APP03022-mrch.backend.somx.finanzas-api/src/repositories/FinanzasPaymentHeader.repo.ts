import { datasource } from "@/config/typeorm-datasource.js";
import { FinanzasPaymentHeader } from "@/entities/FinanzasPaymentHeader.entity.js";
import type { FindOptionsWhere } from "typeorm";

export const repo = () =>
    datasource.getRepository(FinanzasPaymentHeader);

export async function findById(
    paymentHeaderUuid: string
): Promise<FinanzasPaymentHeader | null> {
    return repo().findOneBy({
        paymentHeaderUuid,
    });
}

export async function findAll(
    filter: FindOptionsWhere<FinanzasPaymentHeader>
): Promise<FinanzasPaymentHeader[]> {
    return repo().find({
        where: filter,
        order: {
            paymentDate: "DESC",
            createdAt: "DESC",
        },
    });
}

export async function createOne(
    data: Partial<FinanzasPaymentHeader>
): Promise<FinanzasPaymentHeader> {
    const entity = repo().create(data);

    return repo().save(entity);
}

export async function updateOne(
    paymentHeaderUuid: string,
    patch: Partial<FinanzasPaymentHeader>
): Promise<FinanzasPaymentHeader | null> {
    const result = await repo().update(
        {
            paymentHeaderUuid,
        },
        patch
    );

    if (!result.affected) {
        return null;
    }

    return findById(paymentHeaderUuid);
}