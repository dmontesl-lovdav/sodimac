import { ShippingGuide } from "@/entities/ShippingGuide.entity.js";
import * as guides from "@/repositories/shippingGuide.repo.js";
import type {
    CreateShippingGuideDto,

} from "@/schemas/shippingGuide.schema.js";
import {

    type CreatePurchaseOrderDto,

} from "@/schemas/purchaseOrder.schema.js";
import { ResponseHandler } from '@/response/ResponseHandler.js';
import { StatusCodes, ReasonPhrases } from 'http-status-codes';
import * as svcShipping from "@/services/shippingGuide.service.js";
import * as svcPurchaseO from "@/services/purchaseOrder.service.js";
import { getDataSource } from "@/config/typeorm-datasource.js"; // Your DataSource instance
import * as svcAxios from "@/services/axios.service.js";
import 'dotenv/config';



export async function createGuia(dtoShipping: CreateShippingGuideDto[] = [], files: Express.Multer.File[], folder: string, origin: number) {
  const status = 1; //Nace Guia de embarque sin OC
  let created : any;
    await getDataSource().transaction( async (transactionalEntityManager) => {
    created = await svcShipping.create(dtoShipping, files, folder, origin, status, transactionalEntityManager);
    });
    return created;
}

export async function createOc(dtoPurchase: CreatePurchaseOrderDto, origin: number) {
  let created : any;
  await getDataSource().transaction( async (transactionalEntityManager) => {
      created = await svcPurchaseO.create(dtoPurchase, transactionalEntityManager, null, null, origin);
    
  });
  return created;
}


export async function createAll(dtoPurchase: CreatePurchaseOrderDto, files: Express.Multer.File[], folder: string, origin: number) {
  let created : any;
  await getDataSource().transaction( async (transactionalEntityManager) => {     
    const createdOC = await svcPurchaseO.create(dtoPurchase, transactionalEntityManager, files, folder, origin);
  });
  const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BBF?? "") +  process.env.CATALOGS_API_NEGOCIO + process.env.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS211);
  return ResponseHandler.responseBuilder(CatMsgExc.description ,null,0, StatusCodes.CREATED, true, "");
}



