package com.zhuchao.ticket.util;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class StringUtils extends org.apache.commons.lang.StringUtils {

	public static String extractToken(String key, String content, int type) {
		String startSign = "'";
		String endSign = ";";
		if (type == 2) {
			startSign = "{";
		}
		int i = -1;
		if ((i = content.indexOf(key)) != -1) {
			content = content.substring(i);
			int start = content.indexOf(startSign);
			int end = content.indexOf(endSign);
			return content.substring(start, end);
		}
		return "notFound";
	}

	/**
	 * 执行js函数，得到需要的值的值
	 * 
	 * @param paras
	 * @return
	 * @throws ScriptException
	 * @throws FileNotFoundException
	 * @throws NoSuchMethodException
	 */
	public static String ExcuteJs(String script, String function,
			Object... paras) throws ScriptException, FileNotFoundException,
			NoSuchMethodException {
		ScriptEngineManager engineManager = new ScriptEngineManager();
		ScriptEngine engine = engineManager.getEngineByName("JavaScript"); // 得到脚本引擎
		engine.eval(new java.io.FileReader(script)); // "resources/fuck.js"
		Invocable inv = (Invocable) engine;
		Object a = inv.invokeFunction(function, paras);
		return a.toString();
	}

	public static String format(Date date, String pattern) {
		return new SimpleDateFormat(pattern).format(date);
	}

}
