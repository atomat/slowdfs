package com.hcb168.slowdfs.config;

public class HostConfig {
	private String[] hostList = new String[] {};// slowdfs集群各节点

	public String[] getHostList() {
		return hostList;
	}

	public void setHostList(String[] hostList) {
		this.hostList = hostList;
	}

}
