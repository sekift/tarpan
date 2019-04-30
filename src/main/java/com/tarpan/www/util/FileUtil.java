package com.tarpan.www.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

/**
 * 文件操作类
 * @author sekift
 *
 */
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
	
	/**
	 * 文件转set
	 * @param path
	 * @return
	 */
	public static Set<String> file2Set(String path) {
		Set<String> set = new HashSet<String>();
		try{
			LineIterator lines = FileUtils.lineIterator(new File(path), Charsets.UTF_8.toString());
			while(lines.hasNext()){
				String line = lines.next().trim();
				if(!StringUtil.isNullOrBlank(line)){
					set.add(line);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return set;
	}
	
	/**
	 * 文件转list
	 * @param path
	 * @return
	 */
	public static List<String> file2List(String path) {
		List<String> list = new ArrayList<String>();
		try{
			LineIterator lines = FileUtils.lineIterator(new File(path), Charsets.UTF_8.toString());
			while(lines.hasNext()){
				String line = lines.next().trim();
				if(!StringUtil.isNullOrBlank(line)){
					list.add(line);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return list;
	}
	
    /**
     * 文件转map，行为key，值为1
     * @param path
     * @return
     */
    public static Map<String, Integer> file2Dic(String path){
		Map<String, Integer> map = new HashMap<String, Integer>();
		try{
			LineIterator lines = FileUtils.lineIterator(new File(path), Charsets.UTF_8.toString());
			while(lines.hasNext()){
				String line = lines.next().trim();
				if(!StringUtil.isNullOrBlank(line)){
					map.put(line, 1);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return map;
    }
    
    /**
     * 文件转map，行为key，值为1
     * @param path
     * @return
     */
    public static Map<String, String> file2DicStr(String path){
		Map<String, String> map = new HashMap<String, String>();
		try{
			LineIterator lines = FileUtils.lineIterator(new File(path), Charsets.UTF_8.toString());
			while(lines.hasNext()){
				String line = lines.next().trim();
				if(!StringUtil.isNullOrBlank(line)){
					map.put(line.split(" ")[0]+line.split(" ")[0], line.split(" ")[2]);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return map;
    }
}
