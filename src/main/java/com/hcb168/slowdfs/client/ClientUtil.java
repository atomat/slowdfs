package com.hcb168.slowdfs.client;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EntityUtils;

public class ClientUtil {
	/**
	 * 文件上传
	 * 
	 * @param slowdfsHost
	 *            eg. http://127.0.0.1:8080
	 * @param url
	 *            eg. /slowdfs/upload
	 * @param groupId
	 *            文件所属组
	 * @param srcPathFile
	 *            待上传文件所在的路径
	 * @param fileName
	 *            文件名称
	 * @param iConnectTimeout
	 * @param iSocketTimeout
	 * @return
	 * @throws Exception
	 */
	public static String fileUpload(String slowdfsHost, String url, String groupId, String srcPathFile, String fileName,
			int iConnectTimeout, int iSocketTimeout) throws Exception {
		String hostUrl = slowdfsHost + url + "/" + groupId;
		File file = new File(srcPathFile);
		if (!file.exists()) {
			throw new Exception("文件不存在：" + srcPathFile);
		}

		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		String result = "";
		try {
			HttpPost httpPost = new HttpPost(hostUrl);
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(iConnectTimeout)
					.setSocketTimeout(iSocketTimeout).setConnectionRequestTimeout(1000).build();
			httpPost.setConfig(requestConfig);

			MultipartEntityBuilder mEntityBuilder = MultipartEntityBuilder.create();
			mEntityBuilder.setCharset(CharsetUtils.get("UTF-8"));// 设置请求的编码格式
			mEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);// 设置浏览器兼容模式
			mEntityBuilder.addBinaryBody("files", file, ContentType.DEFAULT_BINARY, fileName);
			httpPost.setEntity(mEntityBuilder.build());
			response = httpClient.execute(httpPost);

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity resEntity = response.getEntity();
				result = EntityUtils.toString(resEntity);
				EntityUtils.consume(resEntity);
			} else {
				result = "{\"result\":\"err\",\"msg\":\"statusCode=" + statusCode + "\"}";
			}
		} catch (Exception e) {
			throw e;
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
		return result;
	}

	public static String fileUpload(String slowdfsHost, String url, String groupId, String srcPathFile, String fileName)
			throws Exception {
		return fileUpload(slowdfsHost, url, groupId, srcPathFile, fileName, 5 * 1000, 5 * 1000);
	}

	public static String fileUploadSimple(String slowdfsHost, String groupId, String srcPathFile, String fileName)
			throws Exception {
		return fileUpload(slowdfsHost, "/slowdfs/upload", groupId, srcPathFile, fileName);
	}

	/**
	 * 下载文件
	 * 
	 * @param slowdfsHost
	 *            eg. http://127.0.0.1:8080
	 * @param url
	 *            eg. /slowdfs/download
	 * @param groupId
	 *            文件所属组
	 * @param fileId
	 *            文件ID
	 * @param pathFile
	 *            放置下载文件的目标路径
	 * @param iConnectTimeout
	 * @param iSocketTimeout
	 * @throws Exception
	 */
	public static String fileDownload(String slowdfsHost, String url, String groupId, String fileId, String pathFile,
			int iConnectTimeout, int iSocketTimeout) throws Exception {
		String hostUrl = slowdfsHost + url + "/" + groupId + "/" + fileId;

		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		try {
			HttpGet httpGet = new HttpGet(hostUrl); // 使用Get方法提交
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(iConnectTimeout)
					.setSocketTimeout(iSocketTimeout).setConnectionRequestTimeout(1000).build();
			httpGet.setConfig(requestConfig);

			response = httpClient.execute(httpGet);

			FileOutputStream fos = null;
			try {
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
					HttpEntity resEntity = response.getEntity();
					fos = new FileOutputStream(pathFile);
					IOUtils.copy(resEntity.getContent(), fos);
					fos.flush();
					EntityUtils.consume(resEntity);
					return "{\"result\":\"succ\",\"msg\":\"statusCode=" + statusCode + "\"}";
				} else {
					return EntityUtils.toString(response.getEntity());
				}
			} catch (Exception e) {
				throw e;
			} finally {
				IOUtils.closeQuietly(fos);
				fos = null;
			}
		} catch (Exception e) {
			new File(pathFile).delete();
			throw e;
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}

	public static String fileDownload(String slowdfsHost, String url, String groupId, String fileId, String pathFile)
			throws Exception {
		return fileDownload(slowdfsHost, url, groupId, fileId, pathFile, 5 * 1000, 5 * 1000);
	}

	public static String fileDownloadSimple(String slowdfsHost, String groupId, String fileId, String pathFile)
			throws Exception {
		return fileDownload(slowdfsHost, "/slowdfs/download", groupId, fileId, pathFile);
	}

	public static void main(String[] args) throws Exception {
//		String result = fileUploadSimple("http://127.0.0.1:8080", "pub.lic", "E:\\book\\Java性能权威指南 (图灵程序设计丛书).epub",
//				"Java性能权威指南 (图灵程序设计丛书).epub");
//		System.out.println(result);

		 String result = ClientUtil.fileDownload("http://127.0.0.1:8080",
		 "/slowdfs/download", "default",
		 "44ecc1a0a205bf7dde7961c6c8e53178.log", "e:/tmp/Java程序性能优化.pdf", 3000, 1000);
		 System.out.println(result);
	}
}
