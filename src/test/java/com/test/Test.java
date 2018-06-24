package com.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

import com.hcb168.slowdfs.util.MyFileUtil;
import com.hcb168.slowdfs.util.MyUtil;

public class Test {

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub

		String url="addFile：http://127.0.0.1:8181/slowdfs/notify/addfile/{\"groupId\":\"default\",\"originalFileName\":\"error.log\",\"prefix\":\"log\",\"fileSize\":9034,\"fileMD5Value\":\"54940d73af1ecdfcd76781ab5f164597\",\"fileId\":\"44ecc1a0a205bf7dde7961c6c8e53178\",\"downloadUrl\":\"/download/default/44ecc1a0a205bf7dde7961c6c8e53178.log\",\"storePathFile\":\"/5/94/54940d73af1ecdfcd76781ab5f164597.log\",\"dateTime\":\"20180623 20:17:54.744 +0800\",\"uploadStatus\":true,\"msg\":\"\"}";
		String b=Base64.encodeBase64URLSafeString(url.getBytes());
		String d=Base64.encodeBase64String(url.getBytes());
		System.out.println(b);
		System.out.println(d);
		String c=new String(Base64.decodeBase64(b));
		System.out.println(c);
		System.out.println(url.equals(c));
	}

}
