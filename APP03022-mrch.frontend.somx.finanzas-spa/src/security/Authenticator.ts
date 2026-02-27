import { localHomeStore } from '../store/localStore';
import { jwtDecode } from 'jwt-decode';
import type { JwtPayload } from 'jwt-decode';

/** Estructura mínima del estado para tomar el token (ajústala si tu store tiene más) */
interface AppState {
    authentication?: {
        token?: string;
        groups?: string[]; // opcional si luego quieres validar grupos
    };
}

/** Claims típicos de un JWT + campos que podrías usar */
interface Claims extends JwtPayload {
    groups?: string[];
    [k: string]: unknown;
}

/** Helper seguro para obtener el token: defaultToken > store > null */
function getToken(defaultToken?: string): string | null {
    if (defaultToken && defaultToken !== '') return defaultToken;
    const state = localHomeStore.getState() as AppState;
    return state.authentication?.token ?? null;
}

/** Factoría de autenticador en modo funcional */
export function createAuthenticator(opts: {
    adminGroup: string;
    proveedorGroup: string;
    defaultToken?: string;
}) {
    const { adminGroup, proveedorGroup, defaultToken } = opts;

    const isAdmin = async (): Promise<boolean> => {
        const token = getToken(defaultToken);
        if (!token) throw new Error('No token');

        const decoded = jwtDecode<Claims>(token);
        // return Array.isArray(decoded.groups) && decoded.groups.includes(adminGroup);
        return true;
    };

    const isProveedor = async (): Promise<boolean> => {
        const token = getToken(defaultToken);
        if (!token) throw new Error('No token');

        const decoded = jwtDecode<Claims>(token);
        // return Array.isArray(decoded.groups) && decoded.groups.includes(proveedorGroup);
        return true;
    };

    return { isAdmin, isProveedor };
}
