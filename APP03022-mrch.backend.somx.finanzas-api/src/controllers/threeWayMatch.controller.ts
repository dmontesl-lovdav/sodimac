import type { Request, Response, NextFunction } from "express";
import * as svc from "@/services/threeWayMatchQuery.service.js";
import {
    ListThreeWayMatchQuerySchema,
    type ListThreeWayMatchQuery,
} from "@/schemas/threeWayMatch.schema.js";

import { initDataSource } from "@/config/typeorm-datasource.js";
import { runThreeWayMatch } from "@/services/threeWayMatch.service.js";
import { RunThreeWayMatchBodySchema, type RunThreeWayMatchBody } from "@/schemas/threeWayMatchRun.schema.js";
import { AuthenticatedRequest } from "@/middlewares/authToken.js";
import * as sharedCatalogService from "@/services/sharedCatalog.service.js";

function buildExportTimestamp(): string {
    const now = new Date();

    const yyyy = now.getFullYear();
    const mm = String(now.getMonth() + 1).padStart(2, "0");
    const dd = String(now.getDate()).padStart(2, "0");
    const hh = String(now.getHours()).padStart(2, "0");
    const min = String(now.getMinutes()).padStart(2, "0");
    const ss = String(now.getSeconds()).padStart(2, "0");

    return `${yyyy}${mm}${dd}_${hh}${min}${ss}`;
}

const WRN7029 = { success: false, code: "WRN7029", message: "El usuario no tiene configurado los atributos para el manejo de información, favor de validar con el administrador" };

function allowedVendors(req: Request): string[] | null | "wrn7029" {
    const sec = req.security;
    if (!sec) return null;
    if (Array.isArray(sec.vendors) && sec.vendors.length === 0) return "wrn7029";
    return sec.vendors;   // null = sin restricción, string[] = filtro
}

function securityTypeIds(req: Request): number[] {
    return (req.security?.types ?? [])
        .map((t) => Number(String(t).replace(/\D/g, "")))
        .filter((n) => !Number.isNaN(n) && n > 0);
}

async function securityGroupSuppliers(req: Request): Promise<string[] | null> {
    const groups = (req.security?.groups ?? []).map((g) => String(g).trim()).filter((g) => g.length > 0);
    if (groups.length === 0) return null;
    const suppliers = await sharedCatalogService.getActiveSupplierNumbersByGroups(groups);
    return suppliers.map(String);
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
        const result = await svc.list(q, vendors, securityTypeIds(req), await securityGroupSuppliers(req));
        res.json(result);
    } catch (e) {
        next(e);
    }
}

export async function exportCsv(
    req: AuthenticatedRequest,
    res: Response,
    next: NextFunction
) {
    try {
        const vendors = allowedVendors(req);
        if (vendors === "wrn7029") { res.status(400).json(WRN7029); return; }

        const q: ListThreeWayMatchQuery =
            ListThreeWayMatchQuerySchema.parse(req.query);

        const csv = await svc.exportCsv(q, vendors, securityTypeIds(req), req.authToken ?? "", await securityGroupSuppliers(req));

        const timestamp = buildExportTimestamp();

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
    req: AuthenticatedRequest,
    res: Response,
    next: NextFunction
) {
    try {
        const vendors = allowedVendors(req);
        if (vendors === "wrn7029") { res.status(400).json(WRN7029); return; }

        const q: ListThreeWayMatchQuery =
            ListThreeWayMatchQuerySchema.parse(req.query);

        const buffer = await svc.exportXlsx(q, vendors, securityTypeIds(req), req.authToken ?? "", await securityGroupSuppliers(req));

        const timestamp = buildExportTimestamp();

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