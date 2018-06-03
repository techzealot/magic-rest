package com.homura.magic.server.core;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.homura.magic.server.core.handler.MagicHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

public class MagicRequest {

	private final ChannelHandlerContext ctx;

	private FullHttpRequest request;

	private Set<Cookie> cookies;

	private QueryStringDecoder qs;

	private String remoteAddress;

	private MagicHandler handler;

	private String handlerMapping;

	private final Map<String, Object> attributes = new HashMap<>();

	public MagicRequest(ChannelHandlerContext ctx) {
		this(ctx, null);
	}

	public MagicRequest(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {
		super();
		this.ctx = ctx;
		this.request = httpRequest;
		// 将本对象传入channelContext的attributeMap中供response使用
		setChannelAttribute("request", this);
	}

	public <T> MagicRequest setChannelAttribute(String key, T value) {
		this.ctx.channel().attr(AttributeKey.<T> valueOf(key)).set(value);
		return this;
	}

	// ---更换名字，易引起歧义,添加属性集合
	public <T> T getChannelAttribute(String key) {
		return ctx.channel().attr(AttributeKey.<T> valueOf(key)).get();
	}

	public FullHttpRequest getHttpRequest() {
		return request;
	}

	public void setHttpRequest(FullHttpRequest httpRequest) {
		this.request = httpRequest;
	}

	public String path() {
		if (qs == null) {
			qs = new QueryStringDecoder(request.uri(), CharsetUtil.UTF_8);
		}
		return qs.path();
	}

	public HttpMethod method() {
		return request.method();
	}

	public Map<String, List<String>> params() {
		if (qs == null) {
			qs = new QueryStringDecoder(request.uri(), CharsetUtil.UTF_8);
		}
		return qs.parameters();
	}

	public HttpHeaders headers() {
		return request.headers();
	}

	public String getRemoteAddress() {
		return remoteAddress == null ? ((InetSocketAddress) ctx.channel().remoteAddress()).getHostString()
				: remoteAddress;
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public Set<Cookie> cookies() {
		if (cookies == null) {
			String rawCookies = request.headers().get(HttpHeaderNames.COOKIE);
			cookies = rawCookies != null ? ServerCookieDecoder.STRICT.decode(rawCookies)
					: Collections.<Cookie> emptySet();
		}
		return cookies;
	}

	public boolean isAjax() {
		return "XMLHttpRequest".equals(request.headers().getAsString("X-Requested-With"));
	}

	public MagicHandler getHandler() {
		return handler;
	}

	public void setHandler(MagicHandler handler) {
		this.handler = handler;
	}

	public String getHandlerMapping() {
		return handlerMapping;
	}

	public void setHandlerMapping(String handlerMapping) {
		this.handlerMapping = handlerMapping;
	}

	public MagicRequest setAttribute(String key, Object value) {
		attributes.put(key, value);
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String key) {
		return (T) attributes.get(key);
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
