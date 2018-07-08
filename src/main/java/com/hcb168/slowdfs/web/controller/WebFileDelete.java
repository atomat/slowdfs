package com.hcb168.slowdfs.web.controller;

import java.io.File;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hcb168.slowdfs.core.Notice;
import com.hcb168.slowdfs.core.ResultOfFileUpload;
import com.hcb168.slowdfs.dao.SlowFile;
import com.hcb168.slowdfs.util.MyFileUtil;
import com.hcb168.slowdfs.util.MyUtil;
import com.hcb168.slowdfs.util.SysParams;

@Controller
public class WebFileDelete {
	@ResponseBody
	@RequestMapping(value = "/deletefile/{groupId:.+}/{fileId}", method = { RequestMethod.GET,
			RequestMethod.POST }, produces = "text/html;charset=UTF-8")
	public String deleteFile(@PathVariable("groupId") String groupId, @PathVariable("fileId") String fileId)
			throws Exception {
		// 删除文件API必输同时输入正确的groupId和fileId，防止误删或恶意删除
		MyUtil.getLogger().debug("删除文件：" + groupId + "/" + fileId);

		if (StringUtils.isEmpty(fileId)) {
			return MyUtil.getReturnErr("fileId不能为空");
		}
		if (StringUtils.isEmpty(groupId)) {
			return MyUtil.getReturnErr("groupid不能为空");
		}

		ResultOfFileUpload resultOfFileUpload = SlowFile.getInstance().getResultOfFileUpload(fileId);
		if (resultOfFileUpload == null) {
			// 本机不存在该文件
			Notice.deleteFile(groupId, fileId);
			return MyUtil.getReturnSucc("文件已删除");
		}

		if (!groupId.equals(resultOfFileUpload.getGroupId())) {
			Notice.deleteFile(groupId, fileId);
			return MyUtil.getReturnSucc("文件已删除");
		}

		// 删除该文件信息
		SlowFile.getInstance().removeResultOfFileUpload(fileId);

		// 删除文件
		if (!SlowFile.getInstance().existsFileMD5(resultOfFileUpload.getFileMD5Value())) {
			String strPathFile = MyFileUtil.formatPath(
					SysParams.getInstance().getSysParam("file.store.path") + resultOfFileUpload.getStorePathFile());
			new File(strPathFile).delete();
		}

		Notice.deleteFile(groupId, fileId);

		return MyUtil.getReturnSucc("成功删除文件");
	}
}
