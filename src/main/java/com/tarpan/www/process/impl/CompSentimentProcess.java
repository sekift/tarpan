package com.tarpan.www.process.impl;

import com.tarpan.www.process.SentimentProcess;

import java.util.List;

/**
 * @author sekift
 * @date 2021/02/07 14:00
 * @desc 新的情感打分算法：Comp
 */
public class CompSentimentProcess implements SentimentProcess {

    /**
     * @param posedStr ： 酒店#NN 实在#AD 差#VV 房间#NN 又#AD 小#VA 又#AD 脏#VA 卫生间#NN 环境#NN 太#AD 差#VV 整#DT 个#M 酒店#NN
     *      * 有点#AD 像#VV 马路#NN 边上#LC 的#DEG 招待所#NN
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
        //第一步 将posed与parsed合并


        return null;
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
