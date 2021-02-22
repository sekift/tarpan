package com.tarpan.www.process.impl;

import com.tarpan.www.Constants;
import com.tarpan.www.pre.LoadFile;
import com.tarpan.www.process.SentimentProcess;
import com.tarpan.www.util.LogUtils;
import com.tarpan.www.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author sekift
 * @date 2021/02/07 14:00
 * @desc 2021年开发情感打分逻辑：Comp
 */
public class CompSentimentProcess implements SentimentProcess {

    private Map<String, Double> negAndPos = LoadFile.getNegAndPos();
    private Map<String, Double> sentiment = LoadFile.getSentiment();
    private Map<String, Double> advxxx = LoadFile.getAdvxxx();

    public static void main(String[] args) {
        String posedStr = "酒店#NN 实在#AD 差#VA ，#PU 房间#NN 又#AD 小#VA 又#AD 脏#VA ，#PU 卫生间#NN 环境#NN 太#AD 差#VA ，#PU 整#DT 个#M 酒店#NN 有点#AD 像#VV 马路#NN 边上#LC 的#DEG 招待所#NN 。#PU";
        String parsedStr = "root(ROOT-0, 差-3)   nsubj(差-3, 酒店-1)   advmod(差-3, 实在-2)   punct(差-3, ，-4)   nsubj(小-7, 房间-5)   advmod(小-7, 又-6)   conj(差-3, 小-7)   advmod(脏-9, 又-8)   conj(小-7, 脏-9)   punct(差-3, ，-10)   compound:nn(环境-12, 卫生间-11)   nsubj(差-14, 环境-12)   advmod(差-14, 太-13)   conj(差-3, 差-14)   punct(差-3, ，-15)   det(酒店-18, 整-16)   mark:clf(整-16, 个-17)   nsubj(像-20, 酒店-18)   advmod(像-20, 有点-19)   conj(差-3, 像-20)   nmod(招待所-24, 马路-21)   case(马路-21, 边上-22)   case(马路-21, 的-23)   dobj(像-20, 招待所-24)   punct(差-3, 。-25)";
        SentimentProcess process = new CompSentimentProcess();
        List<String> list = process.findPhrase(posedStr, parsedStr);
        process.filterPhrase(list);

    }

    /**
     * @param posedStr  ： 酒店#NN 实在#AD 差#VA ，#PU 房间#NN 又#AD 小#VA 又#AD 脏#VA ，#PU 卫生间#NN 环境#NN 太#AD 差#VA ，#PU
     *                  整#DT 个#M 酒店#NN 有点#AD 像#VV 马路#NN 边上#LC 的#DEG 招待所#NN 。#PU
     * @param parsedStr ： root(ROOT-0, 差-3)   nsubj(差-3, 酒店-1)   advmod(差-3, 实在-2)   punct(差-3, ，-4)   nsubj(小-7, 房间-5)
     *                  advmod(小-7, 又-6)   conj(差-3, 小-7)   advmod(脏-9, 又-8)   conj(小-7, 脏-9)   punct(差-3, ，-10)
     *                  compound:nn(环境-12, 卫生间-11)   nsubj(差-14, 环境-12)   advmod(差-14, 太-13)   conj(差-3, 差-14)
     *                  punct(差-3, ，-15)   det(酒店-18, 整-16)   mark:clf(整-16, 个-17)   nsubj(像-20, 酒店-18)
     *                  advmod(像-20, 有点-19)   conj(差-3, 像-20)   nmod(招待所-24, 马路-21)   case(马路-21, 边上-22)
     *                  case(马路-21, 的-23)   dobj(像-20, 招待所-24)   punct(差-3, 。-25)
     * @return
     */
    @Override
    public List<String> findPhrase(String posedStr, String parsedStr) {
        // 1 重新定义结构，结果为：
        /**
         * [root%0#ROOT#ROOT#0.0&3#差#VA#-3.0, nsubj%3#差#VA#-3.0&1#酒店#NN#0.0, advmod%3#差#VA#-3.0&2#实在#AD#1.1,
         * punct%3#差#VA#-3.0&4#，#PU#0.0, nsubj%7#小#VA#-2.5&5#房间#NN#0.0, advmod%7#小#VA#-2.5&6#又#AD#1.1,
         * conj%3#差#VA#-3.0&7#小#VA#-2.5, advmod%9#脏#VA#-3.0&8#又#AD#1.1, conj%7#小#VA#-2.5&9#脏#VA#-3.0,
         * punct%3#差#VA#-3.0&10#，#PU#0.0, compound:nn%12#环境#NN#0.0&11#卫生间#NN#0.0, nsubj%14#差#VA#-3.0&12#环境#NN#0.0,
         * advmod%14#差#VA#-3.0&13#太#AD#1.3, conj%3#差#VA#-3.0&14#差#VA#-3.0, punct%3#差#VA#-3.0&15#，#PU#0.0,
         * det%18#酒店#NN#0.0&16#整#DT#0.0, mark:clf%16#整#DT#0.0&17#个#M#0.0, nsubj%20#像#VV#0.0&18#酒店#NN#0.0,
         * advmod%20#像#VV#0.0&19#有点#AD#0.3, conj%3#差#VA#-3.0&20#像#VV#0.0, nmod%24#招待所#NN#0.0&21#马路#NN#0.0,
         * case%21#马路#NN#0.0&22#边上#LC#0.0, case%21#马路#NN#0.0&23#的#DEG#0.0, dobj%20#像#VV#0.0&24#招待所#NN#0.0,
         * punct%3#差#VA#-3.0&25#。#PU#0.0]
         */
        List<String> parsedList = redefineStructure(posedStr, parsedStr);
        //2 过滤两项得分都是0.0的值
        List<String> filterScoreList = filterZeroScore(parsedList);
        //3 过滤标点符号和root的值
        List<String> filterPuList = filterPuValue(filterScoreList);
        // 4 重新组织词组
        /**
         * 1#酒店#NN#0.0&2#实在#AD#1.1&3#差#VA#-3.0,
         * 5#房间#NN#0.0&6#又#AD#1.1&7#小#VA#-2.5&8#又#AD#1.1&9#脏#VA#-3.0,
         * 12#环境#NN#0.0&13#太#AD#1.3&14#差#VA#-3.0,
         * 19#有点#AD#0.3&20#像#VV#0.0
         */
        List<String> reorganizeWords = reorganizePhrases(filterPuList);
        return reorganizeWords;
    }

