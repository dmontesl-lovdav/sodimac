import { localHomeStore } from '@/store/localStore';

interface DecodedToken {
    sub?: string;
    preferred_username?: string;
    email?: string;
}

export function getCurrentUserKey(): string {
    if (typeof window !== 'undefined') {
        const w = window as unknown as { __FINANZAS_USER_KEY__?: string };
        if (w.__FINANZAS_USER_KEY__) return w.__FINANZAS_USER_KEY__;
    }

    try {
        const state = localHomeStore.getState() as unknown as {
            authentication?: { tokenDecoded?: DecodedToken };
        };
        const decoded = state.authentication?.tokenDecoded;
        if (decoded?.sub) return decoded.sub;
        if (decoded?.preferred_username) return decoded.preferred_username;
    } catch {
    }

    if (typeof process !== 'undefined' && process.env.FINANZAS_CURRENT_USER_KEY) {
        return process.env.FINANZAS_CURRENT_USER_KEY;
    }
    return '';
}
