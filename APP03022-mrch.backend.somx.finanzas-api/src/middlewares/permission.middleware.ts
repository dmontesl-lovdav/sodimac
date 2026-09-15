import type { Response, NextFunction } from "express";
import { getDataSource } from "@/config/typeorm-datasource.js";
import type { AuthenticatedRequest } from "@/middlewares/authToken.js";

function decodeUserKey(token?: string): string | null {
    if (!token) return null;
    const parts = token.split(".");
    if (parts.length < 2 || !parts[1]) return null;
    try {
        const payload = JSON.parse(Buffer.from(parts[1], "base64url").toString("utf8")) as Record<string, unknown>;
        const sub = payload.sub ?? payload.preferred_username ?? payload.email;
        return sub ? String(sub) : null;
    } catch {
        return null;
    }
}

async function userHasPermissionForEvent(userKey: string, eventKey: string): Promise<boolean> {
    const rows = await getDataSource().query(
        `SELECT 1
         FROM shared_catalogs.catalog_detail ev
         JOIN shared_catalogs.catalog_header hev ON hev.id = ev.header_id AND hev.code = 'CatEvento'
         JOIN core_security.event_permission epn ON epn.catalog_detail_process_id = ev.id AND epn.status = 1
         JOIN core_security.role_permission rp ON rp.catalog_detail_permission_id = epn.catalog_detail_permission_id AND rp.status = 1
         JOIN core_security.role_user ru ON ru.catalog_detail_role_id = rp.catalog_detail_role_id AND ru.status = 1
         JOIN core_security.user_data ud ON ud.user_data_id = ru.user_data_id AND ud.status = 1
         WHERE ev.key = $1
           AND (ud.sub = $2 OR ud.preferred_username = $2 OR ud.email = $2)
         LIMIT 1`,
        [eventKey, userKey],
    );
    return Array.isArray(rows) && rows.length > 0;
}

export function requirePermission(eventKey: string) {
    return async (req: AuthenticatedRequest, res: Response, next: NextFunction) => {
        try {
            const userKey = decodeUserKey(req.authToken);
            if (!userKey) {
                res.status(403).json({ success: false, message: "No autorizado: no se pudo identificar al usuario." });
                return;
            }
            const allowed = await userHasPermissionForEvent(userKey, eventKey);
            if (!allowed) {
                res.status(403).json({ success: false, message: "El rol asignado no cuenta con el permiso requerido para esta acción." });
                return;
            }
            next();
        } catch (err) {
            next(err);
        }
    };
}
