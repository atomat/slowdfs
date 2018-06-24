package com.hcb168.slowdfs.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.codesnippets4all.json.parsers.JSONParser;
import com.codesnippets4all.json.parsers.JsonParserFactory;

import net.sf.json.JSONObject;

/**
 * 
 * @author yangli
 * 
 */
public class MyUtil {
	private static final Logger logger = Logger.getLogger("LOGFILE");
	static {
		String pathFile = System.getProperties().getProperty("user.dir") + "/src/main/webapp/WEB-INF/conf/log4j.xml";
		pathFile = MyFileUtil.formatPath(pathFile);
		if ((new File(pathFile)).exists()) {

			DOMConfigurator.configure(pathFile);
		} else {
			BasicConfigurator.configure();
		}
	}

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final SecureRandom secureRandom = new SecureRandom();
	private static final Random random = new Random();

	private static int iSequence = 9999997;

	public static Logger getLogger() {
		return logger;
	}

	public static Logger getLogger(String name) {
		return logger;
	}

	public static String getFLN() {
		StackTraceElement ste = new Throwable().getStackTrace()[1];
		return ste.getFileName() + ":" + ste.getLineNumber();
	}

	public static String getFLNb() {
		StackTraceElement ste = new Throwable().getStackTrace()[1];
		return "[" + ste.getFileName() + ":" + ste.getLineNumber() + "]";
	}

	public static byte[] readInput(BufferedInputStream bis) throws IOException {
		byte[] retBuf = new byte[0];
		byte[] buf = new byte[1024];
		int iRet = 0;
		while (true) {
			iRet = bis.read(buf, 0, buf.length);
			if (iRet > 0) {
				byte[] oldBuf = retBuf;
				retBuf = new byte[retBuf.length + iRet];
				System.arraycopy(oldBuf, 0, retBuf, 0, oldBuf.length);
				System.arraycopy(buf, 0, retBuf, oldBuf.length, iRet);
			}
			if (iRet <= 0) {
				break;
			} else if (iRet < buf.length) {
				// 判断是否有后续数据
				if (checkAvailable(bis)) {
					continue;
				} else {
					break;
				}
			}
		}
		return retBuf;
	}

	private static boolean checkAvailable(BufferedInputStream bis) throws IOException {
		for (int i = 0; i < 3; i++) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {

			}
			if (bis.available() > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取异常堆栈
	 * 
	 * @param e
	 * @return
	 */
	public static String exceptionMsg(Exception e) {
		StringBuilder errMsg = new StringBuilder();
		errMsg.append(e.getMessage()).append("\n").append(e).append("\n");
		for (StackTraceElement ste : e.getStackTrace()) {
			errMsg.append(ste).append("\n");
		}
		return errMsg.toString();
	}

	/**
	 * 获取应用基础路径
	 */
	public static String getAppBasePath(HttpServletRequest request) {
		String path = request.getContextPath();
		String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path;
		return basePath;
	}

	/**
	 * 获取客户端IP
	 */
	public static String getClientIP(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
			// 多次反向代理后会有多个ip值，第一个ip才是真实ip
			int index = ip.indexOf(",");
			if (index != -1) {
				return ip.substring(0, index);
			} else {
				return ip;
			}
		}
		ip = request.getHeader("X-Real-IP");
		if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
			return ip;
		}
		return request.getRemoteAddr();
	}

	/**
	 * 转成JSON字符串
	 * 
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	public static String getJsonString(Object obj) throws JsonGenerationException, JsonMappingException, IOException {
		return objectMapper.writeValueAsString(obj);
	}

	/**
	 * 根据JSON转换成对象
	 * 
	 * @param jsonStr
	 * @param tClass
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static <T> T getObjectByJson(String jsonStr, Class<T> tClass)
			throws JsonParseException, JsonMappingException, IOException {
		T obj = objectMapper.readValue(jsonStr, tClass);
		return obj;
	}

	/**
	 * 生成错误返回JSON串的快捷方法
	 * 
	 * @param msg
	 * @return
	 * @throws Exception
	 */
	public static String getReturnErr(String msg) throws Exception {
		Map<String, String> resultMap = new HashMap<String, String>();
		resultMap.put("result", "err");
		resultMap.put("msg", msg);
		return MyUtil.getJsonString(resultMap);
	}

	/**
	 * 生成通用的成功返回JSON串的快捷方法
	 * 
	 * @param msg
	 * @return
	 * @throws Exception
	 */
	public static String getReturnSucc(String msg) throws Exception {
		Map<String, String> resultMap = new HashMap<String, String>();
		resultMap.put("result", "succ");
		resultMap.put("msg", msg);
		return MyUtil.getJsonString(resultMap);
	}

	/**
	 * 转换json串为Map对象
	 * 
	 * @param jsonStr
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Map getMapByJsonStr(String jsonStr) {
		JsonParserFactory parserFac = JsonParserFactory.getInstance();
		JSONParser parser = parserFac.newJsonParser();
		Map jsonData = parser.parseJson(jsonStr);
		return jsonData;
	}

	/**
	 * 转换Map为json串
	 * 
	 * @param jsonData
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String getJsonStrByMap(Map jsonData) {
		JSONObject jsonObject = JSONObject.fromObject(jsonData);
		return jsonObject.toString();
	}

	/**
	 * 获取用于盐化密码的随机数
	 * 
	 * @return
	 */
	public static byte[] getSalt() {
		byte[] bytes = new byte[16];
		secureRandom.nextBytes(bytes);
		return bytes;
	}

