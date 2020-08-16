/**
 * 
 */
package com.banyan.logger.config;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.banyan.logger.properties.BanyanStarterFTPProperties;
import com.banyan.logger.properties.BanyanStarterProperties;
import com.banyan.logger.server.BanyanLoggerCollectorServer;
import com.banyan.logger.server.impl.BanyanLoggerCollectorServerImpl;
import com.banyan.logger.utils.BanyanLoggerCollectorServerUtils;

/**
 * 自动配置
 * @author 皇甫逸彬
 *
 */
@Configuration
@EnableConfigurationProperties({BanyanStarterProperties.class,BanyanStarterFTPProperties.class})
//导入业务组件BanyanLoggerCollectorServerImpl
@Import(BanyanLoggerCollectorServerImpl.class)
//@ConditionalOnClass(BanyanLoggerCollectorServerImpl.class)
public class BanyanLoggerAutoConfiguration {

	@Autowired
	private BanyanStarterProperties properties;
	@Autowired
	private BanyanStarterFTPProperties ftpProperties;
	
	@Bean
	public BanyanLoggerCollectorServer banyanLoggerCollectorServer() {
		
		BanyanLoggerCollectorServer banyanLoggerCollectorServer = new BanyanLoggerCollectorServerImpl();
		banyanLoggerCollectorServer.launch(properties,ftpProperties);
		BanyanLoggerCollectorServerUtils.banyanLoggerCollectorServer = banyanLoggerCollectorServer;
		return banyanLoggerCollectorServer;
	}
	
    @PreDestroy
    public void destory() {
    	BanyanLoggerCollectorServerUtils.banyanLoggerCollectorServer.onDestory();
    }
}
