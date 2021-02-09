package com.tarpan.www.process.impl;

import com.tarpan.www.pre.LoadFile;
import com.tarpan.www.process.SentimentProcess;
import com.tarpan.www.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author sekift
 * @date 2021/02/07 14:00
 * @desc 新的情感打分算法：Comp
 */
public class CompSentimentProcess implements SentimentProcess {

    private Map<String, Double> negAndPos = LoadFile.getNegAndPos();
    private Map<String, Double> sentiment = LoadFile.getSentiment();
    private Map<String, Double> advxxx = LoadFile.getAdvxxx();

    public static void main(String[] args) {
        String posedStr = "酒店#NN 实在#AD 差#VA ，#PU 房间#NN 又#AD 小#VA 又#AD 脏#VA ，#PU 卫生间#NN 环境#NN 太#AD 差#VA ，#PU 整#DT 个#M 酒店#NN 有点#AD 像#VV 马路#NN 边上#LC 的#DEG 招待所#NN 。#PU";
        String parsedStr = "root(ROOT-0, 差-3)   nsubj(差-3, 酒店-1)   advmod(差-3, 实在-2)   punct(差-3, ，-4)   nsubj(小-7, 房间-5)   advmod(小-7, 又-6)   conj(差-3, 小-7)   advmod(脏-9, 又-8)   conj(小-7, 脏-9)   punct(差-3, ，-10)   compound:nn(环境-12, 卫生间-11)   nsubj(差-14, 环境-12)   advmod(差-14, 太-13)   conj(差-3, 差-14)   punct(差-3, ，-15)   det(酒店-18, 整-16)   mark:clf(整-16, 个-17)   nsubj(像-20, 酒店-18)   advmod(像-20, 有点-19)   conj(差-3, 像-20)   nmod(招待所-24, 马路-21)   case(马路-21, 边上-22)   case(马路-21, 的-23)   dobj(像-20, 招待所-24)   punct(差-3, 。-25)";
        SentimentProcess process = new CompSentimentProcess();
        process.findPhrase(posedStr, parsedStr);

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
        //1 将-符号换成&


        //1 将posed与parsed合并，含打分
        //输出：
        /**
         * [root(ROOT#ROOT-0, 差#VA[-3.0]-3), nsubj(差#VA[-3.0]-3, 酒店#NN[0.0]-1), advmod(差#VA[-3.0]-3, 实在#AD[1.1]-2),
         * punct(差#VA[-3.0]-3, ，#PU[0.0]-4), nsubj(小#VA[-2.5]-7, 房间#NN[0.0]-5), advmod(小#VA[-2.5]-7, 又#AD[0.0]-6),
         * conj(差#VA[-3.0]-3, 小#VA[-2.5]-7), advmod(脏#VA[-3.0]-9, 又#AD[0.0]-8), conj(小#VA[-2.5]-7, 脏#VA[-3.0]-9),
         * punct(差#VA[-3.0]-3, ，#PU[0.0]-10), compound:nn(环境#NN[0.0]-12, 卫生间#NN[0.0]-11), nsubj(差#VA[-3.0]-14,
         * 环境#NN[0.0]-12), advmod(差#VA[-3.0]-14, 太#AD[1.3]-13), conj(差#VA[-3.0]-3, 差#VA[-3.0]-14), punct(差#VA[-3.0]-3, ，#PU[0.0]-15),
         * det(酒店#NN[0.0]-18, 整#DT[0.0]-16), mark:clf(整#DT[0.0]-16, 个#M[0.0]-17), nsubj(像#VV[0.0]-20, 酒店#NN[0.0]-18),
         * advmod(像#VV[0.0]-20, 有点#AD[0.3]-19), conj(差#VA[-3.0]-3, 像#VV[0.0]-20), nmod(招待所#NN[0.0]-24, 马路#NN[0.0]-21),
         * case(马路#NN[0.0]-21, 边上#LC[0.0]-22), case(马路#NN[0.0]-21, 的#DEG[0.0]-23), dobj(像#VV[0.0]-20, 招待所#NN[0.0]-24),
         * punct(差#VA[-3.0]-3, 。#PU[0.0]-25)]
         */
        List<String> mergeList = mergePosedAndParsed(posedStr, parsedStr);
        System.out.println(mergeList);
        //2 过滤两项得分都是0.0的值
        List<String> filterScoreList = filterZeroScore(mergeList);
        System.out.println(filterScoreList);
        //3 过滤标点符号和root的值
        //剩下：
        /**
         * [nsubj(差#VA[-3.0]-3, 酒店#NN[0.0]-1), advmod(差#VA[-3.0]-3, 实在#AD[1.1]-2), nsubj(小#VA[-2.5]-7, 房间#NN[0.0]-5),
         * advmod(小#VA[-2.5]-7, 又#AD[0.0]-6), conj(差#VA[-3.0]-3, 小#VA[-2.5]-7), advmod(脏#VA[-3.0]-9, 又#AD[0.0]-8),
         * conj(小#VA[-2.5]-7, 脏#VA[-3.0]-9), nsubj(差#VA[-3.0]-14, 环境#NN[0.0]-12), advmod(差#VA[-3.0]-14, 太#AD[1.3]-13),
         * conj(差#VA[-3.0]-3, 差#VA[-3.0]-14), advmod(像#VV[0.0]-20, 有点#AD[0.3]-19), conj(差#VA[-3.0]-3, 像#VV[0.0]-20)]
         */
        List<String> filterPuList = filterPuValue(filterScoreList);
        System.out.println(filterPuList);
        // 重新组织词组


        return null;
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
     * 过滤两项得分都是0.0的值
     *
     * @param mergeList
     * @return
     */
    private List<String> filterZeroScore(List<String> mergeList) {
        Predicate<String> predicate = merge -> !merge.split(", ")[0].contains("[0.0]")
                || !merge.split(", ")[1].contains("[0.0]");
        List<String> list = mergeList.stream().filter(predicate).collect(Collectors.toList());
        return list;
    }

    /**
     * 将posed与parsed合并
     *
     * @param posedStr
     * @param parsedStr
     * @return
     */
    private List<String> mergePosedAndParsed(String posedStr, String parsedStr) {
        String[] posedArray = posedStr.trim().split(" ");
        String[] parsedArray = parsedStr.trim().split("   ");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < parsedArray.length; i++) {
            String[] parsedItemArray = parsedArray[i].split(", ");
            String parsedItemElementFirst = parsedItemArray[0];
            String parsedItemElementSecond = parsedItemArray[1];
            String firstName = parsedNameMerge(posedArray, parsedItemElementFirst);
            String secondName = parsedNameMerge(posedArray, parsedItemElementSecond);
            list.add(firstName + ", " + secondName + ")");
        }

        return list;
    }

    /**
     * 合并词性并打分
     *
     * @param posedArray
     * @param item
     * @return
     */
    private String parsedNameMerge(String[] posedArray, String item) {
        String[] parsedItemElementArray = item.split("-");
        String parsedItemElementName = parsedItemElementArray[0];
        String parsedItemElementIndex = parsedItemElementArray[1].replace(")", "");
        int index = Integer.parseInt(parsedItemElementIndex);
        if (index == 0) {
            return parsedItemElementName + "#ROOT-0";
        }
        String posedName = posedArray[index - 1];
        String posedNameWord = StringUtil.getWord(posedName);
        String name = parsedItemElementName.replace(posedNameWord, posedName);
        Double score = wordScore(posedNameWord);
        name = name + "[" + score + "]" + "-" + index;
        return name;
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

    @Override
    public List<String> filterPhrase(List<String> phrases) {
        return null;
    }

    @Override
    public String calAll(List<String> finalPh) {
        return null;
    }
}
