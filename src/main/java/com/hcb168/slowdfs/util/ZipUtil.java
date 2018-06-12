package com.hcb168.slowdfs.util;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

/**
 * 
 * @author yanglih
 *
 */
public class ZipUtil {
	/**
	 * 
	 * @param srcPath
	 *            要压缩的源文件路径。如果是文件，则为该文件的全路径；如果是一个目录，则为该目录的上层目录路径。
	 * @param zipPath
	 *            压缩文件保存的路径。注意：zipPath不能是srcPath路径下的子目录
	 * @param zipFileName
	 *            zip文件名
	 * @param zipRootPathName
	 *            设置将内容压缩到zip文件中指定的根目录，该参数可为null或空字符
	 * @throws Exception
	 */
	public static void zip(String srcPath, String zipPath, String zipFileName, String zipRootPathName)
			throws Exception {
		zip(srcPath, zipPath, zipFileName, zipRootPathName, null, false);
	}

	/**
	 * 
	 * @param srcPath
	 *            要压缩的源文件路径。如果是文件，则为该文件的全路径；如果是一个目录，则为该目录的上层目录路径。
	 * @param zipPath
	 *            压缩文件保存的路径。注意：zipPath不能是srcPath路径下的子目录
	 * @param zipFileName
	 *            zip文件名
	 * @param zipRootPathName
	 *            设置将内容压缩到zip文件中指定的根目录，该参数可为null或空字符
	 * @param pwd
	 *            压缩文件密码
	 * @throws Exception
	 */
	public static void zip(String srcPath, String zipPath, String zipFileName, String zipRootPathName, char[] pwd)
			throws Exception {
		zip(srcPath, zipPath, zipFileName, zipRootPathName, pwd, false);
	}

