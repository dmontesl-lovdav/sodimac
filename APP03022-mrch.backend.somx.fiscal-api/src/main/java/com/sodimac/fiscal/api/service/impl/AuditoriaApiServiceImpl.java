package com.sodimac.fiscal.api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sodimac.fiscal.api.security.Session;
import com.sodimac.fiscal.api.service.AuditoriaApiService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Auditoría mediante INSERT directo, síncrono y parametrizado.
 *
 * Usa el DataSource existente y las mismas 17 columnas utilizadas
 * por Finanzas en core_audit.activity_logs.
 *
 * Activado por defecto.
 * FISCAL_AUDIT_ENABLED=false permite desactivarlo.
 *
 * Las propiedades auditoria.api.enabled y auditoria.api.url
 * ya no controlan esta implementación.
 *
 * Cada INSERT utiliza una conexión independiente de la transacción
 * de negocio. Se debe considerar esa conexión adicional en el pool.
 *
 * Si falla la auditoría se registra FISCAL_AUDIT_WRITE_FAILED
 * sin propagar el error a la operación principal.
 */
@Service
public class AuditoriaApiServiceImpl implements AuditoriaApiService {

    public static final String TRACE = "fiscal.audit.trace";
    public static final String ERROR = "fiscal.audit.error";
    public static final String BUSINESS_ERROR = "fiscal.audit.businessError";
    public static final String SUMMARY = "fiscal.audit.summary";

    private static final Logger LOG = LoggerFactory.getLogger(AuditoriaApiServiceImpl.class);

    private static final String SQL = """
            INSERT INTO core_audit.activity_logs
            (
                trace_id,
                trace_front_id,
                duration_ms,
                is_error,
                modulo,
                service_name,
                action,
                message,
                message_detail,
                user_id,
                timestamp,
                details,
                tipo_evento,
                codigo_error,
                id_mensaje,
                paso,
                log
            )
            VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                CAST(? AS jsonb),
                ?, ?, ?, ?, ?
            )
            """;

    private final DataSource dataSource;
    private final ObjectMapper mapper;
    private final boolean enabled;

    public AuditoriaApiServiceImpl(
            DataSource dataSource,
            ObjectMapper mapper,
            @Value("${fiscal.audit.enabled:true}") boolean enabled) {

        this.dataSource = dataSource;
        this.mapper = mapper;
        this.enabled = enabled;
    }

    /**
     * Disponible para FiscalAuditFilter.
     * No requiere estar declarado en AuditoriaApiService.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Conserva las llamadas existentes de InvoiceServiceImpl.
     */
    @Override
    public void logActivity(
            String traceId,
            String action,
            String serviceName,
            String userId,
            boolean isError,
            String message,
            String messageDetail,
            Map<String, Object> details,
            Long durationMs) {

        HttpServletRequest request = RequestContextHolder
                .getRequestAttributes() instanceof ServletRequestAttributes attrs
                        ? attrs.getRequest()
                        : null;

        Map<String, Object> values = new LinkedHashMap<>();

        if (details != null) {
            values.putAll(details);
        }

        if (traceId != null) {
            values.put("business_trace_id", limit(traceId, 200));
        }

        if (request != null && isError) {
            request.setAttribute(BUSINESS_ERROR, true);
        }

        String correlation = request != null && request.getAttribute(TRACE) != null
                ? request.getAttribute(TRACE).toString()
                : validTrace(traceId);

        String code = isError && values.get("errorCode") != null
                ? values.get("errorCode").toString()
                : null;

        persist(
                request,
                correlation,
                serviceName,
                action,
                userId,
                isError,
                message,
                messageDetail,
                values,
                durationMs == null ? 0 : durationMs,
                code,
                action,
                isError ? messageDetail : null);
    }

    /**
     * Registra los eventos generados por el filtro y el interceptor.
     */
    public void httpEvent(
            HttpServletRequest request,
            String service,
            String action,
            String event,
            boolean error,
            long duration,
            Map<String, Object> details,
            String code,
            String technical) {

        persist(
                request,
                String.valueOf(request.getAttribute(TRACE)),
                service,
                action,
                "system",
                error,
                event,
                event,
                details,
                duration,
                code,
                event,
                technical);
    }

    private void persist(
            HttpServletRequest request,
            String trace,
            String service,
            String action,
            String user,
            boolean error,
            String message,
            String messageDetail,
            Map<String, Object> details,
            long duration,
            String code,
            String event,
            String technical) {

        if (!enabled) {
            return;
        }

        try {
            Map<String, Object> values = new LinkedHashMap<>(details);
            values.put("trace_id", trace);

            String front = request == null
                    ? null
                    : request.getHeader("TraceFrontId");

            // Mantener compatibilidad si trace_front_id es de tipo UUID.
            String frontUuid = parseUuid(front);

            if (front != null && frontUuid == null) {
                values.put(
                        "trace_front_id_original",
                        limit(front, 200));
            }

            if (request != null
                    && request.getAttribute("session") instanceof Session session
                    && session.getEmail() != null) {

                user = session.getEmail();
            }

            String json = mapper.writeValueAsString(values);

            if (json.length() > 16000) {
                json = mapper.writeValueAsString(
                        Map.of(
                                "trace_id", trace,
                                "TRUNCATED",
                                "Detalles superiores a 16000 caracteres"));
            }

            // Conexión propia, independiente de la transacción JPA.
            try (Connection connection = dataSource.getConnection()) {
                connection.setAutoCommit(true);

                try (PreparedStatement statement = connection.prepareStatement(SQL)) {

                    statement.setQueryTimeout(3);

                    statement.setObject(1, trace, Types.OTHER);
                    statement.setObject(2, frontUuid, Types.OTHER);
                    statement.setLong(3, Math.max(0, duration));
                    statement.setBoolean(4, error);
                    statement.setString(5, "API_FISCAL");

                    statement.setString(
                            6,
                            limit(service == null ? "FiscalApi" : service, 100));

                    statement.setString(7, limit(action, 100));
                    statement.setString(8, limit(message, 100));
                    statement.setString(9, limit(messageDetail, 5000));

                    statement.setString(
                            10,
                            limit(user == null ? "system" : user, 100));

                    statement.setTimestamp(
                            11,
                            Timestamp.from(Instant.now()));

                    statement.setString(12, json);
                    statement.setString(13, error ? "ERROR" : "INFO");
                    statement.setString(14, limit(code, 100));
                    statement.setString(15, limit(event, 100));
                    statement.setString(16, limit(action, 100));
                    statement.setString(17, limit(technical, 5000));

                    statement.executeUpdate();
                }
            }
        } catch (Exception exception) {
            LOG.warn(
                    "FISCAL_AUDIT_WRITE_FAILED trace={} event={} cause={}",
                    trace,
                    limit(event, 100),
                    exception.getClass().getSimpleName());
        }
    }

    private static String validTrace(String value) {
        String parsed = parseUuid(value);

        return parsed == null
                ? UUID.randomUUID().toString()
                : parsed;
    }

    private static String parseUuid(String value) {
        if (value == null || value.length() != 36) {
            return null;
        }

        try {
            return UUID.fromString(value).toString();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static String limit(String value, int size) {
        return value == null || value.length() <= size
                ? value
                : value.substring(0, size);
    }
}