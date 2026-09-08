import type { Request, Response, NextFunction } from "express";
import { IdParamSchema } from "@/schemas/rebate.schema.js";
import { getRebateFiscalDetail } from "@/services/rebateDetail.service.js";

export async function getFiscalDetail(
    req: Request,
    res: Response,
    next: NextFunction
) {
    try {
        const { uuid } = IdParamSchema.parse(req.params);
        const detail = await getRebateFiscalDetail(uuid);

        res.json(detail);
    } catch (error) {
        next(error);
    }
}