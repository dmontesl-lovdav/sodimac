import type { Request, Response, NextFunction } from "express";
import * as sharedCatalogService from "@/services/sharedCatalog.service.js";

export interface SecurityContext {
    vendors: string[] | null; // null = sin restricción (admin)
    types: string[] | null;
    groups: string[] | null;
    rebates: string[] | null; 
}

declare module "express-serve-static-core" {
    interface Request {
        security?: SecurityContext;
    }
}

const WRN7029 = {
    code: "WRN7029",
    message:
        "El usuario no tiene configurado los atributos para el manejo de información, favor de validar con el administrador",
};

function parseHeader(value: string | undefined): string[] | null | "empty" {
    if (value === undefined) return null;
    const trimmed = value.trim();
    if (trimmed === "") return "empty";
    if (trimmed === "-1") return null;

    return trimmed
        .split(",")
        .map((v) => v.trim())
        .filter(Boolean);
}

export async function attachSecurityContext(
    req: Request,
    _res: Response,
    next: NextFunction,
) {
    const vendors = parseHeader(req.headers["x-user-vendors"] as string | undefined);
    const types = parseHeader(req.headers["x-user-types"] as string | undefined);
    const groups = parseHeader(req.headers["x-user-groups"] as string | undefined);
    const rebates = parseHeader(req.headers["x-user-rebates"] as string | undefined);
    const rfcs = parseHeader(req.headers["x-user-rfcs"] as string | undefined);

    let effectiveVendors: string[] | null = vendors === "empty" ? [] : vendors;

    const rfcList = rfcs === "empty" ? [] : rfcs;
    if (rfcList && rfcList.length > 0) {
        try {
            const rfcSuppliers = await sharedCatalogService.getSupplierNumbersByRfcs(rfcList);
            if (rfcSuppliers.length > 0) {
                if (effectiveVendors === null) {
                    effectiveVendors = rfcSuppliers;
                } else {
                    const allowed = new Set(rfcSuppliers.map(String));
                    effectiveVendors = effectiveVendors.filter((v) => allowed.has(String(v)));
                }
            }
        } catch {
        }
    }

    req.security = {
        vendors: effectiveVendors,
        types: types === "empty" ? [] : types,
        groups: groups === "empty" ? [] : groups,
        rebates: rebates === "empty" ? [] : rebates,
    };

    next();
}

export function requireVendorAttribute(
    req: Request,
    res: Response,
    next: NextFunction,
) {
    if (!req.security) return next();

    if (Array.isArray(req.security.vendors) && req.security.vendors.length === 0) {
        res.status(400).json({
            success: false,
            code: WRN7029.code,
            message: WRN7029.message,
        });
        return;
    }

    next();
}