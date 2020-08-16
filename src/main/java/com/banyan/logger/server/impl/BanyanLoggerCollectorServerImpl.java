/**
 * 
 */
package com.banyan.logger.server.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.banyan.logger.ftp.BanyanFtpServer;
import com.banyan.logger.properties.BanyanStarterFTPProperties;
import com.banyan.logger.properties.BanyanStarterProperties;
import com.banyan.logger.server.BanyanLoggerCollectorServer;
import com.banyan.logger.stream.BanyanFilePrintStream;
import com.banyan.logger.stream.BanyanFilePrintStreamHelper;
import com.banyan.logger.utils.BanyanFileUtils;
import com.banyan.logger.utils.BanyanLoggerCollectorServerUtils;

/**
 * 日志收集器具体业务实现
 * 
 * @author 皇甫逸彬
 *
 */
public class BanyanLoggerCollectorServerImpl implements BanyanLoggerCollectorServer {

	private final static String REGREX = "[\\u200b-\\u200f]|[\\u200e-\\u200f]|[\\u202a-\\u202e]|[\\u2066-\\u2069]|\ufeff|\u06ec";

	private Logger logger = LoggerFactory.getLogger(getClass());

	private BanyanStarterProperties properties;
	private BanyanStarterFTPProperties ftpProperties;
	/**
	 * ConsoleStream
	 */
	private BanyanFilePrintStreamHelper consoleStreamHelper;
	/**
	 * ThreadPoolExecutor
	 */
	private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

	public BanyanLoggerCollectorServerImpl() {
		scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(10);
	}

	@Override
	public void launch(BanyanStarterProperties properties, BanyanStarterFTPProperties ftpProperties) {
		this.properties = properties;
		this.ftpProperties = ftpProperties;
		
		logger.info("--------------------------------------------------");
		logger.info("-------------------日志收集器开始工作---------------------");
		logger.info("--------------------------------------------------");
		/**
		 * 
		 * 1.在项目文件夹下面创建缓存目录(temp)
		 * 2.开启定时任务删除过期的缓存日志文件
		 * 3.修改控制台输入流为BanyanFilePrintStream
		 * 4.在BanyanFilePrintStream打印信息的时候将内容输出到缓存日志文件中
		 * 5.开启定时任务传输文件
		 */
		
		String path = BanyanFileUtils.dirCache();
		this.properties.setPath(path);
		System.setProperty("banyan.logger.file.path", path);
		System.setProperty("LOG_HOME", path);

		//systemMonitor();

		clearHistory(this.properties);

		setConsoleOutput();
		
	}

	/**
	 * 设置控制台输出信息为文件
	 */
	private void setConsoleOutput() {
		PrintStream old = System.out;

		BanyanFilePrintStream consoleStream = new BanyanFilePrintStream(old);
		consoleStreamHelper = new BanyanFilePrintStreamHelper(properties);
		BanyanFtpServer banyanFtpServer = new BanyanFtpServer(ftpProperties);
		consoleStreamHelper.setBanyanFtpServer(banyanFtpServer);
		consoleStream.setConsoleStreamHelper(consoleStreamHelper);
		// 设置新的输出流
		System.setOut(new PrintStream(consoleStream));
	}

	/**
	 * 系统运行情况监控
	 */
	private void systemMonitor() {
		logger.info("----------------系统监控服务开始运行-----------------");
		scheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				logger.info("--------------------systemMonitor--------------------");
			}
		}, 100, 30000, TimeUnit.MILLISECONDS);
	}

	/**
	 * 定时清除缓存日志文件
	 * 
	 * @param properties
	 */
	private void clearHistory(BanyanStarterProperties properties) {
		scheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				String path = properties.getPath();
				List<File> list = BanyanFileUtils.listFiles(new File(path));
				BanyanFileUtils.clearHistory(list, properties.getMaxHistory());
				list.clear();
				list = BanyanFileUtils.listFiles(new File(path));
				for (File file : list) {
					if (file.isDirectory()) {
						continue;
					}
					// 判断是否已经上传
					// 文件第一行为文件头
					BufferedReader bufferedReader;
					try {
						file.setReadable(true);
						FileReader reader = new FileReader(file);
						bufferedReader = new BufferedReader(reader);
						String line = bufferedReader.readLine();
						reader.close();
						bufferedReader.close();

						if (StringUtils.isEmpty(line)) {
							continue;
						}
						line = line.replaceAll(REGREX, "");
						// 文件头信息, 1表示未上传, 0 表示已上传
						if (!StringUtils.isEmpty(line) && line.startsWith("1")) {
							if (!BanyanLoggerCollectorServerUtils.cacheFileMap.containsKey(file.getName())) {
								BanyanLoggerCollectorServerUtils.cacheFileMap.put(file.getName(), file);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}, 100, 30000, TimeUnit.MILLISECONDS);
	}

	@Override
	public void onDestory() {
		logger.info("--------------------onDestory--------------------");
		consoleStreamHelper.destory();
	}

	///////////////////////////////////////////////////////

}
