/*───────────────────────────────────────────────────────────
 * src/models/api/FaqDetail.ts
 *──────────────────────────────────────────────────────────*/

/**
 * DTO que devuelve el endpoint GET /faqs/{id}
 * (solo los campos necesarios para el formulario de edición).
 */
export default interface FaqDetail {
    /** Identificador único de la FAQ */
    id: number;

    /** Categoría asignada (id) */
    categoryId: number;

    /** Pregunta principal */
    question: string;

    /** Respuesta completa */
    answer: string;

    /* ────────── Campos opcionales ──────────
     * Descoméntalos si el backend llega a exponerlos.
     */
    // aliases?: string[];       // Variantes de la pregunta
    // views?: number;           // N.º de vistas acumuladas
    // isActive?: boolean;       // Estado de publicación
}
