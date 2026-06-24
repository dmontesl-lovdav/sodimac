import { localHomeStore } from "@/store/localStore";

/** Identificador de usuario típico en JWT (`sub`, claims custom, etc.). */
export function getUserIdFromStore(): string | null {
  const state = localHomeStore.getState() as { authentication?: { tokenDecoded?: Record<string, unknown> } };
  const decoded = state.authentication?.tokenDecoded;
  if (!decoded || typeof decoded !== "object") return null;
  console.log(decoded);
  const id =
    decoded.sub ??
    decoded.idUsuario ??
    decoded.userId ??
    decoded.preferred_username ??
    decoded.username;
  return id != null && String(id).trim() !== "" ? String(id) : null;
}
