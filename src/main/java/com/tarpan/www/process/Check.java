package com.tarpan.www.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tarpan.www.Constants;
import com.tarpan.www.util.FileUtil;
import com.tarpan.www.util.RegexUtil;
import com.tarpan.www.util.StringUtil;

/**
 * 检查类
 * @author sekift
 *
 */
public class Check {
	
    private static Logger logger = LoggerFactory.getLogger(Check.class);
    
    // path to the final phrase,return empty phrase line number
    public static List<Integer> checkoutPharse(String path) {
    	List<Integer> emptyList = new ArrayList<Integer>();
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
			logger.error("[情感分析]checkoutPharse出错，", e);
			e.printStackTrace();
		}
		logger.info("the length of empty phrase reviews is: "+emptyList.size());
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
			logger.error("[情感分析]countPharse出错，", e);
			e.printStackTrace();
		}
		logger.info("the number of length 1 2 3 and more than three in a phrase is: " 
		     + len1 + "," + len2 + "," + len3 + "," + lenx);
    }
    
    public static Set<String> findLabels(String path) {
    	Set<String> a = new HashSet<String>();
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
			logger.error("[情感分析]checkoutPharse出错，", e);
			e.printStackTrace();
		}
		return a;
    }
    
    //read a file ,output  k,v
    public static void file2count(String path) {
    	Map<String, Integer> dict = new HashMap<String, Integer>();
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
			logger.error("[情感分析]file2count出错，", e);
			e.printStackTrace();
		}
		
		logger.info(dict.size()+"");
    }
    
	public static void recordOOV(String[] oov) {
		for (String str : oov) {
			try {
				FileUtils.writeStringToFile(new File(FileUtil.getDataPath(
						Constants.OOV_FILE)), str + "   \n", Charsets.UTF_8, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.debug("the length of OOV is " + oov.length);
	}
    
	/**
	 * 处理ADVS词性的词
	 * @param line
	 * @return
	 */
	public static String processADVS(String line){
	    String[] li=line.split(" ");
	    if(li.length == 3){
	    	if("不太".equals(li[0]+""+li[1])){
	    		return "不太 "+li[2]+" "; // minus -5
	    	}
	    }
	    return line + " ";
	}
	
	/**
	 * 
	 * 查找AD或VE词性后面的句子
	 * 输入：在这里#NN分#VV几乎#AD没有#VE什么#DT合口味#NN
	 * 输出：
	 * m 几乎#AD没有#VE什么#DT合口味#NN
	 * m1 没有#VE什么#DT合口味#NN
	 * @param path
	 * @return
	 */
	public static String findADorVE(String phrase){
		String[] li = phrase.split("#PU");
		if(li.length == 2){
			phrase = li[1];
		}
		String m = RegexUtil.fetchStr("[\u4e00-\u9fa5]+#AD.*", phrase);
		String m1 = RegexUtil.fetchStr("[\u4e00-\u9fa5]+#VE.*", phrase);
		if(!StringUtil.isNullOrBlank(m)){
			phrase = m;
		}else if(!StringUtil.isNullOrBlank(m1)){
			phrase = m1;
		}
		return phrase;
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
			logger.error("[情感分析]splitSegmented出错，", e);
			e.printStackTrace();
		}
	}
	
	public static Map<String, String> loadAspectsenti(String inPath){
    	Map<String, String> dic = new HashMap<String, String>();
		try{
			LineIterator lines = FileUtils.lineIterator(new File(inPath), Charsets.UTF_8.toString());
			while(lines.hasNext()){
				String line = lines.next().trim();
				if(!StringUtil.isNullOrBlank(line)){
					String[] li = line.split(" ");
					if(li.length == 3){
						String li02 = li[0]+" "+li[1];
						if(dic.containsKey(li02)){
							continue;
						}else{
							dic.put(li02, li[2]);
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return dic;
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
							logger.info("line = ", line);
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
		logger.info("path length = "+a.size());
		
		Set<String> b = FileUtil.file2Set(path2);
		logger.info("path length = "+b.size());
		
		try{
			LineIterator lines = FileUtils.lineIterator(new File(path2), Charsets.UTF_8.toString());
			while(lines.hasNext()){
				String line = lines.next().trim();
				if(!StringUtil.isNullOrBlank(line) 
						&& !a.contains(line)){
					logger.info(line);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void diffTwoFile(String path1, String path2){
		Set<String> a = new HashSet<String>();
		Set<String> b = new HashSet<String>();
		try{
			LineIterator lines = FileUtils.lineIterator(new File(path1), Charsets.UTF_8.toString());
			while(lines.hasNext()){
				String line = lines.next().trim();
				if(!StringUtil.isNullOrBlank(line)){
					String li = line.split(" ")[0];
					if(a.contains(li)){
						logger.info(li);
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
			logger.info(a.size()+" "+b.size());
			a.removeAll(b);
			logger.info("c = " + a);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public static void main(String args[]){
		System.out.println(findADorVE("在这里#NN分#VV几乎#AD没有#VE什么#DT合口味#NN"));
		String[] li = "分为非发。发违法.fawf、feewff we！分。为非!分".split("。|．|！|？|\\?|!|\\.");
		for(String str : li){
		System.out.println(str);
		}
	}
}
