package com.tarpan.www.feature;

import com.tarpan.www.Constants;
import com.tarpan.www.util.FileUtil;
import com.tarpan.www.util.LogUtils;
import com.tarpan.www.util.RegexUtil;
import com.tarpan.www.util.StringUtil;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 检查类
 * @author sekift
 *
 */
public class Check {

	/**
	 * path to the final phrase,return empty phrase line number
	 * @param path
	 * @return
	 */
	public static List<Integer> checkoutPharse(String path) {
    	List<Integer> emptyList = new ArrayList<>();
		try{
		    int flag = 0;
		    int lineNO = 0;
			LineIterator lines = FileUtils.lineIterator(new File(path), Charsets.UTF_8.toString());
			while(lines.hasNext()){
				String line = lines.next().trim();
				if(!StringUtil.isNullOrBlank(line)){
					if("----------".equals(line)){
						lineNO += 1;
						if(flag == 1){
							emptyList.add(lineNO);
						}
						flag = 1;
					}else{
						flag = 0;
					}
				}
			}
		}catch(Exception e){
			LogUtils.logError("[情感分析]checkoutPharse出错，", e);
			e.printStackTrace();
		}
		LogUtils.logInfo("the length of empty phrase reviews is: "+emptyList.size());
		return emptyList;
    }
    
    public static void countPharse(String path) {
    	int len1 = 0,len2=0,len3=0,lenx=0;
		try{
			LineIterator lines = FileUtils.lineIterator(new File(path), Charsets.UTF_8.toString());
			while(lines.hasNext()){
				String line = lines.next().trim();
				if(!StringUtil.isNullOrBlank(line)){
					int num = line.split(" ").length;
					if(num == 1){
						len1+=1;
					}else if(num == 2){
						len2+=1;
					}else if(num == 3){
						len3+=1;
					}else{
						lenx+=1;
					}
				}
			}
		}catch(Exception e){
			LogUtils.logError("[情感分析]countPharse出错，", e);
			e.printStackTrace();
		}
		LogUtils.logInfo("the number of length 1 2 3 and more than three in a phrase is: "
		     + len1 + "," + len2 + "," + len3 + "," + lenx);
    }
    
    public static Set<String> findLabels(String path) {
    	Set<String> a = new HashSet<>();
    	//"./pos_phrase.txt"
		try{
			LineIterator lines = FileUtils.lineIterator(new File(path), Charsets.UTF_8.toString());
			while(lines.hasNext()){
				String line = lines.next().trim();
				int index = line.indexOf("#");
				if(index>-1){
					a.add(line.substring(index+1));
				}
			}
		}catch(Exception e){
			LogUtils.logError("[情感分析]checkoutPharse出错，", e);
			e.printStackTrace();
		}
		return a;
    }
    
    //read a file ,output  k,v
    public static void file2count(String path) {
    	Map<String, Integer> dict = new HashMap<>(128);
    	//"./pos_phrase.txt"
		try{
			LineIterator lines = FileUtils.lineIterator(new File(path), Charsets.UTF_8.toString());
			while(lines.hasNext()){
				String line = lines.next().trim();
				if(!StringUtil.isNullOrBlank(line)){
					if(dict.containsKey(line)){
						dict.put(line, dict.get(line) + 1);
					}else{
						dict.put(line, 1);
					}
				}
			}
		}catch(Exception e){
			LogUtils.logError("[情感分析]file2count出错，", e);
			e.printStackTrace();
		}

		LogUtils.logInfo(dict.size()+"");
    }

