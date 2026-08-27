package com.sodimac.batch.fiscal.download.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Value("${fiscal-api.timeout-ms:10000}")
    private int timeoutMs;

    // El WAF (Cloudflare) del portal FBC rechaza el User-Agent por defecto de Java (error 1010)
    @Value("${fiscal-api.user-agent:batch-fiscal-download/1.0}")
    private String userAgent;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // El servidor UAT solo acepta TLS 1.3. HttpURLConnection de Java 17+ negocia TLS 1.3
        // correctamente; Apache HttpClient 4.5.x tiene incompatibilidades con TLS 1.3.
        // Requiere ejecutar con Java 17+ (no Java 8u302).
        return builder
                .setConnectTimeout(Duration.ofMillis(timeoutMs))
                .setReadTimeout(Duration.ofMillis(timeoutMs * 2L))
                .additionalInterceptors((request, body, execution) -> {
                    request.getHeaders().set(HttpHeaders.USER_AGENT, userAgent);
                    return execution.execute(request, body);
                })
                .build();
    }
}
