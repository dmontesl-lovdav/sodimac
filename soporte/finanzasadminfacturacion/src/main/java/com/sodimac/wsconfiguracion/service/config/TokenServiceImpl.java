package com.sodimac.wsconfiguracion.service.config;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(transactionManager = "transactionManagerConfig")
public class TokenServiceImpl implements TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);

    @PersistenceContext(unitName = "entityManagerFactoryConfig")
    private EntityManager entityManager;

    @Override
    public String getToken(String sessionId, String username) {
        try {
            logger.info("getToken - sessionId: {}, username: {}", sessionId, username);
            String sql = "call uspGetTokenMultiple (:sessionId, :username)";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("sessionId", sessionId);
            query.setParameter("username", username);

            Object result = query.getSingleResult();
            String token = result != null ? result.toString() : null;
            logger.info("getToken - Token generado: {}", token);
            return token;
        } catch (Exception e) {
            logger.error("Error al obtener token para user: " + username, e);
            return null;
        }
    }

    @Override
    public String validateToken(String sessionId, String token) {
        try {
            logger.info("validateToken - sessionId: {}, token: {}", sessionId, token);
            String sql = "call uspExistToken (:sessionId, :token)";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("sessionId", sessionId);
            query.setParameter("token", token);

            Object result = query.getSingleResult();
            String user = result != null ? result.toString() : null;
            logger.info("validateToken - Usuario obtenido: {}", user);
            return user;
        } catch (Exception e) {
            logger.error("Error al validar token: " + token, e);
            return null;
        }
    }
}