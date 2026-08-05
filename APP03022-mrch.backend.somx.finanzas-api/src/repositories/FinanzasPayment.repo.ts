import { datasource } from "@/config/typeorm-datasource.js";
import { FinanzasPayment } from "@/entities/FinanzasPayment.entities.js";
import type { FindOptionsWhere } from "typeorm";

export const repo = () =>
    datasource.getRepository(FinanzasPayment);

export async function findAllPaginated(
    filter: FindOptionsWhere<FinanzasPayment>,
    pageSize: number,
    pageNumber: number
): Promise<[FinanzasPayment[], number, number]> {
    const start = Date.now();
    const skip = (pageNumber - 1) * pageSize;

    console.log(
        "[finanzas-payment][repo.findAllPaginated] START",
        {
            filter,
            pageSize,
            pageNumber,
            skip,
        }
    );

    const [result, total] =
        await repo().findAndCount({
            where: filter,
            take: pageSize,
            skip,
            order: {
                paymentDate: "DESC",
            },
        });

    console.log(
        "[finanzas-payment][repo.findAllPaginated] END",
        {
            elapsedMs: Date.now() - start,
            rows: result.length,
            total,
        }
    );

    return [
        result,
        total,
        result.length,
    ];
}

export async function countFinanzasPayment(
    filter: FindOptionsWhere<FinanzasPayment>
): Promise<number> {
    const start = Date.now();

    console.log(
        "[finanzas-payment][repo.countFinanzasPayment] START",
        {
            filter,
        }
    );

    const totalItems = await repo().count({
        where: filter,
    });

    console.log(
        "[finanzas-payment][repo.countFinanzasPayment] END",
        {
            elapsedMs: Date.now() - start,
            totalItems,
        }
    );

    return totalItems;
}

export async function findById(
    finanzasPaymentUuid: string
): Promise<FinanzasPayment | null> {
    const start = Date.now();

    console.log(
        "[finanzas-payment][repo.findById] START",
        {
            finanzasPaymentUuid,
        }
    );

    const result = await repo().findOneBy({
        finanzasPaymentUuid,
    });

    console.log(
        "[finanzas-payment][repo.findById] END",
        {
            elapsedMs: Date.now() - start,
            found: Boolean(result),
        }
    );

    return result;
}

export async function findAll(
    filter: FindOptionsWhere<FinanzasPayment>
): Promise<FinanzasPayment[]> {
    const start = Date.now();

    console.log(
        "[finanzas-payment][repo.findAll] START",
        {
            filter,
        }
    );

    const result = await repo().find({
        where: filter,
    });

    console.log(
        "[finanzas-payment][repo.findAll] END",
        {
            elapsedMs: Date.now() - start,
            rows: result.length,
        }
    );

    return result;
}

export async function createOne(
    data: Partial<FinanzasPayment>
): Promise<FinanzasPayment> {
    const start = Date.now();

    console.log(
        "[finanzas-payment][repo.createOne] START"
    );

    const entity = repo().create(data);
    const saved = await repo().save(entity);

    console.log(
        "[finanzas-payment][repo.createOne] END",
        {
            elapsedMs: Date.now() - start,
        }
    );

    return saved;
}

/**
 * Actualiza parcialmente un payment por su UUID primario.
 *
 * Regresa null cuando no existe el registro.
 */
export async function updateOne(
    finanzasPaymentUuid: string,
    patch: Partial<FinanzasPayment>
): Promise<FinanzasPayment | null> {
    const start = Date.now();

    console.log(
        "[finanzas-payment][repo.updateOne] START",
        {
            finanzasPaymentUuid,
            patchFields: Object.keys(patch),
        }
    );

    const result = await repo().update(
        {
            finanzasPaymentUuid,
        },
        patch
    );

    if (!result.affected) {
        console.log(
            "[finanzas-payment][repo.updateOne] END",
            {
                elapsedMs: Date.now() - start,
                affected: 0,
                found: false,
            }
        );

        return null;
    }

    const updated =
        await findById(finanzasPaymentUuid);

    console.log(
        "[finanzas-payment][repo.updateOne] END",
        {
            elapsedMs: Date.now() - start,
            affected: result.affected,
            found: Boolean(updated),
        }
    );

    return updated;
}

