package com.homura.magic.server.bootstrap;

import java.util.Objects;

import com.homura.magic.server.core.plugin.Plugin;
import com.homura.magic.server.core.renderer.FreemarkerRender;
import com.homura.magic.server.handler.ServerHandler;
import com.homura.magic.server.router.MagicRouter;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class Magic {

	private boolean useEpoll = false;
	private Channel channel;
	EventLoopGroup bossGroup;
	EventLoopGroup workerGroup;
	private MagicRouter router;
	private String templateRoot="/template";
	private Plugin[] plugins = new Plugin[0];

	public void bindAwait(int port) throws InterruptedException {
		bind(port);
		channel.closeFuture().sync();
	}

	public void bind(int port) throws InterruptedException {
		channel = bindInternal(port).channel();
	}

	public ChannelFuture bindInternal(int port) throws InterruptedException {
		ServerBootstrap b = new ServerBootstrap();
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		b.group(bossGroup, workerGroup).option(ChannelOption.SO_BACKLOG, 1024)
				.channel(useEpoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast("httpCodec", new HttpServerCodec()).addLast(new HttpContentCompressor())
								// .addLast(new HttpRequestDecoder())
								.addLast(new HttpObjectAggregator(65536))
								// .addLast(new HttpResponseEncoder())
								.addLast(new ChunkedWriteHandler()).addLast(new ServerHandler(router, new FreemarkerRender(templateRoot, "UTF-8"),plugins));
					}
				});
		return b.bind(port).sync();
	}

	public void stop() {
		if (channel != null) {
			channel.close().syncUninterruptibly();
		}
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}

	public Magic router(MagicRouter router) {
		this.router = router;
		return this;
	}

	public Magic plugins(Plugin... plugins) {
		this.plugins = Objects.requireNonNull(plugins, "添加的插件不能为空");
		return this;
	}

	public Magic useEpoll() {
		this.useEpoll = true;
		return this;
	}

	public Magic templateRoot(String templateRoot) {
		this.templateRoot = templateRoot;
		return this;
	}
}
