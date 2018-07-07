package com.hcb168.slowdfs.core;

public class ResultOfFileUpload {
	private String groupId = "";// 文件所属组
	private String originalFileName = "";// 原始文件名称
	private String prefix = "";// 文件后缀
	private long fileSize = 0;// 文件大小
	private String fileMD5Value = "";// 文件内容的MD5
	private String fileId = "";// 文件ID
	private String fileName = "";// slowdfs文件名

	private String downloadUrl = "";// 文件下载路径
	private String storePathFile = "";// 文件存储路径
	private String dateTime = "";// 时间
	private boolean uploadStatus = false;// 文件上传状态 true-成功、false-失败
	private String msg = "";// 文件上传描述信息

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public String getFileMD5Value() {
		return fileMD5Value;
	}

	public void setFileMD5Value(String fileMD5Value) {
		this.fileMD5Value = fileMD5Value;
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

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getStorePathFile() {
		return storePathFile;
	}

	public void setStorePathFile(String storePath) {
		this.storePathFile = storePath;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
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
