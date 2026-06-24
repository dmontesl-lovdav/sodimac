// ✅ FILE: src/features/shipping-guides/api/ShippingGuideClient.ts
import { createApiClient } from "@/services/ApiClient";
import {
    ShippingGuide,
    ShippingGuideDetail,
    ShippingGuideFilter,
    ShippingGuideStatusHistory,
    CancelShippingGuidesPayload,
    UpdateShippingGuideStatusPayload,
} from "../interfaces";

const api = createApiClient();
const ROUTE = "shipping-guide";

// Esto evita enviar variables vacías, nulas o mal formadas que suelen causar el Error 500.
function toQuery(params: any = {}) {
    const sp = new URLSearchParams();

    Object.entries(params).forEach(([k, v]) => {
        if (v === undefined || v === null || v === "") return;

        if (Array.isArray(v)) {
            v.forEach((item) => {
                if (item === undefined || item === null || item === "") return;
                sp.append(k, String(item));
            });
            return;
        }

        sp.append(k, String(v));
    });

    return sp.toString();
}

export const shippingGuideService = {
    async get(
        filter: ShippingGuideFilter,
        binary?: boolean
    ): Promise<ShippingGuide[]> {
        // Construimos un objeto limpio con los parámetros
        const params: any = {};

        if (filter.guideNumber) params.guideNumber = filter.guideNumber;

        // Mapeo doble (mantenido de tu código original por si el backend pide ambos)
        if (filter.vendorNumber) {
            params.vendorNumber = filter.vendorNumber;
            params.supplierNumber = filter.vendorNumber;
        }
        if (filter.sourceId) {
            params.sourceId = filter.sourceId;
            params.origin = filter.sourceId;
        }
        if (filter.truckPlate) {
            params.truckPlate = filter.truckPlate;
            params.plate = filter.truckPlate;
        }

        if (filter.trailerPlate) params.trailerPlate = filter.trailerPlate;
        if (filter.deliveryType) params.deliveryType = filter.deliveryType;
        if (filter.status != null && !Number.isNaN(Number(filter.status))) {
            params.status = filter.status;
        }

        if (filter.from) params.from = filter.from;
        if (filter.to) params.to = filter.to;
        params.pageNumber = 1;
        params.pageSize = 500;

        const query = toQuery(params);
        const queryString = query ? `?${query}` : "";

        if (binary) {
            await api.requestBinary(`${ROUTE}/csv${queryString}`, "get");
            return [];
        }

        console.log(queryString);

        const raw = await api.request<unknown>(`${ROUTE}${queryString}`, "get");

        let content: ShippingGuide[] = [];
        if (Array.isArray(raw)) {
            content = raw;
        } else if (raw && typeof raw === "object") {
            const o = raw as Record<string, unknown>;
            const data = o.data as Record<string, unknown> | undefined;
            if (Array.isArray(data?.content)) {
                content = data.content as ShippingGuide[];
            } else if (Array.isArray(o.content)) {
                content = o.content as ShippingGuide[];
            }
        }

        return content;
    },

    async getDetail(
        shippingGuideId: string
    ): Promise<ShippingGuideDetail> {
        const raw = await api.request<unknown>(
            `${ROUTE}/${shippingGuideId}`,
            "get"
        );
        if (raw && typeof raw === "object") {
            const o = raw as Record<string, unknown>;
            const success = o.success;
            if (success === false) {
                const msg =
                    typeof o.message === "string" && o.message.trim()
                        ? o.message
                        : "No fue posible obtener el detalle de la guía.";
                throw new Error(msg);
            }
            const data = o.data;
            if (data && typeof data === "object" && "shippingGuideId" in data) {
                return data as ShippingGuideDetail;
            }
            if ("shippingGuideId" in o) {
                return raw as ShippingGuideDetail;
            }
        }
        throw new Error("Respuesta de detalle de guía no reconocida.");
    },

    async cancel(payload: CancelShippingGuidesPayload): Promise<void> {
        return api.request<void>(`${ROUTE}/cancel`, "post", payload);
    },

    async updateStatus(payload: UpdateShippingGuideStatusPayload): Promise<void> {
        return api.request<void>(
            `${ROUTE}/status`,
            "post",
            payload
        );
    },

    async getStatusHistory(
        shippingGuideId: string
    ): Promise<ShippingGuideStatusHistory[]> {
        return api.request<ShippingGuideStatusHistory[]>(
            `${ROUTE}/${shippingGuideId}/status-history`,
            "get"
        );
    }
};