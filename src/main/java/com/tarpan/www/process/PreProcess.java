package com.tarpan.www.process;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tarpan.www.Constants;
import com.tarpan.www.util.FileUtil;
import com.tarpan.www.util.LanguageUtil;
import com.tarpan.www.util.StringUtil;

/**
 * 数据预处理
 * @author sekift
 *
 */
public class PreProcess {

	private static Logger logger = LoggerFactory.getLogger(PreProcess.class);
	
	/**
	 * 
	 * 抽取ADV词性词语 
	 * @param path
	 * @return
	 */
	public static void extractADV(String path){
		Set<String> set = new HashSet<String>();
		try{
			LineIterator lines = FileUtils.lineIterator(new File(path), Charsets.UTF_8.toString());
			while(lines.hasNext()){
				String line = lines.next().trim();
				if(!StringUtil.isNullOrBlank(line)){
					String[] words = line.split(" ");
					if(words.length == 2){
						set.add(words[0]);
					}
				}
			}
			logger.info("the maybe adv has" + set.size());
			for(String str : set){
				FileUtils.writeStringToFile(new File(Constants.MAYBEADV_FILE), str, 
						Charsets.UTF_8, true);
			}
		}catch(Exception e){
			logger.error("[情感分析]抽取ADV出错，", e);
			e.printStackTrace();
		}
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
			logger.error("[情感分析]文件转set出错，", e);
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
			logger.error("[情感分析]文件转set出错，", e);
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * 预处理
	 * @param path
	 */
	public static void preProcess(String inPath, String outPath){
		List<String> emojiList = file2List(FileUtil.getDataPath(Constants.EMOJI_FILE));
		try{
			LineIterator lines = FileUtils.lineIterator(new File(inPath), Charsets.UTF_8.toString());
			while(lines.hasNext()){
				String line = lines.next().trim();
				if(!StringUtil.isNullOrBlank(line)){
					line = LanguageUtil.convertToGB2(line); // 中文繁转简
					for(String emoji : emojiList){
						String[] arr = emoji.split(Constants.EQUAL_SEP);
						line = line.replaceAll(arr[1], arr[0]); // 表情转汉字含义
						// TODO 是否作IV词性过滤
					}
				}
				FileUtils.writeStringToFile(new File(outPath), line, 
						Charsets.UTF_8, true);
			}
		}catch(Exception e){
			logger.error("[情感分析]文件转set出错，", e);
			e.printStackTrace();
		}
	}
	
	/**
	 * 加载分析词典
	 */
	public static Map<String, Integer> sentimentLoad(){
		Map<String, Integer> map = new HashMap<String, Integer>();
		Set<String> stopwordSet = file2Set(FileUtil.getDataPath(
				Constants.STOPWORD_FILE));
		try{
			LineIterator negLines = FileUtils.lineIterator(FileUtil.getDataFile(
					Constants.NEG_FILE), Charsets.UTF_8.toString());
			LineIterator posLines = FileUtils.lineIterator(FileUtil.getDataFile(
					Constants.POS_FILE), Charsets.UTF_8.toString());
			while(negLines.hasNext()){
				String lines = negLines.next().trim();
				if(!StringUtil.isNullOrBlank(lines)){
					map.put(lines, 1);
				}
			}
			
			while(posLines.hasNext()){
				String lines = posLines.next().trim();
				if(!StringUtil.isNullOrBlank(lines)){
					map.put(lines, 1);
				}
			}
			
			for(String sw : stopwordSet){
				if(map.containsKey(sw)){
					map.remove(sw);
				}
			}
		}catch(Exception e){
			logger.error("[情感分析]加载分析词典出错，", e);
			e.printStackTrace();
		}
		return map;
	}
	/**
	 * 获取分词(AA#BB)的词语
	 */
	public static String getWord(String str){
		return str.substring(0, str.indexOf("#"));
	}
	
	/**
	 * 获取分词(AA#BB)的词性
	 */
	public static String getLabel(String str){
		return str.substring(str.indexOf("#")+1, str.length());
	}
	
	/**
	 * 搜索list
	 * @param li
	 * @param ty
	 * @param str
	 * @return
	 */
	public static String searchList(List<String> list, String ty, String ele){
		String result = "";
		for(String str : list){
			if(str.startsWith(ty)
					&& str.indexOf(ele) != -1){
				result = str;
			}
		}
		return result;
	}
	
	/**
	 * 找出dobj或nsubj词语
	 * @param yList
	 * @param str
	 * @param i
	 * @param phraseList
	 * @param map
	 * @return
	 */
	public static String doNo(List<String> yList, String str, int i,
			List<String> phraseList, Map<String, Integer> map){
		String key = str.split("#")[0];
		String ele = searchList(yList, "dobj", key + "-" +(i+1));
		if(StringUtil.isNullOrBlank(ele)){
			ele = searchList(yList, "nsubj", key + "-" +(i+1));
		}
		
		if(!StringUtil.isNullOrBlank(ele)){
			List<String> pair = new ArrayList<String>();
			Matcher m = Pattern.compile(Constants.REGEX_ID.CHINESE).matcher(ele);
			while (m.find()) {
				pair.add(m.group());
			}
			if(pair.size() == 2){
				pair.remove(key);
				if(!map.containsKey(pair.get(0))){
					phraseList.add("没有");
					//int lb = i;
				}else{
                    // dobj all right; nsubj ? 
					String el = ele.split("\\,")[1];
					return el.substring(0, el.length() - 1);
				}
			}
		}
		
		return null;
	}
	
	
}
