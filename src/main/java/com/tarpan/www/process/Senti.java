package com.tarpan.www.process;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tarpan.www.util.StringUtil;

public class Senti {
	
	private static Logger logger = LoggerFactory.getLogger(FeaturePIPE.class);

	//load the sentiment lexicon
	public static Map<String, Double> loadSenti(String path){
		Map<String, Double> sentiDict = new HashMap<String, Double>();
		try {
			LineIterator lines = FileUtils.lineIterator(new File(path), Charsets.UTF_8.toString());
			while (lines.hasNext()) {
				String line = lines.next().trim();
				if(!StringUtil.isNullOrBlank(line)){
					String[] li = line.split(" ");
					if(li.length == 2){
						try{
							sentiDict.put(li[0], Double.parseDouble(li[1]));
						}catch(Exception e){
							logger.error("type error, not number = ", line);
						}
					}
				}
			}
		}catch(Exception e){
			
		}
		logger.info("Length of sentiment lexion in "+path+" is " +sentiDict.size());
		return sentiDict;
	}
	
	
}
