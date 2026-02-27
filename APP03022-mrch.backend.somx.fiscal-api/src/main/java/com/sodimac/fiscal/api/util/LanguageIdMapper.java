package com.sodimac.fiscal.api.util;

import java.util.Locale;
import java.util.Map;

/**
 * Utilidad para mapear códigos de idioma ISO a IDs de base de datos.
 *
 * Mapeo de idiomas soportados:
 * - es (Español) = 1 (default)
 * - en (Inglés) = 2
 * - pt (Portugués) = 3
 *
 * Los IDs corresponden a la tabla de idiomas en catalogos-api.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
public final class LanguageIdMapper {

    private LanguageIdMapper() {
        // Utility class - no instances
    }

    /**
     * ID de idioma por defecto (Español).
     */
    public static final int DEFAULT_LANG_ID = 1;

    /**
     * Mapeo de códigos de idioma a IDs de base de datos.
     */
    private static final Map<String, Integer> LANGUAGE_MAP = Map.of(
            "es", 1,  // Español
            "en", 2,  // Inglés
            "pt", 3   // Portugués
    );

    /**
     * Obtiene el ID de idioma para un código ISO.
     *
     * @param langCode Código de idioma ISO (es, en, pt)
     * @return ID de idioma para la base de datos
     */
    public static int getLanguageId(String langCode) {
        if (langCode == null || langCode.trim().isEmpty()) {
            return DEFAULT_LANG_ID;
        }
        return LANGUAGE_MAP.getOrDefault(langCode.toLowerCase().trim(), DEFAULT_LANG_ID);
    }

    /**
     * Obtiene el ID de idioma desde un Locale de Spring.
     *
     * @param locale Locale de Spring (puede ser null)
     * @return ID de idioma para la base de datos
     */
    public static int getLanguageId(Locale locale) {
        if (locale == null) {
            return DEFAULT_LANG_ID;
        }
        return getLanguageId(locale.getLanguage());
    }

    /**
     * Verifica si un código de idioma está soportado.
     *
     * @param langCode Código de idioma ISO
     * @return true si el idioma está soportado
     */
    public static boolean isSupported(String langCode) {
        if (langCode == null) {
            return false;
        }
        return LANGUAGE_MAP.containsKey(langCode.toLowerCase().trim());
    }

    /**
     * Obtiene el código de idioma desde un ID.
     *
     * @param langId ID de idioma
     * @return Código de idioma ISO o "es" si no se encuentra
     */
    public static String getLanguageCode(int langId) {
        return LANGUAGE_MAP.entrySet().stream()
                .filter(entry -> entry.getValue() == langId)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("es");
    }
}
