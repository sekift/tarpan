package com.tarpan.www.process.impl;

import com.tarpan.www.Constants;
import com.tarpan.www.pre.LoadFile;
import com.tarpan.www.process.SentimentProcess;
import com.tarpan.www.util.LogUtils;
import com.tarpan.www.util.RegexUtil;
import com.tarpan.www.util.StringUtil;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author sekift
 * @date 2019/02/07 14:00
 * @desc 旧式的情感处理逻辑
 */
public class GoopSentimentProcess implements SentimentProcess {

    public static Set<String> oov = new HashSet<>();

    /**保存否定词汇的list*/
    private static List<String> notList = Arrays.asList("不#AD", "没#VE", "没有#VE");

    /**
     * 寻找短语
     *
     * @param posed 带词性的句子，分词后
     * @param parsed 句法依存关系句子，分词后
     * @return
     */
    @Override
    public List<String> findPhrase(String posed, String parsed) {
        if (StringUtil.isNullOrBlank(posed.trim())) {
            return Arrays.asList("");
        }
        //正负面的词典
        Map<String, Double> negAndPos = LoadFile.getNegAndPos();
        //情感名词
        Set<String> sentiNN = LoadFile.getSentiNN();
        //情感动词
        Set<String> sentiVV = LoadFile.getSentiVV();
        //情感副词
        Set<String> sentiAD = LoadFile.getSentiAD();
        //总结性词语
        List<String> summary = LoadFile.getSummary();
        //指示性词语
        Map<String, String> aspect = LoadFile.getAspect();
        //有歧义的词语
        List<String> ambiguity = LoadFile.getAmbiguity();

        // 用于保存阶段性结果
        List<String> phraseList = new ArrayList<>();
        // 用于保存结果
        List<String> resultList = new ArrayList<>();
        // 用于保存带有否定的词语，带词性，例如“不”、“没有”、“没”时
        List<String> negSenti = new ArrayList<>();
        // 用于保存带有否定的词语，例如“不”时
        List<String> noSenti = new ArrayList<>();

        // 例子：
        // 设施#NN 还#AD 将#AD 就#P ,#PU 但#AD 服务#NN 是#VC 相当#AD 的#DEV 不#AD 到位#VV ,#PU
        // 休息#VV 了#AS 一#CD 个#M 晚上#NT 我#PN 白天#NT 出去#VV ,
        // #PU 中午#NT 回来#VV 的#DEC 时候#NN 居然#AD 房间#NN 都#AD 没有#VE 整理#VV ,#PU
        // 尽管#CS 我#PN 挂#VV 了#AS 要求#NN 整理#VV 房间#NN 的#DEC 牌子#NN .#PU
        String[] posedArray = posed.trim().split(" ");

        // 例子：
        //root(ROOT-0, 休息-14)   nsubj(休息-14, 设施-1)   advmod(休息-14, 还-2)   advmod(休息-14, 将-3)
        // case(到位-12, 就-4)   punct(到位-12, ,-5)   advmod(到位-12, 但-6)   nsubj(到位-12, 服务-7)
        // cop(到位-12, 是-8)   dep(到位-12, 相当-9)   mark(相当-9, 的-10)   neg(到位-12, 不-11)
        // nmod:prep(休息-14, 到位-12)   punct(休息-14, ,-13)   aux:asp(休息-14, 了-15)   nummod(晚上-18, 一-16)
        // mark:clf(一-16, 个-17)   nmod:topic(出去-21, 晚上-18)   nsubj(出去-21, 我-19)   dep(出去-21, 白天-20)
        // dep(回来-24, 出去-21)   punct(出去-21, ,-22)   dobj(回来-24, 中午-23)   acl(时候-26, 回来-24)
        // mark(回来-24, 的-25)   nmod:topic(整理-31, 时候-26)   advmod(房间-28, 居然-27)   nsubj(整理-31, 房间-28)
        // advmod(整理-31, 都-29)   dep(整理-31, 没有-30)   ccomp(休息-14, 整理-31)   punct(休息-14, ,-32)
        // advmod(挂-35, 尽管-33)   nsubj(挂-35, 我-34)   conj(休息-14, 挂-35)   aux:asp(挂-35, 了-36)
        // nsubj(整理-38, 要求-37)   acl(牌子-41, 整理-38)   dobj(整理-38, 房间-39)   mark(整理-38, 的-40)
        // dobj(挂-35, 牌子-41)   punct(休息-14, .-42)"
        String[] parsedArray = parsed.trim().split("   ");
        // 下标 lower bound, record the wrote position
        int lb = 0;
        for (int i = 0; i < posedArray.length; i++) {
            //System.out.println(i+" : "+list[i]+" : "+yList[i]);
            String currentWord = posedArray[i];
            String seger = StringUtil.getWord(currentWord);
            String label = StringUtil.getLabel(currentWord);

            // 如果是总结性词语
            if (summary.contains(seger)) {
                phraseList.add("SUM");
                lb = i;
            } else if (notList.contains(currentWord)) {
                // 如果是否定词语
                String ret = doNo(parsedArray, currentWord, i, phraseList, negAndPos);
                if (!StringUtil.isNullOrBlank(ret)) {
                    negSenti.add(ret);
                }
            } else if (negAndPos.containsKey(seger)) {
                /** 进入到正负面的分析*/
                // VA - 谓语形容词
                if ("VA".equals(label)) {
                    if (i == 0) {
                        phraseList.add(posedArray[0] + "-1");
                        lb = i;
                    } else {
                        String pLabel = StringUtil.getLabel(posedArray[i - 1]);
                        // DEV-表示方式状语的“地” DEG-所有格/联结作用“的”
                        boolean flag = i > 1 && ("DEV".equals(pLabel) || "DEG".equals(pLabel));
                        if (flag) {
                            phraseList.add(posedArray[i - 2] + posedArray[i - 1] + currentWord);
                            lb = i;
                        } else if ("AD".equals(pLabel)) {
                            int ind = i - 1;
                            try {
                                for (int j = i - 2; j > -1; j--) {
                                    if ("AD".equals(StringUtil.getLabel(posedArray[j]))) {
                                        ind = j;
                                    } else {
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                LogUtils.logError("out of range.");
                            }
                            if ((ind == (i - 1)) && i > 2) {
                                if ("VC".equals(StringUtil.getLabel(posedArray[i - 2]))
                                        && "AD".equals(StringUtil.getLabel(posedArray[i - 3]))) {
                                    phraseList.add(posedArray[i - 3] + posedArray[i - 2] + posedArray[i - 1] + currentWord);
                                    lb = i;
                                } else {
                                    phraseList.add(posedArray[i - 1] + currentWord);
                                    lb = i;
                                }
                            } else {
                                if (ind <= lb) {
                                    //avoid repeated extraction
                                    ind = lb + 1;
                                }
                                String temp = "";
                                for (int j = ind; j < i + 1; j++) {
                                    temp += posedArray[j];
                                }
                                phraseList.add(temp);
                                lb = i;
                            }
                        } else {
                            phraseList.add(currentWord + '-' + (i + 1));
                            lb = i;
                        }
                    }
                }

                // NN - 普通名词
                if ("NN".equals(label)) {
                    // 跳过无情感词语
                    if (sentiNN.contains(seger)) {
                        continue;
                    }

                    if (i == 0) {
                        phraseList.add(posedArray[0] + "-1");
                        lb = i;
                    } else {
                        String p_label = StringUtil.getLabel(posedArray[i - 1]);
                        List<String> tempList = Arrays.asList("AD", "JJ", "VE", "CD");
                        if (tempList.contains(p_label)) {
                            // VE: 有/没有;CD:一点点 -- //most use of lb
                            if (lb != i - 1) {
                                phraseList.add(posedArray[i - 1] + currentWord);
                                lb = i;
                            }
                        } else if ("DT".equals(p_label) && i > 1) {
                            phraseList.add(findADorVE(posedArray[i - 2] + posedArray[i - 1] + currentWord));
                            lb = i;
                        } else {
                            phraseList.add(currentWord + "-" + (i + 1));
                            lb = i;
                        }
                    }
                }

                if ("VV".equals(label)) {
                    if (sentiVV.contains(seger)) {
                        continue;
                    }
                    if (i == 0) {
                        phraseList.add(posedArray[0] + "-1");
                        lb = i;
                    } else {
                        String p_label = StringUtil.getLabel(posedArray[i - 1]);
                        List<String> tempList = Arrays.asList("AD", "PN");
                        if (tempList.contains(p_label)) {
                            phraseList.add(posedArray[i - 1] + currentWord);
                            lb = i;
                        } else {
                            phraseList.add(currentWord + "-" + (i + 1));
                            lb = i;
                        }
                    }
                }

                if ("AD".equals(label)) {
                    if (sentiAD.contains(seger)) {
                        continue;
                    }
                    if (i == 0) {
                        phraseList.add(currentWord + '-' + (i + 1));
                        lb = i;
                    } else {
                        String p_label = StringUtil.getLabel(posedArray[i - 1]);
                        if ("AD".equals(p_label)) {
                            int ind = i - 1;
                            try {
                                for (int j = i - 2; j > -1; j--) {
                                    if ("AD".equals(StringUtil.getLabel(posedArray[j]))) {
                                        ind = j;
                                    } else {
                                        break;
                                    }
                                }
                            } catch (Exception e) {

                            }
                            if ("重".equals(seger)
                                    && ("再".equals(StringUtil.getWord(posedArray[i - 1]))
                                    || "往复".equals(StringUtil.getWord(posedArray[i - 1])))) {
                                continue;
                            } else {
                                if (ind <= lb) {
                                    ind = lb + 1;
                                }
                                String temp = "";
                                for (int j = ind; j < i + 1; j++) {
                                    temp += posedArray[j];
                                }
                                phraseList.add(temp);
                                lb = i;
                            }
                        } else {
                            phraseList.add(currentWord + '-' + (i + 1));
                            lb = i;
                        }
                    }
                }

                // 感叹词：interjection
                if ("IJ".equals(label)) {
                    phraseList.add(currentWord);
                    lb = i;
                }

                if ("JJ".equals(label)) {
                    LogUtils.logInfo(seger + " J;J " + label);

                    // 处理有歧义的词语
                    if (ambiguity.contains(seger)) {
                        String jjj = searchList(parsedArray, "amod", seger + "-" + (i + 1));
                        if (!StringUtil.isNullOrBlank(jjj)) {
                            Object[] m = RegexUtil.eregReplaceArray("[^\u4e00-\u9fa5]", jjj, " ");
                            if (m.length > 0) {
                                if (m.length == 2 && !negAndPos.containsKey(m[0])) {
                                    String temp = "";
                                    for (Object s : m) {
                                        temp += s + " ";
                                    }
                                    temp = temp.trim();
                                    if (aspect.containsKey(temp)) {
                                        phraseList.add(aspect.get(temp) + "");
                                        lb = i;
                                    } else {
                                        //default
                                        phraseList.add(currentWord);
                                    }
                                }
                            }
                        }
                    } else {
                        phraseList.add(currentWord);
                        lb = i;
                    }
                }

                if ("CD".equals(label)) {
                    phraseList.add(currentWord);
                    lb = i;
                }
            } else {
                if ("VV".equals(label)) {
                    //TODO
                    try {
                        if ("不#AD会#VV再#AD".equals(posedArray[i - 3] + posedArray[i - 2] + posedArray[i - 1])) {
                            phraseList.add("-4");
                            // add a const
                            lb = i;
                        }
                    } catch (Exception e) {

                    }
                }

                if ("不".equals(seger)) {
                    String ele = searchList(parsedArray, "neg", "不-" + (i + 1));
                    if (!StringUtil.isNullOrBlank(ele)) {
                        String ele1 = ele.split(",")[0];
                        if (ele1.length() >= 4) {
                            noSenti.add(ele1.substring(4));
                        }
                    }
                }
            }
        }

        LogUtils.logInfo("phraseList= " + phraseList);

        for (String p : phraseList) {
            String p1 = RegexUtil.eregReplace("#\\w{1,3}", p, "");
            if (negSenti.contains(p1)) {
                resultList.add("shift   " + p.split("-")[0]);
            } else if (noSenti.contains(p1)) {
                resultList.add("shift   " + p.split("-")[0]);
            } else {
                if (p.startsWith("-")) {
                    resultList.add(p);
                } else {
                    resultList.add(p.split("-")[0]);
                }
            }
        }
        return resultList;
    }

    /**
     * 文件的形式
     *
     * @param taggedFile
     * @param phraseFile
     */
    public static void findPhrase1(String taggedFile, String phraseFile) {
        Map<String, Double> dict = LoadFile.getNegAndPos();
        try {
            LineIterator lines = FileUtils.lineIterator(new File(taggedFile), Charsets.UTF_8.toString());
            while (lines.hasNext()) {
                //List<String> phraseList = new ArrayList<String>();
                String line = lines.next().trim();
                //a line from taggedFILE
                if (!StringUtil.isNullOrBlank(line)) {
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
                        String seger = StringUtil.getWord(list[i]);
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

    @Override
    public List<String> filterPhrase(List<String> phraseList) {
        Map<String, Double> dict = LoadFile.getNegAndPos();
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

    /**
     * 应用短语来计算得分
     *
     * @param finalPhs
     * @return
     */
    @Override
    public String calAll(List<String> finalPhs) {
        StringBuilder sb = new StringBuilder();
        if (null == finalPhs || finalPhs.size() == 0) {
            return "0.0";
        }

        Map<String, Double> sentiDict = LoadFile.getSentiment();
        Map<String, Double> nonLinear = LoadFile.getNonLinear();
        Map<String, Double> advxxx = LoadFile.getAdvxxx();

        for (String line : finalPhs) {
            if ("SUM".equals(line)) {
                sb.append("s").append("|");
            } else {
                double strength = calPhraseStrength(sentiDict, nonLinear, line, advxxx);
                sb.append(strength + "").append("|");
            }
        }
        return sb.substring(0, sb.length() - 1);
    }

    public static double calPhraseStrength(Map<String, Double> sentiDict, Map<String, Double> nonLinear, String phrase,
                                           Map<String, Double> advDict) {
        double strength = 0.0;
        if (StringUtil.isNullOrBlank(phrase)) {
            return strength;
        }

        phrase = phrase.trim().replace("   ", " ");
        String[] li = phrase.split(" ");
        int len = li.length;
        //System.out.println("len: " + len );
        if (len == 1) {
            if (null != sentiDict.get(li[0])) {
                strength = sentiDict.get(li[0]);
            } else {
                oov.add(li[0]);
                strength = 0.0;
            }
        } else if (null != nonLinear.get(StringUtil.arrayToString(li, " "))) {
            strength = nonLinear.get(StringUtil.arrayToString(li, " "));
        } else if (len == 2) {
            boolean flag = false;
            if (null != sentiDict.get(li[1])) {
                strength = sentiDict.get(li[1]);
                flag = true;
            } else {
                oov.add(li[1]);
                strength = 0.0;
            }

            LogUtils.logInfo("strength2: " + strength);
            if ("shift".equals(li[0]) && flag) {
                strength = strength > 0.0 ? strength - Constants.SHIFT_VALUE : strength + Constants.SHIFT_VALUE;
            } else if ("不太".equals(li[0]) && flag) {
                strength = strength > 0.0 ? strength - Constants.BUTAI_VALUE : strength + Constants.BUTAI_VALUE;
            } else if (advDict.containsKey(li[0])) {
                LogUtils.logInfo("strength3: " + advDict.get(li[0]) + " * " + strength);
                strength *= advDict.get(li[0]);
            }
        } else if (len == 3) {
            if (null != sentiDict.get(li[2])) {
                strength = sentiDict.get(li[2]);
            } else {
                oov.add(li[2]);
                strength = 0.0;
            }
            if (advDict.containsKey(li[1])) {
                LogUtils.logInfo("strength4: " + advDict.get(li[1]) + "*" + strength);
                strength *= advDict.get(li[1]);
            }
            List<String> tempList = Arrays.asList("shift", "没", "没有");
            if (tempList.contains(li[0])) {
                strength = strength > 0.0 ? strength - Constants.SHIFT_VALUE : strength + Constants.SHIFT_VALUE;
            } else {
                if (advDict.containsKey(li[0])) {
                    LogUtils.logInfo("strength5: " + advDict.get(li[0]) + " * " + strength);
                    strength *= advDict.get(li[0]);
                }
            }
        } else {
            if (null != sentiDict.get(li[len - 1])) {
                strength = sentiDict.get(li[len - 1]);
            } else {
                oov.add(li[len - 1]);
                strength = 0.0;
            }
            for (int i = len - 2; i > -1; i--) {
                if (advDict.containsKey(li[i])) {
                    LogUtils.logInfo("strength6: " + advDict.get(li[i]) + " * " + strength);
                    strength *= advDict.get(li[i]);
                }
            }
        }
        strength = ((int) (strength * 100)) / 100.0;
        return strength;
    }

    /**
     * 搜索list，找出和一个词在用一个依存关系的另一个词
     * 找到一个就返回
     * @param parsedArray
     * @param ty   开头
     * @param ele  包括
     * @return
     */
    public static String searchList(String[] parsedArray, String ty, String ele) {
        String result = "";
        for (String str : parsedArray) {
            if (str.startsWith(ty) && str.indexOf(ele) != -1) {
                result = str;
                break;
            }
        }
        return result;
    }

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
     * 找出dobj或nsubj中被否定的词语
     *
     * @param parsedArray
     * @param currentWord
     * @param index
     * @param phraseList
     * @param negAndPos
     * @return
     */
    public static String doNo(String[] parsedArray, String currentWord, int index,
                              List<String> phraseList, Map<String, Double> negAndPos) {
        String seger = currentWord.split("#")[0];
        //直接宾语
        String ele = searchList(parsedArray, "dobj", seger + "-" + (index + 1));
        if (StringUtil.isNullOrBlank(ele)) {
            //名词主语
            ele = searchList(parsedArray, "nsubj", seger + "-" + (index + 1));
        }

        if (StringUtil.isNullOrBlank(ele)) {
            return null;
        }
        List<String> pair = new ArrayList<>();
        Matcher m = Pattern.compile(Constants.REGEX_ID.CHINESE).matcher(ele);
        while (m.find()) {
            pair.add(m.group());
        }
        if (pair.size() == 2) {
            pair.remove(seger);
            if (!negAndPos.containsKey(pair.get(0))) {
                phraseList.add("没有");
                //int lb = i;
            } else {
                // dobj all right; nsubj ?
                String el = ele.split("\\,")[1];
                return el.substring(0, el.length() - 1);
            }
        }
        return null;
    }
}
