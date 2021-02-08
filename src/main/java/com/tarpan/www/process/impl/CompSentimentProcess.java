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
        String posedStr = "设施#NN 还#AD 将#AD 就#P ,#PU 但#AD 服务#NN 是#VC 相当#AD 的#DEV 不#AD 到位#VV ,#PU 休息#VV 了#AS 一#CD 个#M 晚上#NT 我#PN 白天#NT 出去#VV ,#PU 中午#NT 回来#VV 的#DEC 时候#NN 居然#AD 房间#NN 都#AD 没有#VE 整理#VV ,#PU 尽管#CS 我#PN 挂#VV 了#AS 要求#NN 整理#VV 房间#NN 的#DEC 牌子#NN .#PU";
        String parsedStr = "root(ROOT-0, 休息-14)   nsubj(休息-14, 设施-1)   advmod(休息-14, 还-2)   advmod(休息-14, 将-3)   case(到位-12, 就-4)   punct(到位-12, ,-5)   advmod(到位-12, 但-6)   nsubj(到位-12, 服务-7)   cop(到位-12, 是-8)   dep(到位-12, 相当-9)   mark(相当-9, 的-10)   neg(到位-12, 不-11)   nmod:prep(休息-14, 到位-12)   punct(休息-14, ,-13)   aux:asp(休息-14, 了-15)   nummod(晚上-18, 一-16)   mark:clf(一-16, 个-17)   nmod:topic(出去-21, 晚上-18)   nsubj(出去-21, 我-19)   dep(出去-21, 白天-20)   dep(回来-24, 出去-21)   punct(出去-21, ,-22)   dobj(回来-24, 中午-23)   acl(时候-26, 回来-24)   mark(回来-24, 的-25)   nmod:topic(整理-31, 时候-26)   advmod(房间-28, 居然-27)   nsubj(整理-31, 房间-28)   advmod(整理-31, 都-29)   dep(整理-31, 没有-30)   ccomp(休息-14, 整理-31)   punct(休息-14, ,-32)   advmod(挂-35, 尽管-33)   nsubj(挂-35, 我-34)   conj(休息-14, 挂-35)   aux:asp(挂-35, 了-36)   nsubj(整理-38, 要求-37)   acl(牌子-41, 整理-38)   dobj(整理-38, 房间-39)   mark(整理-38, 的-40)   dobj(挂-35, 牌子-41)   punct(休息-14, .-42)";
        SentimentProcess process = new CompSentimentProcess();
        process.findPhrase(posedStr, parsedStr);

    }

    /**
     * @param posedStr  ： 酒店#NN 实在#AD 差#VV 房间#NN 又#AD 小#VA 又#AD 脏#VA 卫生间#NN 环境#NN 太#AD 差#VV 整#DT 个#M 酒店#NN
     *                  * 有点#AD 像#VV 马路#NN 边上#LC 的#DEG 招待所#NN
     * @param parsedStr ： root(ROOT-0, 差-3)   nsubj(差-3, 酒店-1)   advmod(差-3, 实在-2)   dobj(差-3, 房间-4)
     *                  advmod(小-6, 又-5)   conj(差-3, 小-6)   advmod(脏-8, 又-7)   conj(小-6, 脏-8)
     *                  compound:nn(环境-10, 卫生间-9)   dobj(脏-8, 环境-10)   advmod(差-12, 太-11)   conj(差-3, 差-12)
     *                  det(酒店-15, 整-13)   mark:clf(整-13, 个-14)   dobj(差-12, 酒店-15)   advmod(像-17, 有点-16)
     *                  conj(差-12, 像-17)   nmod(招待所-21, 马路-18)   case(马路-18, 边上-19)   case(马路-18, 的-20)
     *                  dobj(像-17, 招待所-21)
     * @return
     */
    @Override
    public List<String> findPhrase(String posedStr, String parsedStr) {
        //1 将posed与parsed合并，含打分
        //输出：
        // root(ROOT#ROOT-0, 差#VA[-3.0]-3)   nsubj(差#VA[-3.0]-3, 酒店#NN[0.0]-1)   advmod(差#VA[-3.0]-3, 实在#AD[1.1]-2)
        // punct(差#VA[-3.0]-3, ，#PU[0.0]-4)   nsubj(小#VA[-2.5]-7, 房间#NN[0.0]-5)   advmod(小#VA[-2.5]-7, 又#AD[0.0]-6)
        // conj(差#VA[-3.0]-3, 小#VA[-2.5]-7)   advmod(脏#VA[-3.0]-9, 又#AD[0.0]-8)   conj(小#VA[-2.5]-7, 脏#VA[-3.0]-9)
        // punct(差#VA[-3.0]-3, ，#PU[0.0]-10)   compound:nn(环境#NN[0.0]-12, 卫生间#NN[0.0]-11)
        // nsubj(差#VA[-3.0]-14, 环境#NN[0.0]-12)   advmod(差#VA[-3.0]-14, 太#AD[1.3]-13)   conj(差#VA[-3.0]-3, 差#VA[-3.0]-14)
        // punct(差#VA[-3.0]-3, ，#PU[0.0]-15)   det(酒店#NN[0.0]-18, 整#DT[0.0]-16)   mark:clf(整#DT[0.0]-16, 个#M[0.0]-17)
        // nsubj(像#VV[0.0]-20, 酒店#NN[0.0]-18)   advmod(像#VV[0.0]-20, 有点#AD[0.3]-19)   conj(差#VA[-3.0]-3, 像#VV[0.0]-20)
        // nmod(招待所#NN[0.0]-24, 马路#NN[0.0]-21)   case(马路#NN[0.0]-21, 边上#LC[0.0]-22)
        // case(马路#NN[0.0]-21, 的#DEG[0.0]-23)   dobj(像#VV[0.0]-20, 招待所#NN[0.0]-24)   punct(差#VA[-3.0]-3, 。#PU[0.0]-25)
        List<String> mergeList = mergePosedAndParsed(posedStr, parsedStr);
        System.out.println(mergeList);
        //2 过滤两项得分都是0.0和root的值
        List<String> filterScoreList = filterZeroScore(mergeList);
        System.out.println(filterScoreList);
        //3 过滤标点符号和root的值
        //剩下：
        // nsubj(差#VA[-3.0]-3, 酒店#NN[0.0]-1)   advmod(差#VA[-3.0]-3, 实在#AD[1.1]-2) nsubj(小#VA[-2.5]-7, 房间#NN[0.0]-5)
        // advmod(小#VA[-2.5]-7, 又#AD[0.0]-6)    conj(差#VA[-3.0]-3, 小#VA[-2.5]-7)    advmod(脏#VA[-3.0]-9, 又#AD[0.0]-8)
        // conj(小#VA[-2.5]-7, 脏#VA[-3.0]-9)    nsubj(差#VA[-3.0]-14, 环境#NN[0.0]-12)    advmod(差#VA[-3.0]-14, 太#AD[1.3]-13)
        // conj(差#VA[-3.0]-3, 差#VA[-3.0]-14)    advmod(像#VV[0.0]-20, 有点#AD[0.3]-19)    conj(差#VA[-3.0]-3, 像#VV[0.0]-20)
        List<String> filterPuList = filterPuValue(filterScoreList);
        System.out.println(filterPuList);

        return null;
    }

    /**
     * 过滤标点符号和root的值
     * @param filterScoreList
     * @return
     */
    private List<String> filterPuValue(List<String> filterScoreList){
        Predicate<String> predicate = merge -> !merge.contains("#PU")&&!merge.contains("#ROOT");
        List<String> list = filterScoreList.stream().filter(predicate).collect(Collectors.toList());
        return list;
    }

        /**
         * 过滤两项得分都是0.0的值
         * @param mergeList
         * @return
         */
    private List<String> filterZeroScore(List<String> mergeList){
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
