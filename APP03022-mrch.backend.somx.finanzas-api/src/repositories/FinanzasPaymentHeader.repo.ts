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

/**
 * Obtiene las cabeceras de pago de forma paginada.
 *
 * La pantalla principal de pagos trabaja a nivel cabecera,
 * por lo que cada payment_header representa un único registro.
 */
export async function findAllPaginated(
    filter: FindOptionsWhere<FinanzasPaymentHeader>,
    pageSize: number,
    pageNumber: number
): Promise<
    [
        FinanzasPaymentHeader[],
        number,
        number
    ]
> {
    const skip =
        (pageNumber - 1) * pageSize;

    const [result, total] =
        await repo().findAndCount({
            where: filter,
            take: pageSize,
            skip,
            order: {
                paymentDate: "DESC",
                createdAt: "DESC",
            },
        });

    return [
        result,
        total,
        result.length,
    ];
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