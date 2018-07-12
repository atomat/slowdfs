package com.hcb168.slowdfs.web.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
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
	public void download(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("groupId") String groupId, @PathVariable("fileName") String fileName) throws Exception {
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

		getFileEntity(request, response, resultOfFileUpload);
	}

	@RequestMapping(value = "/download/{fileName:.+}", method = { RequestMethod.GET, RequestMethod.POST })
	public void download(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("fileName") String fileName) throws Exception {
		download(request, response, "default", fileName);
	}

	/**
	 * 各节点间同步文件专用接口，避免使用download接口形成循环调用
	 * 
	 * @param request
	 * @param response
	 * @param groupId
	 * @param fileName
	 * @throws Exception
	 */
	@RequestMapping(value = "/syncfile/{groupId:.+}/{fileName:.+}", method = { RequestMethod.GET, RequestMethod.POST })
	public void syncfile(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("groupId") String groupId, @PathVariable("fileName") String fileName) throws Exception {
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

		getFileEntity(request, response, resultOfFileUpload);
	}

	/**
	 * 根据文件信息输出文件流
	 * 
	 * @param request
	 * @param response
	 * @param resultOfFileUpload
	 * @throws Exception
	 */
	private void getFileEntity(HttpServletRequest request, HttpServletResponse response,
			ResultOfFileUpload resultOfFileUpload) throws Exception {
		String strPathFile = MyFileUtil.formatPath(
				SysParams.getInstance().getSysParam("file.store.path") + resultOfFileUpload.getStorePathFile());
		File file = new File(strPathFile);
		if (!file.exists()) {
			SlowFile.getInstance().removeResultOfFileUpload(resultOfFileUpload.getFileId());
			throw new Exception("该文件ID指向的文件不存在：" + resultOfFileUpload.getFileId() + "，请稍后重试");
		}

		/*
		 * edge: mozilla/5.0 (windows nt 10.0; win64; x64) applewebkit/537.36 (khtml,
		 * like gecko) chrome/64.0.3282.140 safari/537.36 edge/17.17134
		 * 
		 * chrome: mozilla/5.0 (windows nt 10.0; win64; x64) applewebkit/537.36 (khtml,
		 * like gecko) chrome/67.0.3396.99 safari/537.36
		 * 
		 * firefox:mozilla/5.0 (windows nt 10.0; win64; x64; rv:60.0) gecko/20100101
		 * firefox/60.0
		 * 
		 * safari: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_5) AppleWebKit/537.36
		 * (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36
		 * 
		 * ie: mozilla/5.0 (windows nt 10.0; wow64; trident/7.0; rv:11.0) like gecko
		 */
		// 下载显示的文件名，根据不同浏览器解决中文名称乱码问题
		String userAgent = request.getHeader("user-agent").toLowerCase();
		String downloadFielName = resultOfFileUpload.getOriginalFileName();
		if (userAgent.contains("msie") || userAgent.contains("edge")) {
			// win10 IE edge 浏览器 和其他系统的IE
			downloadFielName = URLEncoder.encode(downloadFielName, "UTF-8");
			downloadFielName = downloadFielName.replaceAll("\\+", "%20");
		} else if (userAgent.contains("firefox") || userAgent.contains("chrome") || userAgent.contains("safari")) {
			downloadFielName = new String(downloadFielName.getBytes("UTF-8"), "iso-8859-1");
		} else {
			downloadFielName = URLEncoder.encode(downloadFielName, "UTF-8");
			downloadFielName = downloadFielName.replaceAll("\\+", "%20");
		}

		InputStream fis = null;
		OutputStream out = null;

		try {
			fis = new BufferedInputStream(new FileInputStream(file));

			response.reset();
			// application/octet-stream ： 二进制流数据（最常见的文件下载）。
			response.setContentType("application/octet-stream");
			// 通知浏览器以attachment（下载方式）打开
			response.addHeader("Content-Disposition", "attachment;filename=\"" + downloadFielName + "\"");
			response.addHeader("Content-Length", "" + file.length());
			response.setStatus(200);

			out = new BufferedOutputStream(response.getOutputStream());
			byte[] buffer = new byte[128 * 1024];
			int i = -1;
			while ((i = fis.read(buffer)) != -1) {
				out.write(buffer, 0, i);
			}
			out.flush();
		} catch (Exception e) {
			throw e;
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(fis);
			out = null;
			fis = null;
		}

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
					String jsonFileInfo = new String(Base64.decodeBase64(base64FileInfo), Charset.forName("UTF-8"));
					ResultOfFileUpload fileInfo = (ResultOfFileUpload) MyUtil.getObjectByJson(jsonFileInfo,
							ResultOfFileUpload.class);

					if (FileWorkerOperate.getFileFromHosts(fileInfo, new String[] { host })) {
						return fileInfo;
					}
					continue;
				} else {
					String msg = map.get("msg");
					MyUtil.getLogger().warn("访问失败：" + hostUrl + "," + msg);
				}
			} catch (Exception e) {
				MyUtil.getLogger().error("访问失败：" + hostUrl + "," + e);
			}
		}

		return null;
	}
}
