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
import org.apache.commons.lang3.StringUtils;

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
    public static Map<String, String> sentiFly(SentimentProcess sentimentProcess,String line) {
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
        for (int i = 0; i < seged.size(); i++) {
            List<String> phrases = sentimentProcess.findPhrase(posed.get(i), parsed.get(i));
            //LogUtils.logInfo("phrases: " + phrases);
            List<String> finalPh = sentimentProcess.filterPhrase(phrases);
            LogUtils.logInfo("finalPh: " + finalPh);
            fph.add(StringUtil.listToString(finalPh, " ,"));
            String phraseNumberSeqs = sentimentProcess.calAll(finalPh);
//            LogUtils.logInfo("phraseNumberSeqs: " + phraseNumberSeqs);
            if(StringUtils.isNotEmpty(phraseNumberSeqs)) {
                seqs.add(phraseNumberSeqs);
            }
        }

        double sentiScore = 0.0, positiveProb=0.0,negativeProb=0.0;
        if(sentimentProcess instanceof GoopSentimentProcess){
            sentiScore = Evaluate.statistics(StringUtil.listToString(seqs, "|"), 1);
        }else if(sentimentProcess instanceof CompSentimentProcess){
            sentiScore = Evaluate.statistics(StringUtil.listToString(seqs, "|"), 2);
            Map<String, Double> prob = Evaluate.sentiProb(StringUtil.listToString(seqs, "|"));
            positiveProb = prob.get("positiveProb");
            negativeProb = prob.get("negativeProb");
        }
        String segedStr = StringUtil.listToString(seged, " ");
        String posedStr = StringUtil.listToString(posed, " ");
        String parsedStr = StringUtil.listToString(parsed, " ");
        resultMap.put("input", line);
        resultMap.put("seged", segedStr);
        resultMap.put("posed", posedStr);
        resultMap.put("parsed", parsedStr);
        resultMap.put("fph", StringUtil.listToString(fph, " ,"));
        resultMap.put("seqs", StringUtil.listToString(seqs, "|"));
        resultMap.put("positiveProb", String.valueOf(positiveProb));
        resultMap.put("negativeProb", String.valueOf(negativeProb));
        resultMap.put("score", String.valueOf(sentiScore));
        //LogUtils.logInfo("part " + StringUtil.listToString(seqs, "|"));
        LogUtils.logInfo("positiveProb="+positiveProb+";negativeProb="+negativeProb+";score " + sentiScore);
        return resultMap;
    }

    public static void parserFromFile(SentimentProcess sentimentProcess, String inPath, String outPath) {
        // sentence length :80 tokens
        try {
            int i = 1;
            LineIterator lines = FileUtils.lineIterator(new File(inPath), Charsets.UTF_8.toString());
            while (lines.hasNext()) {
                String line = lines.next().trim();
                Map<String, String> result = sentiFly(sentimentProcess, line);
                FileUtils.writeStringToFile(new File(outPath),
                        i+ ": " + result.get("positiveProb") + " ; " +result.get("negativeProb") + " ; " +result.get("score")
                                + " ; " + line + "====" + result.get("seqs") + " ; " + result.get("fph") + "\n",
                        Charsets.UTF_8, true);
                i++;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        SentimentProcess sentimentProcess = new CompSentimentProcess();
//        System.out.println(sentiFly(sentimentProcess, "酒店实在差，房间又小又脏，卫生间环境太差，整个酒店有点像马路边上的招待所。"));
                //String words = "酒店实在差，房间又小又脏，卫生间环境太差，整个酒店有点像马路边上的招待所。";
        //房间的设施还算过得去～但是我遇到的最差的酒店服务就是这家酒店了，特别是前台服务简直让人恶心
        //设施还将就,但服务是相当的不到位。休息了一个晚上我白天出去,中午回来的时候居然房间都没有整理。尽管我挂了要求整理房间的牌子.
        parserFromFile(sentimentProcess, "F:\\workspace\\data\\test\\posall.txt",
                "F:\\workspace\\data\\test\\posall-comp-result-prob.txt");
    }

}
