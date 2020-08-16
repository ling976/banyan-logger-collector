/**
 * 
 */
package com.banyan.logger.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 皇甫逸彬
 *
 */
@ConfigurationProperties(prefix = "banyan.logger.file")
public class BanyanStarterProperties {
	/**
	 * 日志路径,如果不填写,默认为项目路径
	 */
	private String path;
	/**
	 * 文件名
	 */
	private String fileNamePattern = "info.%d{yyyy-MM-dd}.%i.log";
	/**
	 * 单文件大小
	 */
	private String maxFileSize = "10M";
	/**
	 * 文件保存时间(天)
	 */
	private int maxHistory = 10;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getFileNamePattern() {
		return fileNamePattern;
	}

	public void setFileNamePattern(String fileNamePattern) {
		this.fileNamePattern = fileNamePattern;
	}

	public String getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(String maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public int getMaxHistory() {
		return maxHistory;
	}

	public void setMaxHistory(int maxHistory) {
		this.maxHistory = maxHistory;
	}

}