	/**
	 * 获取指定长度的随机数组
	 * 
	 * @param length
	 * @return
	 */
	public static byte[] getSecureRandomBytes(int length) {
		byte[] bytes = new byte[length];
		secureRandom.nextBytes(bytes);
		return bytes;
	}

	public static String getSHA256Hex(byte[] data) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hashCode = md.digest(data);
		return new String(Hex.encodeHex(hashCode));
	}

	/**
	 * 生成带盐的密码hash值
	 * 
	 * @param userId
	 * @param pwdSHA256
	 * @return
	 */
	public static String getPwdHash(String userId, String pwdSHA256, String saltSHA256) throws Exception {
		String userIdSHA256 = getSHA256Hex(userId.getBytes());
		String pwd = userIdSHA256 + pwdSHA256 + saltSHA256;
		return getSHA256Hex(pwd.getBytes());
	}

	/**
	 * 格式化金额为带2位小数，含四舍五入
	 * 
	 * @param amount
	 * @return
	 */
	public static String fmtAmount(String amount) throws Exception {
		BigDecimal decimal = new BigDecimal(amount);
		decimal = decimal.setScale(2, RoundingMode.HALF_UP);
		return decimal.toPlainString();
	}

	public static String divide100RetIntStr(String number) {
		return (new BigDecimal(number).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP)).toPlainString();
	}

	/**
	 * 返回一个伪随机源
	 * 
	 * @return
	 */
	public static Random getRandom() {
		return random;
	}

	/**
	 * 获取某日期是星期几？1"星期日", 2"星期一", 3"星期二", 4"星期三", 5"星期四", 6"星期五", 7"星期六"
	 * 
	 * @param date
	 * @return
	 */
	public static int getDayOfWeek(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * 根据格式转换日期时间字符串
	 * 
	 * @param datetime
	 * @param srcFormat
	 * @param destFormat
	 * @return
	 * @throws Exception
	 */
	public static String transferDatetime(String datetime, String srcFormat, String destFormat) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat(srcFormat);
		Date date = sdf.parse(datetime);
		String dateStr = new SimpleDateFormat(destFormat).format(date);
		return dateStr;
	}

	private static final String[] mobileNum3f = new String[] { "130", "131", "132", "133", "134", "135", "136", "137",
			"138", "139", "145", "147", "150", "151", "152", "153", "155", "156", "157", "158", "159", "170", "176",
			"177", "178", "180", "181", "182", "182", "183", "184", "185", "186", "186", "187", "188", "189" };// 手机号段

	/**
	 * 检查手机号码合法性
	 * 
	 * @param mobileNum
	 * @return
	 */
	public static boolean isMobileNum(String mobileNum) {
		// 检查手机号长度
		if (mobileNum == null || mobileNum.trim().length() != 11) {
			return false;
		}

		mobileNum = mobileNum.trim();
		// 检查手机号组成，必须数字
		{
			Pattern pattern = Pattern.compile("^[0-9]+$");
			Matcher matcher = pattern.matcher(mobileNum);
			if (!matcher.matches()) {
				MyUtil.getLogger().info("新注册 手机号=" + mobileNum + ":手机号必须是数字，请重新输入");
				return false;
			}
		}

		// 检查手机号号段
		String mNum3f = mobileNum.substring(0, 3);
		return Arrays.asList(mobileNum3f).contains(mNum3f);
	}

	/**
	 * 检查手机号合法性
	 * 
	 * @param mobileNum
	 * @return
	 * @throws Exception
	 */
	public static String checkMobileNum(String mobileNum) throws Exception {
		// 检查手机号长度
		if (mobileNum == null || mobileNum.trim().length() != 11) {
			MyUtil.getLogger().info("新注册 手机号=" + mobileNum + ":请输入11位手机号");
			return MyUtil.getReturnErr("请输入11位手机号");
		}

		mobileNum = mobileNum.trim();
		// 检查手机号组成，必须数字
		{
			Pattern pattern = Pattern.compile("^[0-9]+$");
			Matcher matcher = pattern.matcher(mobileNum);
			if (!matcher.matches()) {
				MyUtil.getLogger().info("新注册 手机号=" + mobileNum + ":手机号必须是数字，请重新输入");
				return MyUtil.getReturnErr("手机号必须是数字，请重新输入");
			}
		}

		if (MyUtil.isMobileNum(mobileNum) == false) {
			MyUtil.getLogger().info("新注册 手机号=" + mobileNum + ":手机号起始3位号段未支持，请重新输入");
			return MyUtil.getReturnErr("手机号起始3位号段未支持，请重新输入");
		}

		return "ok";
	}

	/**
	 * 判断当前运行环境是否为windows
	 * 
	 * @return
	 */
	public static boolean isOSWindows() {
		String os = System.getenv().get("OS");
		if (null != os && os.toUpperCase().indexOf("WINDOWS") != -1) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 提供一个进程本地的序列号
	 * 
	 * @return
	 */
	public static synchronized String getLocalSequence() {
		String strSeq = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + String.format("%07d", iSequence);
		if (iSequence >= 9999999) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				MyUtil.logger.warn(e);
			}
			iSequence = 0;
		} else {
			iSequence++;
		}
		return strSeq;
	}

	/**
	 * 随机化一个数组
	 * 
	 * @param array
	 *            数组类型不能是基本类型，必须是类和基本类型的包装类
	 * @return
	 */
	public static <T> T[] randomizeArray(T[] array) {
		int len = array.length;
		for (int i = 0; i < len; i++) {
			int iR = secureRandom.nextInt(len);
			T tmp = array[i];
			array[i] = array[iR];
			array[iR] = tmp;
		}
		return array;
	}
}
