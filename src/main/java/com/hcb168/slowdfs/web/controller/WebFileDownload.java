package com.hcb168.slowdfs.web.controller;

import java.io.File;

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

import com.hcb168.slowdfs.core.ResultOfFileUpload;
import com.hcb168.slowdfs.dao.SlowFile;
import com.hcb168.slowdfs.util.MyFileUtil;
import com.hcb168.slowdfs.util.MyUtil;
import com.hcb168.slowdfs.util.SysParams;

@Controller
public class WebFileDownload {
	@RequestMapping(value = "/download/{groupId:.+}/{fileName:.+}", method = { RequestMethod.GET, RequestMethod.POST })
	public ResponseEntity<byte[]> exportapp(@PathVariable("groupId") String groupId,
			@PathVariable("fileName") String fileName) throws Exception {
		MyUtil.getLogger().debug("下载：" + groupId + "/" + fileName);

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

		String strPathFile = MyFileUtil.formatPath(
				SysParams.getInstance().getSysParam("file.store.path") + "/" + resultOfFileUpload.getStorePathFile());

		File file = new File(strPathFile);
		if (!file.exists()) {
			throw new Exception("该文件ID指向的文件不存在：" + fileId);
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

	@RequestMapping(value = "/download/{fileName:.+}", method = { RequestMethod.GET, RequestMethod.POST })
	public ResponseEntity<byte[]> exportapp(@PathVariable("fileName") String fileName) throws Exception {
		return exportapp("default", fileName);
	}
}
