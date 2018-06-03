package com.homura.magic.server.core.plugin;

import com.homura.magic.server.core.handler.MagicHandler;

public class PluginAdapter implements Plugin {

	private String mapping;
	private MagicHandler handler;

	public PluginAdapter(String mapping, MagicHandler handler) {
		super();
		this.mapping = mapping;
		this.handler = handler;
	}

	@Override
	public String getMapping() {
		return this.mapping;
	}

	@Override
	public MagicHandler getHandler() {
		return this.handler;
	}

	public void setMapping(String mapping) {
		this.mapping = mapping;
	}

	public void setHandler(MagicHandler handler) {
		this.handler = handler;
	}

}
