package com.hcb168.slowdfs.core;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.util.StringUtils;

import com.hcb168.slowdfs.client.ClientUtil;
import com.hcb168.slowdfs.config.ConfigHandle;
import com.hcb168.slowdfs.dao.SlowFile;
import com.hcb168.slowdfs.db.JdbcHelper;
import com.hcb168.slowdfs.util.MyFileUtil;
import com.hcb168.slowdfs.util.MyUtil;
import com.hcb168.slowdfs.util.SysParams;

public class FileWorker extends Thread {
	@Override
	public void run() {
		MyUtil.getLogger().info("FileWorker线程启动");
		while (true) {
			try {
				List<Map<String, Object>> listNotice = JdbcHelper.getNotice();
				for (Map<String, Object> map : listNotice) {
					String seqId = (String) map.get("seqid");
					String operType = (String) map.get("oper_type");
					String jsonFileInfo = (String) map.get("file_info");
					try {
						boolean result = doThis(seqId, operType, jsonFileInfo);
						if (result == true) {
							JdbcHelper.delNotice(seqId);
						} else {
							JdbcHelper.increaseNoticeErrNum(seqId);
						}

					} catch (Exception e) {
						MyUtil.getLogger().error("FileWorker:" + seqId + "|" + operType + "|" + jsonFileInfo + "," + e);
					}
				}
			} catch (Exception e) {
				MyUtil.getLogger().error("FileWorker:" + e);
			}

			try {
				sleep(500);
			} catch (InterruptedException e) {
				MyUtil.getLogger().error(e);
			}
		}
	}

	private boolean doThis(String seqId, String operType, String jsonFileInfo) throws Exception {
		ResultOfFileUpload fileInfo = (ResultOfFileUpload) MyUtil.getObjectByJson(jsonFileInfo,
				ResultOfFileUpload.class);
		if ("addfile".equals(operType)) {
			MyUtil.getLogger().info("FileWorker:处理" + operType + "," + seqId + "|" + jsonFileInfo);
			return addFile(fileInfo);
		} else if ("delfile".equals(operType)) {
			MyUtil.getLogger().info("FileWorker:处理" + operType + "," + seqId + "|" + jsonFileInfo);
		} else {
			MyUtil.getLogger().warn("FileWorker:不支持的操作" + operType + "," + seqId + "|" + jsonFileInfo);
		}
		return false;
	}

	private boolean addFile(ResultOfFileUpload fileInfo) throws Exception {
		// 判断本机是否已存在
		ResultOfFileUpload resultOfFileUpload = SlowFile.getInstance().getResultOfFileUpload(fileInfo.getFileId());
		if (resultOfFileUpload != null) {
			// 本机已存在该文件，直接返回
			return true;
		}

		// 从各个节点中下载该文件
		String[] hostList = ConfigHandle.getInstance().getHostConfig().getHostList();
		hostList = MyUtil.randomizeArray(hostList);
		for (String host : hostList) {
			String url = ConfigHandle.getInstance().getSlowDFSConfig().getDownloadUrl();
			String groupId = fileInfo.getGroupId();
			String fileId = fileInfo.getFileId();
			String fileUUIDname = UUID.randomUUID().toString();
			String pathFile = MyFileUtil
					.formatPath(SysParams.getInstance().getSysParam("app.path") + "/tmpfiles/" + fileUUIDname);
			try {
				String jsonResult = ClientUtil.fileDownload(host, url, groupId, fileId, pathFile, 3 * 1000, 3 * 1000);
				@SuppressWarnings("rawtypes")
				Map map = MyUtil.getMapByJsonStr(jsonResult);
				String result = (String) map.get("result");
				if ("succ".equals(result)) {
					// 成功下载文件到本地，移动文件到存储目录
					String prefix = fileInfo.getPrefix();
					String md5FileName = "";
					if (StringUtils.isEmpty(prefix)) {
						md5FileName = fileInfo.getFileMD5Value();
					} else {
						md5FileName = fileInfo.getFileMD5Value() + "." + prefix;
					}
					String storePathFile = MyFileUtil.moveToStorePath(pathFile, fileInfo.getFileMD5Value(),
							md5FileName);
					if (fileInfo.getStorePathFile().equals(storePathFile)) {
						SlowFile.getInstance().putResultOfFileUpload(fileInfo);
						return true;
					} else {
						MyUtil.getLogger()
								.error("storePathFile不一致：" + fileInfo.getStorePathFile() + "|" + storePathFile);
					}
				} else {
					String msg = (String) map.get("msg");
					MyUtil.getLogger().warn("从节点：" + host + "下载文件失败" + fileId + "," + msg);
					continue;
				}
			} catch (Exception e) {
				MyUtil.getLogger().error("从节点：" + host + "下载文件失败" + fileId + "," + e);
			} finally {
				new File(pathFile).delete();
			}
		}
		return false;
	}

}
