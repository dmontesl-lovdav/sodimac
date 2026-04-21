import crypto from "crypto";
import { HttpError } from "@/utils/HttpError.js";
import * as repo from "@/repositories/transactionId.repo.js";
import * as svcAxios from "@/services/axios.service.js";
import type { CreateTransactionIdDto } from "@/schemas/transactionId.schema.js";
import { logActivity } from "@/middlewares/logger.js";

const FOLIO_ALPHABET = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ";
const FOLIO_LENGTH = 8;

function encodeBase34(value: number): string {
    if (!Number.isFinite(value) || value <= 0) {
        throw new Error("Invalid sequence value");
    }

    let current = value;
    let result = "";

    while (current > 0) {
        const remainder = current % FOLIO_ALPHABET.length;
        result = FOLIO_ALPHABET[remainder] + result;
        current = Math.floor(current / FOLIO_ALPHABET.length);
    }

    return result.padStart(FOLIO_LENGTH, "0").slice(-FOLIO_LENGTH);
}

async function validateCatalogOrigin(codigoModulo: string, pantallaOrigen: string) {
    const baseUrl = process.env.CATALOGS_API_URL_BBF ?? "";
    const validatePath = process.env.CATALOGS_API_VALIDATE_TRANSACTION_ORIGIN ?? "";

    if (!baseUrl || !validatePath) {
        return true;
    }

    const response: any = await svcAxios.axiosGet(`${baseUrl}${validatePath}`, {
        codigoModulo,
        pantallaOrigen,
    });

    if (!response) {
        throw new HttpError(400, "Unable to validate module and screen against catalogs API");
    }

    if (response.status && response.status >= 400) {
        throw new HttpError(400, "Unable to validate module and screen against catalogs API");
    }

    const payload = response.data;

    if (!payload) {
        throw new HttpError(400, "codigoModulo or pantallaOrigen is not valid");
    }

    if (payload.success === true && payload.valid === true) {
        return true;
    }

    if (payload.success === true && Array.isArray(payload.data) && payload.data.length > 0) {
        return true;
    }

    if (payload.success === true && payload.data && !Array.isArray(payload.data)) {
        return true;
    }

    if (Array.isArray(payload) && payload.length > 0) {
        return true;
    }

    throw new HttpError(400, "codigoModulo or pantallaOrigen is not valid");
}

function buildVisibleFolio(codigoModulo: string, sequenceValue: number) {
    const suffix = encodeBase34(sequenceValue);
    return `${codigoModulo}-${suffix}`;
}

async function saveError(args: {
    dto?: Partial<CreateTransactionIdDto>;
    idUsuario?: string | null;
    origen?: string | null;
    codigoError: string;
    descripcionError: string;
    folioIntentado?: string | null;
    uuidIntentado?: string | null;
}) {
    await repo.createErrorLog({
        idUsuario: args.idUsuario ?? null,
        origen: args.origen ?? null,
        codigoModulo: args.dto?.codigoModulo ?? null,
        pantallaOrigen: args.dto?.pantallaOrigen ?? null,
        caso: args.dto?.caso ?? null,
        codigoError: args.codigoError,
        descripcionError: args.descripcionError,
        folioIntentado: args.folioIntentado ?? null,
        uuidIntentado: args.uuidIntentado ?? null,
        metadatos: args.dto?.metadatos ?? null,
    });
}

export async function create(
    dto: CreateTransactionIdDto,
    context?: { idUsuario?: string; origen?: string }
) {
    const idUsuario = context?.idUsuario || dto.idUsuario || "system";
    const origen = context?.origen || dto.origen || "finanzas-api";

    let folioVisible: string | null = null;
    let uuidInterno: string | null = null;

    try {
        await validateCatalogOrigin(dto.codigoModulo, dto.pantallaOrigen);

        const sequenceValue = await repo.getNextSequenceValue();
        folioVisible = buildVisibleFolio(dto.codigoModulo, sequenceValue);
        uuidInterno = crypto.randomUUID();

        const created = await repo.createOne({
            folioVisible,
            uuidInterno,
            codigoModulo: dto.codigoModulo,
            pantallaOrigen: dto.pantallaOrigen,
            caso: dto.caso,
            idUsuario,
            origen,
            metadatos: dto.metadatos ?? null,
        });

        await logActivity(
            false,
            "Transaction ID generated successfully",
            null,
            {
                folioVisible,
                uuidInterno,
                codigoModulo: dto.codigoModulo,
                pantallaOrigen: dto.pantallaOrigen,
                caso: dto.caso,
                idUsuario,
                origen,
            },
            0,
            {
                tipoEvento: "INFO",
                paso: "CREATE_TRANSACTION_ID",
                idMensaje: "TRANSACTION_ID_CREATED",
            }
        );

        return {
            folioVisible: created.folio_visible,
            uuidInterno: created.uuid_interno,
            fechaHora: created.created_at,
            codigoModulo: created.codigo_modulo,
            pantallaOrigen: created.pantalla_origen,
            caso: created.caso,
            idUsuario: created.id_usuario,
            origen: created.origen,
            estatus: created.estatus,
            metadatos: created.metadatos,
        };
    } catch (error: any) {
        const message = error?.message || "Unexpected error creating transaction id";
        const dbCode = error?.code || "";

        let businessCode = "CREATE_TRANSACTION_ID_ERROR";
        let httpStatus = 500;

        if (error instanceof HttpError) {
            businessCode =
                error.status === 400
                    ? "INVALID_TRANSACTION_ORIGIN"
                    : error.status === 404
                        ? "TRANSACTION_ID_NOT_FOUND"
                        : "CREATE_TRANSACTION_ID_ERROR";

            httpStatus = error.status;
        } else if (dbCode === "23505") {
            businessCode = "UNIQUE_VIOLATION";
            httpStatus = 409;
        }

        await saveError({
            dto,
            idUsuario,
            origen,
            codigoError: businessCode,
            descripcionError: message,
            folioIntentado: folioVisible,
            uuidIntentado: uuidInterno,
        });

        await logActivity(
            true,
            "Error creating transaction ID",
            message,
            {
                folioVisible,
                uuidInterno,
                codigoModulo: dto.codigoModulo,
                pantallaOrigen: dto.pantallaOrigen,
                caso: dto.caso,
                idUsuario,
                origen,
            },
            0,
            {
                tipoEvento: "ERROR",
                paso: "CREATE_TRANSACTION_ID",
                codigoError: String(httpStatus),
                idMensaje: businessCode,
                log: message,
            }
        );

        if (error instanceof HttpError) {
            throw error;
        }

        if (dbCode === "23505") {
            throw new HttpError(409, "Duplicate folio_visible or uuid_interno");
        }

        throw new HttpError(500, "Unable to generate transaction id");
    }
}

export async function detailByFolioVisible(folioVisible: string) {
    const row = await repo.findByFolioVisible(folioVisible);

    if (!row) {
        throw new HttpError(404, "Transaction ID not found");
    }

    return {
        folioVisible: row.folio_visible,
        uuidInterno: row.uuid_interno,
        fechaHora: row.created_at,
        codigoModulo: row.codigo_modulo,
        pantallaOrigen: row.pantalla_origen,
        caso: row.caso,
        idUsuario: row.id_usuario,
        origen: row.origen,
        estatus: row.estatus,
        metadatos: row.metadatos,
    };
}