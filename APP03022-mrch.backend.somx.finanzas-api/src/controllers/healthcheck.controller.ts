import type { Request, Response, NextFunction } from "express";
import * as svc from "@/services/healthcheck.service.js";
import { HealthcheckQuerySchema } from "@/schemas/healthcheck.schema.js";
import { HttpError } from "@/utils/HttpError.js";

export async function list(req: Request, res: Response, next: NextFunction) {
    try {
        HealthcheckQuerySchema.parse(req.query);

        const rows = await svc.list();

        if (!rows.length) {
            throw new HttpError(404, "No healthcheck records found");
        }

        res.json({
            alive: true,
            count: rows.length,
            data: rows,
        });
    } catch (e) {
        next(e);
    }
}