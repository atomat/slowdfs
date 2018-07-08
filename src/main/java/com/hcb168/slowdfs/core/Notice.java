package com.hcb168.slowdfs.core;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.hcb168.slowdfs.util.SpringUtil;

public class Notice {
	private static ThreadPoolTaskExecutor noticeThreadPool = (ThreadPoolTaskExecutor) SpringUtil.getApplicationContext()
			.getBean("noticeThreadPool");

	/**
	 * 通知各节点收到新文件
	 * 
	 * @param resultOfFileUpload
	 * @throws Exception
	 */
	public static void addFile(ResultOfFileUpload resultOfFileUpload) {
		NoticeAddFileTask noticeAddFileTask = new NoticeAddFileTask(resultOfFileUpload);
		noticeThreadPool.execute(noticeAddFileTask);
	}

	/**
	 * 通知各节点删除文件
	 * 
	 * @param resultOfFileUpload
	 */
	public static void deleteFile(String groupId, String fileId) {
		NoticeDeleteFileTask noticeDeleteFileTask = new NoticeDeleteFileTask(groupId, fileId);
		noticeThreadPool.execute(noticeDeleteFileTask);
	}
}
