package com.tarpan.www.nlp;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.tarpan.www.util.HttpUtil;
import com.tarpan.www.util.LogUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 斯坦福分词与句法依存关系处理
 * 可以自行实现处理
 *
 * @author sekift
 */
public class NlpProcess {
    private static final String PARSER_URL = "http://nlp.stanford.edu:8080/parser/index.jsp";
    private static final String NATIVE_URL = "http://10.0.2.155:8087/nlp/parser";

    /**
     * 分词和句法依存关系的示例
     */
    public static Map<String, String> parseExample(String text) {
        Map<String, String> map = new HashMap<>(4);
        text = "酒店实在差，房间又小又脏，卫生间环境太差，整个酒店有点像马路边上的招待所。";
        //分析，作为例子，下面是结果
        String seged = "酒店 实在 差 ， 房间 又 小 又 脏 ， 卫生间 环境 太 差 ， 整个 酒店 有点 像 马路 边上 的 招待所 。";
        String posed = "酒店#NN 实在#AD 差#VA ，#PU 房间#NN 又#AD 小#VA 又#AD 脏#VA，#PU 卫生间#NN 环境#NN 太#AD 差#VA ，#PU 整个#DT 酒店#NN 有点#AD 像#VV 马路#NN 边上#LC 的#DEG 招待所#NN 。#PU";
        String parsed = "nsubj(差-3, 酒店-1)   advmod(差-3, 实在-2)   root(ROOT-0, 差-3)   punct(差-3, ，-4)   nsubj(小-7, 房间-5)   advmod(小-7, 又-6)   conj(差-3, 小-7)   advmod(小-7, 又-8)   dep(小-7, 脏-9)   punct(差-3, ，-10)   compound:nn(环境-12, 卫生间-11)   nsubj(差-14, 环境-12)   advmod(差-14, 太-13)   conj(差-3, 差-14)   punct(差-3, ，-15)   det(酒店-17, 整个-16)   nsubj(像-19, 酒店-17)   advmod(像-19, 有点-18)   conj(差-3, 像-19)   nmod(招待所-23, 马路-20)   case(马路-20, 边上-21)   case(马路-20, 的-22)   dobj(像-19, 招待所-23)   punct(差-3, 。-24)";
        map.put("seged", seged);
        map.put("posed", posed);
        map.put("parsed", parsed);
        return map;
    }

    /**
     * 从http://nlp.stanford.edu:8080/parser/index.jsp获取分词结果
     */
    public static Map<String, String> parseFromWeb(String text) {
        Map<String, String> map = new HashMap<>(4);
        if (text.length() > 72) {
            LogUtils.logInfo("句子长度为：" + text.length() + " ,已经大于web分词的72个字符限制。");
            return map;
        }

        Map<String, String> params = new HashMap<>(16);
        params.put("chineseParseButton", "剖析 (Parse)");
        params.put("query", text);
        params.put("parserSelect", "Chinese");
        params.put("parse", "剖析 (Parse)");
        String response = HttpUtil.post(PARSER_URL, params, null, 10 * 3600, 10 * 3600, "utf-8");
        Document doc = Jsoup.parse(response);
        // 结果解析
        Elements parserOutputMonospace = doc.getElementsByClass("parserOutputMonospace");
        Elements spacingFree = doc.getElementsByClass("spacingFree");

        map.put("seged", parserOutputMonospace.get(0).text());
        map.put("posed", parserOutputMonospace.get(1).text().replaceAll("/", "#"));
        String parse = spacingFree.get(1).text();
        parse = parse.replaceAll("\\)", "\\)   ").trim();
        map.put("parsed", parse);
        return map;
    }

    /**
     * 本地开启分词
     *
     * @param text
     * @return
     */
    public static Map<String, String> parseFromNative(String text) {
        Map<String, String> params = new HashMap<>(2);
        Map<String, String> resultMap = null;
        try {
            params.put("input", URLEncoder.encode(text, "UTF-8"));
            String response = HttpUtil.post(NATIVE_URL, params, 10 * 3600, 10 * 3600, "utf-8");
            resultMap = JSONObject.parseObject(response, Map.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return resultMap;
    }

    public static void main(String args[]) {
        System.out.println(parseFromNative("酒店实在差 房间又小又脏 卫生间环境太差 整个酒店有点像马路边上的招待所 "));
    }

    /**
     * 看另外一个项目
     * 数据包括：
     * seged = []
     * posed = []
     * parsed = []
     * <p>
     * 返回的数据是：
     * 1、seged: [My, dog, also, likes, eating, sausage, .]
     * 2、posed: [My#PRP$, dog#NN, also#RB, likes#VBZ, eating#JJ, sausage#NN, .#PU]
     * 3、parsed: [root(ROOT-0, likes-4), nmod:poss(dog-2, My-1), nsubj(likes-4, dog-2), advmod(likes-4, also-3), dobj(likes-4, sausage-6), punct(likes-4, .-7), amod(sausage-6, eating-5)]
     * 经过转换后的数据为：
     * 1、seged: My dog also likes eating sausage .
     * 2、posed: My#PRP$ dog#NN also#RB likes#VBZ eating#JJ sausage#NN .#PU
     * 3、parsed: root(ROOT-0, likes-4)   nmod:poss(dog-2, My-1)   nsubj(likes-4, dog-2)   advmod(likes-4, also-3)   dobj(likes-4, sausage-6)   punct(likes-4, .-7)   amod(sausage-6, eating-5)
     */
    public static Map<String, List<String>> parser(String text, int type) {
        Map<String, String> result;
        switch (type) {
            case 1:
                result = parseFromNative(text);
                break;
            case 2:
                result = parseFromWeb(text);
                break;
            default:
                result = parseExample(text);
        }
        Map<String, List<String>> map = new HashMap<>(4);
        if (result == null || result.size() == 0) {
            return map;
        }
        List<String> seList = new ArrayList<>(2);
        List<String> poList = new ArrayList<>(2);
        List<String> paList = new ArrayList<>(2);

        seList.add(result.get("seged"));
        poList.add(result.get("posed"));
        paList.add(result.get("parsed"));
        map.put("seged", seList);
        map.put("posed", poList);
        map.put("parsed", paList);
        return map;
    }
}
