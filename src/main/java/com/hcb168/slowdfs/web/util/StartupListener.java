package com.hcb168.slowdfs.web.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.hcb168.slowdfs.config.ConfigHandle;
import com.hcb168.slowdfs.config.SlowDFSConfig;
import com.hcb168.slowdfs.core.FileWorker;
import com.hcb168.slowdfs.db.JdbcHelper;
import com.hcb168.slowdfs.util.MyFileUtil;
import com.hcb168.slowdfs.util.MyUtil;
import com.hcb168.slowdfs.util.SpringUtil;
import com.hcb168.slowdfs.util.SysParams;

public class StartupListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		MyUtil.getLogger().info("startup init begin");

		try {
			ApplicationContext ac = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
			SpringUtil.setApplicationContext(ac);

			// 初始化系统参数
			String appPath = MyFileUtil.formatPath(event.getServletContext().getRealPath("/"));
			String webInfPath = MyFileUtil.formatPath(event.getServletContext().getRealPath("/WEB-INF"));

			SysParams.getInstance().putSysParam("app.path", appPath);
			SysParams.getInstance().putSysParam("WEB-INF.path", webInfPath);
			MyUtil.getLogger().debug("app.path=" + appPath);
			MyUtil.getLogger().debug("WEB-INF.path=" + webInfPath);

			SysParams.getInstance().putSysParam("conf.path", MyFileUtil.formatPath(webInfPath + "/conf"));
			SysParams.getInstance().putSysParam("file.store.path", MyFileUtil.formatPath(appPath + "/files"));

			// 添加库文件目录
			String javaLibPath = System.getProperty("java.library.path");
			if (MyUtil.isOSWindows()) {
				javaLibPath += ";" + webInfPath + "/lib";
			} else {
				javaLibPath += ":" + webInfPath + "/lib";
			}
			System.setProperty("java.library.path", MyFileUtil.formatPath(javaLibPath));
			MyUtil.getLogger().debug(javaLibPath);
			final java.lang.reflect.Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
			sysPathsField.setAccessible(true);
			sysPathsField.set(null, null);

			// 载入配置文件
			SlowDFSConfig slowDFSConfig = ConfigHandle.getInstance().getSlowDFSConfig();
			SysParams.getInstance().putSysParam("file.maxUploadSize", "" + slowDFSConfig.getMaxUploadSize());
			SysParams.getInstance().putSysParam("file.maxFileSize", "" + slowDFSConfig.getMaxFileSize());

			// 检查数据库是否已经初始化
			if (JdbcHelper.checkDBInit() == false) {
				// 初始化数据库
				MyUtil.getLogger().debug("开始初始化数据库");
				JdbcHelper.initDB();
			} else {
				MyUtil.getLogger().debug("数据库已初始化");
			}
			
			// 启动文件同步线程
			new FileWorker().start();

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
