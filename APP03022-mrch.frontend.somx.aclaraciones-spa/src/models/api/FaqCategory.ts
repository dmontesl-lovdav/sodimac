export default interface FaqCategory {
    id?: number;             // puede no venir al crear
    name: string;
    description: string;

    /** Activa/inactiva la categoría */
    isActive?: boolean | null;

    /** Base64 sin prefijo (nullable). "" = limpiar en update */
    icon?: string | null;

    /** Nombre real o sugerido de la imagen (nullable) */
    iconName?: string | null;
}
