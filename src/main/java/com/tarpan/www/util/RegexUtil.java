package com.tarpan.www.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 这是个正则表达式应用类，用来匹配和替换字串用的
 *
 * @author
 * @version
 */

public class RegexUtil {

	/**
	 * 要求大小写都匹配正则表达式
	 *
	 * @param pattern
	 *            正则表达式模式
	 * @param str
	 *            要匹配的字串
	 * @return boolean值
	 * @since 1.0
	 */
	public static final boolean ereg(String pattern, String str) throws PatternSyntaxException {
		try {
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(str);
			return m.find();
		} catch (PatternSyntaxException e) {
			throw e;
		}
	}

	/**
	 * 匹配且替换字串
	 *
	 * @param pattern
	 *            正则表达式模式
	 * @param str
	 *            原始字串
	 * @param newstr
	 *            要替换匹配到的新字串
	 * @return 匹配后的字符串
	 * @since 1.0
	 */
	public static final String eregReplace(String pattern, String str, String newstr) throws PatternSyntaxException {
		try {
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(str);
			return m.replaceAll(newstr);
		} catch (PatternSyntaxException e) {
			throw e;
		}
	}

	public static final Object[] eregReplaceArray(String pattern, String str, String newstr) throws PatternSyntaxException {
		String m = eregReplace(pattern, str, newstr);
		String[] n = m.split(" ");
		List<String> list = new ArrayList<>();
		for(String s : n){
			if(!StringUtil.isNullOrBlank(s)){
				list.add(s.trim());
			}
		}
		return list.toArray();
	}

	/**
	 * 主要用于模板中模块标记分析函数 把查找到的元素加到vector中 vector已经不建议使用
	 *
	 * @param pattern 为正则表达式模式
	 * @param str
	 *            原始字串
	 * @return vector
	 * @since 1.0
	 */
	public static final Vector<String> splitTags2Vector(String pattern, String str) throws PatternSyntaxException {
		Vector<String> vector = new Vector<>();
		try {
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(str);
			while (m.find()) {
				vector.add(eregReplace("(\\[\\#)|(\\#\\])", m.group(), ""));
			}
			return vector;
		} catch (PatternSyntaxException e) {
			throw e;
		}
	}

	/**
	 * 模块标记分析函数 功能主要是把查找到的元素加到vector中
	 *
	 * @param pattern 为正则表达式模式
	 * @param str
	 *            原始字串
	 * @since 1.0
	 */
	public static final String[] splitTags(String pattern, String str) {
		try {
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(str);
			String[] array = new String[m.groupCount()];
			int i = 0;
			while (m.find()) {
				array[i] = eregReplace("(\\[\\#)|(\\#\\])", m.group(), "");
				i++;
			}
			return array;
		} catch (PatternSyntaxException e) {
			throw e;
		}
	}

	/**
	 * 匹配所有符合模式要求的字串并加到矢量vector数组中
	 *
	 * @param pattern 为正则表达式模式
	 * @param str
	 *            原始字串
	 * @return vector
	 * @since 1.0
	 */
	public static final Vector<String> regMatchAll2Vector(String pattern, String str) throws PatternSyntaxException {
		Vector<String> vector = new Vector<>();
		try {
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(str);
			while (m.find()) {
				vector.add(m.group());
			}
			return vector;
		} catch (PatternSyntaxException e) {
			throw e;
		}
	}

	/**
	 * 匹配所有符合模式要求的字串并加到字符串数组中
	 *
	 * @param pattern 为正则表达式模式
	 * @param str
	 *            原始字串
	 * @return array
	 * @since 1.0
	 */
	public static final String[] regMatchAll2Array(String pattern, String str) throws PatternSyntaxException {
		try {
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(str);
			String[] array = new String[m.groupCount()];
			int i = 0;
			while (m.find()) {
				array[i] = m.group();
				i++;
			}
			return array;
		} catch (PatternSyntaxException e) {
			throw e;
		}
	}

	/**
	 * 转义正则表达式字符(之所以需要将\和$字符用escapeDollarBackslash方法的方式是因为用repalceAll是不行的，简单的试试
	 * "$".repalceAll("\\$","\\\\$")你会发现这个调用会导致数组越界错误)
	 *
	 * @param original
	 * @return array
	 * @since 1.0
	 */
	public static String escapeDollarBackslash(String original) {
		StringBuffer buffer = new StringBuffer(original.length());
		for (int i = 0; i < original.length(); i++) {
			char c = original.charAt(i);
			if (c == '\\' || c == '$') {
				buffer.append("\\").append(c);
			} else {
				buffer.append(c);
			}
		}
		return buffer.toString();
	}

	/**
	 * 提取指定字串的函数 功能主要是把查找到的元素
	 *
	 * @param pattern 为正则表达式模式
	 * @param str
	 *            原始字串
	 * @since 1.0
	 */
	public static final String fetchStr(String pattern, String str) {
		String returnValue = null;
		try {
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(str);
			if (m.find()) {
				returnValue = m.group();
			}
			return returnValue;
		} catch (PatternSyntaxException e) {
			return returnValue;
		}
	}

	public static void main(String args[]) {
		String str = "在这里#NN分#VVfwaf 几乎#AD没fewf有#EE什么#DT合口味#NN";
		String pattern = "#\\w{1,3}";
		Object[] result = eregReplaceArray(pattern,str," ");
		System.out.println(result[1]);
		System.out.println("faea".substring(4));

		System.out.println(eregReplace(pattern,str,""));
	}
}