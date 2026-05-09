import type { Request, Response, NextFunction } from "express";

export interface SecurityContext {
    vendors: string[] | null;   // null = sin restriccion (admin/dev)
    types: string[] | null;
    groups: string[] | null;
}

interface CachedContext {
    data: SecurityContext;
    expiresAt: number;
}

const WRN7029 = {
    code: "WRN7029",
    message: "El usuario no tiene configurado los atributos para el manejo de información, favor de validar con el administrador",
};

const SECURITY_CACHE = new Map<string, CachedContext>();
const SECURITY_CACHE_TTL_MS = 5 * 60 * 1000;

declare global {
    namespace Express {
        interface Request {
            security?: SecurityContext;
            userSub?: string;
        }
    }
}

function isSecurityEnabled(): boolean {
    return String(process.env.SECURITY_ENABLED).toLowerCase() === "true";
}

function getUtilApiUrl(): string {
    return process.env.UTIL_API_URL || "http://localhost:3712";
}

function decodeJwtSub(token: string): string | null {
    try {
        const parts = token.split(".");
        if (parts.length !== 3) return null;
        const payloadPart = parts[1];
        if (!payloadPart) return null;
        const payload = JSON.parse(Buffer.from(payloadPart, "base64url").toString("utf8"));
        return payload.sub || payload.preferred_username || null;
    } catch {
        return null;
    }
}

function extractBearerToken(req: Request): string | null {
    const auth = req.headers.authorization;
    if (!auth || !auth.startsWith("Bearer ")) return null;
    const token = auth.slice("Bearer ".length).trim();
    return token || null;
}

function buildSecurityContext(attributes: Array<{ typeKey: string; valueKey: string | null }>): SecurityContext {
    const valuesFor = (typeKey: string): string[] | null => {
        const values = attributes
            .filter(a => a.typeKey === typeKey)
            .map(a => a.valueKey)
            .filter((v): v is string => Boolean(v));
        if (values.length === 0) return [];
        if (values.includes("-1")) return null;
        return values;
    };

    return {
        vendors: valuesFor("ATR001"),
        types: valuesFor("ATR002"),
        groups: valuesFor("ATR004"),
    };
}

async function fetchSecurityContext(sub: string): Promise<SecurityContext | null> {
    const now = Date.now();
    const cached = SECURITY_CACHE.get(sub);
    if (cached && cached.expiresAt > now) return cached.data;

    try {
        const url = `${getUtilApiUrl()}/api/security/user-attributes-by-key/${encodeURIComponent(sub)}`;
        const res = await fetch(url);
        if (!res.ok) {
            console.warn(`[security] util-api returned ${res.status} for sub=${sub}`);
            return null;
        }
        const body: { data?: { attributes?: Array<{ typeKey: string; valueKey: string | null }> } } = await res.json();
        const attributes = body.data?.attributes ?? [];

        const ctx = buildSecurityContext(attributes);
        SECURITY_CACHE.set(sub, { data: ctx, expiresAt: now + SECURITY_CACHE_TTL_MS });
        return ctx;
    } catch (err) {
        console.warn(`[security] util-api fetch error for sub=${sub}:`, err instanceof Error ? err.message : err);
        return null;
    }
}

export async function attachSecurityContext(req: Request, res: Response, next: NextFunction): Promise<void> {
    if (!isSecurityEnabled()) {
        req.security = { vendors: null, types: null, groups: null };
        return next();
    }

    const token = extractBearerToken(req);
    if (!token) {
        res.status(401).json({ success: false, message: "Authorization header required" });
        return;
    }

    const sub = decodeJwtSub(token);
    if (!sub) {
        res.status(401).json({ success: false, message: "Invalid token" });
        return;
    }

    req.userSub = sub;
    const ctx = await fetchSecurityContext(sub);

    if (!ctx) {
        req.security = { vendors: null, types: null, groups: null };
        return next();
    }

    req.security = ctx;
    next();
}

export function requireVendorAttribute(req: Request, res: Response, next: NextFunction): void {
    if (!req.security) return next();
    if (Array.isArray(req.security.vendors) && req.security.vendors.length === 0) {
        res.status(400).json({ success: false, code: WRN7029.code, message: WRN7029.message });
        return;
    }
    next();
}

export function _clearSecurityCache(): void {
    SECURITY_CACHE.clear();
}
