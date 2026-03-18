package com.sodimac.facturacion.exception;

import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.SQLGrammarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.sodimac.facturacion.cliente.ClienteTicketTimbrarExpRespTYPE;
import com.sodimac.facturacion.util.UtilsApi;
import com.sodimac.facturacion.util.enums.ECodigo;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	Logger logger = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);
	
    public RestResponseEntityExceptionHandler() {
        super();
    }

    // API

    // 400

    @ExceptionHandler({ DataIntegrityViolationException.class })
    public ResponseEntity<Object> handleBadRequest(final DataIntegrityViolationException ex, final WebRequest request) {
        final String bodyOfResponse = "This should be application specific 1";
        logger.error("400 Status 1 Code", ex);
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(final HttpMessageNotReadableException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
    	logger.error("400 Status Code", ex);
    	ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
    	UtilsApi.setRespuesta(response, ECodigo.Error.getValor());

        return new ResponseEntity<Object>(response, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
    	final String bodyOfResponse = "This should be application specific 3";
    	logger.error("400 Status 3 Code", ex);
        return handleExceptionInternal(ex, bodyOfResponse, headers, HttpStatus.BAD_REQUEST, request);
    }

    // 401
    @ExceptionHandler({ AuthenticationException.class })
    public ResponseEntity<Object> handleAuthenticationException(final Exception ex, final WebRequest request) {
    	logger.error("401 Status Code", ex);
    	ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
    	UtilsApi.setRespuesta(response, ECodigo.AccesoDenegado.getValor());

        return new ResponseEntity<Object>(response, HttpStatus.UNAUTHORIZED);
    }

    // 401
    @ExceptionHandler({ BadCredentialsException.class })
    public ResponseEntity<Object> handleBadCredentialsException(final Exception ex, final WebRequest request) {
    	logger.error("401 Status Code", ex);
    	ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
    	UtilsApi.setRespuesta(response, ECodigo.AccesoDenegado.getValor());

        return new ResponseEntity<Object>(response, HttpStatus.UNAUTHORIZED);
    }

    // 403
    @ExceptionHandler({ AccessDeniedException.class })
    public ResponseEntity<Object> handleAccessDeniedException(final Exception ex, final WebRequest request) {
    	logger.error("403 Status Code", ex);
    	ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
    	UtilsApi.setRespuesta(response, ECodigo.AccesoDenegado.getValor());

        return new ResponseEntity<Object>(response, HttpStatus.FORBIDDEN);
    }

    // 409

    @ExceptionHandler({ InvalidDataAccessApiUsageException.class, DataAccessException.class })
    protected ResponseEntity<Object> handleConflict(final RuntimeException ex, final WebRequest request) {
    	logger.error("409 Status Code", ex);
    	ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
    	UtilsApi.setRespuesta(response, ECodigo.Error.getValor());

        return new ResponseEntity<Object>(response, HttpStatus.CONFLICT);
    }

    // 412

    // 500

    @ExceptionHandler({ NullPointerException.class, IllegalArgumentException.class, IllegalStateException.class, JDBCConnectionException.class, SQLGrammarException.class })
    public ResponseEntity<Object> handleInternal(final RuntimeException ex, final WebRequest request) {
        logger.error("500 Status Code", ex);
        ClienteTicketTimbrarExpRespTYPE response = new ClienteTicketTimbrarExpRespTYPE();
    	UtilsApi.setRespuesta(response, ECodigo.Error.getValor());

        return new ResponseEntity<Object>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}