package com.homura.magic.server.filter;

import java.util.List;

import com.homura.magic.server.core.MagicRequest;
import com.homura.magic.server.core.MagicResponse;
import com.homura.magic.server.core.handler.MagicHandler;
import com.homura.magic.server.exception.FilterInternalException;
import com.homura.magic.server.exception.HandlerInternalException;

/**
 * 参考Tomcat filterChain的实现 一个handler对应于一个FilterChain实例
 * 
 * @author Administrator
 *
 */
public class MagicFilterChain implements FilterChain {

	private MagicHandler handler;
	private Filter[] filters = new Filter[0];
	/**
	 * The int which is used to maintain the current position in the filter
	 * chain.
	 */
	private int pos = 0;

	/**
	 * The int which gives the current number of filters in the chain.
	 */
	private int n = 0;
	/**
	 * filters size increment
	 */
	public static final int INCREMENT = 10;

	@Override
	public void doFilter(MagicRequest request, MagicResponse response) throws Exception {
		internalDoFilter(request, response);
	}

	private void internalDoFilter(MagicRequest request, MagicResponse response) throws Exception {
		// Call the next filter if there is one
		if (pos < n) {
			Filter filter = filters[pos++];
			try {
				filter.doFilter(request, response, this);
			} catch (Exception e) {
				e.printStackTrace();
				throw FilterInternalException.INSTANCE;
			}
			return;
		}
		// We fell off the end of the chain -- call the handler instance
		try {
			if (handler != null) {
				handler.handle(request, response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw HandlerInternalException.INSTANCE;
		}
	}

	@Override
	public Filter[] getFilters() {
		return this.filters;
	}

	@Override
	public FilterChain addFilter(Filter filter) {
		// prevent null
		if (filter == null) {
			return this;
		}
		// Prevent the same filter being added multiple times
		for (Filter f : this.filters) {
			// same pointer
			if (f == filter) {
				return this;
			}
		}
		if (n == filters.length) {
			Filter[] newFilters = new Filter[n + INCREMENT];
			System.arraycopy(filters, 0, newFilters, 0, n);
			filters = newFilters;
		}
		filters[n++] = filter;
		return this;
	}

	@Override
	public FilterChain addFilters(List<Filter> filters) {
		for (Filter filter : filters) {
			addFilter(filter);
		}
		return this;
	}

	@Override
	public FilterChain setHandler(MagicHandler handler) {
		this.handler = handler;
		return this;
	}

}
