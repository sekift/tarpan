package com.tarpan.www.process;

import com.tarpan.www.Constants;
import com.tarpan.www.util.*;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据预处理
 *
 * @author sekift
 */
public class PreProcess {
    /**
     * 处理ADVS词性的词
     *
     * @param line
     * @return
     */
    public static String processADVS(String line) {
        String[] li = line.split(" ");
        if (li.length == 3) {
            if ("不太".equals(li[0] + "" + li[1])) {
                // minus -5
                return "不太 " + li[2] + " ";
            }
        }
        return line + " ";
    }

    /**
     * 查找AD或VE词性后面的句子
     * 输入：在这里#NN分#VV几乎#AD没有#VE什么#DT合口味#NN
     * 输出：
     * m 几乎#AD没有#VE什么#DT合口味#NN
     * m1 没有#VE什么#DT合口味#NN
     *
     * @param phrase
     * @return
     */
    public static String findADorVE(String phrase) {
        String[] li = phrase.split("#PU");
        if (li.length == 2) {
            phrase = li[1];
        }
        String m = RegexUtil.fetchStr("[\u4e00-\u9fa5]+#AD.*", phrase);
        String m1 = RegexUtil.fetchStr("[\u4e00-\u9fa5]+#VE.*", phrase);
        if (!StringUtil.isNullOrBlank(m)) {
            phrase = m;
        } else if (!StringUtil.isNullOrBlank(m1)) {
            phrase = m1;
        }
        return phrase;
    }

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
     * 文件的预处理
     *
     * @param inPath
     * @param outPath
     */
    public static void preProcess(String inPath, String outPath) {
        List<String> emojiList = FileUtil.file2List(FileUtil.getDataPath(Constants.EMOJI_FILE));
        List<String> ivList = FileUtil.file2List(FileUtil.getDataPath(Constants.IV_FILE));
        try {
            LineIterator lines = FileUtils.lineIterator(new File(inPath), Charsets.UTF_8.toString());
            while (lines.hasNext()) {
                String line = lines.next().trim();
                if (!StringUtil.isNullOrBlank(line)) {
                    // 中文繁转简
                    line = LanguageUtil.convertToGB2(line);
                    for (String emoji : emojiList) {
                        // 表情转汉字含义
                        String[] arr = emoji.split(Constants.EQUAL_SEP);
                        line = line.replaceAll(arr[1], arr[0]);
                    }
                    //remove intensional verb and something unsure
                    // TODO 是否作IV词性过滤
                    String lineCopy = line;
                    lineCopy = lineCopy.replace("。", "\n").replace("，", "\n").replace(",", "\n");
                    String[] copyArray = lineCopy.split("\n");
                    for (int i = 0; i < copyArray.length; i++) {
                        for (int j = 0; j < ivList.size(); j++) {
                            if (copyArray[i].contains(ivList.get(j))) {
                                line = line.replace(copyArray[i], " ");
                            }
                        }
                    }
                }
                FileUtils.writeStringToFile(new File(outPath), line,
                        Charsets.UTF_8, true);
            }
        } catch (Exception e) {
            LogUtils.logError("[情感分析]文件的预处理出错，", e);
            e.printStackTrace();
        }
    }

