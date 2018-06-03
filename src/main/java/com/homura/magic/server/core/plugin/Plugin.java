package com.homura.magic.server.core.plugin;

import com.homura.magic.server.core.handler.MagicHandler;

public interface Plugin {

	public String getMapping();

	public MagicHandler getHandler();

}
