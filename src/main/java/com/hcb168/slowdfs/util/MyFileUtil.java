package com.hcb168.slowdfs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;

public class MyFileUtil {
	/**
	 * 格式化目录分隔符
	 * 
	 * @param path
	 * @return
	 */
	public static String formatPath(String path) {
		path = path.replace('\\', File.separatorChar);
		path = path.replace('/', File.separatorChar);

		// 去掉尾部斜线
		int length = path.length();
		char endChar = path.charAt(length - 1);
		while (File.separatorChar == endChar) {
			path = path.substring(0, length - 1);
			length = path.length();
			endChar = path.charAt(length - 1);
		}

		return path;
	}

	/**
	 * 生成文本类文件的快捷方法
	 * 
	 * @param strPathFile
	 * @param text
	 * @throws Exception
	 */
	public static void writeTextFile(String strPathFile, String text) throws Exception {
		// 生成文件
		FileWriter fw = null;
		try {
			fw = new FileWriter(strPathFile);
			fw.write(text);
		} catch (Exception e) {
			throw e;
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			fw = null;
		}
	}

	/**
	 * 获取文件名后缀
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileNamePrefix(String fileName) {
		// 获取后缀
		String prefix = "";
		int prefixIndex = fileName.lastIndexOf(".");
		if (prefixIndex >= 0) {
			prefix = fileName.substring(prefixIndex + 1);
		} else {
			prefix = "";
		}
		return prefix;
	}

	/**
	 * 获取文件内容的MD5
	 * @param srcPath
	 * @param srcFileName
	 * @return
	 * @throws Exception
	 */
	public static String getFileMD5Value(String srcPath, String srcFileName) throws Exception {
		String srcPathFile = srcPath + File.separator + srcFileName;

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(srcPathFile);
			String fileMD5Value = DigestUtils.md5Hex(fis);
			fis.close();
			fis = null;

			return fileMD5Value;
		} catch (Exception e) {
			MyUtil.getLogger().error(e, e);
			throw new Exception("获取文件MD5值异常");
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
					MyUtil.getLogger().error(e, e);
				}
				fis = null;
			}
		}
	}
}
