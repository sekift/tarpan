package com.tarpan.www.pro;

import com.tarpan.www.util.LogUtils;
import com.tarpan.www.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 计算得分
 *
 * @author sekift
 */
public class Evaluate {
    /**
     * 统计得分
     *
     * @param phraseNumberSeq
     * @return
     */
    public static double statistics(String phraseNumberSeq) {
        double strength;
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

    public static void main(String args[]) {
        System.out.println(findSentiDropPoint("-1.0"));
        System.out.println(findSentiDropPoint("s|1.8|-5.85|0|s|1.0|0"));
        System.out.println(commonSenti("s|1.8|-5.85|0|s|1.0|0"));
        String str = "s|1.8|-5.85|0|s|1.0|0";
        System.out.println(statistics(str));
    }
}
