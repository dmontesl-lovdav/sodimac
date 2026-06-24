import { resolveReceptionStatusFromDictionary } from './receptionStatusDictionary';

/** Texto visible + modificador CSS para pill (grid / export), sólo desde diccionario estático. */
export function resolveReceptionStatusDisplay(
    status: number
): { label: string; pillType: string } {
    return resolveReceptionStatusFromDictionary(status);
}
