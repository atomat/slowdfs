package com.hcb168.slowdfs.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.hcb168.slowdfs.core.ResultOfFileUpload;
import com.hcb168.slowdfs.util.MyUtil;
import com.hcb168.slowdfs.util.SpringUtil;

public class JdbcHelper {
	/**
	 * 检查数据库是否已经初始化
	 * 
	 * @return
	 * @throws Exception
	 */
	public static boolean checkDBInit() throws Exception {
		JdbcTemplate jdbcTemplate = SpringUtil.getJdbcTemplate();

		DataSource dataSource = null;
		Connection connection = null;
		try {
			dataSource = jdbcTemplate.getDataSource();
			connection = dataSource.getConnection();
			DatabaseMetaData meta = connection.getMetaData();
			ResultSet resultSet = meta.getTables(null, null, null, new String[] { "TABLE" });
			HashSet<String> set = new HashSet<String>();
			while (resultSet.next()) {
				set.add(resultSet.getString("TABLE_NAME"));
			}
			resultSet.close();
			resultSet = null;

			if (set.contains("file_info".toUpperCase())) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			MyUtil.getLogger().error(e.getMessage());
			throw e;
		} finally {
			// 释放数据库连接
			DataSourceUtils.releaseConnection(connection, dataSource);
			if (connection.isClosed()) {
				MyUtil.getLogger().debug("JdbcHelper.checkDBInit() 关闭数据库连接");
			} else {
				MyUtil.getLogger().warn("DataSourceUtils.releaseConnection未成功关闭连接。现强制关闭。");
				connection.close();
			}
			connection = null;
			dataSource = null;
		}
	}

	/**
	 * 初始化数据库表
	 */
	public static void initDB() {
		JdbcTemplate jdbcTemplate = SpringUtil.getJdbcTemplate();
		// 创建文件信息表
		jdbcTemplate.execute("create table file_info (file_id varchar(32), info varchar(2048))");
		jdbcTemplate.execute("create unique index idx_file_info_id on file_info(file_id)");

		// 创建文件变化通知消息表
		jdbcTemplate.execute(
				"create table notice_queue (seqid varchar(32), oper_type varchar(16), file_info varchar(2048), err_num INTEGER)");
		jdbcTemplate.execute("create unique index idx_notice_queue_seqid on notice_queue(seqid)");
	}

	/**
	 * 文件信息入库
	 * 
	 * @param resultOfFileUpload
	 * @throws Exception
	 */
	public static void putFileInfo(ResultOfFileUpload resultOfFileUpload) throws Exception {
		String info = MyUtil.getJsonString(resultOfFileUpload);
		String fileId = resultOfFileUpload.getFileId();
		JdbcTemplate jdbcTemplate = SpringUtil.getJdbcTemplate();
		jdbcTemplate.update("insert into file_info(file_id,info) values(?,?)", new Object[] { fileId, info });
		MyUtil.getLogger().debug("存储文件信息：" + info);
	}

	/**
	 * 获取文件信息
	 * 
	 * @param fileId
	 * @return
	 * @throws Exception
	 */
	public static ResultOfFileUpload getFileInfo(String fileId) throws Exception {
		JdbcTemplate jdbcTemplate = SpringUtil.getJdbcTemplate();
		List<String> result = (List<String>) jdbcTemplate.queryForList("SELECT info FROM file_info WHERE file_id = ?",
				new Object[] { fileId }, String.class);
		if (result.size() <= 0) {
			return null;
		}
		String info = result.get(0);
		ResultOfFileUpload resultOfFileUpload = (ResultOfFileUpload) MyUtil.getObjectByJson(info,
				ResultOfFileUpload.class);
		return resultOfFileUpload;
	}

	/**
	 * 收到新增文件的通知
	 * 
	 * @param jsonFileInfo
	 */
	public static void putNoticeAdd(String jsonFileInfo) {
		MyUtil.getLogger().debug("收到新增文件通知：" + jsonFileInfo);
		String seqId = MyUtil.getLocalSequence();
		JdbcTemplate jdbcTemplate = SpringUtil.getJdbcTemplate();
		jdbcTemplate.update("insert into notice_queue values(?,?,?,0)",
				new Object[] { seqId, "addfile", jsonFileInfo });

	}

	/**
	 * 从通知消息表中获取记录
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> getNotice() {
		JdbcTemplate jdbcTemplate = SpringUtil.getJdbcTemplate();
		List<Map<String, Object>> result = jdbcTemplate.queryForList(
				"SELECT seqid,oper_type,file_info,err_num FROM NOTICE_QUEUE order by SEQID asc OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY");
		return result;
	}

	/**
	 * 删除文件变化通知消息表指定记录
	 * 
	 * @param seqId
	 */
	public static void delNotice(String seqId) {
		JdbcTemplate jdbcTemplate = SpringUtil.getJdbcTemplate();
		jdbcTemplate.update("delete from NOTICE_QUEUE where seqid=?", new Object[] { seqId });
		MyUtil.getLogger().debug("删除通知表消息：" + seqId);
	}

	/**
	 * 增加文件变化通知消息表指定记录的错误次数
	 * 
	 * @param seqId
	 */
	public static void increaseNoticeErrNum(String seqId) {
		JdbcTemplate jdbcTemplate = SpringUtil.getJdbcTemplate();
		jdbcTemplate.update("update NOTICE_QUEUE set err_num=err_num+1 where SEQID=?", new Object[] { seqId });
		MyUtil.getLogger().debug("通知表消息：" + seqId + "增加错误次数");
	}
}
