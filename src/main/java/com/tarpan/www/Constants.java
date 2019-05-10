package com.tarpan.www;

/**
 * 常量值
 * @author sekift
 *
 */
public class Constants {

	public static final String PRE = "/data/";
	public static final String PRO = ".txt";
	
	public static final String IV_FILE = PRE + "iv" + PRO;
	public static final String MAYBEADV_FILE = PRE + "maybeADV" + PRO;
	public static final String EMOJI_FILE = PRE + "emoji" + PRO;
	public static final String STOPWORD_FILE = PRE + "stopword" + PRO;
	public static final String NEG_FILE = PRE + "neg" + PRO;
	public static final String POS_FILE = PRE + "pos" + PRO;
	public static final String OOV_FILE = PRE + "oov" + PRO;
	public static final String ADV_FILE = PRE + "advxxx" + PRO;
	public static final String SENTI_FILE = PRE + "sentiment2" + PRO;
	public static final String NONLI_FILE = PRE + "nonlinear" + PRO;
	public static final String SENTINN_FILE = PRE + "sentiNN" + PRO;
	public static final String SENTIVV_FILE = PRE + "sentiVV" + PRO;
	public static final String SENTIAD_FILE = PRE + "sentiAD" + PRO;
	public static final String SUMMARY_FILE = PRE + "summary" + PRO;
	public static final String ASPECT_FILE = PRE + "aspectDICT" + PRO;
	public static final String AM_FILE = PRE + "ambiguity" + PRO;
	

	// 分割符号
	public static final String EQUAL_SEP = "====";
	
	// shift 情感的分值
	public static final double SHIFT_VALUE = 4.0;
	// 不太 情感的分值
	public static final double BUTAI_VALUE = 5.0;
	
	/**
	 * 匹配
	 * 
	 */
	public static final class REGEX_ID {
		// EMAIL
		public static final String EMAIL = "[\\w[.-]]+@+[\\w[.-]]+\\.[a-z]{2,4}+";
		// PHONE
		public static final String PHONE = "^(((13[0-9])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$";
		// IPHONE
		public static final String IPHONE = "\\b1[358]\\d{9}\\b";
		// URL
		public static final String URL = "http://(([a-zA-z0-9]|-){1,}\\.){1,}[a-zA-z0-9]{1,}-*";
		// 非负整数
		public static final String NONNEGATIVEINTEGER = "^\\d+$";
		// 正整数
		public static final String POSITIVEINTEGER = "^[0-9]*[1-9][0-9]*$";
		// 非正整数
		public static final String NONPOSITIVEINTEGER = "^-[1-9]\\d*|0$";
		// 负整数
		public static final String NEGATIVEINTEGER = "^-[0-9]*[1-9][0-9]*$";
		// 整数
		public static final String INTEGER = "^-?\\d+$";
		// 非负浮点数
		public static final String NONNEGATIVEFLOAT = "^\\d+(\\.\\d+)?$";
		// 正浮点数
		public static final String POSITIVEFLOAT = "^(([0-9]+\\.[0-9]*[1-9][0-9]*)?([0-9]*[1-9][0-9]*\\.[0-9]+)?([0-9]*[1-9][0-9]*))$";
		// 非正浮点数
		public static final String NONPOSITVEFLOAT = "^((-\\d+(\\.\\d+)?)?(0+(\\.0+)?))$";
		// 负浮点数
		public static final String NEGATIVEFLOAT = "^(-(([0-9]+\\.[0-9]*[1-9][0-9]*)?([0-9]*[1-9][0-9]*\\.[0-9]+)?([0-9]*[1-9][0-9]*)))$";
		// 浮点数
		public static final String FLOAT = "^(-?\\d+)(\\.\\d+)?$";
		// 字母
		public static final String ALPHABET = "^[a-zA-Z]+$";
		// 大写字母
		public static final String ALPHABETUPPER = "^[A-Z]+$";
		// 小写字母
		public static final String ALPHABETLOWER = "^[a-z]+$";
		// 字母加数字
		public static final String ALPHABETNUMBER = "^[a-zA-Z0-9]+$";
		// 字母加数字加下划线
		public static final String ALPHABETNUMBERLINE = "^\\w+$";
		// 中文
		public static final String CHINESE = "[\u4e00-\u9fa5]+$";
		// 非中文
		public static final String NONCHINESE = "[^\u4e00-\u9fa5]";
		// 空行
		public static final String BLANKLINE = "\\n[\\s ?]*\\r";
		// HTML(有错)
		// public static final String HTML="<(\\S*?)[^>]*>.*? </\\1> | <.*? />";
		// QQ
		public static final String QQ = "[1-9][0-9]{4,9}";
		// 身份证
		public static final String IDCARD = "\\d{15}?\\d{18}";
		// IP
		public static final String IP = "\\d+\\.\\d+\\.\\d+\\.\\d+";
		// 所有符号(http://www.unicode.org/reports/tr18/)
		public static final String PUNCTUATION = "[\\pP|\\pM|\\pZ|\\pS|\\pC|\\pL|\\pN]";
		// P：标点字符。
		public static final String PUNCTUATION_P = "[\\pP]";
		// L Letter：字母（包括中文）
		public static final String PUNCTUATION_L = "[\\pL]";
		// M Mark：标记符号（一般不会单独出现）；
		public static final String PUNCTUATION_M = "[\\pM]";
		// Z Separator：分隔符（比如空格、换行等）；
		public static final String PUNCTUATION_Z = "[\\pZ]";
		// S Symbol：符号（比如数学符号、货币符号等）；
		public static final String PUNCTUATION_S = "[\\pS]";
		// N number：数字（比如阿拉伯数字、罗马数字等）；
		public static final String PUNCTUATION_N = "[\\pN]";
		// C other：其他字符
		public static final String PUNCTUATION_C = "[\\pC]";
	}

	public static final class String_ID {
		// allchar
		public static final String ALLCHAR = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		// char
		public static final String CHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		// number
		public static final String NUMBERCHAR = "0123456789";
	}
}
