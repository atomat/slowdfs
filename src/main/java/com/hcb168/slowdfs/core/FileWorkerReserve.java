package com.hcb168.slowdfs.core;

import java.util.List;
import java.util.Map;

import com.hcb168.slowdfs.db.JdbcHelper;
import com.hcb168.slowdfs.util.MyUtil;

public class FileWorkerReserve extends Thread {
	@Override
	public void run() {
		MyUtil.getLogger().info("FileWorkerReserve线程启动");
		while (true) {
			try {
				sleep(10 * 60 * 1000);
			} catch (InterruptedException e) {
				MyUtil.getLogger().error(e);
			}
			try {
				List<Map<String, Object>> listNotice = JdbcHelper.getNoticeReserve();
				for (Map<String, Object> map : listNotice) {
					String seqId = (String) map.get("seqid");
					String operType = (String) map.get("oper_type");
					String jsonFileInfo = (String) map.get("file_info");
					int errNum = (int) map.get("err_num");

					boolean result = FileWorkerOperate.doThis(seqId, operType, jsonFileInfo);
					if (result == true) {
						JdbcHelper.delNoticeReserve(seqId);
					} else {
						if (errNum < 8) {
							JdbcHelper.increaseNoticeReserveErrNum(seqId);
						} else {
							JdbcHelper.delNoticeReserve(seqId);
						}
					}
				}
			} catch (Exception e) {
				MyUtil.getLogger().error("FileWorkerReserve:" + e);
			}
		}
	}

}
