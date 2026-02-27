import { datasource } from "@/config/typeorm-datasource.js";
import { Reception } from "@/entities/Reception.entity.js";
import { Addendum } from "@/entities/tenant_fiscal.addendum.entity.js"
import { Between,  } from "typeorm";
import type {
ListReceptionQueryDto
} from "@/schemas/reception.schema.js";
import { number, z } from "zod/v4";

export const repo = () => datasource.getRepository(Reception);

export async function findById(receptionId: string) {
    return repo().findOneBy({ receptionId });
}

export async function updateOne(receptionId: string, rec: Reception) {
    await repo().update({ receptionId }, rec);
    return findById(receptionId);
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
            whereClause.status != 8;   //No se le permite ver al front las de estatus= 8 Borrado logico
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
                                " AND reception.status != 8 "  //No incluye las Recepciones borradas
                                    , { startDate: filter.receptionDateAtInitial,
                                        endDate: filter.receptionDateAtEnd
                                        });
        
                // Get the total count with pagination

        const total = await receptionQuery.getCount();
        const result = await receptionQuery.skip(skip).take(pageSize).getMany(); 

    return [result, total, result.length];
}