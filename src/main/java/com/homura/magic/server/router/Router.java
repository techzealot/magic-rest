package com.homura.magic.server.router;

import com.homura.magic.server.core.MagicRequest;
import com.homura.magic.server.core.MagicResponse;

public interface Router {
	public void route(MagicRequest request, MagicResponse response) throws Exception;
}
