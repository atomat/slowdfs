package com.hcb168.slowdfs.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpUtil {
	/**
	 * 快速Get请求，用于调用rest接口
	 * 
	 * @param hostUrl
	 * @return
	 * @throws Exception
	 */
	public static String GetQuickly(String hostUrl) throws Exception {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		try {
			HttpGet httpGet = new HttpGet(hostUrl); // 使用Get方法提交
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(500).setSocketTimeout(500)
					.setConnectionRequestTimeout(500).build();
			httpGet.setConfig(requestConfig);

			response = httpClient.execute(httpGet);

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity resEntity = response.getEntity();
				String result = EntityUtils.toString(resEntity);
				EntityUtils.consume(resEntity);
				return result;
			} else {
				throw new Exception("访问url=" + hostUrl + "异常，statusCode=" + statusCode);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			HttpClientUtils.closeQuietly(response);
			HttpClientUtils.closeQuietly(httpClient);
		}
	}
}