/**
 * Se conserva por compatibilidad con el PATCH anterior.
 *
 * El nuevo flujo debe preferir updateOne(finanzasPaymentUuid, patch).
 */
export async function updateStatus(
    filter: FindOptionsWhere<FinanzasPayment>,
    status: number
) {
    const start = Date.now();

    console.log(
        "[finanzas-payment][repo.updateStatus] START",
        {
            filter,
            status,
        }
    );

    const result = await repo().update(
        filter,
        {
            status,
        }
    );

    console.log(
        "[finanzas-payment][repo.updateStatus] END",
        {
            elapsedMs: Date.now() - start,
            affected: result.affected,
        }
    );

    return result;
}

/**
 * Se conserva para no romper llamadas existentes.
 *
 * Internamente utiliza updateOne.
 */
export async function update(
    finanzasPaymentUuid: string,
    patch: Partial<FinanzasPayment>
): Promise<FinanzasPayment | null> {
    const start = Date.now();

    console.log(
        "[finanzas-payment][repo.update] START",
        {
            finanzasPaymentUuid,
        }
    );

    const updated = await updateOne(
        finanzasPaymentUuid,
        patch
    );

    console.log(
        "[finanzas-payment][repo.update] END",
        {
            elapsedMs: Date.now() - start,
            found: Boolean(updated),
        }
    );

    return updated;
}

/**
 * Se conserva por compatibilidad con operaciones previas.
 */
export async function updateByFilter(
    filter: FindOptionsWhere<FinanzasPayment>,
    status: number
) {
    const start = Date.now();

    console.log(
        "[finanzas-payment][repo.updateByFilter] START",
        {
            filter,
            status,
        }
    );

    const result = await repo().update(
        filter,
        {
            status,
        }
    );

    console.log(
        "[finanzas-payment][repo.updateByFilter] END",
        {
            elapsedMs: Date.now() - start,
            affected: result.affected,
        }
    );

    return result;
}

export async function findByPaymentHeaderUuid(
    paymentHeaderUuid: string
): Promise<FinanzasPayment[]> {
    const start = Date.now();

    console.log(
        "[finanzas-payment][repo.findByPaymentHeaderUuid] START",
        {
            paymentHeaderUuid,
        }
    );

    const result = await repo().find({
        where: {
            paymentHeaderUuid,
        },
        order: {
            createdAt: "ASC",
        },
    });

    console.log(
        "[finanzas-payment][repo.findByPaymentHeaderUuid] END",
        {
            elapsedMs: Date.now() - start,
            rows: result.length,
        }
    );

    return result;
}

export async function findAllPaginatedByPaymentHeaderUuid(
    paymentHeaderUuid: string,
    pageSize: number,
    pageNumber: number
): Promise<[FinanzasPayment[], number, number]> {
    const start = Date.now();
    const skip = (pageNumber - 1) * pageSize;

    console.log(
        "[finanzas-payment][repo.findAllPaginatedByPaymentHeaderUuid] START",
        {
            paymentHeaderUuid,
            pageSize,
            pageNumber,
            skip,
        }
    );

    const [result, total] =
        await repo().findAndCount({
            where: {
                paymentHeaderUuid,
            },
            take: pageSize,
            skip,
            order: {
                createdAt: "ASC",
            },
        });

    console.log(
        "[finanzas-payment][repo.findAllPaginatedByPaymentHeaderUuid] END",
        {
            elapsedMs: Date.now() - start,
            rows: result.length,
            total,
        }
    );

    return [
        result,
        total,
        result.length,
    ];
}