	public static void recordOOV(String[] oov) {
		try {
			for (String str : oov) {
				FileUtils.writeStringToFile(new File(FileUtil.getDataPath(
						Constants.OOV_FILE)), str + "   \n", Charsets.UTF_8, true);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		LogUtils.logDebug("the length of OOV is " + oov.length);
	}
	
	/**
	 * post-process the seged file
	 * todo : remove above method   
	 * @param inPath
	 * @param outPath
	 */
	public static void splitSegmented(String inPath, String outPath){
		// sentence length :80 tokens
		try{
			LineIterator lines = FileUtils.lineIterator(new File(inPath), Charsets.UTF_8.toString());
			while(lines.hasNext()){
				String line = lines.next().trim();
				if(!StringUtil.isNullOrBlank(line)){
					if("-- -- -- -- --".equals(line)){
						FileUtils.writeStringToFile(new File(outPath), line+"\n", 
								Charsets.UTF_8, true);
					}else{
						String[] li = line.split("。|．|！|？|\\?|!|\\.");
						for(String i : li){
							i = i.trim();
							if(!StringUtil.isNullOrBlank(i)){
								if(i.split(" ").length<81){
									FileUtils.writeStringToFile(new File(outPath), i+"\n", 
											Charsets.UTF_8, true);
								}else{
									
								}
							}
						}
					}
				}
			}
		}catch(Exception e){
			LogUtils.logError("[情感分析]splitSegmented出错，", e);
			e.printStackTrace();
		}
	}
	
	public static String statSentences(String path){
    	int cnt = 0, maxl = 1;
		try{
			LineIterator lines = FileUtils.lineIterator(new File(path), Charsets.UTF_8.toString());
			while(lines.hasNext()){
				String line = lines.next().trim();
				if(!StringUtil.isNullOrBlank(line)){
					if("-- -- -- -- --".equals(line)){
						int len = line.split(" ").length;
						if(len > maxl){
							maxl = len;
						}
						if(len > 80){
							LogUtils.logInfo("line = " + line);
							cnt+=1;
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return cnt + " " + maxl;
	}
	
	public static int statParsed(String path){
		int cnt = 0;
		try{
			LineIterator lines = FileUtils.lineIterator(new File(path), Charsets.UTF_8.toString());
			while(lines.hasNext()){
				String line = lines.next().trim();
				if(!StringUtil.isNullOrBlank(line)){
					if("".equals(line)){
						cnt+=1;
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return cnt;
	}
	
	
	public static void checkDICT(String path, String path2){
		Set<String> a = FileUtil.file2Set(path);
		LogUtils.logInfo("path length = "+a.size());
		
		Set<String> b = FileUtil.file2Set(path2);
		LogUtils.logInfo("path length = "+b.size());
		
		try{
			LineIterator lines = FileUtils.lineIterator(new File(path2), Charsets.UTF_8.toString());
			while(lines.hasNext()){
				String line = lines.next().trim();
				if(!StringUtil.isNullOrBlank(line) 
						&& !a.contains(line)){
					LogUtils.logInfo(line);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void diffTwoFile(String path1, String path2){
		Set<String> a = new HashSet<>();
		Set<String> b = new HashSet<>();
		try{
			LineIterator lines = FileUtils.lineIterator(new File(path1), Charsets.UTF_8.toString());
			while(lines.hasNext()){
				String line = lines.next().trim();
				if(!StringUtil.isNullOrBlank(line)){
					String li = line.split(" ")[0];
					if(a.contains(li)){
						LogUtils.logInfo(li);
					}
					a.add(li);
				}
			}
			
			LineIterator lines2 = FileUtils.lineIterator(new File(path2), Charsets.UTF_8.toString());
			while(lines2.hasNext()){
				String line = lines2.next().trim();
				if(!StringUtil.isNullOrBlank(line)){
					String li = line.split(" ")[0];
					b.add(li);
				}
			}
			LogUtils.logInfo(a.size()+" "+b.size());
			a.removeAll(b);
			LogUtils.logInfo("c = " + a);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public static void main(String args[]){
		String[] li = "分为非发。发违法.fawf、feewff we！分。为非!分".split("。|．|！|？|\\?|!|\\.");
		for(String str : li){
		System.out.println(str);
		}
	}
}
