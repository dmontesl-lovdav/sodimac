import axios from 'axios';
import { localHomeStore } from '@/store/localStore';

interface AccessContextEvent {
    key: string;
    name?: string;
}

interface AccessContextApp {
    key: string;
    name?: string;
    events?: AccessContextEvent[];
}

interface AccessContextEntity {
    key: string;
    name?: string;
}

interface AccessContextPermission extends AccessContextEntity {
    rol?: AccessContextEntity;
}

export interface AccessContext {
    user?: { key?: string; name?: string; email?: string };
    profiles?: AccessContextEntity[];
    apps?: AccessContextApp[];
    roles?: AccessContextEntity[];
    permissions?: AccessContextPermission[];
    providers?: AccessContextPermission[];
}

interface ApiEnvelope<T> {
    success?: boolean;
    data?: T;
    message?: string;
}

function resolveBaseUrl(): string {
    const explicit =
        process.env.UTIL_SECURITY_API_URL ||
        process.env.REACT_APP_UTIL_SECURITY_API_URL ||
        process.env.CATALOGS_API_URL ||
        process.env.REACT_APP_CATALOGS_API_URL ||
        '';
    return explicit.replace(/\/+$/, '');
}

function resolveToken(): string | null {
    const state = localHomeStore.getState() as any;
    const storeToken =
        state?.authentication?.token || state?.authentication?.idToken;
    if (typeof storeToken === 'string' && storeToken.trim()) return storeToken;
    const envToken =
        process.env.REACT_APP_AUTH_DEFAULT_TOKEN || process.env.AUTH_DEFAULT_TOKEN;
    return envToken?.trim() ? envToken : null;
}

export const securityService = {
    async getAccessContext(userKey: string): Promise<AccessContext | null> {
        const base = resolveBaseUrl();
        if (!base || !userKey) return null;

        const token = resolveToken();
        const url = `${base}/security/user-details/${encodeURIComponent(userKey)}`;

        const headers: Record<string, string> = { Accept: 'application/json' };
        if (token) headers.Authorization = `Bearer ${token}`;

        const res = await axios.get<ApiEnvelope<AccessContext>>(url, {
            headers,
            timeout: 15000,
        });
        const body = res.data;
        if (body && typeof body === 'object' && 'data' in body && body.data) {
            return body.data as AccessContext;
        }
        return (body as unknown as AccessContext) ?? null;
    },
};
