package com.sodimac.fiscal.api.config;

import com.sodimac.fiscal.api.service.impl.AuditoriaApiServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Observa handlers y errores; conserva las respuestas y el ControllerAdvisor existente. */
@ControllerAdvice
public class FiscalAuditConfig implements WebMvcConfigurer, HandlerInterceptor, ResponseBodyAdvice<Object> {
    private final AuditoriaApiServiceImpl audit;

    public FiscalAuditConfig(AuditoriaApiServiceImpl audit) { this.audit = audit; }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).order(Ordered.HIGHEST_PRECEDENCE);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getAttribute(AuditoriaApiServiceImpl.TRACE) != null
                && request.getAttribute("fiscal.audit.init") == null && handler instanceof HandlerMethod method) {
            request.setAttribute("fiscal.audit.init", true);
            audit.httpEvent(request, method.getBeanType().getSimpleName(), method.getMethod().getName(),
                    "INIT_METHOD", false, 0, Map.of("endpoint", request.getRequestURI()), null, null);
        }
        return true;
    }

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(0, (request, response, handler, exception) -> {
            request.setAttribute(AuditoriaApiServiceImpl.ERROR, exception);
            return null; // Continúa el manejo original de excepciones.
        });
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType contentType,
            Class<? extends HttpMessageConverter<?>> converterType, ServerHttpRequest request,
            ServerHttpResponse response) {
        if (body == null || !(request instanceof ServletServerHttpRequest servlet)
                || servlet.getServletRequest().getAttribute(AuditoriaApiServiceImpl.TRACE) == null) return body;
        try {
            // Solo indicadores escalares: no serializar XML, archivos, páginas ni entidades completas.
            Map<String, Object> summary = new LinkedHashMap<>();
            BeanWrapperImpl bean = body instanceof Map<?, ?> ? null : new BeanWrapperImpl(body);
            for (String key : List.of("success", "valid", "isValid", "code", "errorCode", "businessCode", "message", "errorMessage")) {
                Object value = body instanceof Map<?, ?> map ? map.get(key)
                        : bean.isReadableProperty(key) ? bean.getPropertyValue(key) : null;
                if (value instanceof Boolean || value instanceof Number) summary.put(key, value);
                else if (value instanceof String text) summary.put(key, AuditoriaApiServiceImpl.limit(text, 1000));
            }
            HttpServletRequest http = servlet.getServletRequest();
            http.setAttribute(AuditoriaApiServiceImpl.SUMMARY, summary);
            if (Boolean.FALSE.equals(summary.get("success")) || Boolean.FALSE.equals(summary.get("valid"))
                    || Boolean.FALSE.equals(summary.get("isValid"))) {
                http.setAttribute(AuditoriaApiServiceImpl.BUSINESS_ERROR, true);
            }
        } catch (RuntimeException ignored) {
            // La inspección de auditoría nunca impide escribir el cuerpo original.
        }
        return body;
    }
}
