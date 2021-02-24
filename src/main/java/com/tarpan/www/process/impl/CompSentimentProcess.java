package com.tarpan.www.process.impl;

import com.tarpan.www.Constants;
import com.tarpan.www.pre.LoadFile;
import com.tarpan.www.process.SentimentProcess;
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
        String posedStr = "特别#AD 值得#VV 推荐#NN 的#DEC 是#VC 餐饮#NN 很#AD 好#VA ，#PU 不管#AD 是#VC 堂食#NN 还是#CC 送餐#NN ，#PU 味道#NN 不错#VA ，#PU 价格#NN 也#AD 不#AD 贵#VA 。#PU";
        String parsedStr = "root(ROOT-0, 好-8)   advmod(值得-2, 特别-1)   nsubj(好-8, 值得-2)   dobj(值得-2, 推荐-3)   mark(值得-2, 的-4)   cop(好-8, 是-5)   nsubj(好-8, 餐饮-6)   advmod(好-8, 很-7)   punct(好-8, ，-9)   advmod(送餐-14, 不管-10)   cop(送餐-14, 是-11)   conj(送餐-14, 堂食-12)   cc(送餐-14, 还是-13)   conj(好-8, 送餐-14)   punct(好-8, ，-15)   nsubj(不错-17, 味道-16)   conj(好-8, 不错-17)   punct(好-8, ，-18)   nsubj(贵-22, 价格-19)   advmod(贵-22, 也-20)   neg(贵-22, 不-21)   conj(好-8, 贵-22)   punct(好-8, 。-23)";
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
        // 2 过滤无意义的词
        List<String> filterSpeechList = filterSpeechP(filterZeroList);
        //System.out.println("filterSpeechList= " + filterSpeechList);
        // 3 将部分只有一个词的过滤掉，词性包括：DT、P、PN、PU、M
        filterSpeechList = filterSpeechPart(filterSpeechList);
        // 4 长句拆解成短句
        List<String> splitAdList = splitAdPart(filterSpeechList);
        System.out.println("splitAdList= " + splitAdList);

        return splitAdList;
    }

    /**
     * 计算分部得分
     *
     * @param finalPh
     * @return String "-3.3|-2.75|-3.3|-3.9"
     */
    @Override
    public String calAll(List<String> finalPh) {
        // 输入：[2#实在#AD#1.1&3#差#VA#-3.0, 6#又#AD#1.1&7#小#VA#-2.5&8#又#AD#1.1&9#脏#VA#-3.0, 13#太#AD#1.3&14#差#VA#-3.0, 19#有点#AD#0.3]
        StringBuilder sb = new StringBuilder();
        for (String value : finalPh) {
            String[] array = value.split(Constants.TWO_WORD_SEP);
            if (array.length == 1) {
                double firstPoint = Double.parseDouble(array[0].split(Constants.WORD_SEG_SEP)[3]);
                if (array[0].contains("#AD") || array[0].contains("#VE")) {
                    sb.append(((int) ((0.4 * firstPoint) * 100)) / 100.0).append(Constants.SCORE_SEP);
                } else {
                    sb.append(firstPoint).append(Constants.SCORE_SEP);
                }
            } else if (array.length > 1) {
                Double[] pointArray = new Double[array.length];
                for (int i = 0; i < array.length; i++) {
                    pointArray[i] = Double.parseDouble(array[i].split(Constants.WORD_SEG_SEP)[3]);
                }
                List<Integer> adList = new ArrayList<>();
                List<Integer> notAdList = new ArrayList<>();
                for (int i = 0; i < array.length; i++) {
                    if (array[i].contains("#AD") || array[i].contains("#VE")) {
                        adList.add(i);
                    } else {
                        notAdList.add(i);
                    }
                }
                double multiPoint = 1.0, sumPoint = 0.0;
                if (adList.isEmpty()) {
                    for (Integer in : notAdList) {
                        sumPoint += pointArray[in];
                    }
                    sb.append(((int) (sumPoint * 100)) / 100.0).append(Constants.SCORE_SEP);
                } else if (notAdList.isEmpty()) {
                    for (Integer in : adList) {
                        multiPoint *= pointArray[in];
                    }
                    sb.append(((int) (0.4 * multiPoint * 100)) / 100.0).append(Constants.SCORE_SEP);
                } else {
                    for (Integer in : adList) {
                        multiPoint *= pointArray[in];
                    }
                    for (Integer in : notAdList) {
                        sumPoint += pointArray[in];
                    }
                    sb.append(((int) ((multiPoint * sumPoint) * 100)) / 100.0).append(Constants.SCORE_SEP);
                }
            }
        }
        return sb.toString();
    }

    /**
     * #AD#XX#AD#XX，拆成#AD#XX，#AD#XX
     *
     * @param phrases
     * @return
     */
    private List<String> splitAdPart(List<String> phrases) {
        List<String> resultList = new ArrayList<>();
        for (String value : phrases) {
            String[] array = value.split(Constants.TWO_WORD_SEP);
            StringBuilder sb = new StringBuilder();
            if (array.length >= 4) {
                for (int i = 0; i < array.length; i++) {
                    if (i != 0 && array[i].contains("#AD") && !array[i-1].contains("#AD")) {
                        if(i>1){
                            sb.append(Constants.DEPE_SEP).append(array[i]).append(Constants.TWO_WORD_SEP);
                        }else{
                            sb.append(array[i]).append(Constants.TWO_WORD_SEP);
                        }
                    } else {
                        sb.append(array[i]).append(Constants.TWO_WORD_SEP);
                    }
                    sb.subSequence(0, sb.length() -1);
                }
                String[] strArray = sb.toString().split(Constants.DEPE_SEP);
                for (int j = 0; j < strArray.length; j++) {
                     resultList.add(strArray[j].substring(0, strArray[j].length() - 1));
                }
            } else {
                resultList.add(value);
            }
        }
        return resultList;
    }

    /**
     * 将部分只有一个词的过滤掉，词性包括：DT、P、PN、PU
     *
     * @param phrases
     * @return
     */
    private List<String> filterSpeechPart(List<String> phrases) {
        List<String> resultList = new ArrayList<>();
        for (String value : phrases) {
            String[] valueArray = value.split(Constants.TWO_WORD_SEP);
            boolean flag = valueArray.length == 1 &&
                    (valueArray[0].contains("#DT") || valueArray[0].contains("#P")|| valueArray[0].contains("#M#"));
            if (!flag) {
                resultList.add(value);
            }
        }
        return resultList;
    }

    /**
     * 将词性为P的介词，为M的量词过滤掉
     *
     * @param phrases
     * @return
     */
    private List<String> filterSpeechP(List<String> phrases) {
        List<String> resultList = new ArrayList<>();
        for (String value : phrases) {
            String[] valueArray = value.split(Constants.TWO_WORD_SEP);
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<valueArray.length;i++){
                if(!valueArray[i].contains("#P#") && !valueArray[i].contains("#M#")){
                    sb.append(valueArray[i]).append(Constants.TWO_WORD_SEP);
                }
            }
            if(StringUtils.isNotEmpty(sb)) {
                resultList.add(sb.substring(0, sb.length() - 1));
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
