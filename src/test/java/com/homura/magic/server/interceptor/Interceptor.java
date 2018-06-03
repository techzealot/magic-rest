package com.homura.magic.server.interceptor;

public interface Interceptor<T> {
	public void intercept(T target) throws Exception;
}
