package com.tarpan.www.process;

import com.tarpan.www.Constants;
import com.tarpan.www.util.LogUtils;
import com.tarpan.www.util.StringUtil;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.util.*;

public class Senti {
    /**
     * load the sentiment lexicon
     * 加载分析后的词语和得分
     *
     * @param path
     * @return
     */
    public static Map<String, Double> loadSenti(String path) {
        Map<String, Double> sentiDict = new HashMap<>(128);
        try {
            LineIterator lines = FileUtils.lineIterator(new File(path), Charsets.UTF_8.toString());
            while (lines.hasNext()) {
                String line = lines.next().trim();
                if (!StringUtil.isNullOrBlank(line)) {
                    String[] li = line.split(" ");
                    if (li.length == 2) {
                        sentiDict.put(li[0], Double.parseDouble(li[1].trim()));
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.logError("[情感分析]加载词语出错了", e);
        }
        return sentiDict;
    }

    /**
     * load the non-linear sentiment lexicon
     * 加载组合词语与得分
     *
     * @param path
     * @return
     */
    public static Map<String, Double> loadLexicon(String path) {
        Map<String, Double> lexicon = new HashMap<>(32);
        try {
            LineIterator lines = FileUtils.lineIterator(new File(path), Charsets.UTF_8.toString());
            while (lines.hasNext()) {
                String line = lines.next().trim();
                if (!StringUtil.isNullOrBlank(line)) {
                    String[] li = line.split(" ");
                    lexicon.put(StringUtil.arrayToString(li, " ", 0, li.length - 1),
                            Double.parseDouble(li[li.length - 1]));
                }
            }
        } catch (Exception e) {
            LogUtils.logError("[情感分析]加载词语出错了", e);
        }
        return lexicon;
    }

    /**
     * 加载方面的词语
     *
     * @param inPath
     * @return
     */
    public static Map<String, String> loadAspectSenti(String inPath) {
        Map<String, String> dic = new HashMap<>(32);
        try {
            LineIterator lines = FileUtils.lineIterator(new File(inPath), Charsets.UTF_8.toString());
            while (lines.hasNext()) {
                String line = lines.next().trim();
                if (!StringUtil.isNullOrBlank(line)) {
                    String[] li = line.split(" ");
                    if (li.length == 3) {
                        String li02 = li[0] + " " + li[1];
                        if (!dic.containsKey(li02)) {
                            dic.put(li02, li[2]);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.logError("[情感分析]加载词语出错了", e);
        }
        return dic;
    }

    public static Set<String> oov = new HashSet<>();

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

            System.out.println("strength2: " + strength);
            if ("shift".equals(li[0]) && flag) {
                strength = strength > 0.0 ? strength - Constants.SHIFT_VALUE : strength + Constants.SHIFT_VALUE;
            } else if ("不太".equals(li[0]) && flag) {
                strength = strength > 0.0 ? strength - Constants.BUTAI_VALUE : strength + Constants.BUTAI_VALUE;
            } else if (advDict.containsKey(li[0])) {
                System.out.println("strength3: " + advDict.get(li[0]) + " * " + strength);
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
                System.out.println("strength4: " + advDict.get(li[1]) + "*" + strength);
                strength *= advDict.get(li[1]);
            }
            List<String> tempList = Arrays.asList("shift", "没", "没有");
            if (tempList.contains(li[0])) {
                strength = strength > 0.0 ? strength - Constants.SHIFT_VALUE : strength + Constants.SHIFT_VALUE;
            } else {
                if (advDict.containsKey(li[0])) {
                    System.out.println("strength5: " + advDict.get(li[0]) + " * " + strength);
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
                    System.out.println("strength6: " + advDict.get(li[i]) + " * " + strength);
                    strength *= advDict.get(li[i]);
                }
            }
        }
        strength = ((int) (strength * 100)) / 100.0;
        return strength;
    }

    /**
     * apply final phrases to calculate number sequences
     * @param sentiDict
     * @param nonLinear
     * @param advDictfilePath
     * @param finalPhs
     * @return
     */
    public static String calAll(Map<String, Double> sentiDict, Map<String, Double> nonLinear, String advDictfilePath,
                                List<String> finalPhs) {
        Map<String, Double> advDict = loadSenti(advDictfilePath);
        StringBuilder sb = new StringBuilder();

        if (null == finalPhs || finalPhs.size() == 0) {
            return "0.0";
        }

        for (String line : finalPhs) {
            if ("SUM".equals(line)) {
                sb.append("s").append("|");
            } else {
                sb.append(calPhraseStrength(sentiDict, nonLinear, line, advDict) + "").append("|");
            }
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * 统计得分
     * @param phraseNumberSeq
     * @return
     */
    public static double statistics(String phraseNumberSeq) {
        double strength = 0.0;
        double strength1 = Evaluate.findSentiDropPoint(phraseNumberSeq);
        double strength2 = Evaluate.commonSenti(phraseNumberSeq);
        if (strength1 * strength2 > 0) {
            strength = strength2;
        } else if (strength1 == 0) {
            strength = strength2;
        } else if (strength2 == 0) {
            strength = strength1;
        } else {
            if (strength1 > 0 && strength2 < 0) {
                strength = strength1;
            } else {
                strength = strength2;
            }
        }
        return strength;
    }
}
