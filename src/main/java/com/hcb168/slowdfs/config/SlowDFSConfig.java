package com.hcb168.slowdfs.config;

public class SlowDFSConfig {
	private long maxUploadSize = 0;// 上传数据的最大值
	private long maxFileSize = 0;// 上传单个文件大小的最大值
	private String noticeHostUrl = "";// 通知服务的主url
	private String downloadUrl = "";// 下载服务url

	public long getMaxUploadSize() {
		return maxUploadSize;
	}

	public void setMaxUploadSize(long maxUploadSize) {
		this.maxUploadSize = maxUploadSize;
	}

	public long getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public String getNoticeHostUrl() {
		return noticeHostUrl;
	}

	public void setNoticeHostUrl(String noticeHostUrl) {
		this.noticeHostUrl = noticeHostUrl;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

}
