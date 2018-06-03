package com.homura.magic.server.timeout;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class MagicReadTimeoutHandler extends ReadTimeoutHandler {

	public MagicReadTimeoutHandler(int timeoutSeconds) {
		super(timeoutSeconds);
	}

	public MagicReadTimeoutHandler(long timeout, TimeUnit unit) {
		super(timeout, unit);
	}

	@Override
	protected void readTimedOut(ChannelHandlerContext ctx) throws Exception {
		// TODO 对于非Keep-alive连接直接断开，否则维持长连接
		super.readTimedOut(ctx);
	}
	
}
