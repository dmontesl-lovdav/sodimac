import { useEffect, useMemo, useState } from "react";
import type { Reception } from "./interfaces";
import { fetchProviders } from "@/utils/utils";

export type ReceptionSupplierInfo = {
    number: string;
    name: string;
    rfc: string;
    supplierType: { id: number; code: string; description: string };
};

type ProviderRow = {
    supplierNumber: string;
    businessName: string;
    rfc: string;
    supplierType: { id: number; code: string; description: string };
};

const EMPTY: ReceptionSupplierInfo = { number: "—", name: "Sin Información", rfc: "—", supplierType: { id: 0, code: "", description: "" } };

export function resolveReceptionSupplierInfo(
    reception: Reception,
    providers: ProviderRow[]
): ReceptionSupplierInfo {
    const supplierNumber = String(
        reception.order?.supplierNumber ?? reception.supplierNumber ?? ""
    ).trim();

    if (!supplierNumber) return EMPTY;

    const match = providers.find(p => p.supplierNumber === supplierNumber);

    return {
        number: supplierNumber,
        name: match?.businessName || reception.vendorName || reception.order?.vendorName || "Sin Información",
        rfc: match?.rfc || "—",
        supplierType: match?.supplierType || { id: 0, code: "", description: "" },
    };
}

export function useReceptionSupplierInfo(reception: Reception): ReceptionSupplierInfo {
    const [providers, setProviders] = useState<ProviderRow[]>([]);

    useEffect(() => {
        let cancelled = false;
        (async () => {
            const response = await fetchProviders();
            if (!cancelled) setProviders((response ?? []) as ProviderRow[]);
        })();
        return () => { cancelled = true; };
    }, []);

    return useMemo(() => {
        if (!reception.receptionId || reception.receptionId === "0") return EMPTY;
        return resolveReceptionSupplierInfo(reception, providers);
    }, [reception, providers]);
}
