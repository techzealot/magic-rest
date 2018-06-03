package com.homura.magic.server.exception;

public class ResponseHasBeenClosedException extends RuntimeException {
	private static final long serialVersionUID = 2430232682570736556L;
	public static final ResponseHasBeenClosedException INSTANCE = new ResponseHasBeenClosedException();

	private ResponseHasBeenClosedException() {
	}
}
