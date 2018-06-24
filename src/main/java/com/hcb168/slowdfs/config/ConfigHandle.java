package com.hcb168.slowdfs.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.hcb168.slowdfs.util.MyUtil;
import com.hcb168.slowdfs.util.SysParams;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ConfigHandle {
	private static final ConfigHandle instance = new ConfigHandle();

	private final SlowDFSConfig slowDFSConfig;

	private final HostConfig hostConfig;

	private ConfigHandle() {
		// 载入Config配置
		{
			XStream xs = new XStream(new DomDriver("UTF-8"));
			XStream.setupDefaultSecurity(xs);
			xs.allowTypes(new Class[] { SlowDFSConfig.class });
			xs.alias("SlowDFSConfig", SlowDFSConfig.class);

			SlowDFSConfig slowDFSConfig = new SlowDFSConfig();
			String pathFile = SysParams.getInstance().getSysParam("conf.path") + "/slowdfs.xml";
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(pathFile);
				xs.fromXML(fis, slowDFSConfig);
			} catch (Exception e) {
				MyUtil.getLogger().error(MyUtil.exceptionMsg(e));
			} finally {
				try {
					if (fis != null) {
						fis.close();
					}
					fis = null;
				} catch (Exception e) {
					MyUtil.getLogger().error(MyUtil.exceptionMsg(e));
				}
			}
			this.slowDFSConfig = slowDFSConfig;
		}

		{
			XStream xs = new XStream(new DomDriver("UTF-8"));
			XStream.setupDefaultSecurity(xs);
			xs.allowTypes(new Class[] { HostConfig.class });
			xs.alias("HostConfig", HostConfig.class);

			HostConfig hostConfig = new HostConfig();
			String pathFile = SysParams.getInstance().getSysParam("conf.path") + "/slowdfshost.xml";
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(pathFile);
				xs.fromXML(fis, hostConfig);
			} catch (Exception e) {
				MyUtil.getLogger().error(MyUtil.exceptionMsg(e));
			} finally {
				try {
					if (fis != null) {
						fis.close();
					}
					fis = null;
				} catch (Exception e) {
					MyUtil.getLogger().error(MyUtil.exceptionMsg(e));
				}
			}
			this.hostConfig = hostConfig;
		}

	}

	public static ConfigHandle getInstance() {
		return instance;
	}

	public SlowDFSConfig getSlowDFSConfig() {
		return this.slowDFSConfig;
	}

	public HostConfig getHostConfig() {
		return this.hostConfig;
	}

	public static void main(String[] args) {
		{
			XStream xs = new XStream(new DomDriver("UTF-8"));
			xs.alias("SlowDFSConfig", SlowDFSConfig.class);

			SlowDFSConfig slowDFSConfigTmp = new SlowDFSConfig();
			slowDFSConfigTmp.setMaxFileSize(1000);
			slowDFSConfigTmp.setMaxUploadSize(2000);

			String pathFile = "e:/tmp/watcher.xml";
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(pathFile);
				xs.toXML(slowDFSConfigTmp, fos);
			} catch (Exception e) {
				MyUtil.getLogger().error(MyUtil.exceptionMsg(e));
			} finally {
				try {
					if (fos != null) {
						fos.close();
					}
				} catch (Exception e) {
					MyUtil.getLogger().error(MyUtil.exceptionMsg(e));
				}
			}
		}
		{
			XStream xs = new XStream(new DomDriver("UTF-8"));
			xs.alias("HostConfig", HostConfig.class);

			HostConfig HostConfig = new HostConfig();
			HostConfig.setHostList(new String[] { "https://127.0.0.1:8080", "https://127.0.0.1:8181" });
			String pathFile = "e:/tmp/HostConfig.xml";
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(pathFile);
				xs.toXML(HostConfig, fos);
			} catch (Exception e) {
				MyUtil.getLogger().error(MyUtil.exceptionMsg(e));
			} finally {
				try {
					if (fos != null) {
						fos.close();
					}
				} catch (Exception e) {
					MyUtil.getLogger().error(MyUtil.exceptionMsg(e));
				}
			}
		}
	}
}
