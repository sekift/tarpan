package com.tarpan.www.feature;

import com.tarpan.www.Constants;
import com.tarpan.www.util.LogUtils;
import com.tarpan.www.util.StringUtil;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * according features(noun phrase) to find the corresponding opinion
 * @author sekift
 *
 */
public class NPfindOP {

	/**
	 * 抽取ADV词性词语
	 *
	 * @param path
	 * @return
	 */
	public static void extractADV(String path) {
		Set<String> set = new HashSet<>();
		try {
			LineIterator lines = FileUtils.lineIterator(new File(path), Charsets.UTF_8.toString());
			while (lines.hasNext()) {
				String line = lines.next().trim();
				if (!StringUtil.isNullOrBlank(line)) {
					String[] words = line.split(" ");
					if (words.length == 2) {
						set.add(words[0]);
					}
				}
			}
			LogUtils.logInfo("the maybe adv has" + set.size());
			for (String str : set) {
				FileUtils.writeStringToFile(new File(Constants.MAYBEADV_FILE), str,
						Charsets.UTF_8, true);
			}
		} catch (Exception e) {
			LogUtils.logError("[情感分析]抽取ADV出错，", e);
			e.printStackTrace();
		}
	}


	/**
	 * extract two key words from a dependency
	 * 从依存关系提取关键字
	 * @param dep
	 * @param order
	 * @return
	 */
    public static List<String> extractDEP(String dep, int order){
    	String[] li1 = dep.split("(");
    	String content = li1[1];
    	String[] li2 = content.split(" ");
    	String ele1 = li2[0].split("-")[0];
    	String ele2 = li2[1].split("-")[0];
    	List<String> list = new ArrayList<>();
    	if(order > 0){
    		list.add(ele2);
    		list.add(ele1);
    		return list;
    	}
		list.add(ele1);
		list.add(ele2);
		return list;
    }
    
    //this method needs a special Chinese parser.
    
    // param: the POSed line
    // load the frequent feature
    public static void opinionSearch3(String str1){
    	
    }
    
}
