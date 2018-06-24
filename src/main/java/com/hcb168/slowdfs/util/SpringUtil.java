package com.hcb168.slowdfs.util;

import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

public class SpringUtil {
	private static volatile ApplicationContext applicationContext;

	public static void setApplicationContext(ApplicationContext ac) {
		applicationContext = ac;
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public static JdbcTemplate getJdbcTemplate() {
		JdbcTemplate jdbcTemplate = (JdbcTemplate) applicationContext.getBean("jdbcTemplate");
		return jdbcTemplate;
	}

}
