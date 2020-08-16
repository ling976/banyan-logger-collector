/**
 * 
 */
package com.banyan.logger.server;

import com.banyan.logger.properties.BanyanStarterFTPProperties;
import com.banyan.logger.properties.BanyanStarterProperties;

/**
 * 日志收集器具体业务实现
 * @author 皇甫逸彬
 *
 */
public interface BanyanLoggerCollectorServer {

	/**
	 * 开启服务
	 * @param properties
	 * @param ftpProperties
	 */
	public void launch(BanyanStarterProperties properties,BanyanStarterFTPProperties ftpProperties);
	/**
	 * 服务器停止的时候调用
	 */
	public void onDestory();
}
