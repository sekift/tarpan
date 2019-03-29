package com.tarpan.www.util;

import java.io.File;
import java.net.URL;

public class FileUtil {
	
	
	/**
	 * 获取完整的路径
	 * @param fileName 文件名称
	 * @return String
	 */
	public static String getDataPath(String fileName) {
		URL resource = FileUtil.class.getResource(fileName);
		if (resource == null) {
			resource = FileUtil.class.getClassLoader().getResource(fileName);
			if (resource == null) {
				resource = ClassLoader.getSystemResource(fileName);
			}
		}
		return resource.getPath();
	}
	
	/**
	 * 获取完整的路径
	 * @param fileName 文件名称
	 * @return File
	 */
	public static File getDataFile(String fileName) {
		URL resource = FileUtil.class.getResource(fileName);
		if (resource == null) {
			resource = FileUtil.class.getClassLoader().getResource(fileName);
			if (resource == null) {
				resource = ClassLoader.getSystemResource(fileName);
			}
		}
		return new File(resource.getPath());
	}
}
