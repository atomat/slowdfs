package com.hcb168.slowdfs.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SysParams {
	private static final SysParams instance = new SysParams();

	private SysParams() {

	}

	public static SysParams getInstance() {
		return instance;
	}

	private final Map<String, String> sysParamMap = new ConcurrentHashMap<String, String>();

	public String getSysParam(String key) {
		return sysParamMap.get(key);
	}

	public void putSysParam(String key, String value) {
		this.sysParamMap.put(key, value);
	}

	public void removeSysParam(String key) {
		this.sysParamMap.remove(key);
	}

}
