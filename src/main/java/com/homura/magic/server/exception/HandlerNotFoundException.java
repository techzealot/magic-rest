package com.homura.magic.server.exception;

public class HandlerNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -8974703918778510896L;
	public static final HandlerNotFoundException INSTANCE = new HandlerNotFoundException();

	private HandlerNotFoundException() {
	}
}
