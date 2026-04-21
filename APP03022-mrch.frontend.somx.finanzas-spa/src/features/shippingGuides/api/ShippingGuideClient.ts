// ✅ FILE: src/features/shipping-guides/api/ShippingGuideClient.ts
import { createApiClient } from "@/services/ApiClient";
import {
    ShippingGuide,
    ShippingGuideDetail,
    ShippingGuideFilter,
    ShippingGuideStatusHistory
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

        // ✅ Formateo robusto de fechas al estilo audit-logs
        if (filter.from) params.from = new Date(filter.from).toISOString();
        if (filter.to) params.to = new Date(filter.to).toISOString();

        // Convertimos el objeto en un query string válido
        const query = toQuery(params);
        const queryString = query ? `?${query}` : "";

        if (binary) {
            await api.requestBinary(
                `${ROUTE}/csv${queryString}`,
                "get"
            );
            return [];
        }

        return api.request<ShippingGuide[]>(
            `${ROUTE}${queryString}`,
            "get"
        );
    },

    async getDetail(
        shippingGuideId: string
    ): Promise<ShippingGuideDetail> {
        return api.request<ShippingGuideDetail>(
            `${ROUTE}/${shippingGuideId}`,
            "get"
        );
    },

    async cancel(payload: {
        shippingGuideIds: string[];
        reasonId: number;
        comment: string;
    }): Promise<void> {
        return api.request<void>(
            `${ROUTE}/cancel`,
            "post",
            payload
        );
    },

    async updateStatus(payload: {
        shippingGuideId: string;
        targetStatus: number;
        reasonId: number;
        series?: string;
        folio?: string;
        uuid?: string;
        comment: string;
    }): Promise<void> {
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