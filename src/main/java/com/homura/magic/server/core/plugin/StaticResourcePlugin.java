package com.homura.magic.server.core.plugin;

import com.homura.magic.server.core.handler.MagicHandler;

public class StaticResourcePlugin extends PluginAdapter {

	public StaticResourcePlugin(String mapping, MagicHandler handler) {
		super(mapping, handler);
	}

}
