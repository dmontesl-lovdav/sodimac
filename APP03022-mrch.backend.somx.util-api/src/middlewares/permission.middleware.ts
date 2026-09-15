import type { Request, Response, NextFunction } from 'express';
import * as securityRepo from '@/repositories/security.repo.js';

function resolveUserKey(req: Request): string | null {
    const raw = req.header('authorization') ?? req.header('Authorization');
    if (!raw) return null;
    const token = raw.startsWith('Bearer ') ? raw.slice(7) : raw;
    const parts = token.split('.');
    if (parts.length < 2 || !parts[1]) return null;
    try {
        const payload = JSON.parse(Buffer.from(parts[1], 'base64url').toString('utf8')) as Record<string, unknown>;
        const sub = payload.sub ?? payload.preferred_username ?? payload.email;
        return sub ? String(sub) : null;
    } catch {
        return null;
    }
}

export function requirePermission(eventKey: string) {
    return async (req: Request, res: Response, next: NextFunction) => {
        try {
            const userKey = resolveUserKey(req);
            if (!userKey) {
                return res.status(403).json({ error: 'No autorizado: no se pudo identificar al usuario.' });
            }
            const allowed = await securityRepo.userHasPermissionForEvent(userKey, eventKey);
            if (!allowed) {
                return res.status(403).json({ error: 'El rol asignado no cuenta con el permiso requerido para esta acción.' });
            }
            return next();
        } catch (err) {
            return next(err);
        }
    };
}
