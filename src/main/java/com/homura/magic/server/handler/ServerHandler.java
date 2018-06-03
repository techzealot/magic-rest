package com.homura.magic.server.handler;

import com.homura.magic.server.core.MagicRequest;
import com.homura.magic.server.core.MagicResponse;
import com.homura.magic.server.core.plugin.Plugin;
import com.homura.magic.server.core.renderer.Renderer;
import com.homura.magic.server.exception.FilterInternalException;
import com.homura.magic.server.exception.HandlerInternalException;
import com.homura.magic.server.exception.HandlerNotFoundException;
import com.homura.magic.server.exception.ResponseHasBeenClosedException;
import com.homura.magic.server.router.MagicRouter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AttributeKey;

public class ServerHandler extends ChannelInboundHandlerAdapter {
	private MagicRouter router;
	private Renderer renderer;

	public ServerHandler(MagicRouter router, Plugin... plugins) {
		this(router, null, plugins);
	}

	public ServerHandler(MagicRouter router, Renderer renderer, Plugin... plugins) {
		super();
		this.renderer = renderer;
		this.router = router;
		for (Plugin plugin : plugins) {
			this.router.handler(plugin.getMapping(), plugin.getHandler());
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof FullHttpRequest) {
			FullHttpRequest req = (FullHttpRequest) msg;
			// 将上下文传入request和response之中
			MagicResponse response = new MagicResponse(ctx);
			// 设置渲染器
			response.setRednerer(this.renderer);
			MagicRequest request = new MagicRequest(ctx, req);
			router.route(request, response);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		MagicRequest request = ctx.channel().attr(AttributeKey.<MagicRequest> valueOf("request")).get();
		// ajax请求返回json信息
		if (request != null && request.isAjax()) {
			if (cause instanceof HandlerNotFoundException) {
				ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
			} else if (cause instanceof HandlerInternalException || cause instanceof FilterInternalException
					|| cause instanceof ResponseHasBeenClosedException) {
				ctx.writeAndFlush(
						new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR));
			} else {
				ctx.writeAndFlush(
						new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR));
			}
		} else {
			// 非ajax请求,或者request解析失败等异常
			if (cause instanceof HandlerNotFoundException) {
				ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
			} else if (cause instanceof HandlerInternalException || cause instanceof FilterInternalException
					|| cause instanceof ResponseHasBeenClosedException) {
				ctx.writeAndFlush(
						new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR));
			} else {
				ctx.writeAndFlush(
						new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR));
			}
		}
		// 出现异常后关闭通道,keepalive连接也要关闭，避免异常的通道状态被保留，影响下次请求
		ctx.close();
	}

}
