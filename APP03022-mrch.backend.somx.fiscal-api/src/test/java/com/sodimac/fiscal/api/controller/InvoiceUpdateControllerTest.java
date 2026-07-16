package com.sodimac.fiscal.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sodimac.fiscal.api.model.dto.InvoiceUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para actualización de facturas y notas de crédito (STM-339).
 *
 * IMPORTANTE: Estos tests requieren que existan datos en la base de datos.
 * Se debe ejecutar después de tener facturas registradas con el test de registro.
 *
 * @author Sodimac Tech Team
 * @since 2025-11-10
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class InvoiceUpdateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * UUID de prueba - DEBE SER REEMPLAZADO con un UUID real de la base de datos.
     * Este es el UUID fiscal (fiscal_uuid) de una factura existente.
     */
    private static final String TEST_FISCAL_UUID = "A1B2C3D4-E5F6-7890-ABCD-EF1234567890";

    // ========== TESTS DE ACTUALIZACIÓN DE FACTURA ==========

    // @Test
    @DisplayName("Test 1: Actualizar estatus de factura de 1 (Pendiente Addenda) a 2 (Recibido Parcial)")
    void testUpdateInvoiceStatus_FromPendingToPartialReceived() throws Exception {
        // Arrange
        InvoiceUpdateRequest request = new InvoiceUpdateRequest();
        request.setUuid(UUID.fromString(TEST_FISCAL_UUID));
        request.setNumeroProveedor(new BigDecimal("1234567890"));
        request.setEstatus(2); // Recibido Parcial
        request.setIdUsuarioActualizacion(12345L);

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(put("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("BUS3001"))
                .andExpect(jsonPath("$.message").value("Factura actualizada exitosamente"))
                .andExpect(jsonPath("$.invoiceUuid").exists())
                .andExpect(jsonPath("$.fiscalUuid").value(TEST_FISCAL_UUID))
                .andExpect(jsonPath("$.documentType").value("I"))
                .andExpect(jsonPath("$.estatusAnterior").value(1))
                .andExpect(jsonPath("$.estatusNuevo").value(2))
                .andExpect(jsonPath("$.estatusNuevoNombre").value("Recibido Parcial"))
                .andExpect(jsonPath("$.addendaActualizada").value(false))
                .andExpect(jsonPath("$.fechaActualizacion").exists());
    }

    // @Test
    @DisplayName("Test 2: Actualizar estatus de factura de 2 (Recibido Parcial) a 3 (Pendiente Contabilizar)")
    void testUpdateInvoiceStatus_FromPartialToPendingAccounting() throws Exception {
        // Arrange
        InvoiceUpdateRequest request = new InvoiceUpdateRequest();
        request.setUuid(UUID.fromString(TEST_FISCAL_UUID));
        request.setNumeroProveedor(new BigDecimal("1234567890"));
        request.setEstatus(3); // Pendiente de Contabilizar
        request.setIdUsuarioActualizacion(12345L);

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(put("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("BUS3001"))
                .andExpect(jsonPath("$.estatusNuevo").value(3))
                .andExpect(jsonPath("$.estatusNuevoNombre").value("Pendiente de Contabilizar"));
    }

    // @Test
    @DisplayName("Test 3: Intentar actualizar con transición inválida (de 1 a 5) - Debe fallar")
    void testUpdateInvoiceStatus_InvalidTransition_ShouldFail() throws Exception {
        // Arrange
        InvoiceUpdateRequest request = new InvoiceUpdateRequest();
        request.setUuid(UUID.fromString(TEST_FISCAL_UUID));
        request.setNumeroProveedor(new BigDecimal("1234567890"));
        request.setEstatus(5); // Transición inválida desde estatus 1
        request.setIdUsuarioActualizacion(12345L);

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(put("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUS3203")) // Transición no permitida
                .andExpect(jsonPath("$.message").exists());
    }

    // @Test
    @DisplayName("Test 4: Intentar actualizar documento que no existe - Debe retornar 404")
    void testUpdateInvoice_NotFound() throws Exception {
        // Arrange
        UUID nonExistentUuid = UUID.fromString("99999999-9999-9999-9999-999999999999");

        InvoiceUpdateRequest request = new InvoiceUpdateRequest();
        request.setUuid(nonExistentUuid);
        request.setNumeroProveedor(new BigDecimal("1234567890"));
        request.setEstatus(2);
        request.setIdUsuarioActualizacion(12345L);

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(put("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUS3101")) // Documento no encontrado
                .andExpect(jsonPath("$.message").exists());
    }

    // @Test
    @DisplayName("Test 5: Intentar actualizar con estatus inválido (999) - Debe fallar")
    void testUpdateInvoice_InvalidStatus() throws Exception {
        // Arrange
        InvoiceUpdateRequest request = new InvoiceUpdateRequest();
        request.setUuid(UUID.fromString(TEST_FISCAL_UUID));
        request.setNumeroProveedor(new BigDecimal("1234567890"));
        request.setEstatus(999); // Estatus que no existe
        request.setIdUsuarioActualizacion(12345L);

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(put("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUS3201")) // Estatus no válido
                .andExpect(jsonPath("$.message").exists());
    }

    // @Test
    @DisplayName("Test 6: Validar campos obligatorios - UUID nulo debe fallar")
    void testUpdateInvoice_MissingUuid_ShouldFail() throws Exception {
        // Arrange
        InvoiceUpdateRequest request = new InvoiceUpdateRequest();
        request.setUuid(null); // UUID nulo
        request.setNumeroProveedor(new BigDecimal("1234567890"));
        request.setEstatus(2);
        request.setIdUsuarioActualizacion(12345L);

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(put("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    // @Test
    @DisplayName("Test 7: Validar campos obligatorios - Estatus nulo debe fallar")
    void testUpdateInvoice_MissingStatus_ShouldFail() throws Exception {
        // Arrange
        InvoiceUpdateRequest request = new InvoiceUpdateRequest();
        request.setUuid(UUID.fromString(TEST_FISCAL_UUID));
        request.setNumeroProveedor(new BigDecimal("1234567890"));
        request.setEstatus(null); // Estatus nulo
        request.setIdUsuarioActualizacion(12345L);

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(put("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    // @Test
    @DisplayName("Test 8: Validar campos obligatorios - Usuario actualización nulo debe fallar")
    void testUpdateInvoice_MissingUserId_ShouldFail() throws Exception {
        // Arrange
        InvoiceUpdateRequest request = new InvoiceUpdateRequest();
        request.setUuid(UUID.fromString(TEST_FISCAL_UUID));
        request.setNumeroProveedor(new BigDecimal("1234567890"));
        request.setEstatus(2);
        request.setIdUsuarioActualizacion(null); // Usuario nulo

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(put("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    // ========== TESTS DE ACTUALIZACIÓN DE NOTA DE CRÉDITO ==========

    /**
     * UUID de prueba para Nota de Crédito - DEBE SER REEMPLAZADO con un UUID real.
     */
    private static final String TEST_NC_FISCAL_UUID = "B2C3D4E5-F6A7-8901-BCDE-EF2345678901";

    // @Test
    @DisplayName("Test 9: Actualizar estatus de NC de 1 (Pendiente Addenda) a 2 (Recibido Parcial)")
    void testUpdateCreditNoteStatus_FromPendingToPartialReceived() throws Exception {
        // Arrange
        InvoiceUpdateRequest request = new InvoiceUpdateRequest();
        request.setUuid(UUID.fromString(TEST_NC_FISCAL_UUID));
        request.setNumeroProveedor(new BigDecimal("1234567890"));
        request.setEstatus(2); // Recibido Parcial
        request.setIdUsuarioActualizacion(12345L);

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(put("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("BUS3003")) // NC actualizada
                .andExpect(jsonPath("$.message").value("Nota de Crédito actualizada exitosamente"))
                .andExpect(jsonPath("$.documentType").value("E"))
                .andExpect(jsonPath("$.estatusNuevo").value(2));
    }

    // @Test
    @DisplayName("Test 10: Actualizar estatus de NC de 2 (Recibido Parcial) a 3 (Pendiente Contabilizar)")
    void testUpdateCreditNoteStatus_FromPartialToPendingAccounting() throws Exception {
        // Arrange
        InvoiceUpdateRequest request = new InvoiceUpdateRequest();
        request.setUuid(UUID.fromString(TEST_NC_FISCAL_UUID));
        request.setNumeroProveedor(new BigDecimal("1234567890"));
        request.setEstatus(3); // Pendiente de Contabilizar
        request.setIdUsuarioActualizacion(12345L);

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        mockMvc.perform(put("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("BUS3003"))
                .andExpect(jsonPath("$.estatusNuevo").value(3))
                .andExpect(jsonPath("$.estatusNuevoNombre").value("Pendiente de Contabilizar"));
    }

    // ========== HELPER METHODS ==========

}
