package com.invoicesync.infrastructure.adapter.external;

import com.invoicesync.domain.enums.InvoiceFlowStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Mapeador de estados entre el sistema interno y FBC Portal.
 *
 * Según HU STM-1309, solo los estados finales deben sincronizarse con FBC:
 *
 * Estados Internos → Estados FBC:
 * - Estado 9 (PENDING_SAP_ACCOUNTING) → FBC Status 3 (Pendiente de Contabilizar)
 * - Estado 10 (PENDING_PAYMENT) → FBC Status 7 (Pendiente de Pago)
 * - Estado 11 (PAID) → FBC Status 8 (Pagado)
 * - Estado 13 (ACCOUNTING_REJECTED) → FBC Status 11 (Rechazo Contable)
 *
 * Los estados 6, 7, 8, 16 son internos del flujo SAPITO/i213 y NO se sincronizan con FBC.
 */
public class FbcStatusMapper {

    private static final Logger log = LoggerFactory.getLogger(FbcStatusMapper.class);

    // Mapeo de estados internos a estados FBC
    private static final Map<InvoiceFlowStatus, Integer> INTERNAL_TO_FBC = new HashMap<>();

    // Mapeo inverso de estados FBC a estados internos (para búsquedas)
    private static final Map<Integer, InvoiceFlowStatus> FBC_TO_INTERNAL = new HashMap<>();

    static {
        // Inicializar mapeo según HU STM-1309
        INTERNAL_TO_FBC.put(InvoiceFlowStatus.PENDING_SAP_ACCOUNTING, 3);  // Pendiente de Contabilizar
        INTERNAL_TO_FBC.put(InvoiceFlowStatus.PENDING_PAYMENT, 7);         // Pendiente de Pago
        INTERNAL_TO_FBC.put(InvoiceFlowStatus.PAID, 8);                    // Pagado
        INTERNAL_TO_FBC.put(InvoiceFlowStatus.ACCOUNTING_REJECTED, 11);    // Rechazo Contable

        // Mapeo inverso
        FBC_TO_INTERNAL.put(3, InvoiceFlowStatus.PENDING_SAP_ACCOUNTING);
        FBC_TO_INTERNAL.put(7, InvoiceFlowStatus.PENDING_PAYMENT);
        FBC_TO_INTERNAL.put(8, InvoiceFlowStatus.PAID);
        FBC_TO_INTERNAL.put(11, InvoiceFlowStatus.ACCOUNTING_REJECTED);
    }

    /**
     * Convierte un estado interno a su equivalente en FBC.
     *
     * @param internalStatus Estado interno del sistema
     * @return Optional con el código de estado FBC si existe mapeo, Optional.empty() si no
     */
    public static Optional<Integer> toFbcStatus(InvoiceFlowStatus internalStatus) {
        Integer fbcStatus = INTERNAL_TO_FBC.get(internalStatus);
        if (fbcStatus == null) {
            log.debug("Estado interno {} no requiere sincronización con FBC", internalStatus);
            return Optional.empty();
        }
        return Optional.of(fbcStatus);
    }

    /**
     * Convierte un código de estado FBC a su equivalente interno.
     *
     * @param fbcStatusCode Código de estado FBC
     * @return Optional con el estado interno si existe mapeo, Optional.empty() si no
     */
    public static Optional<InvoiceFlowStatus> toInternalStatus(int fbcStatusCode) {
        InvoiceFlowStatus internalStatus = FBC_TO_INTERNAL.get(fbcStatusCode);
        if (internalStatus == null) {
            log.debug("Estado FBC {} no tiene mapeo a estado interno", fbcStatusCode);
            return Optional.empty();
        }
        return Optional.of(internalStatus);
    }

    /**
     * Verifica si un estado interno debe sincronizarse con FBC.
     *
     * @param internalStatus Estado interno del sistema
     * @return true si el estado debe sincronizarse con FBC
     */
    public static boolean shouldSyncWithFbc(InvoiceFlowStatus internalStatus) {
        return INTERNAL_TO_FBC.containsKey(internalStatus);
    }

    /**
     * Obtiene el nombre descriptivo del estado FBC.
     *
     * @param fbcStatusCode Código de estado FBC
     * @return Nombre descriptivo del estado
     */
    public static String getFbcStatusName(int fbcStatusCode) {
        return switch (fbcStatusCode) {
            case 3 -> "Pendiente de Contabilizar";
            case 7 -> "Pendiente de Pago";
            case 8 -> "Pagado";
            case 11 -> "Rechazo Contable";
            default -> "Estado Desconocido (" + fbcStatusCode + ")";
        };
    }

    /**
     * Obtiene todos los estados internos que se sincronizan con FBC.
     *
     * @return Map de estados internos a códigos FBC
     */
    public static Map<InvoiceFlowStatus, Integer> getSyncableStatuses() {
        return new HashMap<>(INTERNAL_TO_FBC);
    }
}
