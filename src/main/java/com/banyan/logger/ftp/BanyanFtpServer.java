/**
 * 
 */
package com.banyan.logger.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.banyan.logger.properties.BanyanStarterFTPProperties;
import com.banyan.logger.utils.BanyanFileUtils;
import com.banyan.logger.utils.BanyanLoggerCollectorServerUtils;
import com.banyan.logger.utils.InetAddressUtils;

/**
 * 用于传递日志文件的服务
 * 
 * @author 皇甫逸彬
 *
 */
public class BanyanFtpServer {
	private static Logger logger = LoggerFactory.getLogger(BanyanFtpServer.class);
	/**
	 * ftp文件服务器配置
	 */
	private BanyanStarterFTPProperties ftpProperties;

	private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
	
	private String baseDir = "";
	
	private boolean enable = false;
	
	public BanyanFtpServer(BanyanStarterFTPProperties ftpProperties) {
		this.ftpProperties = ftpProperties;
		scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2);
	}

	/**
	 * groupId作为根文件夹名称
	 * 
	 * @throws XmlPullParserException
	 * @throws Exception
	 * @throws FileNotFoundException
	 */
	private void getBaseDir() throws Exception {
		if(!baseDir.isEmpty()) {
			return;
		}
		if(!StringUtils.isEmpty(ftpProperties.getBaseDir())) {
			baseDir = ftpProperties.getBaseDir();
		}else {
			String rootPath = System.getProperty("user.dir");
			MavenXpp3Reader reader = new MavenXpp3Reader();
			String myPom = rootPath + File.separator + "pom.xml";
			Model model = reader.read(new FileReader(myPom));
			baseDir = model.getGroupId();
		}
	}
	/**
	 * 是否启用文件服务器
	 * 
	 * @return
	 */
	public boolean isEnabled() {
		boolean enabled = ftpProperties.isEnable();
		if (enabled && !StringUtils.isEmpty(ftpProperties.getIp())) {
			FTPClient ftpClient = new FTPClient();
			System.setProperty("ftp4j.activeDataTransfer.acceptTimeout", "30000");
			if (ftpProperties.isCustomPortRange()) {
				System.setProperty("ftp4j.activeDataTransfer.hostAddress", ftpProperties.getIp());
				System.setProperty("ftp4j.activeDataTransfer.portRange", ftpProperties.getPortRange());
			}
			try {
				ftpClient.connect(ftpProperties.getIp(), ftpProperties.getPort());
				if (!ftpProperties.isAnonymous()) {
					ftpClient.login(ftpProperties.getUser(), ftpProperties.getPassword());
				} else {
					ftpClient.login("anonymous", "ftp4j");
				}
				ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
				if (!ftpClient.isConnected()) {
					ftpClient.disconnect();
					enabled = false;
				} else {
					enabled = true;
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				enabled = false;
			}
		}
		this.enable = enabled;
		return enabled;
	}

	/**
	 * 同步文件到文件服务器
	 * 
	 * @param file
	 * @param writeHead
	 */
	public void syncFile(File file,boolean writeHead) {
		if(!enable) {
			logger.info("BanyanFtpServer--->日志服务器无法连接");
			return;
		}
		FTPClient ftpClient = new FTPClient();
		System.setProperty("ftp4j.activeDataTransfer.acceptTimeout", "30000");
		if (ftpProperties.isCustomPortRange()) {
			System.setProperty("ftp4j.activeDataTransfer.hostAddress", ftpProperties.getIp());
			System.setProperty("ftp4j.activeDataTransfer.portRange", ftpProperties.getPortRange());
		}
		try {
			ftpClient.connect(ftpProperties.getIp(), ftpProperties.getPort());
			if (!ftpProperties.isAnonymous()) {
				ftpClient.login(ftpProperties.getUser(), ftpProperties.getPassword());
			} else {
				ftpClient.login("anonymous", "ftp4j");
			}
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			if (!ftpClient.isConnected()) {
				ftpClient.disconnect();
				logger.info("BanyanFtpServer--->日志服务器无法连接");
				return;
			}
			//写入文件头信息, 1表示未上传, 0 表示已上传
			if(writeHead)
				BanyanFileUtils.writeHead(file.getAbsolutePath(), ByteBuffer.wrap("0\r\n".getBytes()));
			
			//服务器根目录
			getBaseDir();
			//上传目录
			Date date = new Date();
			String year = new SimpleDateFormat("yyyy").format(date);
			String month = new SimpleDateFormat("MMdd").format(date);
			String mac = InetAddressUtils.getMACAddress().replace("-", "");
			String[] dirs = new String[] { baseDir, year, month, mac };
			for (String dir : dirs) {
				try {
					ftpClient.makeDirectory(dir);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				ftpClient.changeWorkingDirectory(dir);
			}
			String ftpPath = "/" + baseDir + "/" + year + "/" + month + "/" + mac + "/";
			logger.info("上传文件:"+file.getName());
			logger.info("上传目录:" + ftpPath);
			ftpClient.changeWorkingDirectory(ftpPath);
			ftpClient.storeFile(file.getName(), new FileInputStream(file));
			ftpClient.disconnect();
			BanyanLoggerCollectorServerUtils.cacheFileMap.remove(file.getName());
		} catch (Exception e) {
			if(writeHead)
				BanyanFileUtils.writeHead(file.getAbsolutePath(), ByteBuffer.wrap("1\r\n".getBytes()));//写入文件头信息, 1表示未上传, 0 表示已上传
			logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 30秒同步一次文件
	 */
	public void syncFileThread() {
		scheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if(isEnabled()) {
					for (File file : BanyanLoggerCollectorServerUtils.cacheFileMap.values()) {
						String fileName = System.getProperty("banan.logger.file");
						if(!fileName.equals(file.getAbsolutePath())) {
							syncFile(file,true);
						}else {
							BanyanLoggerCollectorServerUtils.cacheFileMap.remove(file.getName());
						}
							
					}
				}
			}
		}, 100, 45000, TimeUnit.MILLISECONDS);
		
		//当前日志文件2分钟同步一次
		scheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if(isEnabled()) {
					String fileName = System.getProperty("banan.logger.file");
					syncFile(new File(fileName),false);						
				}
			}
		}, 100, 120000, TimeUnit.MILLISECONDS);
	}
}
