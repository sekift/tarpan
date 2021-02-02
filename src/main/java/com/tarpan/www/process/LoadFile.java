package com.tarpan.www.process;

import com.tarpan.www.Constants;
import com.tarpan.www.util.FileUtil;
import com.tarpan.www.util.LogUtils;
import com.tarpan.www.util.StringUtil;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 加载文件或预加载文件
 */
public class LoadFile {

    /**引入正负面词典，去掉停用词*/
    private static Map<String, Integer> negAndPos;
    /**手工打分的情感词典*/
    private static Map<String, Double> sentiment;
    /**手工打分的副词情感词典，即得分因子*/
    private static Map<String, Double> advxxx;
    /**非线性短语*/
    private static Map<String, Double> nonLinear;
    /**指示性词语，字面是正的，但组合可能是负的*/
    private static Map<String, String> aspect;
    /**情感名词*/
    private static Set<String> sentiNN;
    /**情感动词*/
    private static Set<String> sentiVV;
    /**情感副词*/
    private static Set<String> sentiAD;
    /**总结性词语*/
    private static List<String> summary;
    /**有歧义的词语*/
    private static List<String> ambiguity;

    static {
        negAndPos = loadNegAndPos();
        sentiment = loadSenti(FileUtil.getDataPath(Constants.SENTIMENT_FILE));
        advxxx = loadSenti(FileUtil.getDataPath(Constants.ADV_FILE));
        nonLinear = loadLexicon(FileUtil.getDataPath(Constants.NONLI_FILE));
        aspect = loadAspectSenti(FileUtil.getDataPath(Constants.ASPECT_FILE));
        sentiNN = FileUtil.file2Set(FileUtil.getDataPath(Constants.SENTINN_FILE));
        sentiVV = FileUtil.file2Set(FileUtil.getDataPath(Constants.SENTIVV_FILE));
        sentiAD = FileUtil.file2Set(FileUtil.getDataPath(Constants.SENTIAD_FILE));
        summary = FileUtil.file2List(FileUtil.getDataPath(Constants.SUMMARY_FILE));
        ambiguity = FileUtil.file2List(FileUtil.getDataPath(Constants.AMBIGUITY_FILE));
        LogUtils.logInfo("词典加载完毕>>>>>>>>>");
    }

    /**
     * 加载分析词典
     */
    public static Map<String, Integer> loadNegAndPos() {
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
     * 加载指向性的词语
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

    public static Map<String, Integer> getNegAndPos() {
        return negAndPos;
    }

    public static Map<String, Double> getSentiment() {
        return sentiment;
    }

    public static Map<String, Double> getNonLinear() {
        return nonLinear;
    }

    public static Map<String, String> getAspect() {
        return aspect;
    }

    public static Set<String> getSentiNN() {
        return sentiNN;
    }

    public static Set<String> getSentiVV() {
        return sentiVV;
    }

    public static Set<String> getSentiAD() {
        return sentiAD;
    }

    public static List<String> getSummary() {
        return summary;
    }

    public static List<String> getAmbiguity() {
        return ambiguity;
    }

    public static Map<String, Double> getAdvxxx() {
        return advxxx;
    }
}
