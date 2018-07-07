package com.hcb168.slowdfs.web.controller;

import java.io.File;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.hcb168.slowdfs.config.ConfigHandle;
import com.hcb168.slowdfs.core.FileWorkerOperate;
import com.hcb168.slowdfs.core.ResultOfFileUpload;
import com.hcb168.slowdfs.dao.SlowFile;
import com.hcb168.slowdfs.util.HttpUtil;
import com.hcb168.slowdfs.util.MyFileUtil;
import com.hcb168.slowdfs.util.MyUtil;
import com.hcb168.slowdfs.util.SysParams;

@Controller
public class WebFileDownload {
	@RequestMapping(value = "/download/{groupId:.+}/{fileName:.+}", method = { RequestMethod.GET, RequestMethod.POST })
	public ResponseEntity<byte[]> download(@PathVariable("groupId") String groupId,
			@PathVariable("fileName") String fileName) throws Exception {
		MyUtil.getLogger().debug("下载：" + groupId + "/" + fileName);

		if (StringUtils.isEmpty(fileName)) {
			throw new Exception("fileName不能为空");
		}
		if (StringUtils.isEmpty(groupId)) {
			throw new Exception("groupid不能为空");
		}

		String fileId = MyFileUtil.getFileNameNoPrefix(fileName);

		ResultOfFileUpload resultOfFileUpload = SlowFile.getInstance().getResultOfFileUpload(fileId);
		if (resultOfFileUpload == null) {
			// 本机不存在该文件，从其它机获取文件
			resultOfFileUpload = getFileFromHosts(groupId, fileId, fileName);
			if (resultOfFileUpload == null) {
				throw new Exception("该文件不存在：" + fileName);
			}
		} else if (!groupId.equals(resultOfFileUpload.getGroupId())) {
			throw new Exception("该组" + groupId + "下不存在该文件：" + fileName);
		}

		return getFileEntity(resultOfFileUpload);
	}

	@RequestMapping(value = "/download/{fileName:.+}", method = { RequestMethod.GET, RequestMethod.POST })
	public ResponseEntity<byte[]> download(@PathVariable("fileName") String fileName) throws Exception {
		return download("default", fileName);
	}

	@RequestMapping(value = "/syncfile/{groupId:.+}/{fileName:.+}", method = { RequestMethod.GET, RequestMethod.POST })
	public ResponseEntity<byte[]> syncfile(@PathVariable("groupId") String groupId,
			@PathVariable("fileName") String fileName) throws Exception {
		MyUtil.getLogger().debug("同步：" + groupId + "/" + fileName);

		if (StringUtils.isEmpty(fileName)) {
			throw new Exception("fileName不能为空");
		}
		if (StringUtils.isEmpty(groupId)) {
			throw new Exception("groupid不能为空");
		}

		String fileId = MyFileUtil.getFileNameNoPrefix(fileName);

		ResultOfFileUpload resultOfFileUpload = SlowFile.getInstance().getResultOfFileUpload(fileId);
		if (resultOfFileUpload == null) {
			throw new Exception("该文件不存在：" + fileName);
		}

		if (!groupId.equals(resultOfFileUpload.getGroupId())) {
			throw new Exception("该组" + groupId + "下不存在该文件：" + fileName);
		}

		return getFileEntity(resultOfFileUpload);
	}

	/**
	 * 根据文件信息返回ResponseEntity
	 * 
	 * @param resultOfFileUpload
	 * @return
	 * @throws Exception
	 */
	private ResponseEntity<byte[]> getFileEntity(ResultOfFileUpload resultOfFileUpload) throws Exception {
		String strPathFile = MyFileUtil.formatPath(
				SysParams.getInstance().getSysParam("file.store.path") + resultOfFileUpload.getStorePathFile());

		File file = new File(strPathFile);
		if (!file.exists()) {
			throw new Exception("该文件ID指向的文件不存在：" + resultOfFileUpload.getFileId());
		}

		HttpHeaders headers = new HttpHeaders();
		// 下载显示的文件名，解决中文名称乱码问题
		String downloadFielName = new String((resultOfFileUpload.getOriginalFileName()).getBytes("UTF-8"),
				"iso-8859-1");

		// 通知浏览器以attachment（下载方式）打开
		headers.setContentDispositionFormData("attachment", downloadFielName);
		// application/octet-stream ： 二进制流数据（最常见的文件下载）。
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file), headers, HttpStatus.CREATED);
	}

	/**
	 * 从各个节点获取文件
	 * 
	 * @param groupId
	 * @param fileId
	 * @param fileName
	 * @return
	 */
	private ResultOfFileUpload getFileFromHosts(String groupId, String fileId, String fileName) {
		String[] hosts = ConfigHandle.getInstance().getActiveHosts();
		hosts = MyUtil.randomizeArray(hosts);

		for (String host : hosts) {
			String url = SysParams.getInstance().getSysParam("web.context.path") + "/getfileinfo";
			String hostUrl = host + url + "/" + groupId + "/" + fileId;
			try {
				String jsonResult = HttpUtil.GetQuickly(hostUrl);
				Map<String, String> map = MyUtil.getMapByJsonStr(jsonResult);
				String result = map.get("result");
				if ("succ".equals(result)) {
					String base64FileInfo = map.get("fileInfo");
					String jsonFileInfo = new String(Base64.decodeBase64(base64FileInfo));
					ResultOfFileUpload fileInfo = (ResultOfFileUpload) MyUtil.getObjectByJson(jsonFileInfo,
							ResultOfFileUpload.class);

					if (FileWorkerOperate.getFileFromHosts(fileInfo, new String[] { host })) {
						return fileInfo;
					}
					continue;
				} else {
					String msg = map.get("msg");
					MyUtil.getLogger().error("访问失败：" + hostUrl + "," + msg);
				}
			} catch (Exception e) {
				MyUtil.getLogger().error("访问失败：" + hostUrl + "," + e);
			}
		}

		return null;
	}
}
