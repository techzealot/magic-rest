package com.homura.magic.server.exception;

public class HandlerInternalException extends RuntimeException {

	private static final long serialVersionUID = 1208906414716724161L;
	public static final HandlerInternalException INSTANCE = new HandlerInternalException();

	private HandlerInternalException() {
	}
}
