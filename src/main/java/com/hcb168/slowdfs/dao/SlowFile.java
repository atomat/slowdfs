package com.hcb168.slowdfs.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hcb168.slowdfs.core.ResultOfFileUpload;
import com.hcb168.slowdfs.db.JdbcHelper;
import com.hcb168.slowdfs.util.MyUtil;

public class SlowFile {
	private static final SlowFile instance = new SlowFile();

	private SlowFile() {

	}

	public static SlowFile getInstance() {
		return instance;
	}

	private final Map<String, ResultOfFileUpload> map = new ConcurrentHashMap<String, ResultOfFileUpload>();

	public void putResultOfFileUpload(ResultOfFileUpload resultOfFileUpload) throws Exception {
		JdbcHelper.putFileInfo(resultOfFileUpload);
	}

	public ResultOfFileUpload getResultOfFileUpload(String fileId) throws Exception {
		return JdbcHelper.getFileInfo(fileId);
	}

}
