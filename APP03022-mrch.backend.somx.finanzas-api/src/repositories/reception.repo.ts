import { datasource } from "@/config/typeorm-datasource.js";
import { Reception } from "@/entities/Reception.entity.js";
import { PurchaseOrder } from "@/entities/PurchaseOrder.entity.js";
import { Addendum } from "@/entities/tenant_fiscal.addendum.entity.js"
import type {
ListReceptionQueryDto
} from "@/schemas/reception.schema.js";
import { z } from "zod/v4";
import { In, type FindOptionsWhere, Between, DeepPartial } from "typeorm";
import { Not } from 'typeorm';

export const repo = () => datasource.getRepository(Reception);

export async function createOne(dataR: Partial<Reception> | DeepPartial<Reception>, po: PurchaseOrder) {
    const entity = repo().create({...dataR, purchaseOrder: po});
    return entity;
}


// Crear VARIAS recepciones
export function createMany(dataList: Partial<Reception>[], po: PurchaseOrder) {
  const list = dataList.map(r => ({
    ...r,
    purchaseOrder: po,
  }));
  return repo().create(list); // Devuelve Reception[]
}


export async function findById(receptionId: string) {
    return repo().findOneBy({ receptionId });
}

export async function findByGuideNumber(guideNumber: string) {
    return repo().find({ where: { guideNumber: guideNumber.trim() } });
}

export async function updateManyByGuideNumber(
    guideNumber: string,
    patch: Partial<Reception>
): Promise<number> {
    const normalized = guideNumber?.trim();
    if (!normalized) return 0;
    const result = await repo().update({ guideNumber: normalized }, patch);
    return result.affected ?? 0;
}

export async function updateManyByPurchaseOrderIds(
    purchaseOrderIds: string[],
    patch: Partial<Reception>,
    guideNumber?: string
): Promise<number> {
    if (!purchaseOrderIds.length) return 0;

    const where: FindOptionsWhere<Reception> = {
        purchaseOrderId: In(purchaseOrderIds),
    };
    if (guideNumber?.trim()) {
        where.guideNumber = guideNumber.trim();
    }

    const result = await repo().update(where, patch);
    return result.affected ?? 0;
}

export async function findByVendorAndDateRange(vendorNumber: number, start: Date, end: Date): Promise<Reception[]> {
    return datasource.manager
        .createQueryBuilder(Reception, 'r')
        .leftJoinAndSelect('r.purchaseOrder', 'po')
        .where('po.supplierNumber = :vendorNumber', { vendorNumber })
        .andWhere('r.receptionDate BETWEEN :start AND :end', { start, end })
        .andWhere('r.status != 8')
        .orderBy('r.receptionDate', 'ASC')
        .take(500)
        .getMany();
}

export async function updateOne(receptionId: string, rec: Reception) {
    //await repo().update({ receptionId }, rec);
    await repo().save(rec);
    return findById(receptionId);
}

export async function findAll(purchaseOrderId : string){

    
return repo().createQueryBuilder('r')
.where('r.purchase_order_uuid::uuid = :id::uuid', { id: purchaseOrderId })
.getMany();

}

