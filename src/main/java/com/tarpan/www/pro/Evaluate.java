package com.tarpan.www.pro;

import com.tarpan.www.process.AnalyseProcess;
import com.tarpan.www.process.SentimentProcess;
import com.tarpan.www.util.LogUtils;
import com.tarpan.www.util.StringUtil;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.util.*;

/**
 * 计算得分
 *
 * @author sekift
 */
public class Evaluate {
    public static void main(String args[]) {
        parserFromFile("F:\\workspace\\data\\test\\posall-goop-score.txt",
                "F:\\workspace\\data\\test\\posall-goop-score.txt-result.txt");
    }

    public static void parserFromFile(String inPath, String outPath) {
        try {
            int i = 1;
            LineIterator lines = FileUtils.lineIterator(new File(inPath), Charsets.UTF_8.toString());
            while (lines.hasNext()) {
                String line = lines.next().trim();
                Map<String, Double> result = sentiProb(line);
                FileUtils.writeStringToFile(new File(outPath),
                        i + " ; " + result.get("positiveProb") + " ; " + result.get("negativeProb") + "\n",
                        Charsets.UTF_8, true);
                i++;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 情感倾向，正还是负
     * 正  负
     *
     * @param sentence
     * @return
     */
    public static Map<String, Double> sentiProb(String sentence) {
        Map<String, Double> result = new HashMap<>(4);
        double positiveProb = 0.0, negativeProb = 0.0;
        sentence = sentence.trim();
        if (StringUtil.isNullOrBlank(sentence)) {
            result.put("positiveProb", positiveProb);
            result.put("negativeProb", negativeProb);
            return result;
        }
        String[] li = sentence.split("\\|");
        for (String str : li) {
            try {
                if (str.startsWith("-")) {
                    negativeProb += Double.parseDouble(str);
                } else {
                    positiveProb += Double.parseDouble(str);
                }
            } catch (Exception e) {
                continue;
            }
        }
        double sum = positiveProb + Math.abs(negativeProb);
        if (sum - 0.0 < 0.001) {
            result.put("positiveProb", 0.5);
            result.put("negativeProb", 0.5);
            return result;
        }

        positiveProb = positiveProb/sum;
        negativeProb = 1 - positiveProb;
        result.put("positiveProb", positiveProb);
        result.put("negativeProb", negativeProb);
        return result;
    }

    /**
     * 统计得分
     *
     * @param phraseNumberSeq
     * @param status          1-goop使用，2-comp使用
     * @return
     */
    public static double statistics(String phraseNumberSeq, Integer status) {
        double strength = 0.0;
        if (status == 1) {
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
        } else if (status == 2) {
            strength = Evaluate.commonSenti(phraseNumberSeq);
        }
        strength = ((int) Math.round(strength * 100)) / 100.0;
        return strength;
    }

    /**
     * 找出打分的情况
     *
     * @param sentence
     * @return
     */
    public static double findSentiDropPoint(String sentence) {
        sentence = sentence.trim();
        // because of no extraction sentiment
        if (StringUtil.isNullOrBlank(sentence)) {
            return 0.0;
        }

        String[] li = sentence.split("\\|");
        List<String> list = new ArrayList<>();
        for (String s : li) {
            list.add(s);
        }
        //判断第一个值的情况
        if (list.get(0) != null && "0".equals(list.get(0))) {
            list.remove(0);
        }
        //判断最后一个值的情况
        int las = list.size() - 1;
        if (list.get(las) != null && "0".equals(list.get(las))) {
            list.remove(las);
        }

        if (list.isEmpty()) {
            return 0.0;
        }
        // there is a summary
        if (list.contains("s")) {
            int index = 0;
            // find last 's'
            for (int i = 0; i < list.size(); i++) {
                if ("s".equals(list.get(i))) {
                    index = i;
                }
            }
            // s in last position
            if (index == list.size() - 1) {
                try {
                    return Double.parseDouble(list.get(index - 1));
                } catch (Exception e) {
                    LogUtils.logInfo("sentiment miss" + sentence);
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
                end = Double.parseDouble(li[li.length - 1]);
            } catch (Exception e) {
                LogUtils.logInfo("li[0]=" + li[0] + "li[li.length-1]=" + li[li.length - 1]);
            }

            if (Math.abs(begin) > Math.abs(end)) {
                return begin;
            } else if (Math.abs(begin) < Math.abs(end)) {
                return end;
            } else {
                List<Double> absLi = new ArrayList<>();
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

    /**
     * 统计得分
     *
     * @param sentence
     * @return
     */
    public static double commonSenti(String sentence) {
        double sum = 0.0;
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
}
