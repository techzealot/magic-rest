package com.homura.magic.server.core.renderer;

import java.util.Map;

public interface Renderer {

	String render(String path, Map<String, Object> attributes) throws Exception;

}
