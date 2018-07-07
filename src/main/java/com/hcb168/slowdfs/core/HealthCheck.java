package com.hcb168.slowdfs.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hcb168.slowdfs.config.ConfigHandle;
import com.hcb168.slowdfs.util.HttpUtil;
import com.hcb168.slowdfs.util.MyUtil;
import com.hcb168.slowdfs.util.SysParams;

public class HealthCheck extends Thread {
	private static final Logger logger = Logger.getLogger("HEALTH");

	@Override
	public void run() {
		MyUtil.getLogger().info(this.getClass().getName() + "线程启动");
		while (true) {
			try {
				sleep(5 * 1000);
			} catch (InterruptedException e) {
				MyUtil.getLogger().error(e);
			}
			logger.info("开始健康检查");
			String[] hosts = ConfigHandle.getInstance().getHosts();
			List<String> listActiveHost = new ArrayList<String>();
			for (String host : hosts) {
				String hostUrl = host + SysParams.getInstance().getSysParam("web.context.path") + "/healthcheck";
				try {
					String jsonResult = HttpUtil.GetQuickly(hostUrl);

					Map<String, String> map = MyUtil.getMapByJsonStr(jsonResult);
					String result = map.get("result");
					if ("succ".equals(result)) {
						listActiveHost.add(host);
						logger.info("健康检查host[" + host + "]正常");
					} else {
						String msg =  map.get("msg");
						logger.error("健康检查host[" + host + "]异常，" + msg);
					}
				} catch (Exception e) {
					logger.error("健康检查host[" + host + "]异常，e=" + e);
				}
			}
			ConfigHandle.getInstance().setActiveHosts(listActiveHost);
		}
	}
}
