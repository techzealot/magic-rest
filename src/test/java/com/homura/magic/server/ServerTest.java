package com.homura.magic.server;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.homura.magic.server.bootstrap.Magic;
import com.homura.magic.server.core.MagicRequest;
import com.homura.magic.server.core.MagicResponse;
import com.homura.magic.server.core.handler.MagicHandler;
import com.homura.magic.server.core.handler.StaticResourceHandler;
import com.homura.magic.server.core.plugin.StaticResourcePlugin;
import com.homura.magic.server.router.MagicRouter;

import io.netty.handler.codec.http.cookie.DefaultCookie;

public class ServerTest {

	@Test
	public void testServer() {

	}

	public static void main(String[] args) throws InterruptedException {
		new Magic().
		router(new MagicRouter()
				.get("/freemarker", new MagicHandler() {
					
					@Override
					public void handle(MagicRequest request, MagicResponse response) throws Exception {
						Map<String, Object> attr=new HashMap<>();
						attr.put("name", "hello world");
						response.render("hello.ftl", attr).flush();
					}
				})
				.get("/hello*", new MagicHandler() {

			@Override
			public void handle(MagicRequest request, MagicResponse response) throws Exception {
				System.out.println(request.headers().toString());
				response.json(new Person("tom", 10)).addCookie(new DefaultCookie("a", "b"))
						.addCookie(new DefaultCookie("c", "d")).flush();
			}
		})
				)
		.plugins(new StaticResourcePlugin("/statics/**", new StaticResourceHandler("classpath:static/assets"))
				)
		.templateRoot("/template")
		.bind(8080);
	}
}
