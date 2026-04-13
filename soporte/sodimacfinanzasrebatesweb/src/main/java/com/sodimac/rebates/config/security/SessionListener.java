package com.sodimac.rebates.config.security;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionListener implements HttpSessionListener {

	private int sessionExp_socconds = 60 * 15;

	@Autowired
	HttpServletResponse response;

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		event.getSession().setMaxInactiveInterval(sessionExp_socconds);
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		System.out.println("session destroyed");
	}

}
