import type { Request, Response, NextFunction } from "express";

export interface SecurityContext {
    vendors: string[] | null;   // null = sin restricción (admin)
    types: string[] | null;
    groups: string[] | null;
}

const WRN7029 = {
    code: "WRN7029",
    message: "El usuario no tiene configurado los atributos para el manejo de información, favor de validar con el administrador",
};

function parseHeader(value: string | undefined): string[] | null | "empty" {
    if (value === undefined) return null;          // header ausente → sin restricción
    const trimmed = value.trim();
    if (trimmed === "") return "empty";            // header presente pero vacío → WRN7029
    if (trimmed === "-1") return null;             // -1 → acceso total → sin restricción
    return trimmed.split(",").map(v => v.trim()).filter(Boolean);
}

declare global {
    namespace Express {
        interface Request {
            security?: SecurityContext;
        }
    }
}

export function attachSecurityContext(req: Request, _res: Response, next: NextFunction) {
    const vendors = parseHeader(req.headers["x-user-vendors"] as string | undefined);
    const types   = parseHeader(req.headers["x-user-types"]   as string | undefined);
    const groups  = parseHeader(req.headers["x-user-groups"]  as string | undefined);

    req.security = {
        vendors: vendors === "empty" ? [] : vendors,
        types:   types   === "empty" ? [] : types,
        groups:  groups  === "empty" ? [] : groups,
    };

    next();
}

export function requireVendorAttribute(req: Request, res: Response, next: NextFunction) {
    if (!req.security) return next();
    if (Array.isArray(req.security.vendors) && req.security.vendors.length === 0) {
        res.status(400).json({ success: false, code: WRN7029.code, message: WRN7029.message });
        return;
    }
    next();
}
