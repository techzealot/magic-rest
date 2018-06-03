package com.homura.magic.server.core.renderer;

import java.io.StringWriter;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class FreemarkerRender implements Renderer{

	private String templateRoot;

	private Configuration freemarkerConfig;

	private String encoding;

	public FreemarkerRender(String templateRoot, String encoding) {
		super();
		this.templateRoot = templateRoot;
		this.encoding = encoding;
		freemarkerConfig = new Configuration(Configuration.VERSION_2_3_28);
		freemarkerConfig.setClassForTemplateLoading(FreemarkerRender.class, templateRoot);
		freemarkerConfig.setOutputEncoding(this.encoding);
	}

	@Override
	public String render(String path, Map<String, Object> attributes) throws Exception {
		Template template = freemarkerConfig.getTemplate(path);
		StringWriter writer = new StringWriter();
		template.process(attributes, writer);
		return writer.toString();
	}

	public String getTemplateRoot() {
		return templateRoot;
	}

	public void setTemplateRoot(String templateRoot) {
		this.templateRoot = templateRoot;
	}

	public Configuration getFreemarkerConfig() {
		return freemarkerConfig;
	}

	public void setFreemarkerConfig(Configuration freemarkerConfig) {
		this.freemarkerConfig = freemarkerConfig;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
}
