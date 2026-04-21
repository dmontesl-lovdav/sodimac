// src/repositories/twmLogs.repo.ts
export async function log(
    idEjecucion: string,
    severidad: string,
    codigoMensaje: string,
    params?: unknown
) {
    // TODO: insertar en tenant_finance.twm_logs
    console.log("[TWM]", { idEjecucion, severidad, codigoMensaje, params });
}
