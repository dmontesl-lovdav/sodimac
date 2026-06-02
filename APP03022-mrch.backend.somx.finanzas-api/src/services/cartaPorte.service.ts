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
import * as constants from "@/constants/catalogConstantsCodes.js";
import type { Request } from "express";



export async function createGuia(req: Request, dtoShipping: CreateShippingGuideDto[] = [], files: Express.Multer.File[],  origin: number, token: string, folder?: string) {
  const status = 1; //Nace Guia de embarque sin OC
  let created : any;
    await getDataSource().transaction( async (transactionalEntityManager) => {
    created = await svcShipping.create(req, dtoShipping, files, origin, status, transactionalEntityManager, token, folder);
    });
    return created;
}

export async function createOc(req: Request,dtoPurchase: CreatePurchaseOrderDto, origin: number, token: string) {
  let created : any;
  await getDataSource().transaction( async (transactionalEntityManager) => {
      created = await svcPurchaseO.create(req, dtoPurchase, transactionalEntityManager,null, origin, token, undefined);
    
  });
  return created;
}


export async function createAll(req: Request, dtoPurchase: CreatePurchaseOrderDto, files: Express.Multer.File[],  origin: number, token: string, folder?: string) {
  let created : any;
  await getDataSource().transaction( async (transactionalEntityManager) => {     
    const createdOC = await svcPurchaseO.create(req, dtoPurchase, transactionalEntityManager, files, origin, token, folder);
  });
  const CatMsgExc = await svcAxios.GetCatalogDetail((process.env.CATALOGS_API_URL_BFF?? "") +  constants.CatalogNegocio.CATALOGS_API_NEGOCIO + constants.CatalogNegocio.CATALOGS_API_NEGOCIO_DETAILS_KEY_BUS211, token);
  return ResponseHandler.responseBuilder(CatMsgExc.description ,null,0, StatusCodes.CREATED, true, "");
}



