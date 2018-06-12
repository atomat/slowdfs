package com.hcb168.slowdfs.core;

public class ResultOfFileUpload {
	private String originalFileName = "";// 原始文件名称
	private String prefix = "";// 文件后缀
	private long fileSize = 0;// 文件大小
	private String fileMD5Value = "";// 文件内容的MD5
	private String md5FileName = "";// MD5文件名

	private String downloadUrl = "";// 文件下载路径
	private String storePath = "";// 文件存储路径
	private boolean uploadStatus = false;// 文件上传状态 true-成功、false-失败
	private String msg = "";// 文件上传描述信息

	public String getOriginalFileName() {
		return originalFileName;
	}

	public String getFileMD5Value() {
		return fileMD5Value;
	}

	public void setFileMD5Value(String fileMD5Value) {
		this.fileMD5Value = fileMD5Value;
	}

	public String getMd5FileName() {
		return md5FileName;
	}

	public void setMd5FileName(String md5FileName) {
		this.md5FileName = md5FileName;
	}

	public void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getStorePath() {
		return storePath;
	}

	public void setStorePath(String storePath) {
		this.storePath = storePath;
	}

	public boolean isUploadStatus() {
		return uploadStatus;
	}

	public void setUploadStatus(boolean uploadStatus) {
		this.uploadStatus = uploadStatus;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
