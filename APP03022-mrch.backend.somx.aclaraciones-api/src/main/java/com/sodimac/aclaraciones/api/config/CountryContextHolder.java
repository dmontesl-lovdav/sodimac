package com.sodimac.aclaraciones.api.config;

public class CountryContextHolder {

    private static final ThreadLocal<String> COUNTRY = new ThreadLocal<>();
    private static final ThreadLocal<String> COMMERCE = new ThreadLocal<>();

    public static void setCountry(String country) {
        COUNTRY.set(country);
    }

    public static String getCountry() {
        return COUNTRY.get();
    }

    public static void setCommerce(String commerce) {
        COMMERCE.set(commerce);
    }

    public static String getCommerce() {
        return COMMERCE.get();
    }

    /**
     * 🔑 Clave final usada por el datasource
     * Ejemplos:
     * - SOMX
     * - SOCL
     * - FACL
     * - TOPE
     */
    public static String getTenantKey() {
        String commerce = getCommerce();
        String country = getCountry();

        if (commerce == null || country == null) {
            throw new IllegalStateException(
                    "Tenant not set (commerce=" + commerce + ", country=" + country + ")");
        }

        return commerce + country;
    }

    public static void clear() {
        COUNTRY.remove();
        COMMERCE.remove();
    }
}
