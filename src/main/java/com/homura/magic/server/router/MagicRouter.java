package com.homura.magic.server.router;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.homura.magic.server.core.MagicRequest;
import com.homura.magic.server.core.MagicResponse;
import com.homura.magic.server.core.handler.MagicHandler;
import com.homura.magic.server.exception.HandlerNotFoundException;
import com.homura.magic.server.filter.Filter;
import com.homura.magic.server.filter.FilterChain;
import com.homura.magic.server.filter.MagicFilterChain;

import io.netty.handler.codec.http.HttpMethod;

/**
 * 根据URL找到对应的handler 暂不支持子路由
 * 
 * @author Administrator
 *
 */
public class MagicRouter implements Router {

	private ConcurrentHashMap<PatternBinding, MagicHandler> routes = new ConcurrentHashMap<>();

	private List<Filter> filters = new ArrayList<>();

	private PathMatcher antPathMatcher = new AntPathMatcher();

	@Override
	public void route(MagicRequest request, MagicResponse response) throws Exception {
		MagicHandler handler = doMatch(request);
		FilterChain filterChain = prepareFilterChain(request.getHttpRequest().uri(), handler, filters);
		filterChain.doFilter(request, response);
	}

	private FilterChain prepareFilterChain(String handlerMapping, MagicHandler handler, List<Filter> filters) {
		FilterChain filterChain = new MagicFilterChain().setHandler(handler);
		// 找出匹配url pattern的过滤器
		for (Filter filter : filters) {
			String[] urlMappings = filter.getUrlMappings();
			if (urlMappings == null) {
				continue;
			}
			for (String mapping : urlMappings) {
				// ant模式匹配
				if (antPathMatcher.match(mapping, handlerMapping)) {
					filterChain.addFilter(filter);
				}
			}
		}
		return filterChain;
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	public MagicHandler doMatch(MagicRequest request) {
		HttpMethod method = request.method();
		String path = request.path();
		MagicHandler magicHandler = routes.get(new PatternBinding(method, path));
		if (magicHandler != null) {
			return magicHandler;
		}
		Map<String, MagicHandler> handlers = new HashMap<>();
		for (Map.Entry<PatternBinding, MagicHandler> entry : routes.entrySet()) {
			HttpMethod handlerMethod = entry.getKey().method;
			String handlerMapping = entry.getKey().pattern;
			if (!method.equals(handlerMethod)) {
				continue;
			}
			if (antPathMatcher.match(handlerMapping, path)) {
				handlers.put(handlerMapping, entry.getValue());
			}
		}
		if (handlers.isEmpty()) {
			throw HandlerNotFoundException.INSTANCE;
		}
		// TODO 做优先级匹配,取最长的
		MagicHandler mostMatchhandler = null;
		String mostMatchMapping = null;
		int maxLength = 0;
		for (Map.Entry<String, MagicHandler> entry : handlers.entrySet()) {
			if (entry.getKey().length() > maxLength) {
				mostMatchMapping = entry.getKey();
				mostMatchhandler = entry.getValue();
				maxLength = mostMatchMapping.length();
			}
		}
		// 将处理该请求的handler关联起来
		request.setHandler(mostMatchhandler);
		request.setHandlerMapping(mostMatchMapping);
		return mostMatchhandler;

	}

	/**
	 * 
	 * 添加过滤器到Router
	 */

	public Router addFilters(Filter... filters) {
		for (Filter filter : filters) {
			if (filter == null) {
				continue;
			}
			this.filters.add(filter);
		}
		return this;
	}

	private void putIfAbsent(PatternBinding patternBinding, MagicHandler handler) {
		routes.putIfAbsent(patternBinding, handler);
	}

	public MagicRouter get(String pattern, MagicHandler handler) {
		putIfAbsent(new PatternBinding(HttpMethod.GET, pattern), handler);
		return this;
	}

	public MagicRouter post(String pattern, MagicHandler handler) {
		putIfAbsent(new PatternBinding(HttpMethod.POST, pattern), handler);
		return this;
	}

	public MagicRouter put(String pattern, MagicHandler handler) {
		putIfAbsent(new PatternBinding(HttpMethod.PUT, pattern), handler);
		return this;
	}

	public MagicRouter delete(String pattern, MagicHandler handler) {
		putIfAbsent(new PatternBinding(HttpMethod.DELETE, pattern), handler);
		return this;
	}

	// 同时支持get和post方法
	public MagicRouter handler(String pattern, MagicHandler handler) {
		putIfAbsent(new PatternBinding(HttpMethod.GET, pattern), handler);
		putIfAbsent(new PatternBinding(HttpMethod.POST, pattern), handler);
		return this;
	}

	public static class PatternBinding {
		final HttpMethod method;
		final String pattern;

		private PatternBinding(HttpMethod method, String pattern) {
			this.method = method;
			this.pattern = pattern;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof PatternBinding)) {
				return false;
			}

			PatternBinding that = (PatternBinding) o;

			if (!method.equals(that.method)) {
				return false;
			}
			if (!pattern.equals(that.pattern)) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result = method.hashCode();
			result = 31 * result + pattern.hashCode();
			return result;
		}
	}

}
