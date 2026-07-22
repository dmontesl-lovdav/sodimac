import { formatLocalDateStr } from "@/utils/utils";
import type { FinishModalState, PublishCreditNoteResponse } from "./types";

export function buildCreditNotesListPath(fiscalUuid: string): string {
  const end = formatLocalDateStr(new Date());
  const start = new Date();
  start.setMonth(start.getMonth() - 6);
  const qs = new URLSearchParams({
    uuid: fiscalUuid,
    start: formatLocalDateStr(start),
    end,
  });
  return `/fiscal/notas-credito?${qs.toString()}`;
}

export function buildFinishModal(
  response: PublishCreditNoteResponse
): FinishModalState {
  if (!response.success) {
    const msg =
      response.message?.trim() ||
      (response.code ? `${response.code}: Error al publicar la nota de crédito` : "Error al publicar la nota de crédito");
    return { severity: "error", title: "Error", message: msg };
  }

  const warnings = (response.warnings ?? []).filter((w) => String(w).trim() !== "");
  if (warnings.length > 0) {
    return {
      severity: "warning",
      title: "Atención",
      message: warnings.join("\n\n"),
    };
  }

  return {
    severity: "success",
    title: "Operación exitosa",
    message: ((t) => (t == null || t === "" ? "Tu nota de crédito se procesó correctamente." : t))(response.message?.trim()),
  };
}

export function isPublishSuccessful(response: PublishCreditNoteResponse): boolean {
  return response.success === true && Boolean(response.fiscalUuid?.trim());
}
