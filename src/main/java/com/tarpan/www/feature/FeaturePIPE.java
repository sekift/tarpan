package com.tarpan.www.feature;

import com.tarpan.www.Constants;
import com.tarpan.www.util.FileUtil;
import com.tarpan.www.util.LogUtils;
import com.tarpan.www.util.StringUtil;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * make a distribution of the features
 * 
 * @author sekift
 *
 */
public class FeaturePIPE {
	private static Map<String, Integer> posD = FileUtil.file2Dic(FileUtil.getDataPath(Constants.POS_FILE));
	private static Map<String, Integer> negD = FileUtil.file2Dic(FileUtil.getDataPath(Constants.NEG_FILE));

	public static void count(String path, String out) {
		Map<String, Map<String, Integer>> distribution = new HashMap<>(128);
		try {
			LineIterator lines = FileUtils.lineIterator(new File(path), Charsets.UTF_8.toString());
			while (lines.hasNext()) {
				String line = lines.next().trim();
				String[] li = line.split("   ");
				String feature = li[0];
				String opinion = li[1];
				if (!StringUtil.isNullOrBlank(opinion)) {
					if (posD.containsKey(opinion)) {
						Map<String, Integer> map = new HashMap<>(1000);
						if (!distribution.containsKey(feature)) {
							map.put("pos", 1);
							map.put("neg", 0);
							distribution.put(feature, map);
						} else {
							map.put("pos", distribution.get(feature).get("pos") + 1);
							map.put("neg", distribution.get(feature).get("neg"));
							distribution.put(feature, map);
						}
					} else if (negD.containsKey(opinion)) {
						Map<String, Integer> map = new HashMap<>(1000);
						if (!distribution.containsKey(feature)) {
							map.put("pos", 0);
							map.put("neg", 1);
							distribution.put(feature, map);
						} else {
							map.put("pos", distribution.get(feature).get("pos"));
							map.put("neg", distribution.get(feature).get("neg") + 1);
							distribution.put(feature, map);
						}
					}
				}
			}

			for (String str : distribution.keySet()) {
				int posCnt = 0, negCnt = 0;
				if (null != distribution.get(str).get("pos")) {
					posCnt = distribution.get(str).get("pos");
				}
				if (null != distribution.get(str).get("neg")) {
					negCnt = distribution.get(str).get("neg");
				}
				int total = posCnt + negCnt;
				double posRatio = (double) posCnt / total;
				double negRatio = (double) negCnt / total;

				FileUtils.writeStringToFile(new File(out),
						str + ": " + "the positive ratio: " + posRatio + " , the negative ratio: " + negRatio + "\n",
						Charsets.UTF_8, true);

			}
		} catch (Exception e) {
			LogUtils.logError("[情感分析]count出错，", e);
			e.printStackTrace();
		}
	}

	private static Map<String, Map<String, Integer>> distribution = new HashMap<>(128);
	public static void countStream(String npop) {
		npop = npop.trim();
		String[] li = npop.split("   ");
		String feature = li[0];
		String opinion = li[1];
		if (!StringUtil.isNullOrBlank(opinion)) {
			if (posD.containsKey(opinion)) {
				Map<String, Integer> map = new HashMap<>(1000);
				if (!distribution.containsKey(feature)) {
					map.put("pos", 1);
					map.put("neg", 0);
					distribution.put(feature, map);
				} else {
					map.put("pos", distribution.get(feature).get("pos") + 1);
					map.put("neg", distribution.get(feature).get("neg"));
					distribution.put(feature, map);
				}
			} else if (negD.containsKey(opinion)) {
				Map<String, Integer> map = new HashMap<>(1000);
				if (!distribution.containsKey(feature)) {
					map.put("pos", 0);
					map.put("neg", 1);
					distribution.put(feature, map);
				} else {
					map.put("pos", distribution.get(feature).get("pos"));
					map.put("neg", distribution.get(feature).get("neg") + 1);
					distribution.put(feature, map);
				}
			}
		}
		System.out.println(distribution);
	}
	
	public static void showCount() {
		for (String str : distribution.keySet()) {
			int posCnt = 0, negCnt = 0;
			if (null != distribution.get(str).get("pos")) {
				posCnt = distribution.get(str).get("pos");
			}
			if (null != distribution.get(str).get("neg")) {
				negCnt = distribution.get(str).get("neg");
			}
			int total = posCnt + negCnt;
			double posRatio = (double) posCnt / total;
			double negRatio = (double) negCnt / total;
			LogUtils.logInfo(str + ": " + "the positive ratio: " + posRatio + " , the negative ratio: " + negRatio);

		}
	}

	public static void main(String args[]){
		String[] arr = {"并非   玉洁冰清","并非   无地自容", "你   好看", "我   好看","并非   去世"};
		for(String str : arr){
//		   countStream(str);
			count(str, "pipe.txt");
		}
//		showCount();
	}
}
