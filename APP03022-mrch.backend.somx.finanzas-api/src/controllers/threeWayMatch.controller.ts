import type { Request, Response, NextFunction } from "express";
import * as svc from "@/services/threeWayMatchQuery.service.js";
import {
    ListThreeWayMatchQuerySchema,
    type ListThreeWayMatchQuery,
} from "@/schemas/threeWayMatch.schema.js";

import { initDataSource } from "@/config/typeorm-datasource.js";
import { runThreeWayMatch } from "@/services/threeWayMatch.service.js";
import { RunThreeWayMatchBodySchema, type RunThreeWayMatchBody } from "@/schemas/threeWayMatchRun.schema.js";

const WRN7029 = { success: false, code: "WRN7029", message: "El usuario no tiene configurado los atributos para el manejo de información, favor de validar con el administrador" };

function allowedVendors(req: Request): string[] | null | "wrn7029" {
    const sec = req.security;
    if (!sec) return null;
    if (Array.isArray(sec.vendors) && sec.vendors.length === 0) return "wrn7029";
    return sec.vendors;   // null = sin restricción, string[] = filtro
}

export async function list(
    req: Request,
    res: Response,
    next: NextFunction
) {
    try {
        const vendors = allowedVendors(req);
        if (vendors === "wrn7029") { res.status(400).json(WRN7029); return; }

        const q: ListThreeWayMatchQuery = ListThreeWayMatchQuerySchema.parse(req.query);
        const result = await svc.list(q, vendors);
        res.json(result);
    } catch (e) {
        next(e);
    }
}

export async function exportCsv(
    req: Request,
    res: Response,
    next: NextFunction
) {
    try {
        const vendors = allowedVendors(req);
        if (vendors === "wrn7029") { res.status(400).json(WRN7029); return; }

        const q: ListThreeWayMatchQuery =
            ListThreeWayMatchQuerySchema.parse(req.query);

        const csv = await svc.exportCsv(q, vendors);

        const timestamp = new Date().toISOString().replace(/[:.]/g, "-");

        res.setHeader("Content-Type", "text/csv; charset=utf-8");
        res.setHeader(
            "Content-Disposition",
            `attachment; filename="three_way_match_${timestamp}.csv"`
        );

        res.send(csv);
    } catch (e) {
        next(e);
    }
}

export async function exportXlsx(
    req: Request,
    res: Response,
    next: NextFunction
) {
    try {
        const vendors = allowedVendors(req);
        if (vendors === "wrn7029") { res.status(400).json(WRN7029); return; }

        const q: ListThreeWayMatchQuery =
            ListThreeWayMatchQuerySchema.parse(req.query);

        const buffer = await svc.exportXlsx(q, vendors);

        const timestamp = new Date().toISOString().replace(/[:.]/g, "-");

        res.setHeader(
            "Content-Type",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        res.setHeader(
            "Content-Disposition",
            `attachment; filename="three_way_match_${timestamp}.xlsx"`
        );

        res.send(buffer);
    } catch (e) {
        next(e);
    }
}

// ✅ POST /three-way-match/run
export async function run(
    req: Request,
    res: Response,
    next: NextFunction
) {
    try {
        await initDataSource();

        const body: RunThreeWayMatchBody = RunThreeWayMatchBodySchema.parse(req.body ?? {});

        const fechaBase =
            body.fechaBase ??
            (() => {
                const d = new Date();
                d.setDate(d.getDate() - 1);
                return d;
            })();

        const intento = body.intento ?? 1;

        await runThreeWayMatch(fechaBase, intento);

        res.json({
            ok: true,
            message: "ThreeWayMatch executed",
            fechaBase: fechaBase.toISOString(),
            intento,
        });
    } catch (e) {
        next(e);
    }
}