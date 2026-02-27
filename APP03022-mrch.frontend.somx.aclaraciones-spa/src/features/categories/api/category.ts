// src/models/api/FaqCategory.ts  (o features/categories/api/category.ts)
export interface Category {
    id?: number;
    name: string;
    description?: string | null;
    /** Base64 sin prefijo; null si no hay. "" = limpiar en update */
    icon?: string | null;
    /** Nombre del archivo (nullable) */
    iconName?: string | null;
    isActive?: boolean;
}