export async function findAllPaginated(filter: ListReceptionQueryDto, pageSize: number, pageNumber: number) {
    const skip = (pageNumber - 1) * pageSize; // Calculate the offset

        const whereClause: any = {};
        const purchaseOrder: any = {};

        whereClause.receptionDate = Between(filter.receptionDateAtInitial ?? Date, filter.receptionDateAtEnd ?? Date);
        if (filter.orderNumber !== undefined) {
            purchaseOrder.orderNumber = filter.orderNumber;
            whereClause.purchaseOrder = purchaseOrder;

        }
        if (filter.supplierNumber !== undefined) {
            purchaseOrder.supplierNumber = filter.supplierNumber;
            whereClause.purchaseOrder = purchaseOrder;

        }
        if (filter.createdAtInitial !== undefined && filter.createdAtEnd !== undefined) {

            const parsedDateEnd = z.coerce.date().parse(filter.createdAtEnd);
            parsedDateEnd.setDate(parsedDateEnd.getDate() + 1);
            filter.createdAtEnd = parsedDateEnd;

            whereClause.createdAt = Between(filter.createdAtInitial ?? Date, filter.createdAtEnd ?? Date);

        }
        if(filter.status !== undefined && filter.status != 8){  // status=8 Borrado logico
            whereClause.status = filter.status;
        } else {
            whereClause.status = Not(8);   //No se le permite ver al front las de estatus= 8 Borrado logico
        }
        if(filter.receptionId !== undefined){
            whereClause.receptionId = filter.receptionId;
        }

    //     console.log(whereClause);
    //    var [result, total] = await repo().findAndCount({ take: pageSize, skip: skip,
    //         relations: {
    //             purchaseOrder: true

    //         },
    //         select: {
    //             receptionId: true, originId: true, destinationId: true, amount: true,
    //             status: true, comment: true, receptionDate: true, createdBy: true, createdAt: true,
    //             purchaseOrder: {
    //                 orderNumber: true, supplierNumber: true, originId: true, amount: true, status: true,
    //                 createdBy: true, createdAt: true, receptions: false

    //             },
    //         },
    //         where:  whereClause  
    //     });


        const receptionQuery = await datasource.manager
        //.createQueryBuilder(PurchaseOrder, 'purchaseOrder')
        .createQueryBuilder(Reception, 'reception')
        .leftJoinAndSelect('reception.purchaseOrder','purchaseOrder')
        .leftJoinAndSelect('purchaseOrder.shippingGuidePurchaseOrders','shippingGuidePurchaseOrders')
        .leftJoinAndSelect('shippingGuidePurchaseOrders.shippingGuide','shippingGuide')
        // .leftJoinAndSelect('reception.receptionSkus','receptionSku')
        .leftJoinAndMapMany("reception.listAddendum", // The property on the User entity where the mapped Photo will be stored
                            Addendum,       // entity to be joined
                            "addendum",      //Alias of entity
                            'addendum.receptionNumber = reception.receptionNumber', // conditions of JOin
            )
        .leftJoinAndSelect('addendum.invoice', 'invoice');
        // .leftJoinAndMapOne("purchaseOrder.listSupplier", // The property on the User entity where the mapped Photo will be stored
        //                     Supplier,       // entity to be joined
        //                     "supplier",      //Alias of entity
        //                     'supplier.supplierNumber = purchaseOrder.supplierNumber', // conditions of JOin
        //     );
        
                //Las columnas de fechas son obligatorias
                receptionQuery.andWhere("reception.receptionDate BETWEEN :startDate AND :endDate " +
                                " AND reception.status != 8 "
                                    , { startDate: filter.receptionDateAtInitial,
                                        endDate: filter.receptionDateAtEnd
                                        });

        if (filter.orderNumber !== undefined) {
            receptionQuery.andWhere("purchaseOrder.orderNumber = :orderNumber", {
                orderNumber: filter.orderNumber,
            });
        }
        if (filter.supplierNumber !== undefined) {
            receptionQuery.andWhere("purchaseOrder.supplierNumber = :supplierNumber", {
                supplierNumber: filter.supplierNumber,
            });
        }
        if (typeof filter.status === "number" && filter.status !== 8) {
            receptionQuery.andWhere("reception.status = :status", { status: filter.status });
        }
        if (filter.receptionId !== undefined) {
            receptionQuery.andWhere("reception.receptionId = CAST(:receptionId AS uuid)", {
                receptionId: filter.receptionId,
            });
        }
        
                // Get the total count with pagination

        const total = await receptionQuery.getCount();
        const result = await receptionQuery.skip(skip).take(pageSize).getMany(); 

    return [result, total, result.length];
}