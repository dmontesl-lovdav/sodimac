package com.sodimac.facturacion.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class AnonymousUserFilter implements Filter {


	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	  
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
		    HttpServletRequest requeste = (HttpServletRequest) request;
		    HttpSession session = requeste.getSession(false);
		    
			System.out.println("Entra al filtro");
			//validSession = false;
			if (session != null && ((boolean) session.getAttribute("validSession")) ) {
				chain.doFilter(request, response);
			} else {
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				httpResponse.sendRedirect("redirect");
			}
			

		} catch (Exception ex) {
			
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.sendRedirect("redirect");
		}
		
	}

	@Override
	public void destroy() {
		
		
	}

}
