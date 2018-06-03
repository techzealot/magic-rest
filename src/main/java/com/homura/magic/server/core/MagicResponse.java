package com.homura.magic.server.core;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.util.CharsetUtil.UTF_8;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.util.MimeTypeUtils;

import com.alibaba.fastjson.JSONObject;
import com.homura.magic.server.core.renderer.Renderer;
import com.homura.magic.server.exception.ResponseHasBeenClosedException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

public class MagicResponse {

	private ChannelHandlerContext ctx;

	private HttpResponse response;

	private Map<CharSequence, String> headers = new HashMap<>();
	// 采用list的理由，可能重复吗?
	private Set<Cookie> cookies = new HashSet<>();

	// 文件传输
	private boolean closed = false;

	private File file;

	private Renderer renderer;

	public MagicResponse(ChannelHandlerContext ctx) {
		this(ctx, null);
	}

	public MagicResponse(ChannelHandlerContext ctx, FullHttpResponse httpResponse) {
		super();
		this.ctx = ctx;
		this.response = httpResponse;
	}

	public HttpResponse getHttpResponse() {
		return response;
	}

	public void setHttpResponse(FullHttpResponse httpResponse) {
		this.response = httpResponse;
	}

	// 使用channel的attributes机制在request和response间传递数据
	public <T> T getChannelAttribute(String key) {
		return ctx.channel().attr(AttributeKey.<T> valueOf(key)).get();
	}

	public <T> MagicResponse setChannelAttribute(String key, T value) {
		this.ctx.channel().attr(AttributeKey.<T> valueOf(key)).set(value);
		return this;
	}
	// 采用装饰模式，包装ChannelHandlerContext的方法，不直接暴露ChannelHandlerContext，避免通道被异常关闭

	public MagicResponse response(byte[] content) {
		final ByteBuf byteBuf = Unpooled.copiedBuffer(content);
		response = new DefaultFullHttpResponse(HTTP_1_1, OK, byteBuf);
		return this;
	}

	public MagicResponse response(File file) {
		// ***注意：response的类型不能为Full类型的，否则不会返回数据****
		response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		this.file = file;
		return this;
	}

	public MagicResponse response(ByteBuf byteBuf) {
		response = new DefaultFullHttpResponse(HTTP_1_1, OK, byteBuf);
		return this;
	}

	public MagicResponse response(HttpResponseStatus status) {
		response = new DefaultFullHttpResponse(HTTP_1_1, status);
		return this;
	}

	public MagicResponse response(byte[] content, HttpResponseStatus status) {
		final ByteBuf byteBuf = Unpooled.copiedBuffer(content);
		response = new DefaultFullHttpResponse(HTTP_1_1, status, byteBuf);
		return this;
	}

	public MagicResponse response(String content, HttpResponseStatus status) {
		final ByteBuf byteBuf = Unpooled.wrappedBuffer(content.getBytes(UTF_8));
		response = new DefaultFullHttpResponse(HTTP_1_1, status, byteBuf);
		return this;
	}

	public MagicResponse response(FullHttpResponse response) {
		this.response = response;
		return this;
	}

	public MagicResponse render(String path, Map<String, Object> attributes) throws Exception {
		html(renderer.render(path, attributes));
		return this;
	}

	public MagicResponse html(String content) {
		text(content, MimeTypeUtils.TEXT_HTML_VALUE, HttpResponseStatus.OK);
		return this;
	}

	public <T> MagicResponse json(T body) {
		response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
				Unpooled.wrappedBuffer(JSONObject.toJSONString(body).getBytes(CharsetUtil.UTF_8)));
		response.headers().add(HttpHeaderNames.CONTENT_TYPE, String.format("%s; charset=utf-8", "application/json"));
		return this;
	}

	public void text(String content, String contentType, HttpResponseStatus status) {
		byte[] bytes = content.getBytes(CharsetUtil.UTF_8);
		response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(bytes));
		response.headers().add(HttpHeaderNames.CONTENT_TYPE, String.format("%s; charset=utf-8", contentType));
	}

	public MagicResponse addHeader(CharSequence key, String value) {
		headers.put(key, Objects.requireNonNull(value, "添加的header值不能为空"));
		return this;
	}

	public MagicResponse addCookie(Cookie cookie) {
		cookies.add(Objects.requireNonNull(cookie, "添加的cookie值不能为空"));
		return this;
	}

	/**
	 * 输出全部响应内容,
	 * 
	 * @throws IOException
	 */
	public void flush() throws IOException {
		// 如果通道已经关闭，抛出异常
		if (isClosed()) {
			throw ResponseHasBeenClosedException.INSTANCE;
		}
		// 收集响应的各个部分，组成完整的响应并输出,最后关闭本次响应
		if (response == null) {
			response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		}
		// 处理跨域问题
		response.headers().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		response.headers().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET,PUT,GET,DELETE");
		response.headers().add(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE, "3600");
		response.headers().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, HttpHeaderNames.CONTENT_TYPE);
		// 组装cookies
		for (Cookie cookie : cookies) {
			response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
		}
		// 组装headers
		for (Map.Entry<CharSequence, String> header : headers.entrySet()) {
			response.headers().add(header.getKey(), header.getValue());
		}
		// 判断keepalive请求头
		MagicRequest request = this.<MagicRequest> getChannelAttribute("request");
		boolean keepalive = HttpUtil.isKeepAlive(request.getHttpRequest());
		if (keepalive) {
			response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
			// 当连接为keepalive时，不要关闭连接
			if (file != null) {
				response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
				ctx.write(response);

				ChannelFuture sendFileFuture = ctx.write(new HttpChunkedInput(new ChunkedFile(file, 8192)),
						ctx.newProgressivePromise());

				/*
				 * ChannelFuture sendFileFuture = ctx.write(new
				 * DefaultFileRegion(file, 0, file.length()),
				 * ctx.newProgressivePromise());
				 * 
				 * ctx.write(LastHttpContent.EMPTY_LAST_CONTENT);
				 */
				sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
					@Override
					public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
						if (total < 0) { // total unknown
							System.err.println(future.channel() + " Transfer progress: " + progress);
						} else {
							System.err.println(future.channel() + " Transfer progress: " + progress + " / " + total);
						}
					}

					@Override
					public void operationComplete(ChannelProgressiveFuture future) {
						System.err.println(future.channel() + " Transfer complete.");
					}
				});

			} else {
				response.headers().set(HttpHeaderNames.CONTENT_LENGTH,
						((DefaultFullHttpResponse) response).content().readableBytes());
				ctx.write(response);
			}
		} else {
			// 返回数据并关闭连接
			ChannelFuture future = ctx.write(response);
			if (file != null) {
				ctx.write(new DefaultFileRegion(file, 0, file.length()));
				ctx.write(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
			} else {
				future.addListener(ChannelFutureListener.CLOSE);
			}
		}
		ctx.flush();
		close();
	}

	public boolean isClosed() {
		return closed;
	}

	/**
	 * mark the channel closed，通道不一定会关闭,通道的关闭由keepalive和超时机制控制
	 */
	private void close() {
		this.closed = true;
	}

	public Renderer getRednerer() {
		return renderer;
	}

	public void setRednerer(Renderer rednerer) {
		this.renderer = rednerer;
	}

}
