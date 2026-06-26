import { getDataSource } from "@/config/typeorm-datasource.js";
import { Addendum } from "@/entities/tenant_fiscal.addendum.entity.js";

export const repo = () => getDataSource().getRepository(Addendum);

export async function findByInvoideUuid(uuid: string) {
    return repo().findOne({
        where: { invoiceUuid: uuid }
    });
}