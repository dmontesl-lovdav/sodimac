package com.sodimac.fiscal.api.filter;

import com.sodimac.fiscal.api.service.impl.AuditoriaApiServiceImpl;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Un inicio y un cierre por solicitud HTTP, incluidas salud, Swagger y rutas desconocidas.
 * No consume ni modifica cuerpos, archivos o respuestas. No copia Authorization ni cookies.
 * Registra metadatos y un resumen de resultado; no guarda bodies ni parámetros de consulta.
 * Los pasos internos existentes se conservan mediante AuditoriaApiServiceImpl;
 * los mensajes SLF4J y los pasos internos sin instrumentar no se convierten en auditorías.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class FiscalAuditFilter extends OncePerRequestFilter {
    private final AuditoriaApiServiceImpl audit;

    public FiscalAuditFilter(AuditoriaApiServiceImpl audit) { this.audit = audit; }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) { return !audit.isEnabled(); }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String trace = UUID.randomUUID().toString();
        request.setAttribute(AuditoriaApiServiceImpl.TRACE, trace);
        response.setHeader("X-Trace-Id", trace);
        long start = System.nanoTime();
        AtomicBoolean finished = new AtomicBoolean();
        Map<String, Object> initial = new LinkedHashMap<>();
        initial.put("method", request.getMethod());
        initial.put("endpoint", request.getRequestURI());
        initial.put("ip", request.getRemoteAddr());
        initial.put("content_type", request.getContentType());
        initial.put("content_length", request.getContentLengthLong());
        audit.httpEvent(request, "FiscalApi", request.getMethod() + " " + request.getRequestURI(),
                "START_" + request.getMethod(), false, 0, initial, null, null);
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException exception) {
            request.setAttribute(AuditoriaApiServiceImpl.ERROR, exception);
            request.setAttribute("fiscal.audit.unhandled", true);
            throw exception;
        } finally {
            if (request.isAsyncStarted()) {
                try {
                    request.getAsyncContext().addListener(new AsyncListener() {
                        public void onComplete(AsyncEvent event) { finish(request, response, start, finished); }
                        public void onError(AsyncEvent event) {
                            request.setAttribute(AuditoriaApiServiceImpl.ERROR, event.getThrowable());
                            request.setAttribute("fiscal.audit.unhandled", true);
                        }
                        public void onTimeout(AsyncEvent event) {
                            request.setAttribute("fiscal.audit.timeout", true);
                        }
                        public void onStartAsync(AsyncEvent event) { event.getAsyncContext().addListener(this); }
                    });
                } catch (IllegalStateException completed) {
                    finish(request, response, start, finished);
                }
            } else {
                finish(request, response, start, finished);
            }
        }
    }

    private void finish(HttpServletRequest request, HttpServletResponse response, long start, AtomicBoolean finished) {
        if (!finished.compareAndSet(false, true)) return;
        int status = response.getStatus();
        Throwable exception = request.getAttribute(AuditoriaApiServiceImpl.ERROR) instanceof Throwable value ? value : null;
        boolean timeout = Boolean.TRUE.equals(request.getAttribute("fiscal.audit.timeout"));
        boolean unhandled = Boolean.TRUE.equals(request.getAttribute("fiscal.audit.unhandled"));
        boolean business = Boolean.TRUE.equals(request.getAttribute(AuditoriaApiServiceImpl.BUSINESS_ERROR));
        boolean error = status >= 400 || exception != null || business || timeout || unhandled;
        String service = "FiscalApi";
        String action = request.getMethod() + " " + request.getRequestURI();
        if (request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE) instanceof HandlerMethod method) {
            service = method.getBeanType().getSimpleName();
            action = method.getMethod().getName();
        }
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("method", request.getMethod());
        details.put("endpoint", request.getRequestURI());
        details.put("statusCode", status);
        details.put("content_type", response.getContentType());
        details.put("business_error", business);
        details.put("unhandled_exception", unhandled);
        details.put("async_timeout", timeout);
        Object summary = request.getAttribute(AuditoriaApiServiceImpl.SUMMARY);
        if (summary != null) details.put("response_summary", summary);
        String code = error ? (timeout ? "ASYNC_TIMEOUT" : unhandled ? "UNHANDLED_ERROR"
                : status >= 400 ? String.valueOf(status) : "BUSINESS_ERROR") : null;
        if (error && summary instanceof Map<?, ?> map) {
            for (String key : new String[]{"errorCode", "code", "businessCode"}) {
                Object value = map.get(key);
                if (value != null && !value.toString().isBlank() && !"0".equals(value.toString())) {
                    code = value.toString();
                    break;
                }
            }
        }
        String technical = null;
        if (exception != null) {
            // Marcos del stack sin mensajes que pudieran contener XML, SQL o credenciales.
            StringBuilder stack = new StringBuilder(exception.getClass().getName());
            for (StackTraceElement frame : exception.getStackTrace()) {
                if (stack.length() >= 4500) break;
                stack.append("\n at ").append(frame);
            }
            technical = stack.toString();
        }
        long duration = (System.nanoTime() - start) / 1_000_000;
        details.put("duration_ms", duration);
        audit.httpEvent(request, service, action, "END_" + request.getMethod(), error,
                duration, details, code, technical);
    }
}
