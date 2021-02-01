package com.tarpan.www.feature;

import java.util.ArrayList;
import java.util.List;

/**
 * according features(noun phrase) to find the corresponding opinion
 * @author sekift
 *
 */
public class NPfindOP {

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
