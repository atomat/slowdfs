package com.test;

import java.util.Map;

import com.hcb168.slowdfs.client.ClientUtil;

public class Test {

	public static void main(String[] args) throws Exception {
		String[] hosts = new String[] { "http://127.0.0.1:8080", "http://127.0.0.1:18080" };
		Map<String, Object> map = ClientUtil.fileUploadToHosts(hosts, "public", "E:\\tmp/summer.zip", "summer.zip");
		System.out.println(map);

		ClientUtil.fileDownloadFromHosts(hosts, "/download/public/3dbf02307eda03225124c0c324075cec.zip",
				"e:/tmp/abc.zip");

		ClientUtil.deleteFile(hosts[1], "/slowdfs", "public", "3dbf02307eda03225124c0c324075cec");
	}

}
