package com.tarpan.www.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tarpan.www.util.RegexUtil;
import com.tarpan.www.util.StringUtil;

/**
 * 
 * @author sekift
 *
 */
public class Check {
	
    private static Logger logger = LoggerFactory.getLogger(Check.class);
	
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
	 * 处理ADVS词性的词
	 * @param line
	 * @return
	 */
	public static String processADVS(String line){
	    String[] li=line.split(" ");
	    if(li.length == 3){
	    	if("不太".equals(li[0]+""+li[1])){
	    		return "不太 "+li[2]+"\n"; // minus -5
	    	}
	    }
	    return line + "\n";
	}
	
	public static void main(String args[]){
		System.out.println(findADorVE("在这里#NN分#VV几乎#AD没有#VE什么#DT合口味#NN"));
	}
}
