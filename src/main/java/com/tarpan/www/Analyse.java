package com.tarpan.www;

import com.tarpan.www.nlp.NlpProcess;
import com.tarpan.www.pre.PreProcess;
import com.tarpan.www.pro.Evaluate;
import com.tarpan.www.process.SentimentProcess;
import com.tarpan.www.process.impl.CompSentimentProcess;
import com.tarpan.www.process.impl.GoopSentimentProcess;
import com.tarpan.www.util.LogUtils;
import com.tarpan.www.util.StringUtil;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * the example to process a text by the sentiment analysis proposed
 *
 * @author sekift
 */
public class Analyse {

    /**
     * List<String>
     *
     * @param line
     */
    public static Map<String, String> sentiFly(String line) {
        // 返回的map
        Map<String, String> resultMap = new HashMap<>();
        // 预处理
        line = PreProcess.process(line);
        // 自然语言处理，1-native，2-web
        Map<String, List<String>> nlp = NlpProcess.parser(line, 1);
        System.out.println(nlp);
        if (null == nlp || nlp.size() == 0) {
            return resultMap;
        }

        // opinion phrases and compute the sentiment strength
        List<String> seqs = new ArrayList<>();
        // final phrases
        List<String> fph = new ArrayList<>();
        List<String> seged = nlp.get("seged");
        List<String> posed = nlp.get("posed");
        List<String> parsed = nlp.get("parsed");
        SentimentProcess sentimentProcess = new GoopSentimentProcess();
        for (int i = 0; i < seged.size(); i++) {
            List<String> phrases = sentimentProcess.findPhrase(posed.get(i), parsed.get(i));
            //LogUtils.logInfo("phrases: " + phrases);
            List<String> finalPh = sentimentProcess.filterPhrase(phrases);
            //LogUtils.logInfo("finalPh: " + finalPh);
            fph.add(StringUtil.listToString(finalPh, " ,"));
            String phraseNumberSeqs = sentimentProcess.calAll(finalPh);
            seqs.add(phraseNumberSeqs);
        }

        double sentiScore = Evaluate.statistics(StringUtil.listToString(seqs, "|"));
        String segedStr = StringUtil.listToString(seged, " ");
        String posedStr = StringUtil.listToString(posed, " ");
        String parsedStr = StringUtil.listToString(parsed, " ");
        resultMap.put("input", line);
        resultMap.put("seged", segedStr);
        resultMap.put("posed", posedStr);
        resultMap.put("parsed", parsedStr);
        resultMap.put("fph", StringUtil.listToString(fph, " ,"));
        resultMap.put("seqs", StringUtil.listToString(seqs, "|"));
        resultMap.put("score", String.valueOf(sentiScore));
        //LogUtils.logInfo("part " + StringUtil.listToString(seqs, "|"));
        //LogUtils.logInfo("sent " + sentiScore);
        return resultMap;
    }

    public static void parserFromFile(String inPath, String outPath) {
        // sentence length :80 tokens
        try {
            LineIterator lines = FileUtils.lineIterator(new File(inPath), Charsets.UTF_8.toString());
            while (lines.hasNext()) {
                String line = lines.next().trim();
                Map<String, String> result = sentiFly(line);
                FileUtils.writeStringToFile(new File(outPath),
                        result.get("score") + " ; " + result.get("seqs") + " ; " + result.get("fph")
                                + "====" + line + "\n",
                        Charsets.UTF_8, true);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        System.out.println(sentiFly("这个网站的评价真是越来越不可信了，搞不懂为什么这么多好评。真的是很一般，不要迷信什么哪里回来的大厨吧。环境和出品若是当作普通茶餐厅来看待就还说得过去，但是价格又不是茶餐厅的价格，这就很尴尬了。。服务也是有待提高。"));
        //String words = "酒店实在差，房间又小又脏，卫生间环境太差，整个酒店有点像马路边上的招待所。";
        //设施还将就,但服务是相当的不到位,休息了一个晚上我白天出去,中午回来的时候居然房间都没有整理,尽管我挂了要求整理房间的牌子.
//        parserFromFile("F:\\workspace\\data\\test\\negall.txt",
//                "F:\\workspace\\data\\test\\negall-result.txt");
    }

}
