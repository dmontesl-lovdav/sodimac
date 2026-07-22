import { createApiClient } from "@/services/ApiClient";

const utilUrl = process.env.CATALOGS_API_URL;
const api = createApiClient({ baseUrl: utilUrl });

export function syncFinanzasUser(): void {
    if (typeof window === "undefined") return;
    api.request<{ success?: boolean; message?: string }>("security/user-utility", "post", {}).catch(() => {
        /* sin UI; el back puede responder error si el token falta */
    });
}
