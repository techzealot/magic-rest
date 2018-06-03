package com.homura.magic.server.filter;

import com.homura.magic.server.core.MagicRequest;
import com.homura.magic.server.core.MagicResponse;

/**
 * 参考servlet Filter接口定义
 * 
 * @author Administrator
 *
 */
public interface Filter {
	void doFilter(MagicRequest request, MagicResponse response, FilterChain chain) throws Exception;

	String[] getUrlMappings();
}