    @Override
    public List<String> filterPhrase(List<String> phrases) {
        // 1 将得分0.0的过滤掉
        List<String> filterZeroList = filterZeroPart(phrases);
        System.out.println("filterZeroList= " + filterZeroList);
        // 2 将部分只有一个词的过滤掉，词性包括：DT
        List<String> filterSpeechList = filterSpeechPart(filterZeroList);
        System.out.println("filterSpeechList= " + filterSpeechList);
        // 3 将AD向后合并，索引差需要在6以内
        List<String> mergeAdList = mergeAdPart(filterSpeechList);
        System.out.println("mergeAdList= " + mergeAdList);
        return filterSpeechList;
    }

    @Override
    public String calAll(List<String> finalPh) {
        return null;
    }

    /**
     * 将单个AD词性的合并到后一项去，索引差需在一定范围内
     *
     * @param phrases
     * @return
     */
    private List<String> mergeAdPart(List<String> phrases) {
        List<String> resultList = new ArrayList<>();
        for (int i = 0; i < phrases.size() - 1; i++) {
            String preValue = phrases.get(i);
            String proValue = phrases.get(i + 1);
            String[] valueArray = preValue.split(Constants.TWO_WORD_SEP);
            boolean flag = valueArray.length == 1 &&
                    (valueArray[0].contains("#AD"));
            if (flag) {
                Integer preInt = Integer.parseInt(preValue.split(Constants.WORD_SEG_SEP)[0]);
                Integer proInt = Integer.parseInt(proValue.split(Constants.WORD_SEG_SEP)[0]);
                if (proInt - preInt <= 6) {
                    resultList.add(preValue + Constants.TWO_WORD_SEP + proValue);
                }
            } else {
                resultList.add(preValue);
            }
        }
        return resultList;
    }

    /**
     * 将部分只有一个词的过滤掉，词性包括：AD、DT
     *
     * @param phrases
     * @return
     */
    private List<String> filterSpeechPart(List<String> phrases) {
        List<String> resultList = new ArrayList<>();
        for (String value : phrases) {
            String[] valueArray = value.split(Constants.TWO_WORD_SEP);
            boolean flag = valueArray.length == 1 &&
                    (valueArray[0].contains("#DT"));
            if (!flag) {
                resultList.add(value);
            }
        }
        return resultList;
    }

    /**
     * 过滤掉得分为0.0的部分
     *
     * @param phrases
     * @return
     */
    private List<String> filterZeroPart(List<String> phrases) {
        List<String> resultList = new ArrayList<>();
        for (String value : phrases) {
            String[] valueArray = value.split(Constants.TWO_WORD_SEP);
            StringBuilder sb = new StringBuilder();
            for (String str : valueArray) {
                if (!str.endsWith("#0.0")) {
                    sb.append(str).append(Constants.TWO_WORD_SEP);
                }
            }
            if (StringUtils.isNotEmpty(sb)) {
                resultList.add(sb.toString().substring(0, sb.length() - 1));
            }
        }
        return resultList;
    }

