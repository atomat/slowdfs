package com.hcb168.slowdfs.core;

import java.util.Map;

import com.hcb168.slowdfs.config.ConfigHandle;
import com.hcb168.slowdfs.util.HttpUtil;
import com.hcb168.slowdfs.util.MyUtil;
import com.hcb168.slowdfs.util.SysParams;

public class NoticeDeleteFileTask implements Runnable {
	private String groupId;
	private String fileId;

	public NoticeDeleteFileTask(String groupId, String fileId) {
		this.groupId = groupId;
		this.fileId = fileId;
	}

	@Override
	public void run() {
		try {
			String noticeHostUrl = SysParams.getInstance().getSysParam("web.context.path") + "/notify";
			String url = noticeHostUrl + "/deletefile/" + this.groupId + "/" + this.fileId;
			String[] hostList = ConfigHandle.getInstance().getActiveHosts();
			hostList = MyUtil.randomizeArray(hostList);
			for (String host : hostList) {
				String hostUrl = host + url;
				MyUtil.getLogger().debug("发出deleteFile：" + hostUrl);
				try {
					String jsonResult = HttpUtil.GetQuickly(hostUrl);

					Map<String, String> map = MyUtil.getMapByJsonStr(jsonResult);
					String result = map.get("result");
					if ("succ".equals(result)) {
						MyUtil.getLogger().info("通知" + host + "成功，" + this.fileId);
						continue;
					} else {
						String msg = map.get("msg");
						MyUtil.getLogger().error("访问失败：" + hostUrl + "," + msg);
					}
				} catch (Exception e) {
					MyUtil.getLogger().error("访问失败：" + hostUrl + "," + e);
				}

			}
		} catch (Exception e) {
			MyUtil.getLogger().error(this.getClass().getName() + ":" + e);
		}
	}

}
