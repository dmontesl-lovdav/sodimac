import {
    getDataSource,
} from "@/config/typeorm-datasource.js";

import {
    Rebate,
} from "@/entities/Rebate.entity.js";

import type {
    ListRebateQuery,
    RebateFilterDto,
} from "@/schemas/rebate.schema.js";

import {
    Between,
} from "typeorm";

import type {
    FindOptionsWhere,
} from "typeorm";

export const repo = () =>
    getDataSource().getRepository(
        Rebate
    );

export async function findAll(
    filter:
        FindOptionsWhere<Rebate>,
    limit = 100
) {
    return repo().find({
        where: filter,
        take: limit,
        order: {
            postingDate: "DESC",
        },
        relations: [
            "stampedRebate",
        ],
    });
}

/**
 * Consulta principal utilizada por
 * GET /rebates.
 */
export async function findWithListFilters(
    filters: ListRebateQuery
) {
    const queryBuilder =
        repo()
            .createQueryBuilder(
                "rebate"
            )
            .leftJoinAndSelect(
                "rebate.stampedRebate",
                "stampedRebate"
            );

    if (
        filters.vendorNumber !==
        undefined
    ) {
        queryBuilder.andWhere(
            "rebate.vendorNumber = :vendorNumber",
            {
                vendorNumber:
                    filters.vendorNumber,
            }
        );
    }

    if (
        filters.supplierType !==
        undefined
    ) {
        queryBuilder.andWhere(
            `
                EXISTS (
                    SELECT 1
                    FROM shared_catalogs.supplier supplier
                    WHERE
                        supplier.supplier_number::text =
                        CAST(
                            rebate.vendorNumber
                            AS TEXT
                        )
                    AND supplier.supplier_type_id =
                        :supplierType
                )
            `,
            {
                supplierType:
                    filters.supplierType,
            }
        );
    }

    if (
        filters.documentNumber
            ?.trim()
    ) {
        queryBuilder.andWhere(
            "rebate.documentNumber LIKE :documentNumber",
            {
                documentNumber:
                    `%${filters.documentNumber.trim()}%`,
            }
        );
    }

    if (
        filters.sapDocument
            ?.trim()
    ) {
        queryBuilder.andWhere(
            "rebate.sapDocument LIKE :sapDocument",
            {
                sapDocument:
                    `%${filters.sapDocument.trim()}%`,
            }
        );
    }

    if (
        filters.status !==
        undefined
    ) {
        queryBuilder.andWhere(
            "rebate.status = :status",
            {
                status:
                    filters.status,
            }
        );
    }

    if (
        filters.source !==
        undefined
    ) {
        queryBuilder.andWhere(
            "rebate.source = :source",
            {
                source:
                    filters.source,
            }
        );
    }

    if (
        filters.periodId !==
        undefined
    ) {
        queryBuilder.andWhere(
            "rebate.periodId = :periodId",
            {
                periodId:
                    filters.periodId,
            }
        );
    }

    if (
        filters.from &&
        filters.to
    ) {
        queryBuilder.andWhere(
            `
                rebate.postingDate
                BETWEEN :from AND :to
            `,
            {
                from: filters.from,
                to: filters.to,
            }
        );
    } else if (filters.from) {
        queryBuilder.andWhere(
            "rebate.postingDate >= :from",
            {
                from: filters.from,
            }
        );
    } else if (filters.to) {
        queryBuilder.andWhere(
            "rebate.postingDate <= :to",
            {
                to: filters.to,
            }
        );
    }

    const limit =
        filters.limit ?? 20;

    const page =
        filters.page ?? 0;

    queryBuilder
        .orderBy(
            "rebate.postingDate",
            "DESC"
        )
        .take(limit)
        .skip(
            page * limit
        );

    return queryBuilder.getMany();
}

export async function findById(
    id: string
) {
    return repo().findOne({
        where: {
            rebateId: id,
        },
        relations: [
            "stampedRebate",
        ],
    });
}

export async function findByDocumentNumber(
    documentNumber: string
) {
    return repo().find({
        where: {
            documentNumber,
        },
        relations: [
            "stampedRebate",
        ],
    });
}

