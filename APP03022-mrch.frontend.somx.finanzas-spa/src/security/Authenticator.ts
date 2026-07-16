import { localHomeStore } from '../store/localStore';

/** Estructura mínima del estado para tomar el token (ajústala si tu store tiene más) */
interface AppState {
    authentication?: {
        token?: string;
        groups?: string[]; // opcional si luego quieres validar grupos
    };
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
    const { defaultToken } = opts;

    const hasValidToken = async (): Promise<boolean> => {
        const token = getToken(defaultToken);
        if (!token) throw new Error('No token');
        return true;
    };

    return { isAdmin: hasValidToken, isProveedor: hasValidToken };
}
