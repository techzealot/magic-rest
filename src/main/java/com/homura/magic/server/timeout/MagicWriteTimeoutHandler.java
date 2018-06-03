package com.homura.magic.server.timeout;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.WriteTimeoutHandler;

public class MagicWriteTimeoutHandler extends WriteTimeoutHandler{

	public MagicWriteTimeoutHandler(int timeoutSeconds) {
		super(timeoutSeconds);
	}

	public MagicWriteTimeoutHandler(long timeout, TimeUnit unit) {
		super(timeout, unit);
	}

	@Override
	protected void writeTimedOut(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.writeTimedOut(ctx);
	}

}
