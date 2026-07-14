/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.fiscal.api.security;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.sodimac.fiscal.api.exception.GenericException;

/**
 *
 * @author ggalvan
 */
@Component
@Aspect
public class GroupValidator {

    private static final String K_FORBIDDEN = "FORBIDDEN";

    private static final Logger logger = LoggerFactory.getLogger(GroupValidator.class);
    //
    private final String roleOperador;
    private final String roleProveedor;

    public GroupValidator(
            @Value("${fiscal.jwt.role.operador}") String roleOperador,
            @Value("${fiscal.jwt.role.proveedor}") String roleProveedor) {
        this.roleOperador = roleOperador;
        this.roleProveedor = roleProveedor;
    }

    @Before(value = "@annotation(com.sodimac.fiscal.api.security.RequireRole)")
    public void validate(JoinPoint joinPoint) throws GenericException {
        Session session = null;

        logger.info("VALIDATING SESSION WITH - OPERADOR ROLE: {} - PROVEEDOR ROLE: {}", this.roleOperador, this.roleProveedor);
        try {
            for (Object arg : joinPoint.getArgs()) {
                if (arg instanceof Session) {
                    session = ((Session) arg);
                    break;
                }
            }
            if (session == null) {
                logger.warn("NO SESSION IS ATTACHED TO THIS JOINT");
                throw new GenericException(K_FORBIDDEN, HttpStatus.FORBIDDEN.value());
            }
        } catch (Exception e) {
            logger.warn("CAN'T RETRIEVE JOINT OBJECTS: {} - CAUSE: {}", e.getMessage(), e.getCause());
            throw new GenericException("CAN'T RETRIEVE USER GROUPS FROM SESSION", HttpStatus.FORBIDDEN.value());
        }

        if (session.getGroups() == null || session.getGroups().isEmpty()) {
            logger.warn("THIS SESSION HAS NO GROUPS");
            throw new GenericException(K_FORBIDDEN, HttpStatus.FORBIDDEN.value());
        }

        boolean isOperador = session.getGroups().stream()
                .anyMatch(userGroup -> userGroup.equalsIgnoreCase(this.roleOperador));
        boolean isProveedor = session.getGroups().stream()
                .anyMatch(userGroup -> userGroup.equalsIgnoreCase(this.roleProveedor));

        if (!isOperador && !isProveedor) {
            logger.warn("THIS SESSION HAS NO EXPECTED GROUPS. SESSION GROUPS ARE: {}", StringUtils.join(session.getGroups(),","));
            throw new GenericException(K_FORBIDDEN, HttpStatus.FORBIDDEN.value());
        }

        session.setOperator(isOperador);
    }
}