/**
 * Buscar todos los rebates publicados.
 */
export async function findAllPublished() {
    return repo().find({
        where: {
            status: 1,
        },
        order: {
            postingDate: "DESC",
        },
        relations: [
            "stampedRebate",
        ],
    });
}

export async function findByVendorAndPostingDateRange(
    vendorNumber: number,
    start: Date,
    end: Date
): Promise<Rebate[]> {
    return repo().find({
        where: {
            vendorNumber,
            postingDate:
                Between(
                    start,
                    end
                ) as any,
        },
        order: {
            postingDate: "DESC",
        },
        take: 500,
    });
}

/**
 * Búsqueda utilizada por /rebates/search
 * y la exportación filtrada.
 */
export async function findWithDynamicFilters(
    filters: RebateFilterDto
) {
    const queryBuilder =
        repo()
            .createQueryBuilder(
                "rebate"
            )
            .leftJoinAndSelect(
                "rebate.stampedRebate",
                "stampedRebate"
            );

    if (
        filters.vendorNumber !==
        undefined
    ) {
        queryBuilder.andWhere(
            "rebate.vendorNumber = :vendorNumber",
            {
                vendorNumber:
                    filters.vendorNumber,
            }
        );
    }

    if (
        filters.supplierType !==
        undefined
    ) {
        queryBuilder.andWhere(
            `
                EXISTS (
                    SELECT 1
                    FROM shared_catalogs.supplier supplier
                    WHERE
                        supplier.supplier_number::text =
                        CAST(
                            rebate.vendorNumber
                            AS TEXT
                        )
                    AND supplier.supplier_type_id =
                        :supplierType
                )
            `,
            {
                supplierType:
                    filters.supplierType,
            }
        );
    }

    if (
        filters.documentNumber
            ?.trim()
    ) {
        queryBuilder.andWhere(
            "rebate.documentNumber LIKE :documentNumber",
            {
                documentNumber:
                    `%${filters.documentNumber.trim()}%`,
            }
        );
    }

    if (
        filters.sapDocument
            ?.trim()
    ) {
        queryBuilder.andWhere(
            "rebate.sapDocument LIKE :sapDocument",
            {
                sapDocument:
                    `%${filters.sapDocument.trim()}%`,
            }
        );
    }

    if (
        filters.status !==
        undefined
    ) {
        queryBuilder.andWhere(
            "rebate.status = :status",
            {
                status:
                    filters.status,
            }
        );
    }

    if (
        filters.source !==
        undefined
    ) {
        queryBuilder.andWhere(
            "rebate.source = :source",
            {
                source:
                    filters.source,
            }
        );
    }

    if (
        filters.periodId !==
        undefined
    ) {
        queryBuilder.andWhere(
            "rebate.periodId = :periodId",
            {
                periodId:
                    filters.periodId,
            }
        );
    }

    if (
        filters.from &&
        filters.to
    ) {
        queryBuilder.andWhere(
            `
                rebate.postingDate
                BETWEEN :from AND :to
            `,
            {
                from:
                    filters.from,
                to:
                    filters.to,
            }
        );
    } else if (filters.from) {
        queryBuilder.andWhere(
            "rebate.postingDate >= :from",
            {
                from:
                    filters.from,
            }
        );
    } else if (filters.to) {
        queryBuilder.andWhere(
            "rebate.postingDate <= :to",
            {
                to:
                    filters.to,
            }
        );
    }

    const limit =
        filters.limit ?? 20;

    const page =
        filters.page ?? 0;

    queryBuilder
        .orderBy(
            "rebate.postingDate",
            "DESC"
        )
        .take(limit)
        .skip(
            page * limit
        );

    return queryBuilder.getMany();
}

export async function createOne(
    data: Partial<Rebate>
) {
    const entity =
        repo().create(data);

    return repo().save(entity);
}

export async function updateOne(
    id: string,
    patch: Partial<Rebate>
) {
    await repo().update(
        {
            rebateId: id,
        },
        patch
    );

    return findById(id);
}

export async function deleteOne(
    id: string
) {
    await repo().delete({
        rebateId: id,
    });
}