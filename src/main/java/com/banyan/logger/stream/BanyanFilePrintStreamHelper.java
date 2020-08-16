/**
 * 
 */
package com.banyan.logger.stream;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.springframework.util.StringUtils;

import com.banyan.logger.ftp.BanyanFtpServer;
import com.banyan.logger.properties.BanyanStarterProperties;
import com.banyan.logger.utils.BanyanFileNameUtils;
import com.banyan.logger.utils.BanyanFileUtils;

import ch.qos.logback.core.util.CachingDateFormatter;

/**
 * 将控制台输出内容写入文件中
 * 
 * @author 皇甫逸彬
 *
 */
public class BanyanFilePrintStreamHelper {
	/**
	 * 日志文件大小,默认为10M
	 */
	private static long MAX_SIZE = 1024 * 1024 * 10;
	/**
	 * 日志文件配置
	 */
	private BanyanStarterProperties properties;
	/**
	 * 日志文件操作
	 */
	private BanyanFtpServer banyanFtpServer;

	/**
	 * 写文件的FileChannel
	 */
	private RandomAccessFile raf;
	private FileChannel channel;
	/**
	 * 当上传文件的时候写日志操作暂停,此时新的日志信息暂时保存到buff中,当前文件上传完成,将其写入到新日志文件中
	 */
	private ByteBuffer byteBuffer;
	/**
	 * 上传文件的时候写日志操作暂停
	 */
	private boolean pause = false;
	/**
	 * 当前日志文件
	 */
	private String fileName = "";
	/**
	 * 缓存当前日期
	 */
	private String lastDate = "";
	
	public BanyanFilePrintStreamHelper(BanyanStarterProperties properties) {
		this.properties = properties;
		String size = properties.getMaxFileSize();
		if (!StringUtils.isEmpty(size)) {
			try {
				if (size.endsWith("G") || size.endsWith("GB") || size.endsWith("g") || size.endsWith("gb")) {
					MAX_SIZE = 1024 * 1024 * 10;
				} else if (size.endsWith("M") || size.endsWith("MB") || size.endsWith("m") || size.endsWith("mb")) {
					//日志文件最大为50m
					if(Integer.parseInt(size)>50) {
						MAX_SIZE = 1024 * 1024 * 10;
					}else {
						MAX_SIZE = 1024 * 1024 * Integer.parseInt(size);
					}
						
				} else if (size.endsWith("KB") || size.endsWith("KB") || size.endsWith("k") || size.endsWith("kb")) {
					MAX_SIZE = 1024 * Integer.parseInt(size);
				}
				
			} catch (Exception e) {
				//这里没有对配置数据进行校验,有可能parseInt报错,这里出错不处理,采用默认值
			}
		}
		byteBuffer = ByteBuffer.allocate(255);
	}

	public void setBanyanFtpServer(BanyanFtpServer banyanFtpServer) {
		this.banyanFtpServer = banyanFtpServer;
	}

	/**
	 * 是否启用文件服务器
	 * 
	 * @return
	 */
	public boolean ftpEnabled() {
		return banyanFtpServer.isEnabled();
	}

	/**
	 * 写文件操作
	 * 
	 * @param buff
	 */
	public void write(ByteBuffer buff) {
		if (canWrite()) {
			writeData(buff);
		}else {
			this.byteBuffer.put(buff);
		}
	}
	/**
	 * writeData
	 * @param buff
	 */
	private void writeData(ByteBuffer buff) {
		try {
			raf.seek(raf.length());
			channel.write(buff);
			channel.force(true);
			boolean bool = new CachingDateFormatter("yyyy-MM-dd").format(System.currentTimeMillis()).equals(lastDate);
			if (new File(fileName).length() > MAX_SIZE || !bool) {
				stop();
				banyanFtpServer.syncFile(new File(fileName));
				start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 停止写文件
	 */
	public void stop() {
		pause = true;
		try {
			if (canWrite()) {
				channel.close();
				raf.close();
				channel = null;
				raf = null;
			}
		} catch (Exception e) {
		}
	}

	/**
	 * start
	 */
	public void start() {
		fileName = BanyanFileNameUtils.getFileName(properties.getFileNamePattern());
		lastDate = new CachingDateFormatter("yyyy-MM-dd").format(System.currentTimeMillis());
		try {
			// 写入文件头信息, 1表示未上传, 0 表示已上传
			writeHead("1");
			//打开文件
			raf = new RandomAccessFile(new File(fileName), "rw");
			raf.seek(raf.length());
			channel = raf.getChannel();
			pause = false;
			writeCache();
		} catch (Exception e) {
		}
	}

	/**
	 * 服务器停止
	 */
	public void destory() {
		banyanFtpServer.syncFile(new File(fileName));
		stop();
	}

	private boolean canWrite() {
		return channel != null && channel.isOpen() && !pause;
	}
	
	/**
	 * 写入文件头信息, 1表示未上传, 0 表示已上传
	 * @param head
	 */
	private void writeHead(String head) {
		BanyanFileUtils.writeHead(fileName, ByteBuffer.wrap(head.getBytes()));
	}
	/**
	 * writeCache
	 */
	private void writeCache() {
		try {
			if (byteBuffer.position() > 0) {
				channel.write(byteBuffer);
				byteBuffer.clear();
				raf.seek(raf.length());
			}
		} catch (Exception e) {
		}
	}
}
