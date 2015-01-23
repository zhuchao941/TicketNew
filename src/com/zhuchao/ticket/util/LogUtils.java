package com.zhuchao.ticket.util;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LogUtils {
	static {
		PropertyConfigurator.configure(LogUtils.class.getClassLoader()
				.getResource("log4j.properties"));
	}

	public static void debug(Class<?> clazz, Object content) {
		Logger.getLogger(clazz).debug(content);
	}
	
	public static void error(Class<?> clazz, Object content) {
		Logger.getLogger(clazz).error(content);
	}
	
	public static void info(Class<?> clazz, Object content) {
		Logger.getLogger(clazz).info(content);
	}

	public static void main(String args[]) {
		System.out.println(System.getProperty("user.dir"));
		LogUtils.debug(LogUtils.class, "fuck");
	}
}
