package com.hcb168.slowdfs.config;

public class SlowDFSConfig {
	private volatile long maxUploadSize = 0;// 上传数据的最大值
	private volatile long maxFileSize = 0;// 上传单个文件大小的最大值
	private volatile String unallowedPrefix = "";// 不允许的文件类型

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

	public String getUnallowedPrefix() {
		return unallowedPrefix;
	}

	public void setUnallowedPrefix(String unallowedPrefix) {
		this.unallowedPrefix = unallowedPrefix;
	}

}
