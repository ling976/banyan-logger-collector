/**
 * 
 */
package com.banyan.logger.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置文件服务器
 * @author 皇甫逸彬
 *
 */
@ConfigurationProperties(prefix = "banyan.logger.ftp")
public class BanyanStarterFTPProperties {
	/**
	 * 是否启用文件服务器储存日志文件
	 */
	private boolean enable = false;
	/**
	 * 文件服务器ip地址
	 */
	private String ip;
	/**
	 * 端口
	 */
	private int port;
	/**
	 * 用户名
	 */
	private String user;
	/**
	 * 密码
	 */
	private String password;
	/**
	 * 是否可匿名访问
	 */
	private boolean anonymous;
	/**
	 * 是否为固定端口
	 */
	private boolean customPortRange;
	/**
	 * 如果为固定端口,这里为具体的开放端口号
	 */
	private String portRange;
	
	/**
	 * 	服务器根路径,如果不配置则以项目名称为目录
	 */
	private String baseDir;
	
	public boolean isEnable() {
		return enable;
	}
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean isAnonymous() {
		return anonymous;
	}
	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}
	public boolean isCustomPortRange() {
		return customPortRange;
	}
	public void setCustomPortRange(boolean customPortRange) {
		this.customPortRange = customPortRange;
	}
	public String getPortRange() {
		return portRange;
	}
	public void setPortRange(String portRange) {
		this.portRange = portRange;
	}
	public String getBaseDir() {
		return baseDir;
	}
	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}
	
}
