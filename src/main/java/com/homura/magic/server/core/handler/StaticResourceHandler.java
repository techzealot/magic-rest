package com.homura.magic.server.core.handler;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.apache.tika.Tika;
import org.springframework.util.ResourceUtils;

import com.homura.magic.server.core.MagicRequest;
import com.homura.magic.server.core.MagicResponse;
import com.homura.magic.server.exception.HandlerInternalException;
import com.homura.magic.server.utils.MagicUtils;

import io.netty.handler.codec.DateFormatter;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

public class StaticResourceHandler implements MagicHandler {
	// 资源根路径,必须为文件夹,不能以/结束
	private String resourceRoot;
	private final static int DEFAULT_CACHE_EXPIRE = 3600;
	private int cacheDuration = DEFAULT_CACHE_EXPIRE;
	private Tika tika = new Tika();

	public StaticResourceHandler(String resourceRoot) {
		this(resourceRoot, DEFAULT_CACHE_EXPIRE);
	}

	public StaticResourceHandler(String resourceRoot, int cacheDuration) {
		super();
		this.resourceRoot = resourceRoot;
		this.cacheDuration = cacheDuration;
	}

	@Override
	public void handle(MagicRequest request, MagicResponse response) throws Exception {
		String realPath = MagicUtils.compueRealPath(request,resourceRoot);
		File file = ResourceUtils.getFile(realPath);
		String if_modified_since = request.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
		long lastModified = file.lastModified();
		if (if_modified_since != null) {
			Date httpDate = DateFormatter.parseHttpDate(if_modified_since);
			// 文件未修改则返回304
			System.out.println(lastModified);
			System.out.println(httpDate.getTime());
			// 只能传递到秒
			if (lastModified / 1000 == httpDate.getTime() / 1000) {
				response.response(HttpResponseStatus.NOT_MODIFIED);
				response.addHeader(HttpHeaderNames.CACHE_CONTROL, String.format("private, max-age=%s", cacheDuration));
				response.addHeader(HttpHeaderNames.DATE, DateFormatter.format(new Date())).flush();
				return;
			}
		}
		// 添加文件类型
		try {
			String contentType = tika.detect(file);
			if (contentType.equals("text/plain")) {
				contentType = tika.detect(file.getName());
			}
			response.addHeader(HttpHeaderNames.CONTENT_TYPE, contentType);
		} catch (Exception e) {
			e.printStackTrace();
			throw HandlerInternalException.INSTANCE;
		}
		Calendar instance = Calendar.getInstance();
		instance.add(Calendar.SECOND, cacheDuration);
		response.addHeader(HttpHeaderNames.EXPIRES, DateFormatter.format(instance.getTime()));
		response.addHeader(HttpHeaderNames.CACHE_CONTROL, String.format("private, max-age=%s", cacheDuration));
		Calendar lastModifiedDate = Calendar.getInstance();
		lastModifiedDate.setTimeInMillis(lastModified);
		response.addHeader(HttpHeaderNames.LAST_MODIFIED, DateFormatter.format(lastModifiedDate.getTime()));
		//response.response(Files.readAllBytes(file.toPath()));
		response.response(file);
		response.flush();
	}

}
