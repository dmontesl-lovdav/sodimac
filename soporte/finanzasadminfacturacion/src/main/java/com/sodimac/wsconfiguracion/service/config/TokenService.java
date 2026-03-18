package com.sodimac.wsconfiguracion.service.config;

public interface TokenService {

    String getToken(String sessionId, String username);

    String validateToken(String sessionId, String token);
}