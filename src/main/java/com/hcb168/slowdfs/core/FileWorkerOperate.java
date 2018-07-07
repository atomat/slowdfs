package com.hcb168.slowdfs.core;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import org.springframework.util.StringUtils;

import com.hcb168.slowdfs.client.ClientUtil;
import com.hcb168.slowdfs.config.ConfigHandle;
import com.hcb168.slowdfs.dao.SlowFile;
import com.hcb168.slowdfs.util.MyFileUtil;
import com.hcb168.slowdfs.util.MyUtil;
import com.hcb168.slowdfs.util.SysParams;

public class FileWorkerOperate {
	public static boolean doThis(String seqId, String operType, String jsonFileInfo) {
		try {
			ResultOfFileUpload fileInfo = (ResultOfFileUpload) MyUtil.getObjectByJson(jsonFileInfo,
					ResultOfFileUpload.class);
			if ("addfile".equals(operType)) {
				MyUtil.getLogger().info("FileWorker:处理" + operType + "," + seqId + "|" + jsonFileInfo);
				return addFile(fileInfo);
			} else if ("deletefile".equals(operType)) {
				MyUtil.getLogger().info("FileWorker:处理" + operType + "," + seqId + "|" + jsonFileInfo);
				return deleteFile(fileInfo);
			} else {
				MyUtil.getLogger().warn("FileWorker:不支持的操作" + operType + "," + seqId + "|" + jsonFileInfo);
			}
			return false;
		} catch (Exception e) {
			MyUtil.getLogger()
					.error("FileWorkerOperate.doThis():" + seqId + "|" + operType + "|" + jsonFileInfo + "," + e);
			return false;
		}
	}

	private static boolean addFile(ResultOfFileUpload fileInfo) throws Exception {
		// 判断本机是否已存在
		ResultOfFileUpload resultOfFileUpload = SlowFile.getInstance().getResultOfFileUpload(fileInfo.getFileId());
		if (resultOfFileUpload != null) {
			// 本机已存在该文件，直接返回
			return true;
		}

		String[] hosts = ConfigHandle.getInstance().getActiveHosts();
		hosts = MyUtil.randomizeArray(hosts);

		return getFileFromHosts(fileInfo, hosts);
	}

	private static boolean deleteFile(ResultOfFileUpload fileInfo) throws Exception {
		// 判断本机是否已存在
		ResultOfFileUpload resultOfFileUpload = SlowFile.getInstance().getResultOfFileUpload(fileInfo.getFileId());
		if (resultOfFileUpload != null) {
			// 本机已存在该文件
			// 删除该文件信息
			SlowFile.getInstance().removeResultOfFileUpload(fileInfo.getFileId());

			// 删除文件
			if (!SlowFile.getInstance().existsFileMD5(resultOfFileUpload.getFileMD5Value())) {
				String strPathFile = MyFileUtil.formatPath(
						SysParams.getInstance().getSysParam("file.store.path") + resultOfFileUpload.getStorePathFile());
				new File(strPathFile).delete();
			}
		}
		return true;
	}

	/**
	 * 指定节点中下载文件
	 * 
	 * @param fileInfo
	 * @param hosts
	 * @return
	 * @throws Exception
	 */
	public static boolean getFileFromHosts(ResultOfFileUpload fileInfo, String[] hosts) throws Exception {
		// 判断本地MD5文件是否存在
		String strPathFile = MyFileUtil
				.formatPath(SysParams.getInstance().getSysParam("file.store.path") + fileInfo.getStorePathFile());
		if (new File(strPathFile).exists()) {
			MyUtil.getLogger().debug("本机存在该文件实体" + fileInfo.getStorePathFile());
			SlowFile.getInstance().putResultOfFileUpload(fileInfo);
			return true;
		}

		// 从各个节点中下载该文件
		for (String host : hosts) {
			String webContextPath = SysParams.getInstance().getSysParam("web.context.path");

			String groupId = fileInfo.getGroupId();

			String fileName = fileInfo.getFileId();
			String prefix = fileInfo.getPrefix();
			if (!StringUtils.isEmpty(prefix)) {
				fileName = fileName + "." + prefix;
			}
			String downloadUrl = "/syncfile" + groupId + "/" + fileName;

			String fileUUIDname = UUID.randomUUID().toString();
			String pathFile = MyFileUtil
					.formatPath(SysParams.getInstance().getSysParam("app.path") + "/tmpfiles/" + fileUUIDname);
			try {
				String jsonResult = ClientUtil.fileDownload(host, webContextPath, downloadUrl, pathFile, 3 * 1000,
						5 * 1000);
				Map<String, String> map = MyUtil.getMapByJsonStr(jsonResult);
				String result = map.get("result");
				if ("succ".equals(result)) {
					// 成功下载文件到本地，移动文件到存储目录
					prefix = fileInfo.getPrefix();
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
						MyUtil.getLogger().debug("从节点：" + host + "下载文件" + fileName + "成功");
						return true;
					} else {
						MyUtil.getLogger()
								.error("storePathFile不一致：" + fileInfo.getStorePathFile() + "|" + storePathFile);
					}
				} else {
					String msg = map.get("msg");
					MyUtil.getLogger().warn("从节点：" + host + "下载文件失败" + fileName + "," + msg);
					continue;
				}
			} catch (Exception e) {
				MyUtil.getLogger().error("从节点：" + host + "下载文件失败" + fileName + "," + e);
			} finally {
				new File(pathFile).delete();
			}
		}
		return false;
	}
}
