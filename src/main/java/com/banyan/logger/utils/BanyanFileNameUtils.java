/**
 * 
 */
package com.banyan.logger.utils;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import ch.qos.logback.core.util.CachingDateFormatter;

/**
 * @author 皇甫逸彬
 *
 */
public class BanyanFileNameUtils {
	private static int i = 0;
	
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
	 * @param fileNamePattern  %s{project}.info.%d{yyyy-MM-dd}.%i.log
	 * @return
	 */
	public static String getFileName(String fileNamePattern){
		String fileName = System.getProperty("LOG_HOME")+fileNamePattern;
		Map<String, String> data = getDataMap();
		//解析%s
		String reg = "%s\\{(.+?)\\}";
		fileName = parse(reg, fileName, data);
		//解析%d
		reg = "%d\\{(.+?)\\}";
		fileName = parse(reg, fileName, data);
		//解析%i
		i=0;
		fileName  = getName(fileName,fileName);
		return fileName;
	}

	private synchronized static String getI() {
		i += 1;
		return i < 10 ? "0"+i : i+"";
	}
	
	private static String getName(String old,String name) {
		String temp = old.replace("%i", getI());
		File file = new File(temp);
		if(file.exists()) {
			return getName(old,temp);
		}
		return temp;
	}
	
	private static Map<String, String> getDataMap() {
		Map<String, String> data = new HashMap<String, String>();
		data.put("LOG_HOME", System.getProperty("LOG_HOME"));
		data.put("yyyy-MM-dd", new CachingDateFormatter("yyyy-MM-dd").format(System.currentTimeMillis()));
		data.put("yyyy-MM-dd HH:mm:ss,SSS",new CachingDateFormatter("yyyy-MM-dd HH:mm:ss,SSS").format(System.currentTimeMillis()));
		data.put("HH:mm:ss,SSS",new CachingDateFormatter("yyyy-MM-dd HH:mm:ss,SSS").format(System.currentTimeMillis()));
		data.put("HH:mm:ss",new CachingDateFormatter("yyyy-MM-dd HH:mm:ss,SSS").format(System.currentTimeMillis()));
		
		String rootPath = System.getProperty("user.dir");
		MavenXpp3Reader reader = new MavenXpp3Reader();
		String myPom = rootPath + File.separator + "pom.xml";
		Model model;
		try {
			model = reader.read(new FileReader(myPom));
			data.put("project", model.getGroupId());
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	public static void main(String[] args) {
		String fileName = "%s{project}.info.%d{yyyy-MM-dd}.%i.log";
		System.out.println(getFileName(fileName));
		System.out.println(getFileName(fileName));
		System.out.println(getFileName(fileName));
	}
}