	/**
	 * 
	 * @param srcPath
	 *            要压缩的源文件路径。如果是文件，则为该文件的全路径；如果是一个目录，则为该目录的上层目录路径。
	 * @param zipPath
	 *            压缩文件保存的路径。注意：zipPath不能是srcPath路径下的子目录
	 * @param zipFileName
	 *            zip文件名
	 * @param zipRootPathName
	 *            设置将内容压缩到zip文件中指定的根目录，该参数可为null或空字符
	 * @param isCreateDir
	 *            是否在压缩文件里按目标的上级目录创建根目录
	 * @param pwd
	 *            压缩文件密码
	 * @throws Exception
	 */
	public static void zip(String srcPath, String zipPath, String zipFileName, String zipRootPathName, char[] pwd,
			boolean isCreateDir) throws Exception {
		// 检查输入参数
		if (StringUtils.isEmpty(srcPath) || StringUtils.isEmpty(zipPath) || StringUtils.isEmpty(zipFileName)) {
			throw new Exception("输入参数为空");
		}
		srcPath = formatPath(srcPath);
		zipPath = formatPath(zipPath);

		File srcFile = new File(srcPath);
		// 判断压缩文件保存的路径是否为源文件路径的子文件夹，防止无限递归
		if (srcFile.isDirectory()) {
			if (MyUtil.isOSWindows()) {
				if (zipPath.toUpperCase().indexOf(srcPath.toUpperCase()) != -1) {
					throw new Exception("zipPath不能是srcPath路径下的子目录|" + zipPath + "|" + srcPath);
				}
			} else {
				if (zipPath.indexOf(srcPath) != -1) {
					throw new Exception("zipPath不能是srcPath路径下的子目录|" + zipPath + "|" + srcPath);
				}
			}

		}

		// 判断压缩文件保存的路径是否存在，如果不存在，则创建目录
		File zipDir = new File(zipPath);
		if (!zipDir.exists() || !zipDir.isDirectory()) {
			zipDir.mkdirs();
		}

		// 创建压缩文件保存的文件对象
		String zipFilePath = zipPath + File.separator + zipFileName;
		File zipFile = new File(zipFilePath);
		if (zipFile.exists()) {
			zipFile.delete();
		}

		// 设置压缩参数
		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // 压缩方式
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL); // 压缩级别
		if (null != pwd && pwd.length != 0) {
			parameters.setEncryptFiles(true);
			parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES); // 加密方式
			parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
			parameters.setPassword(pwd);
		}
		if (!StringUtils.isEmpty(zipRootPathName)) {
			parameters.setRootFolderInZip(zipRootPathName);
		}

		ZipFile zipFileObj = new ZipFile(zipFilePath);
		if (srcFile.isDirectory()) {
			// 如果不创建目录的话,将直接把给定目录下的文件压缩到压缩文件
			if (!isCreateDir) {
				File[] subFiles = srcFile.listFiles();
				for (int n = 0; n < subFiles.length; n++) {
					if (subFiles[n].isDirectory()) {
						zipFileObj.addFolder(subFiles[n], parameters);
					} else {
						zipFileObj.addFile(subFiles[n], parameters);
					}
				}
			} else {
				zipFileObj.addFolder(srcFile, parameters);
			}
		} else {
			zipFileObj.addFile(srcFile, parameters);
		}
	}

	private static String formatPath(String path) {
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
	 * 
	 * @param zipPathFile
	 *            zip文件全路径
	 * @param unzipFilePath
	 *            解压的目的路径，该方法不会清除与zip文件中不冲突的文件和目录
	 * @param hasZipNamePath
	 *            解压后的文件路径是否以压缩文件名开头。true-是；false-否
	 * @throws Exception
	 */
	public static void unzip(String zipPathFile, String unzipFilePath, boolean hasZipNamePath) throws Exception {
		unzip(zipPathFile, unzipFilePath, hasZipNamePath, null);
	}

	/**
	 * 
	 * @param zipPathFile
	 *            zip文件全路径
	 * @param unzipFilePath
	 *            解压的目的路径，该方法不会清除与zip文件中不冲突的文件和目录
	 * @param hasZipNamePath
	 *            解压后的文件路径是否以压缩文件名开头。true-是；false-否
	 * @param pwd
	 *            解压密码
	 * @throws Exception
	 */
	public static void unzip(String zipPathFile, String unzipFilePath, boolean hasZipNamePath, char[] pwd)
			throws Exception {
		if (StringUtils.isEmpty(zipPathFile) || StringUtils.isEmpty(unzipFilePath)) {
			throw new Exception("输入参数为空");
		}

		zipPathFile = formatPath(zipPathFile);
		unzipFilePath = formatPath(unzipFilePath);

		File zipFile = new File(zipPathFile);
		// 如果解压后的文件路径是否以压缩文件名开头
		if (hasZipNamePath) {
			String fileName = zipFile.getName();
			if (StringUtils.isNotEmpty(fileName)) {
				fileName = fileName.substring(0, fileName.lastIndexOf("."));
			}
			unzipFilePath = unzipFilePath + File.separator + fileName;
		}
		// 创建解压缩文件保存的路径
		File unzipFileDir = new File(unzipFilePath);
		if (!unzipFileDir.exists() || !unzipFileDir.isDirectory()) {
			unzipFileDir.mkdirs();
		}

		ZipFile zipFileObj = new ZipFile(zipFile);
		// zipFileObj.setFileNameCharset("GBK");
		if (!zipFileObj.isValidZipFile()) {
			throw new ZipException("压缩文件不合法，可能被损坏。");
		}

		if (zipFileObj.isEncrypted()) {
			if (null != pwd && pwd.length != 0) {
				zipFileObj.setPassword(pwd);
			} else {
				throw new ZipException("压缩文件已加密，解密密码不能为空");
			}
		}
		zipFileObj.extractAll(unzipFilePath);
	}

	public static void main(String[] args) throws Exception {

		MyUtil.getLogger().info("adsfasdf");
		try {
			zip("D:/tmp/target/", "e:/tmp////", "test.zip", "", "123".toCharArray());
		} catch (Exception e) {
			e.printStackTrace();
		}

		String zipFilePath = "E:/tmp/test.zip\\/";
		String unzipFilePath = "E:/tmp";
		try {
			unzip(zipFilePath, unzipFilePath, true, "123".toCharArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
