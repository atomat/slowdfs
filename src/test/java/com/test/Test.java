package com.test;

import org.springframework.util.StringUtils;

public class Test {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		String prefix="jsp";
		String pf=".abc ".toUpperCase();
		if (!StringUtils.isEmpty(prefix) && pf.indexOf("." + prefix.toUpperCase()) >= 0) {
			System.out.println("dsfas");
		}
	}

}
