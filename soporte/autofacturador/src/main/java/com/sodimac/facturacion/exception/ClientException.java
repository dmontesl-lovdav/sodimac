package com.sodimac.facturacion.exception;

public class ClientException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2940972413278703164L;

	public ClientException() {
	}

	public ClientException(String arg0) {
		super(arg0);
	}

	public ClientException(Throwable arg0) {
		super(arg0);
	}

	public ClientException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ClientException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
