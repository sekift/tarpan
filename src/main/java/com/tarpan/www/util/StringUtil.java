package com.tarpan.www.util;

import com.tarpan.www.Constants;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 提供字符串相关处理的一些方法。
 */
public class StringUtil {

    /**
     * 解析出词语
     *
     * @param list
     * @return
     */
    public static List<String> getWord(List<String> list) {
        List<String> result = new ArrayList<>();
        for (String str : list) {
            String[] arrayStr = str.split(" ,");
            for (int j = 0; j < arrayStr.length; j++) {
                if(StringUtils.isEmpty(arrayStr[j])){
                    continue;
                }
                String[] array = arrayStr[j].split(Constants.TWO_WORD_SEP);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < array.length; i++) {
                    sb.append(array[i].split(Constants.WORD_SEG_SEP)[1]).append(" ");
                }
                result.add(sb.substring(0, sb.length() - 1));
            }
        }
        return result;
    }

    /**
     * 获取全部值
     *
     * @param str
     * @param idx
     * @return
     */
    public static String getValue(String str, Integer idx) {
        String result = "";
        str = str.split("%")[1];
        if (idx == 1) {
            result = str.split("&")[0];
        } else if (idx == 2) {
            result = str.split("&")[1];
        }
        return result;
    }

    /**
     * 获取依存关系的索引
     *
     * @param str 字符串
     * @param idx 第几个索引，1-前面一个，2-后面一个
     * @return
     */
    public static Integer getIndex(String str, Integer idx) {
        String result = "";
        str = str.split("%")[1];
        if (idx == 1) {
            result = str.split("&")[0].split("#")[0];
        } else if (idx == 2) {
            result = str.split("&")[1].split("#")[0];
        }
        return Integer.parseInt(result);
    }


    /**
     * 获取分词(AA#BB)的词语
     */
    public static String getWord(String str) {
        return str.substring(0, str.indexOf("#"));
    }

    /**
     * 获取分词(AA#BB)的词性
     */
    public static String getLabel(String str) {
        return str.substring(str.indexOf("#") + 1);
    }

    /**
     * 当 text 不为 null 且长度不为 0
     *
     * @param text String
     * @return boolean
     */
    public static boolean hasLength(String text) {
        return (text != null) && (text.length() > 0);
    }

    /**
     * text 不能为 null 且必须至少包含一个非空格的字符
     *
     * @param text String
     * @return boolean
     */
    public static boolean hasText(String text) {
        return hasLength(text) && Pattern.matches(".*\\S.*", text);
    }

    /**
     * 取得hash值，可能返回负数
     *
     * @param str
     * @return
     */
    @Deprecated
    public static long getDJBHash(String str) {
        long hash = 5381;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash << 5) + hash + str.charAt(i);
        }
        return hash;
    }

    /**
     * 字符串编码函数。
     *
     * @param str
     * @param targetCode
     * @return
     */
    public static String encodeStr(String str, String targetCode) {
        try {
            if (str == null) {
                return null;
            }
            byte[] bytesStr = str.getBytes();
            return new String(bytesStr, targetCode);
        } catch (Exception ex) {
            return str;
        }
    }

    /**
     * 字符串编码函数。
     *
     * @param str
     * @param srcCode
     * @param targetCode
     * @return
     */
    public static String encodeStr(String str, String srcCode, String targetCode) {
        try {
            if (str == null) {
                return null;
            }

            byte[] bytesStr = str.getBytes(srcCode);
            return new String(bytesStr, targetCode);
        } catch (Exception ex) {
            return str;
        }
    }

    /**
     * 判断字符串是否为空字符。
     *
     * @param value
     * @return
     */
    public static boolean isBlank(String value) {
        boolean ret = false;
        if (value != null && "".equals(value)) {
            ret = true;
        }
        return ret;
    }

    /**
     * 判断字符串是否为null。
     *
     * @param value
     * @return
     */
    public static boolean isNull(String value) {
        return value == null ? true : false;
    }

    /**
     * 判断字符串是否为空字符串或者null。
     *
     * @param value
     * @return
     */
    public static boolean isNullOrBlank(String value) {
        return isNull(value) || isBlank(value);
    }

    /**
     * 截取字符串前面部分字符,后面加省略号.
     *
     * @param str
     * @param length
     * @return
     */
    public static String trimWords(String str, int length) {
        String wordStr = str;

        if (wordStr == null) {
            return "";
        }
        if (wordStr.length() <= length) {
            return wordStr;
        }

        wordStr = wordStr.substring(0, length);
        wordStr += "...";
        return wordStr;
    }

    /**
     * 编码带有中文名称Url。
     *
     * @param url url中的中文
     * @return
     */
    public static String encodeUrl(String url) {
        return encodeUrl(url, "gbk");
    }

    /**
     * 编码带有中文名称Url。
     *
     * @param url        url中的中文
     * @param targetCode 目标字符
     * @return
     */
    public static String encodeUrl(String url, String targetCode) {
        String encodeUrl = "";
        if (StringUtil.isNullOrBlank(url)) {
            return "";
        }
        // 编码并转换空格
        try {
            encodeUrl = URLEncoder.encode(url, targetCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encodeUrl;
    }

    /**
     * 用指定的分隔符合并数组为单个字符串
     *
     * @param array     要合并的数组
     * @param separator 分隔符
     * @return
     */
    public static String joinArray(Object[] array, String separator) {
        if (array == null) {
            return "";
        }
        int startIndex = 0;
        int endIndex = array.length;
        int bufSize = endIndex - startIndex;
        if (bufSize <= 0) {
            return "";
        }

        bufSize *= (array[startIndex] == null ? 16 : array[startIndex].toString().length()) + 1;
        StringBuffer buf = new StringBuffer(bufSize);

        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    /**
     * 过滤内容中的标点符号、空白字符（包括全角空格）
     *
     * @param content 被过滤内容
     * @return 过滤后内容
     */
    public static String filterPunctuation(String content) {
        if (StringUtil.isNullOrBlank(content)) {
            return content;
        }
        // 过滤标点符号：!"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
        // 过滤空白字符：[ \t\n\x0B\f\r] 和 　全角空格
        return content.replaceAll("\\p{Punct}|\\p{Space}|　", "");
    }

    /**
     * get camel style string，eg. last_action_time -> lastActionTime
     *
     * @param str
     * @return
     */
    public static String getCamelCaseString(String str) {
        if (str == null) {
            return "";
        }
        str = str.toLowerCase();
        StringBuilder result = new StringBuilder(str.length());
        boolean toCapitalize = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9') {
                if (toCapitalize) {
                    result.append(Character.toUpperCase(c));
                    toCapitalize = false;
                } else {
                    result.append(c);
                }
            } else {
                toCapitalize = true;
            }
        }
        return result.toString();
    }

    /**
     * 将异常堆栈转换成字符串.
     *
     * @param t -- 异常
     * @return 返回异常堆栈的字符串表示
     */
    public static String getExceptionAsStr(Throwable t) {
        String exptStr = null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        exptStr = sw.toString();
        if (null != sw) {
            try {
                sw.close();
            } catch (Exception e) {
                /* 忽略异常 */
            }
        }
        if (null != pw) {
            pw.close();
        }
        return exptStr;
    }

    /**
     * 判断是否超长，超长截断加…符号
     *
     * @param content
     * @param len
     * @return
     */
    public static String subStringBylen(String content, Integer len) {
        String result = "";
        if (!StringUtil.isNullOrBlank(content)) {
            if (content.length() > len) {
                result = content.substring(0, len) + "…";
            } else {
                result = content;
            }
        }
        return result;
    }

    /**
     * 将数组按sp分隔组成字符串
     *
     * @param arr
     * @param sp
     * @return
     */
    public static String arrayToString(String[] arr, String sp) {
        StringBuilder result = new StringBuilder();
        if (arr.length < 1) {
            return "";
        }
        for (int i = 0; i < arr.length; i++) {
            result.append(arr[i]);
            if (i < arr.length - 1) {
                result.append(sp);
            }
        }

        return result.toString();
    }

    /**
     * 将数组的部分按sp分隔组成字符串
     *
     * @param arr
     * @param sp
     * @return
     */
    public static String arrayToString(String[] arr, String sp, int begin, int end) {
        StringBuilder result = new StringBuilder();
        if (arr.length < 1) {
            return "";
        }
        for (int i = begin; i < end; i++) {
            result.append(arr[i]);
            if (i < end - 1) {
                result.append(sp);
            }
        }
        return result.toString();
    }

    /**
     * 将list按sp分隔组成字符串
     *
     * @param list
     * @param sp
     * @return
     */
    public static String listToString(List<?> list, String sp) {
        StringBuilder result = new StringBuilder();
        int size = list.size();
        if (size < 1) {
            return "";
        }
        for (int i = 0; i < size; i++) {
            result.append(list.get(i));
            if (i < size - 1) {
                result.append(sp);
            }
        }

        return result.toString();
    }

    public static void main(String args[]) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        System.out.println(listToString(list, "="));
    }
}
