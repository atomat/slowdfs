package com.hcb168.slowdfs.web.controller;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hcb168.slowdfs.core.ResultOfFileUpload;
import com.hcb168.slowdfs.dao.SlowFile;
import com.hcb168.slowdfs.db.JdbcHelper;
import com.hcb168.slowdfs.util.MyUtil;

@Controller
public class GetNotice {
	@ResponseBody
	@RequestMapping(value = "/notify/addfile/{base64FileInfo}", method = { RequestMethod.GET,
			RequestMethod.POST }, produces = "text/html;charset=UTF-8")
	public String addFile(@PathVariable("base64FileInfo") String base64FileInfo) throws Exception {
		if (StringUtils.isEmpty(base64FileInfo)) {
			return MyUtil.getReturnErr("文件信息不能为空");
		}
		String jsonFileInfo = new String(Base64.decodeBase64(base64FileInfo));
		MyUtil.getLogger().debug("addFile收到：" + jsonFileInfo);

		ResultOfFileUpload fileInfo;
		try {
			fileInfo = (ResultOfFileUpload) MyUtil.getObjectByJson(jsonFileInfo, ResultOfFileUpload.class);
		} catch (Exception e) {
			MyUtil.getLogger().error("文件信息反序列化失败:" + jsonFileInfo + "," + e);
			return MyUtil.getReturnErr("文件信息反序列化失败：" + e);
		}

		// 检查本Server是否已存在该文件
		ResultOfFileUpload fileInfoTmp = SlowFile.getInstance().getResultOfFileUpload(fileInfo.getFileId());
		if (fileInfoTmp == null) {
			try {
				MyUtil.getLogger().debug("本机不存在该文件，加入新增文件消息队列");
				JdbcHelper.putNoticeAdd(jsonFileInfo);
			} catch (Exception e) {
				MyUtil.getLogger().error("putNoticeAdd 失败：" + jsonFileInfo + "," + e);
				return MyUtil.getReturnErr("putNoticeAdd失败：" + e);
			}
		} else {
			// 本机已存在，忽略
		}
		return MyUtil.getReturnSucc("recieved");
	}
}
