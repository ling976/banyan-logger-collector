/**
 * 
 */
package com.banyan.logger.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.banyan.logger.server.BanyanLoggerCollectorServer;

/**
 * @author 皇甫逸彬
 *
 */
public class BanyanLoggerCollectorServerUtils {
	/**
	 * banyanLoggerCollectorServer
	 */
	public static BanyanLoggerCollectorServer banyanLoggerCollectorServer;
	
	public static Map<String, File> cacheFileMap = new HashMap<String, File>();
}
