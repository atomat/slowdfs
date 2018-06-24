package com.hcb168.slowdfs.web.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.hcb168.slowdfs.core.ResultOfFileUpload;
import com.hcb168.slowdfs.dao.Notice;
import com.hcb168.slowdfs.dao.SlowFile;
import com.hcb168.slowdfs.util.MyFileUtil;
import com.hcb168.slowdfs.util.MyUtil;
import com.hcb168.slowdfs.util.SysParams;

@Controller
public class WebFileUpload {
	@ResponseBody
	@RequestMapping(value = "/upload/{groupId:.+}", method = RequestMethod.POST, produces = "text/html;charset=UTF-8")
	public String uploadFilesByGroupId(HttpServletRequest request, @PathVariable("groupId") String groupId,
			@RequestParam("files") MultipartFile[] files) throws Exception {
		if (StringUtils.isEmpty(groupId)) {
			return MyUtil.getReturnErr("/upload/{groupId}，groupid不能为空");
		}
		if (files == null || files.length <= 0) {
			return MyUtil.getReturnErr("提交的文件清单为空。文件控件input type='file' name='files'中，name必须为files");
		}

		ServletRequestContext ctx = new ServletRequestContext(request);
		long requestSize = ctx.contentLength();
		long maxUploadSize = new Long(SysParams.getInstance().getSysParam("file.maxUploadSize"));
		if (requestSize > maxUploadSize) {
			MyUtil.getLogger().warn("上传文件总大小超过限制，限制为：" + maxUploadSize);
			return MyUtil.getReturnErr("上传文件总大小超过限制，限制为：" + maxUploadSize);
		}

		List<ResultOfFileUpload> list = new ArrayList<ResultOfFileUpload>();
		for (int i = 0; i < files.length; i++) {
			MultipartFile file = files[i];
			ResultOfFileUpload result = storeFile(groupId, file);
			list.add(result);
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("result", "succ");
		resultMap.put("uploadfiles", list);
		return MyUtil.getJsonString(resultMap);
	}

	@ResponseBody
	@RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "text/html;charset=UTF-8")
	public String uploadFiles(HttpServletRequest request, @RequestParam("files") MultipartFile[] files)
			throws Exception {
		return uploadFilesByGroupId(request, "default", files);
	}

	private ResultOfFileUpload storeFile(String groupId, MultipartFile file) throws Exception {
		ResultOfFileUpload result = new ResultOfFileUpload();
		result.setGroupId(groupId);

		if (file != null && !StringUtils.isEmpty(file.getOriginalFilename())) {
			String originalFileName = file.getOriginalFilename();
			MyUtil.getLogger().debug("收到文件:" + originalFileName + "，大小：" + file.getSize());

			long maxFileSize = new Long(SysParams.getInstance().getSysParam("file.maxFileSize"));
			if (file.getSize() > maxFileSize) {
				result.setOriginalFileName(originalFileName);
				result.setFileSize(file.getSize());
				result.setUploadStatus(false);
				result.setMsg("文件大小超出限制数" + maxFileSize + ",该文件大小 ：" + file.getSize());
				return result;
			}

			// 生成临时文件名称 uuid
			String fileUUIDname = UUID.randomUUID().toString();

			// 获取后缀
			String prefix = MyFileUtil.getFileNamePrefix(originalFileName);

			String pf = "JSP";// 基于安全考虑：禁止上传jsp文件
			if (!StringUtils.isEmpty(prefix) && pf.equals(prefix.toUpperCase()) == true) {
				result.setOriginalFileName(originalFileName);
				result.setUploadStatus(false);
				result.setMsg("不支持的文件类型：" + pf);
				return result;
			}

			// 生成临时文件名称
			String newFileName = "";
			if (StringUtils.isEmpty(prefix)) {
				newFileName = fileUUIDname;
			} else {
				newFileName = fileUUIDname + "." + prefix;
			}

			// 上传文件路径
			String path = SysParams.getInstance().getSysParam("app.path") + "/tmpfiles";
			path = MyFileUtil.formatPath(path);

			try {
				File pathFile = new File(path, newFileName);
				// 判断路径是否存在，如果不存在就创建一个
				if (!pathFile.getParentFile().exists()) {
					pathFile.getParentFile().mkdirs();
				}
				if (pathFile.exists()) {
					pathFile.delete();
				}
				// 将上传文件保存到一个目标文件当中
				String newFilePath = path + File.separator + newFileName;
				file.transferTo(new File(newFilePath));

				// 获取文件MD5
				String fileMD5Value = MyFileUtil.getFileMD5Value(path, newFileName);

				String md5FileName = "";
				if (StringUtils.isEmpty(prefix)) {
					md5FileName = fileMD5Value;
				} else {
					md5FileName = fileMD5Value + "." + prefix;
				}

				String fileId = DigestUtils.md5Hex(fileMD5Value + "@" + groupId);

				// 检查本机是否存在
				ResultOfFileUpload resultOfFileUpload = SlowFile.getInstance().getResultOfFileUpload(fileId);
				if (resultOfFileUpload != null) {
					// 本机已存在该文件，删除临时目录下该文件
					MyUtil.getLogger().debug("本机已存在文件：" + fileId);
					new File(newFilePath).delete();
					Notice.addFile(resultOfFileUpload);
					return resultOfFileUpload;
				}

				// 将文件移动到存储目录
				String storePathFile = MyFileUtil.moveToStorePath(newFilePath, fileMD5Value, md5FileName);

				result.setOriginalFileName(originalFileName);
				result.setPrefix(prefix);
				result.setFileSize(file.getSize());
				result.setFileMD5Value(fileMD5Value);
				result.setFileId(fileId);

				if (StringUtils.isEmpty(prefix)) {
					result.setDownloadUrl("/download/" + groupId + "/" + fileId);
				} else {
					result.setDownloadUrl("/download/" + groupId + "/" + fileId + "." + prefix);
				}

				result.setStorePathFile(storePathFile);
				result.setDateTime(new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS Z").format(new Date()));
				result.setUploadStatus(true);
				result.setMsg("");
				SlowFile.getInstance().putResultOfFileUpload(result);
				Notice.addFile(result);

				MyUtil.getLogger().debug("成功接收文件：" + originalFileName);
				return result;
			} catch (Exception e) {
				MyUtil.getLogger().error(e, e);
				result.setOriginalFileName(originalFileName);
				result.setFileSize(file.getSize());
				result.setUploadStatus(false);
				result.setMsg("文件上传处理异常：" + e);
				return result;
			}
		} else {
			result.setUploadStatus(false);
			result.setMsg("文件对象为空");
			return result;
		}
	}
}
