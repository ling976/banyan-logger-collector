/**
 * 
 */
package com.banyan.logger.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 皇甫逸彬
 *
 */
public final class BanyanFileUtils {
	private static Logger logger = LoggerFactory.getLogger(BanyanFileUtils.class);

	/**
	 * 在项目目录下创建日志缓存文件夹
	 */
	public static String dirCache() {
		String path = System.getProperty("user.dir") + File.separator + "temp" + File.separator;
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
			//
			String sets = "attrib +H " + file.getAbsolutePath();
			try {
				Runtime.getRuntime().exec(sets);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return path;
	}

	/**
	 * 遍历文件夹
	 * 
	 * @param path
	 * @return
	 */
	public static List<File> listFiles(File file) {
		List<File> fileList = new ArrayList<File>();
		if (file.exists()) {
			File[] files = file.listFiles();
			if (null == files || files.length == 0) {
				return fileList;
			} else {
				for (File file2 : files) {
					if (file2.isDirectory()) {
						fileList.add(file2);
						fileList.addAll(listFiles(file2));
					} else {
						fileList.add(file2);
					}
				}
			}
		}
		return fileList;
	}

	/**
	 * 删除缓存文件
	 * 
	 * @param fileList
	 * @param maxHistory 默认为10天
	 */
	public static void clearHistory(List<File> fileList, int maxHistory) {
		long days = maxHistory * 24 * 60 * 60 * 1000;
		for (File file : fileList) {
			long time = file.lastModified();
			long current = System.currentTimeMillis();
			boolean del = ((current - time) > days);
			if (del) {
				logger.info("清除缓存日志文件-->" + file.getName());
				file.delete();
			}
		}
	}

	/**
	 * 
	 * @param fileName
	 * @param buffer
	 */
	public static void writeHead(String fileName, ByteBuffer buffer) {
		try {
			RandomAccessFile raf = new RandomAccessFile(new File(fileName), "rw");
			FileChannel channel = raf.getChannel();
			channel.write(buffer, 0);
			channel.force(true);
			raf.close();
			channel.close();
			raf = null;
			channel = null;
		} catch (Exception e) {
		}
	}
	
	public static void main(String[] args) {
		String path = "D:/vms/logs/temp/";
		File file = new File(path);
		if(!file.exists()) {
			file.mkdirs();
		}
		String sets = "attrib +H " + file.getAbsolutePath();
		try {
			Runtime.getRuntime().exec(sets);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
