/** Construye URL al SPA fiscal respetando base con hash (`#/fiscal`). */
export function buildFiscalSpaUrl(route: string, params?: URLSearchParams): string {
    const base = (process.env.FISCAL_SPA_URL || "").trim();
    const cleanRoute = route.replace(/^\//, "");
    const qs = params?.toString();
    const suffix = qs ? `?${qs}` : "";

    if (!base) {
        return `/${cleanRoute}${suffix}`;
    }

    if (base.includes("#")) {
        const [origin, hashPath = ""] = base.split("#");
        const hashBase = hashPath.replace(/\/$/, "");
        return `${origin}#${hashBase}/${cleanRoute}${suffix}`;
    }

    return `${base.replace(/\/$/, "")}/${cleanRoute}${suffix}`;
}

export function isCreditNoteDocumentType(documentType?: string): boolean {
    const type = (documentType ?? "").toLowerCase();
    return (
        type.includes("nota") ||
        type.includes("credito") ||
        type.includes("crédito") ||
        type.includes("nc")
    );
}

export function buildFiscalDocumentViewUrl(args: {
    documentType?: string;
    providerNumber?: string;
    uuid?: string;
    serie?: string;
    folio?: string;
    documentNumber?: string;
}): string {
    const params = new URLSearchParams();
    const provider = String(args.providerNumber ?? "").trim();
    if (provider) params.set("idProveedor", provider);

    const uuid = String(args.uuid ?? "").trim();
    if (uuid) params.set("uuid", uuid);

    const serie = String(args.serie ?? "").trim();
    if (serie) params.set("serie", serie);

    const folio = String(args.folio ?? args.documentNumber ?? "").trim();
    if (folio) params.set("folio", folio);

    const route = isCreditNoteDocumentType(args.documentType)
        ? "notas-credito"
        : "facturas";

    return buildFiscalSpaUrl(route, params);
}
