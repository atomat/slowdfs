package com.hcb168.slowdfs.dao;

import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.hcb168.slowdfs.config.ConfigHandle;
import com.hcb168.slowdfs.core.ResultOfFileUpload;
import com.hcb168.slowdfs.util.HttpUtil;
import com.hcb168.slowdfs.util.MyUtil;

public class Notice {
	/**
	 * 通知各节点收到新文件
	 * 
	 * @param resultOfFileUpload
	 * @throws Exception
	 */
	public static void addFile(ResultOfFileUpload resultOfFileUpload) throws Exception {
		String noticeHostUrl = ConfigHandle.getInstance().getSlowDFSConfig().getNoticeHostUrl();
		String url = noticeHostUrl + "/addfile/"
				+ Base64.encodeBase64URLSafeString(MyUtil.getJsonString(resultOfFileUpload).getBytes());
		String[] hostList = ConfigHandle.getInstance().getHostConfig().getHostList();
		hostList = MyUtil.randomizeArray(hostList);
		for (String host : hostList) {
			String hostUrl = host + url;
			MyUtil.getLogger().debug("发出addFile：" + hostUrl);
			try {
				String jsonResult = HttpUtil.GetQuickly(hostUrl);
				@SuppressWarnings("rawtypes")
				Map map = MyUtil.getMapByJsonStr(jsonResult);
				String result = (String) map.get("result");
				if ("succ".equals(result)) {
					MyUtil.getLogger().info("通知" + host + "成功，" + resultOfFileUpload.getFileId() + "|"
							+ resultOfFileUpload.getOriginalFileName());
					continue;
				} else {
					String msg = (String) map.get("msg");
					MyUtil.getLogger().error("访问失败：" + hostUrl + "," + msg);
				}
			} catch (Exception e) {
				MyUtil.getLogger().error("访问失败：" + hostUrl + "," + e);
			}

		}
	}
}
