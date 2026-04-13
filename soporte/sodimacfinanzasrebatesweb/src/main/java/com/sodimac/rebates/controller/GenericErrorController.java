package com.sodimac.rebates.controller;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class GenericErrorController implements ErrorController {

	private static final String PATH = "/error";

	@RequestMapping(value = PATH)
	public void error(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		ServletContext servletContext = request.getServletContext();

		try {
			Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");

			if (Objects.nonNull(statusCode) && HttpStatus.NOT_FOUND.value() == statusCode) {
				response.sendRedirect(servletContext.getContextPath() + "/404");
			} else {
				if (request.getAttribute("javax.servlet.error.exception") instanceof OutOfMemoryError) {
					response.sendRedirect(servletContext.getContextPath() + "/500" + "?exception=java.lang.OuOfMemoryError: Java heap space");
				} else {
					Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");
					String mensajeException = exception.getMessage() + " - " + exception.getCause().getMessage();
					response.sendRedirect(servletContext.getContextPath() + "/500" + "?exception=" + mensajeException);
				}
			}
		} catch (Exception e) {
			response.sendRedirect(servletContext.getContextPath() + "/500");
		}

	}

	@Override
	public String getErrorPath() {
		// TODO Auto-generated method stub
		return null;
	}

}