    /**
     * 加载分析词典
     */
    public static Map<String, Integer> sentiment() {
        Map<String, Integer> map = new HashMap<>(10000);
        try {
            Set<String> stopwordSet = FileUtil.file2Set(FileUtil.getDataPath(
                    Constants.STOPWORD_FILE));
            LineIterator negLines = FileUtils.lineIterator(FileUtil.getDataFile(
                    Constants.NEG_FILE), Charsets.UTF_8.toString());
            LineIterator posLines = FileUtils.lineIterator(FileUtil.getDataFile(
                    Constants.POS_FILE), Charsets.UTF_8.toString());
            while (negLines.hasNext()) {
                String lines = negLines.next().trim();
                if (!StringUtil.isNullOrBlank(lines)) {
                    map.put(lines, -1);
                }
            }

            while (posLines.hasNext()) {
                String lines = posLines.next().trim();
                if (!StringUtil.isNullOrBlank(lines)) {
                    map.put(lines, 1);
                }
            }

            for (String sw : stopwordSet) {
                if (map.containsKey(sw)) {
                    map.remove(sw);
                }
            }
        } catch (Exception e) {
            LogUtils.logError("[情感分析]加载分析词典出错，", e);
            e.printStackTrace();
        }
        return map;
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
     * 搜索list
     *
     * @param list
     * @param ty   开头
     * @param ele  包括
     * @return
     */
    public static String searchList(String[] list, String ty, String ele) {
        String result = "";
        for (String str : list) {
            if (str.startsWith(ty) && str.indexOf(ele) != -1) {
                result = str;
                break;
            }
        }
        return result;
    }

    /**
     * 找出dobj或nsubj词语
     *
     * @param yList
     * @param str
     * @param i
     * @param phraseList
     * @param dict
     * @return
     */
    public static String doNo(String[] yList, String str, int i,
                              List<String> phraseList, Map<String, Integer> dict) {
        String key = str.split("#")[0];
        String ele = searchList(yList, "dobj", key + "-" + (i + 1));
        if (StringUtil.isNullOrBlank(ele)) {
            ele = searchList(yList, "nsubj", key + "-" + (i + 1));
        }

        if (!StringUtil.isNullOrBlank(ele)) {
            List<String> pair = new ArrayList<>();
            Matcher m = Pattern.compile(Constants.REGEX_ID.CHINESE).matcher(ele);
            while (m.find()) {
                pair.add(m.group());
            }
            if (pair.size() == 2) {
                pair.remove(key);
                if (!dict.containsKey(pair.get(0))) {
                    phraseList.add("没有");
                    //int lb = i;
                } else {
                    // dobj all right; nsubj ? 
                    String el = ele.split("\\,")[1];
                    return el.substring(0, el.length() - 1);
                }
            }
        }

        return null;
    }

    /**
     * 寻找短语
     *
     * @param dict
     * @param nnSet
     * @param vvSet
     * @param adSet
     * @param sumList
     * @param aspect
     * @param am
     * @param line
     * @param y
     * @return
     */
    public static List<String> findPhrase(Map<String, Integer> dict, Set<String> nnSet,
                                          Set<String> vvSet, Set<String> adSet, List<String> sumList,
                                          Map<String, String> aspect, List<String> am,
                                          String line, String y) {
        List<String> phraseList = new ArrayList<>();
        List<String> phraseList2 = new ArrayList<>();
        //do queue
        List<String> farSenti = new ArrayList<>();
        //for not
        List<String> farSenti2 = new ArrayList<>();
        line = line.trim();
        y = y.trim();
        if (!StringUtil.isNullOrBlank(line)) {
            String[] list = line.split(" ");
            String[] yList = y.split("   ");
            int lb = 0; //lowerbound, record the wrote position
            for (int i = 0; i < list.length; i++) {
                //System.out.println(i+" : "+list[i]+" : "+yList[i]);
                String seger = getWord(list[i]);
                String label = getLabel(list[i]);

                if (sumList.contains(seger)) {
                    phraseList.add("SUM");
                    lb = i;
                } else if (list[i] == "没有#VE" || list[i] == "没#VE"
                        || list[i] == "不#AD") {
                    String ret = doNo(yList, list[i], i, phraseList, dict);
                    if (!StringUtil.isNullOrBlank(ret)) {
                        farSenti.add(ret);
                    }
                } else if (dict.containsKey(seger)) {
                    if ("VA".equals(label)) {
                        if (i > 0) {
                            String p_label = getLabel(list[i - 1]);
                            if (("DEV".equals(p_label) || "DEG".equals(p_label))
                                    && i > 1) {
                                phraseList.add(list[i - 2] + list[i - 1] + list[i]);
                                lb = i;
                            } else if ("AD".equals(p_label)) {
                                int ind = i - 1;
                                try {
                                    for (int j = i - 2; j > -1; j--) {
                                        if ("AD".equals(getLabel(list[j]))) {
                                            ind = j;
                                        } else {
                                            break;
                                        }
                                    }
                                } catch (Exception e) {
                                    LogUtils.logError("out of range.");
                                }
                                if ((ind == (i - 1)) && i > 2) {
                                    if ("VC".equals(getLabel(list[i - 2]))
                                            && "AD".equals(getLabel(list[i - 3]))) {
                                        phraseList.add(list[i - 3] + list[i - 2] + list[i - 1] + list[i]);
                                        lb = i;
                                    } else {
                                        phraseList.add(list[i - 1] + list[i]);
                                        lb = i;
                                    }
                                } else {
                                    if (ind <= lb) {
                                        ind = lb + 1; //avoid repeated extraction
                                    }
                                    String temp = "";
                                    for (int j = ind; j < i + 1; j++) {
                                        temp += list[j];
                                    }
                                    phraseList.add(temp);
                                    lb = i;
                                }
                            } else {
                                phraseList.add(list[i] + '-' + (i + 1));
                                lb = i;
                            }
                        } else {
                            phraseList.add(list[0] + "-1");
                            lb = i;
                        }
                    }

                    if ("NN".equals(label)) {
                        if (nnSet.contains(seger)) { // skip the zero strength noun
                            continue;
                        }

                        if (i > 0) {
                            String p_label = getLabel(list[i - 1]);
                            List<String> tempList = Arrays.asList("AD", "JJ", "VE", "CD");
                            if (tempList.contains(p_label)) {
                                // VE: 有/没有;CD:一点点
                                if (lb != i - 1) { //most use of lb
                                    phraseList.add(list[i - 1] + list[i]);
                                    lb = i;
                                }
                            } else if ("DT".equals(p_label) && i > 1) {
                                phraseList.add(findADorVE(list[i - 2] + list[i - 1] + list[i]));
                                lb = i;
                            } else {
                                phraseList.add(list[i] + "-" + (i + 1));
                                lb = i;
                            }
                        } else {
                            phraseList.add(list[0] + "-1");
                            lb = i;
                        }
                    }

                    if ("VV".equals(label)) {
                        if (vvSet.contains(seger)) {
                            continue;
                        }
                        if (i > 0) {
                            String p_label = getLabel(list[i - 1]);
                            List<String> tempList = Arrays.asList("AD", "PN");
                            if (tempList.contains(p_label)) {
                                phraseList.add(list[i - 1] + list[i]);
                                lb = i;
                            } else {
                                phraseList.add(list[i] + "-" + (i + 1));
                                lb = i;
                            }
                        } else {
                            phraseList.add(list[0] + "-1");
                            lb = i;
                        }
                    }

                    if ("AD".equals(label)) {
                        if (adSet.contains(seger)) {
                            continue;
                        }
                        if (i > 0) {
                            String p_label = getLabel(list[i - 1]);
                            if ("AD".equals(p_label)) {
                                int ind = i - 1;
                                try {
                                    for (int j = i - 2; j > -1; j--) {
                                        if ("AD".equals(getLabel(list[j]))) {
                                            ind = j;
                                        } else {
                                            break;
                                        }
                                    }
                                } catch (Exception e) {

                                }
                                if ("重".equals(seger)
                                        && ("再".equals(getWord(list[i - 1]))
                                        || "往复".equals(getWord(list[i - 1])))) {
                                    continue;
                                } else {
                                    if (ind <= lb) {
                                        ind = lb + 1;
                                    }
                                    String temp = "";
                                    for (int j = ind; j < i + 1; j++) {
                                        temp += list[j];
                                    }
                                    phraseList.add(temp);
                                    lb = i;
                                }
                            } else {
                                phraseList.add(list[i] + '-' + (i + 1));
                                lb = i;
                            }
                        } else {
                            phraseList.add(list[i] + '-' + (i + 1));
                            lb = i;
                        }
                    }

                    // interjection
                    if ("IJ".equals(label)) {
                        phraseList.add(list[i]);
                        lb = i;
                    }

                    if ("JJ".equals(label)) {
                        System.out.println(seger + " J;J " + label);

                        if (am.contains(seger)) { //handler  ambiguity
                            String jjj = searchList(yList, "amod", seger + "-" + (i + 1));
                            if (!StringUtil.isNullOrBlank(jjj)) {
                                Object[] m = RegexUtil.eregReplaceArray("[^\u4e00-\u9fa5]", jjj, " ");
                                if (null != m && m.length > 0) {
                                    if (m.length == 2 && !dict.containsKey(m[0])) {
                                        String temp = "";
                                        for (Object s : m) {
                                            temp += (Object) s + " ";
                                        }
                                        temp = temp.trim();
                                        if (aspect.containsKey(temp)) {
                                            phraseList.add(aspect.get(temp) + "");
                                            lb = i;
                                        } else {
                                            //default
                                            phraseList.add(list[i]);
                                        }
                                    }
                                }
                            }
                        } else {
                            phraseList.add(list[i]);
                            lb = i;
                        }
                    }

                    if ("CD".equals(label)) {
                        phraseList.add(list[i]);
                        lb = i;
                    }
                } else {
                    if ("VV".equals(label)) {
                        //TODO
                        try {
                            if ("不#AD会#VV再#AD".equals(list[i - 3] + list[i - 2] + list[i - 1])) {
                                phraseList.add("-4");
                                lb = i; // add a const
                            }
                        } catch (Exception e) {

                        }
                    }

                    if ("不".equals(seger)) {
                        String ele = searchList(yList, "neg", "不-" + (i + 1));
                        if (!StringUtil.isNullOrBlank(ele)) {
                            String ele1 = ele.split(",")[0];
                            if (ele1.length() >= 4) {
                                farSenti2.add(ele1.substring(4));
                            }
                        }
                    }
                }
            }

            for (String p : phraseList) {
                String p1 = RegexUtil.eregReplace("#\\w{1,3}", p, "");
                if (farSenti.contains(p1)) {
                    phraseList2.add("shift   " + p.split("-")[0]);
                } else if (farSenti2.contains(p1)) {
                    phraseList2.add("shift   " + p.split("-")[0]);
                } else {
                    if (p.startsWith("-")) {
                        phraseList2.add(p);
                    } else {
                        phraseList2.add(p.split("-")[0]);
                    }
                }
            }
        }
        return phraseList2;
    }

    public static void findPhrase1(String taggedFile, String phraseFile) {
        Map<String, Integer> dict = sentiment();
        try {
            LineIterator lines = FileUtils.lineIterator(new File(taggedFile), Charsets.UTF_8.toString());
            while (lines.hasNext()) {
                //List<String> phraseList = new ArrayList<String>();
                String line = lines.next().trim();
                if (!StringUtil.isNullOrBlank(line)) { //a line from taggedFILE
                    //if line =='----------#NN':  ## NN
                    //if line =='--#PU --#PU --#PU --#PU --#PU': ## for ctb segment
                    if ("--#NN --#NN --#NN --#NN --#NN".equals(line)) {
                        FileUtils.writeStringToFile(new File(phraseFile), "----------\n",
                                Charsets.UTF_8, true);
                        continue;
                    }
                    String[] list = line.split(" ");
                    //lowerbound, record the wrote position
                    for (int i = 0; i < list.length; i++) {
                        String seger = getWord(list[i]);
                        if (dict.containsKey(seger)) {
                            FileUtils.writeStringToFile(new File(phraseFile), list[i] + "\n",
                                    Charsets.UTF_8, true);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.logError("[情感分析]findPhrase1出错，", e);
            e.printStackTrace();
        }
    }

    public static List<String> filterPhrase(List<String> phraseList) {
        Map<String, Integer> dict = sentiment();
        List<String> finalPH = new ArrayList<>();
        for (String line : phraseList) {
            if ("SUM".equals(line)) {
                finalPH.add("SUM");
                // for const sentiment
            } else if (line.startsWith("+") || line.startsWith("-")) {
                finalPH.add(line);
            } else {
                String[] li = line.split("#");
                int len = li.length;
                if (len == 1) {
                    finalPH.add(li[0]);
                } else if (len == 2) {
                    finalPH.add(li[0]);
                } else if (len == 3) {
                    if ("VA".equals(li[2]) && (li[1].startsWith("NN") || li[1].startsWith("VV"))
                            && !dict.containsKey(li[0])) {
                        finalPH.add(li[1].substring(2));
                    } else if (li[1].startsWith("PU")) {
                        finalPH.add(li[1].substring(2));
                    } else {
                        finalPH.add(RegexUtil.eregReplace("#\\w{1,3}", line, "   "));
                    }
                } else if (len == 4) {
                    //VE:有/没有
                    if (li[1].startsWith("VE")) {
                        if ("没有".equals(li[0]) || "没".equals(li[0])) {
                            List<String> list = new ArrayList<>();
                            for (String s : li) {
                                list.add(s);
                            }
                            list.remove(1);
                            li = list.toArray(new String[1]);
                            finalPH.add(RegexUtil.eregReplace("\\w{1,3}",
                                    StringUtil.arrayToString(li, "   "), ""));
                        } else {

                        }
                    } else if (li[1].startsWith("AD")) {
                        if (li[2].startsWith("DEV")) {
                            finalPH.add(li[0] + "   " + li[2].substring(3));
                        } else {
                            //System.out.println("line2===="+line);
                            List<String> tempList = Arrays.asList("都", "就", "却", "还是");
                            if (tempList.contains(li[0])) {
                                finalPH.add(RegexUtil.eregReplace("\\w{1,3}",
                                        li[1] + "   " + li[2], ""));
                            } else {
                                //System.out.println("line3===="+li[1]+"   "+li[2]);
                                String temp = RegexUtil.eregReplace("\\w{1,3}",
                                        StringUtil.arrayToString(li, "   "), "");
                                finalPH.add(processADVS(temp));
                            }
                        }
                    } else {
                        if (li[2].startsWith("DT")) {
                            finalPH.add(RegexUtil.eregReplace("\\w{1,3}",
                                    li[1] + "   " + li[2], ""));
                        } else {
                            finalPH.add(RegexUtil.eregReplace("\\w{1,3}",
                                    li[2], ""));
                        }
                    }
                } else {
                    if (len == 5) {
                        if (li[2].startsWith("VC")) {
                            if ("不".equals(li[0])) {
                                String temp = "";
                                for (int j = 2; j < li.length; j++) {
                                    temp += li[j] + "   ";
                                }
                                temp = temp.trim();
                                finalPH.add("shift   " + RegexUtil.eregReplace("\\w{1,3}",
                                        temp, ""));
                            } else {
                                finalPH.add(RegexUtil.eregReplace("\\w{1,3}",
                                        li[0] + "   " + li[2] + "   " + li[3], ""));
                            }
                        } else {
                            finalPH.add(RegexUtil.eregReplace("\\w{1,3}",
                                    StringUtil.arrayToString(li, "   "), ""));
                        }
                    } else {
                        finalPH.add(RegexUtil.eregReplace("\\w{1,3}",
                                StringUtil.arrayToString(li, "   "), ""));
                    }
                }
            }
        }
        return finalPH;
    }

}
