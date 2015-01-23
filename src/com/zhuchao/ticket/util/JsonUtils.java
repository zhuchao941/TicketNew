package com.zhuchao.ticket.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.DefaultValueProcessor;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.util.PropertyFilter;

public class JsonUtils {

	public static Object parseJson(Object src) {
		if (src == null) {
			return null;
		}
		JsonConfig jsonConfig = getJsonConfig();
		if (src instanceof List<?> || src.getClass().isArray()) {
			return JSONArray.fromObject(src, jsonConfig);
		}
		return JSONObject.fromObject(src, jsonConfig);
	}

	private static JsonConfig getJsonConfig() {
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setIgnorePublicFields(false);
		jsonConfig.registerDefaultValueProcessor(Integer.class,
				new DefaultValueProcessor() {
					@SuppressWarnings("unchecked")
					@Override
					public Object getDefaultValue(Class type) {
						return null;
					}
				});
		jsonConfig.registerDefaultValueProcessor(Double.class,
				new DefaultValueProcessor() {
					@SuppressWarnings("unchecked")
					@Override
					public Object getDefaultValue(Class type) {
						return null;
					}
				});
		jsonConfig.registerJsonValueProcessor(Date.class,
				new JsonValueProcessor() {

					/**
					 * paramString -> 参数名 paramObject -> 参数值
					 */
					@Override
					public Object processObjectValue(String paramString,
							Object paramObject, JsonConfig paramJsonConfig) {
						if (paramObject == null) {
							return null;
						}
						String ret = null;
						try {
							SimpleDateFormat format = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss");
							ret = format.format((Date) paramObject);
						} catch (Exception e) {
							SimpleDateFormat format = new SimpleDateFormat(
									"yyyy-MM-dd");
							ret = format.format((Date) paramObject);
						}
						return ret;
					}

					@Override
					public Object processArrayValue(Object paramObject,
							JsonConfig paramJsonConfig) {
						return null;
					}
				});
		return jsonConfig;
	}

	public static Object parseJsonFilterNull(Object src) {
		JsonConfig config = new JsonConfig();
		config.setIgnorePublicFields(false);
		config.setJsonPropertyFilter(new PropertyFilter() {
			@Override
			public boolean apply(Object source, String name, Object value) {
				return value == null;
			}
		});
		if (src instanceof List<?> || src.getClass().isArray()) {
			return JSONArray.fromObject(src, config);
		}
		return JSONObject.fromObject(src, config);
	}
}
