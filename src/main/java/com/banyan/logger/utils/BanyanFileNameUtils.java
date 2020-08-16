/**
 * 
 */
package com.banyan.logger.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.core.util.CachingDateFormatter;

/**
 * @author 皇甫逸彬
 *
 */
public class BanyanFileNameUtils {
	private static int i = 0;
	
	private static String fileNameTemp = "";
	
	public static List<String> getParams(String pattern, String content) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(content);

		List<String> result = new ArrayList<String>();
		while (m.find()) {
			result.add(m.group(1));
		}
		return result;
	}

	public static String parse(String pattern, String content, Map<String, String> data) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(content);

		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String key = m.group(1);
			String value = data.get(key);
			m.appendReplacement(sb, value == null ? "" : value);
		}
		m.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 生成一个文件名称
	 * @param fileNamePattern  info.%d{yyyy-MM-dd}.%i.log
	 * @return
	 */
	public static String getFileName(String fileNamePattern){
		//String fileNamePattern = "info.%d{yyyy-MM-dd}.%i.log";
		String reg = "\\$\\{(.+?)\\}";
		String fileName = System.getProperty("LOG_HOME")+fileNamePattern;
		
		Map<String, String> data = new HashMap<String, String>();
		data.put("LOG_HOME", System.getProperty("LOG_HOME"));
		data.put("yyyy-MM-dd", new CachingDateFormatter("yyyy-MM-dd").format(System.currentTimeMillis()));
		data.put("yyyy-MM-dd HH:mm:ss,SSS",new CachingDateFormatter("yyyy-MM-dd HH:mm:ss,SSS").format(System.currentTimeMillis()));
		data.put("HH:mm:ss,SSS",new CachingDateFormatter("yyyy-MM-dd HH:mm:ss,SSS").format(System.currentTimeMillis()));
		data.put("HH:mm:ss",new CachingDateFormatter("yyyy-MM-dd HH:mm:ss,SSS").format(System.currentTimeMillis()));
		
		String text = parse(reg, fileName, data).replaceAll("//", "/");
		reg = "\\{(.+?)\\}";
		text = parse(reg, text, data);
		fileNameTemp = text;
		i=0;
		text  = getName(text);
		fileNameTemp = "";
		return text;
	}

	private synchronized static String getI() {
		i += 1;
		return i < 10 ? "0"+i : i+"";
	}
	
	private static String getName(String name) {
		String temp = fileNameTemp.replace("%i", getI()).replace("%d", "");
		File file = new File(temp);
		if(file.exists()) {
			return getName(temp);
		}
		return temp;
	}
}
