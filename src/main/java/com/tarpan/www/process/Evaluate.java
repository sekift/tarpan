package com.tarpan.www.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tarpan.www.util.StringUtil;

public class Evaluate {

	private static Logger logger = LoggerFactory.getLogger(Evaluate.class);

	public static double findSentiDropPoint(String sentence) {
		sentence = sentence.trim();
		if (!StringUtil.isNullOrBlank(sentence)) {
			String[] li = sentence.split("\\|");
			List<String> list = new ArrayList<String>();
			for(String s : li){
				list.add(s);
			}
			if (list.get(0) != null && list.get(0).equals("0")) {
				list.remove(0);
			}
			int las = list.size() - 1;
			if (list.get(las) != null && list.get(las).equals("0")) {
				list.remove(las);
			}

			if (list.isEmpty()) {
				return 0.0;
			}
			// there is a summary
			if (list.contains("s")) {
				int index = 0;
				for (int i = 0; i < list.size(); i++) {// find last 's'
					if ("s".equals(list.get(i))) {
						index = i;
					}
				}
				if (index == list.size() - 1) {// s in last position
					try {
						return Double.parseDouble(list.get(index - 1));
					} catch (Exception e) {
						logger.info("sentiment miss", sentence);
						return 0.0;
					}
				} else {
					return Double.parseDouble(list.get(index + 1));
				}
			} else {
				// case 2 begin and end
				if (list.size() == 1) {
					try {
						return Double.parseDouble(list.get(0));
					} catch (Exception e) {
						return 0.0;
					}
				}

				double begin = 0.0, end = 0.0;
				try {
					begin = Double.parseDouble(li[0]);
					end = Double.parseDouble(li[li.length-1]);
				} catch (Exception e) {
					logger.info("li[0]=" + li[0] + "li[li.length-1]=" + li[li.length-1]);
				}

				if (Math.abs(begin) > Math.abs(end)) {
					return begin;
				} else if (Math.abs(begin) < Math.abs(end)) {
					return end;
				} else {
					List<Double> absLi = new ArrayList<Double>();
					for (String str : list) {
						absLi.add(Math.abs(Double.parseDouble(str)));
					}
					double max = Collections.max(absLi);
					int ind = absLi.indexOf(max);
					if (ind == 0 || ind == list.size() - 1) {
						return begin;
					} else {
						return Double.parseDouble(list.get(ind));
					}
				}
			}

		}
		// because of no extraction sentiment
		return 0.0;
	}
	
	public static double commonSenti(String sentence) {
		double sum = 0;
		sentence = sentence.trim();
		if (!StringUtil.isNullOrBlank(sentence)) {
			String[] li = sentence.split("\\|");
			for (String str : li) {
				try {
					sum += Double.parseDouble(str);
				} catch (Exception e) {
					continue;
				}
			}
		}
		return sum;
	}
	
	public static int calOrientation(double strength) {
		if (strength > 0) {
			return 1;
		} else if (strength < 0) {
			return -1;
		} else {
			return 0;
		}
	}
	
	public static void main(String args[]){
		System.out.println(findSentiDropPoint("-1.0"));
		System.out.println(findSentiDropPoint("s|1.8|-5.85|0|s|1.0|0"));
		System.out.println(commonSenti("s|1.8|-5.85|0|s|1.0|0"));
	}
}
