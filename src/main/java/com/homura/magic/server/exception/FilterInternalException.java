package com.homura.magic.server.exception;

public class FilterInternalException extends RuntimeException {

	private static final long serialVersionUID = -8974703918778510896L;
	public static final FilterInternalException INSTANCE = new FilterInternalException();

	private FilterInternalException() {
	}
}
