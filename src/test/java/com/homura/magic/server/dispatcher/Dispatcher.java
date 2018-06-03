package com.homura.magic.server.dispatcher;

import com.homura.magic.server.core.MagicRequest;
import com.homura.magic.server.core.MagicResponse;

public interface Dispatcher {
	/**
	 * 初始化过滤器链，并调用真正的service返回结果
	 * @param request
	 * @param response 
	 * @throws Exception
	 */
	void dispatch(MagicRequest request, MagicResponse response) throws Exception;

}
