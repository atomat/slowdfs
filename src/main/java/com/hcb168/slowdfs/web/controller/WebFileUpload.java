package com.hcb168.slowdfs.web.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.hcb168.slowdfs.core.ResultOfFileUpload;
import com.hcb168.slowdfs.util.MyFileUtil;
import com.hcb168.slowdfs.util.MyUtil;
import com.hcb168.slowdfs.util.SysParams;

@Controller
public class WebFileUpload {
	@ResponseBody
	@RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "text/html;charset=UTF-8")
	public String uploadFiles(HttpServletRequest request, @RequestParam("files") MultipartFile[] files)
			throws Exception {
		if (files == null || files.length <= 0) {
			return MyUtil.getReturnErr("提交的文件清单为空");
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
			ResultOfFileUpload result = storeFile(file);
			list.add(result);
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("result", "succ");
		resultMap.put("uploadfiles", list);
		return MyUtil.getJsonString(resultMap);
	}

	private ResultOfFileUpload storeFile(MultipartFile file) throws Exception {
		ResultOfFileUpload result = new ResultOfFileUpload();

		if (!file.isEmpty()) {
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

				String md5FileName = renameToMD5(path, newFileName, prefix, fileMD5Value);

				result.setOriginalFileName(originalFileName);
				result.setPrefix(prefix);
				result.setFileSize(file.getSize());
				result.setFileMD5Value(fileMD5Value);
				result.setMd5FileName(md5FileName);
				result.setDownloadUrl("");
				result.setStorePath("");
				result.setUploadStatus(true);
				result.setMsg("上传文件成功");

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

	private String renameToMD5(String srcPath, String srcFileName, String prefix, String fileMD5Value)
			throws Exception {
		String srcPathFile = srcPath + File.separator + srcFileName;

		try {
			String md5PathFile = "";
			if (StringUtils.isEmpty(prefix)) {
				md5PathFile = srcPath + File.separator + fileMD5Value;
			} else {
				md5PathFile = srcPath + File.separator + fileMD5Value + "." + prefix;
			}

			File fileMD5 = new File(md5PathFile);
			if (fileMD5.exists()) {
				// 如果已存在则删除源文件
				new File(srcPathFile).delete();
			} else {
				new File(srcPathFile).renameTo(fileMD5);
			}
			return fileMD5.getName();

		} catch (Exception e) {
			MyUtil.getLogger().error(e, e);
			throw new Exception("文件上传处理异常(md5)");
		}
	}
}
