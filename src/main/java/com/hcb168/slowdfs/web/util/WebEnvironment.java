package com.hcb168.slowdfs.web.util;

import javax.servlet.ServletContext;

import org.springframework.web.context.ServletContextAware;

import com.hcb168.slowdfs.util.MyFileUtil;
import com.hcb168.slowdfs.util.MyUtil;

public class WebEnvironment implements ServletContextAware {
	private ServletContext servletContext;
	private String appPath = "";
	private String webInfPath = "";

	@Override
	public void setServletContext(ServletContext servletContext) {
		// TODO Auto-generated method stub
		this.servletContext = servletContext;

		this.appPath = MyFileUtil.formatPath(this.servletContext.getRealPath("/"));
		this.webInfPath = MyFileUtil.formatPath(this.servletContext.getRealPath("/WEB-INF"));

		MyUtil.getLogger().debug(this.getClass() + ":" + this.appPath);
		MyUtil.getLogger().debug(this.getClass() + ":" + this.webInfPath);
	}

	public String getAppPath() {
		return appPath;
	}

	public String getWebInfPath() {
		return webInfPath;
	}
}
