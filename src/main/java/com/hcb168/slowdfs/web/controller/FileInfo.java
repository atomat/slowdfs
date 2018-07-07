package com.hcb168.slowdfs.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hcb168.slowdfs.core.ResultOfFileUpload;
import com.hcb168.slowdfs.dao.SlowFile;
import com.hcb168.slowdfs.util.MyUtil;

@Controller
public class FileInfo {
	@ResponseBody
	@RequestMapping(value = "/getfileinfo/{groupId:.+}/{fileId}", method = { RequestMethod.GET,
			RequestMethod.POST }, produces = "text/html;charset=UTF-8")
	public String getFileInfo(@PathVariable("groupId") String groupId, @PathVariable("fileId") String fileId)
			throws Exception {
		MyUtil.getLogger().debug("获取文件信息：" + groupId + "/" + fileId);

		if (StringUtils.isEmpty(fileId)) {
			return MyUtil.getReturnErr("fileId不能为空");
		}
		if (StringUtils.isEmpty(groupId)) {
			return MyUtil.getReturnErr("groupid不能为空");
		}

		ResultOfFileUpload resultOfFileUpload = SlowFile.getInstance().getResultOfFileUpload(fileId);
		if (resultOfFileUpload == null) {
			MyUtil.getLogger().debug("该文件不存在：" + fileId);
			return MyUtil.getReturnErr("该文件不存在：" + fileId);
		}

		if (!groupId.equals(resultOfFileUpload.getGroupId())) {
			MyUtil.getLogger().debug("该组" + groupId + "下不存在该文件：" + fileId);
			return MyUtil.getReturnErr("该组" + groupId + "下不存在该文件：" + fileId);
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("result", "succ");
		resultMap.put("fileInfo",
				Base64.encodeBase64URLSafeString(MyUtil.getJsonString(resultOfFileUpload).getBytes()));
		return MyUtil.getJsonString(resultMap);
	}
}