    /**
     * 重新组织词组，按索引连接起来
     * TODO 可优化，此处未考虑依存关系前缀
     *
     * @param filterPuList
     * @return
     */
    private List<String> reorganizePhrases(List<String> filterPuList) {
        Set<Integer> currentIndexSet = new TreeSet<>();
        Integer leftIndex, rightIndex;
        for (String value : filterPuList) {
            leftIndex = StringUtil.getIndex(value, 1);
            rightIndex = StringUtil.getIndex(value, 2);
            currentIndexSet.add(leftIndex);
            currentIndexSet.add(rightIndex);
        }
        List<List<Integer>> outsideList = new ArrayList<>();
        List<Integer> setList = new ArrayList<>(currentIndexSet);
        List<Integer> insideList = new ArrayList<>();
        for (int i = 0; i < setList.size() - 1; i++) {
            int preValue = setList.get(i);
            int proValue = setList.get(i + 1);
            if (i == 0) {
                insideList.add(preValue);
                outsideList.add(insideList);
            }
            if (proValue - preValue == 1) {
                insideList.add(proValue);
            } else {
                insideList = new ArrayList<>();
                insideList.add(proValue);
                outsideList.add(insideList);
            }
        }
        List<String> resultList = new ArrayList<>();
        for (List<Integer> inList : outsideList) {
            StringBuilder sb = new StringBuilder();
            for (Integer index : inList) {
                for (String value : filterPuList) {
                    Integer preValue = StringUtil.getIndex(value, 1);
                    if (preValue.equals(index)) {
                        String wordValue = StringUtil.getValue(value, 1);
                        sb.append(wordValue).append(Constants.TWO_WORD_SEP);
                        break;
                    } else {
                        Integer proValue = StringUtil.getIndex(value, 2);
                        if (proValue.equals(index)) {
                            String wordValue = StringUtil.getValue(value, 2);
                            sb.append(wordValue).append(Constants.TWO_WORD_SEP);
                            break;
                        }
                    }
                }
            }
            resultList.add(sb.toString().substring(0, sb.length() - 1));
        }
        return resultList;
    }

    /**
     * 重新定义parsed结果的结构，包含词性和得分
     *
     * @param parsedStr
     * @return
     */
    private List<String> redefineStructure(String posedStr, String parsedStr) {
        List<String> parsedList = new ArrayList<>();
        String[] parsedArray = parsedStr.trim().split("   ");
        for (int i = 0; i < parsedArray.length; i++) {
            Map<String, List<String>> outMap = new HashMap<>(2);
            // root(ROOT-0, 差-3)
            String[] outMapArray = parsedArray[i].split("\\(");
            String outMapKey = outMapArray[0];
            String outMapValueStr = outMapArray[1].replace(")", "");
            String[] outMapValueArray = outMapValueStr.split(", ");
            List<String> inList = new ArrayList<>(2);
            String[] firstMapArray = outMapValueArray[0].split("-");
            String[] secondMapArray = outMapValueArray[1].split("-");
            Integer firstMapKey = Integer.valueOf(firstMapArray[1]);
            Integer secondMapKey = Integer.valueOf(secondMapArray[1]);

            String firstMapWord = firstMapArray[0];
            String secondMapWord = secondMapArray[0];
            String firstMapValue = parsedNameMerge(posedStr, firstMapWord, firstMapKey);
            String secondMapValue = parsedNameMerge(posedStr, secondMapWord, secondMapKey);

            String firstWord = firstMapKey + Constants.WORD_SEG_SEP + firstMapValue;
            String secondWord = secondMapKey + Constants.WORD_SEG_SEP + secondMapValue;
            String resultWord = outMapKey + Constants.DEPE_SEP + firstWord + Constants.TWO_WORD_SEP + secondWord;
            parsedList.add(resultWord);
        }
        return parsedList;
    }

    /**
     * 过滤两项得分都是0.0的值
     *
     * @param mergeList
     * @return
     */
    private List<String> filterZeroScore(List<String> mergeList) {
        Predicate<String> predicate = merge -> !merge.split("&")[0].contains("#0.0")
                || !merge.split("&")[1].contains("#0.0");
        return mergeList.stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * 合并词性并打分
     *
     * @param posedStr
     * @param item
     * @param index
     * @return
     */
    private String parsedNameMerge(String posedStr, String item, Integer index) {
        String[] posedArray = posedStr.trim().split(" ");
        if (index == 0) {
            return item + Constants.WORD_SEG_SEP + "ROOT" + Constants.WORD_SEG_SEP + "0.0";
        }
        String posedName = posedArray[index - 1];
        String posedNameWord = StringUtil.getWord(posedName);
        String name = item.replace(posedNameWord, posedName);
        Double score = wordScore(posedNameWord);
        name = name + Constants.WORD_SEG_SEP + score;
        return name;
    }

    /**
     * 过滤标点符号和root的值
     *
     * @param filterScoreList
     * @return
     */
    private List<String> filterPuValue(List<String> filterScoreList) {
        Predicate<String> predicate = merge -> !merge.contains("#PU") && !merge.contains("#ROOT");
        List<String> list = filterScoreList.stream().filter(predicate).collect(Collectors.toList());
        return list;
    }

    /**
     * 获取单一词语的得分
     *
     * @param word
     * @return
     */
    private Double wordScore(String word) {
        Double score = 0.0;
        if (negAndPos.containsKey(word)) {
            score = negAndPos.get(word);
        }
        if (sentiment.containsKey(word)) {
            score = sentiment.get(word);
        }
        if (advxxx.containsKey(word)) {
            score = advxxx.get(word);
        }
        return score;
    }
}
