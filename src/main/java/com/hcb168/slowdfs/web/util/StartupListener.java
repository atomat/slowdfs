package com.hcb168.slowdfs.web.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hcb168.slowdfs.config.ConfigHandle;
import com.hcb168.slowdfs.config.SlowDFSConfig;
import com.hcb168.slowdfs.util.MyFileUtil;
import com.hcb168.slowdfs.util.MyUtil;
import com.hcb168.slowdfs.util.SysParams;

public class StartupListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		MyUtil.getLogger().info("startup init begin");

		try {
			String webinfPath = event.getServletContext().getRealPath("/WEB-INF");
			String appPath = webinfPath.substring(0, webinfPath.length() - "/WEB-INF".length());

			SysParams.getInstance().setSysParam("app.path", MyFileUtil.formatPath(appPath));
			SysParams.getInstance().setSysParam("WEB-INF.path", MyFileUtil.formatPath(webinfPath));
			MyUtil.getLogger().debug("app.path=" + appPath);
			MyUtil.getLogger().debug("WEB-INF.path" + webinfPath);

			SysParams.getInstance().setSysParam("conf.path", MyFileUtil.formatPath(webinfPath + "/conf"));

			// 添加库文件目录
			String javaLibPath = System.getProperty("java.library.path");
			if (MyUtil.isOSWindows()) {
				javaLibPath += ";" + webinfPath + "/lib";
			} else {
				javaLibPath += ":" + webinfPath + "/lib";
			}
			System.setProperty("java.library.path", MyFileUtil.formatPath(javaLibPath));
			MyUtil.getLogger().debug(javaLibPath);
			final java.lang.reflect.Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
			sysPathsField.setAccessible(true);
			sysPathsField.set(null, null);

			// 载入配置文件
			SlowDFSConfig slowDFSConfig = ConfigHandle.getInstance().getSlowDFSConfig();
			SysParams.getInstance().setSysParam("file.maxUploadSize", "" + slowDFSConfig.getMaxUploadSize());
			SysParams.getInstance().setSysParam("file.maxFileSize", "" + slowDFSConfig.getMaxFileSize());

			MyUtil.getLogger().info("startup init end");
		} catch (Exception e) {
			MyUtil.getLogger().error("startup init failed");
			MyUtil.getLogger().error(e, e);
		}

	}

	public void contextDestroyed(ServletContextEvent event) {
		MyUtil.getLogger().info("startup destroy");
	}
}
