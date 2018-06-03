package com.homura.magic.server.filter;

import java.util.List;

import com.homura.magic.server.core.MagicRequest;
import com.homura.magic.server.core.MagicResponse;
import com.homura.magic.server.core.handler.MagicHandler;

/**
 * 参考servlet FilterChain接口定义
 * 
 * @author Administrator
 * @see org.apache.catalina.core.ApplicationFilterFactory#createFilterChain
 */

public interface FilterChain {
	void doFilter(MagicRequest request, MagicResponse response) throws Exception;

	Filter[] getFilters();

	FilterChain setHandler(MagicHandler handler);

	public FilterChain addFilters(List<Filter> filters);

	public FilterChain addFilter(Filter filter);
}
