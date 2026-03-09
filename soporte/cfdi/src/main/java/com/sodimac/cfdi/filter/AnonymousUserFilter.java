package com.sodimac.cfdi.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.sodimac.cfdi.model.menu.MenuItem;


@Component
public class AnonymousUserFilter implements Filter {
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	  
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest requeste = (HttpServletRequest) request;
		String uri =requeste.getRequestURI();
		String servletPath = requeste.getServletPath();
		String contextPath = requeste.getContextPath();
	    HttpSession session = requeste.getSession();
	   
		try {
		    if (session != null && session.getAttribute("validSession") != null) {
			    boolean isValid = Boolean.parseBoolean(session.getAttribute("validSession").toString());
			    if(isValid) {
				    if(servletPath.equals("/inicio")) {
						chain.doFilter(request, response);
						return;
				    } else {
					    String paginas = session.getAttribute("paginas").toString();
					    String url = servletPath;

					    	if (IsAuthorized(paginas, url)) {
								chain.doFilter(request, response);
								return;
					    	} else {
								HttpServletResponse httpResponse = (HttpServletResponse) response;
								httpResponse.sendRedirect(requeste.getContextPath() + "/inicio");
								return;
					    	}

						
				    }
			    }


		    }
		    
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.sendRedirect(requeste.getContextPath() + "/index");

		} catch (Exception ex) {
			
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.sendRedirect(requeste.getContextPath() + "/index");
		}
		
	}
	
	private void verificaPermiso(List<MenuItem> menu, String pagina) {
		
	}

	@Override
	public void destroy() {
		
		
	}
	
	private boolean IsAuthorized(String paginas, String url) {
    	if (paginas.indexOf(url) >= 0) {
			return true;
    	} else {
    		return false;
    	}
	}
	

}
