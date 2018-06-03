package com.homura.magic.server.utils;

import com.homura.magic.server.core.MagicRequest;

public class MagicUtils {
	public static String compueRealPath(MagicRequest request, String resourceRoot) {
		// 如： /a/b/c/**
		String handlerMapping = request.getHandlerMapping().trim();
		// 得到 /a/b/c
		String part = handlerMapping.substring(0, handlerMapping.lastIndexOf("/"));
		// 真实请求为:/a/b/c/pics/a.jpg
		String path = request.path().trim();
		// 真实路径的后半部分 /pics/a.jpg
		String end = path.substring(path.indexOf(part) + part.length());
		// 真实路径的前半部分
		String start = "";
		if (resourceRoot.endsWith("/")) {
			start = resourceRoot.substring(0, resourceRoot.length() - 1);
		} else {
			start = resourceRoot;
		}
		return start + end;
	}

}
