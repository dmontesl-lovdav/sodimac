import type { Request, Response, NextFunction } from "express";
import { IdParamSchema } from "@/schemas/rebate.schema.js";
import { RebateOrdersSchema, syncRebateOrders, getRebateOrders } from "@/services/rebateOrders.service.js";

export async function syncOrders(req: Request, res: Response, next: NextFunction) {
    try { res.json(await syncRebateOrders(RebateOrdersSchema.parse(req.body))); }
    catch (error) { next(error); }
}
export async function getOrders(req: Request, res: Response, next: NextFunction) {
    try {
        const { uuid } = IdParamSchema.parse(req.params);
        res.json(await getRebateOrders(uuid));
    } catch (error) { next(error); }
}
