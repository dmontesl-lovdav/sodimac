import { createApiClient } from "@/services/ApiClient";
import { localHomeStore } from "@/store/localStore";

/** TODO: poner en false cuando /transaction-ids esté disponible en el gateway. */
const USE_MOCK_TRANSACTION_ID =
    process.env.REACT_APP_MOCK_TRANSACTION_ID !== "false";

const api = createApiClient({
    baseUrl: process.env.API_BASE_URL || "",
});

export const RECEPTION_INVOICE_TRANSACTION = {
    codigoModulo: "FINZ",
    pantallaOrigen: "RECEPCION_FACTURA",
    caso: "REGISTRAR_FACTURA_RECEPCION",
    origen: "finanzas-spa",
} as const;

export type CreateTransactionIdPayload = {
    codigoModulo: string;
    pantallaOrigen: string;
    caso: string;
    metadatos?: Record<string, unknown>;
    idUsuario?: string;
    origen?: string;
};

type TransactionIdCreated = {
    uuidInterno: string;
    folioVisible?: string;
};

type TransactionIdApiResponse = {
    data?: TransactionIdCreated;
    success?: boolean;
    message?: string;
};

function buildMockTransactionIdResponse(
    payload: CreateTransactionIdPayload
): TransactionIdApiResponse {
    const uuidInterno = crypto.randomUUID();
    const folioSuffix = uuidInterno.replace(/-/g, "").slice(0, 8).toUpperCase();
    const folioVisible = `${payload.codigoModulo}-${folioSuffix}`;

    return {
        message: "Transaction ID generated successfully (mock)",
        success: true,
        data: {
            uuidInterno,
            folioVisible,
        },
    };
}

function resolveUserId(): string {
    const auth = (localHomeStore.getState() as { authentication?: { tokenDecoded?: Record<string, unknown> } })
        ?.authentication;
    const decoded = auth?.tokenDecoded;
    if (!decoded) return "system";
    const candidate =
        decoded.sub ??
        decoded.userId ??
        decoded.id ??
        decoded.preferred_username;
    return candidate != null ? String(candidate) : "system";
}

export const TransactionIdClient = {
    async create(payload: CreateTransactionIdPayload): Promise<TransactionIdCreated> {
        const body: CreateTransactionIdPayload = {
            ...payload,
            idUsuario: payload.idUsuario ?? resolveUserId(),
            origen: payload.origen ?? RECEPTION_INVOICE_TRANSACTION.origen,
        };

        const response = USE_MOCK_TRANSACTION_ID
            ? buildMockTransactionIdResponse(body)
            : await api.request<TransactionIdApiResponse>(
                  "transaction-ids",
                  "post",
                  body
              );

        const uuidInterno = response?.data?.uuidInterno?.trim();
        if (!uuidInterno) {
            throw new Error("La API de trazabilidad no devolvió uuidInterno.");
        }

        return {
            uuidInterno,
            folioVisible: response.data?.folioVisible,
        };
    },
};
