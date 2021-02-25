package com.tarpan.www.process;

import com.alibaba.fastjson.JSONObject;
import com.tarpan.www.nlp.NlpProcess;
import com.tarpan.www.pre.PreProcess;
import com.tarpan.www.pro.Evaluate;
import com.tarpan.www.process.impl.CompSentimentProcess;
import com.tarpan.www.process.impl.GoopSentimentProcess;
import com.tarpan.www.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 组合分析
 *
 * @author sekift
 */
public class AnalyseProcess {

    /**
     * 分析过程
     * @param sentimentProcess 得分实现
     * @param parserType 处理选择，0-不用分词，1-native，2-web
     * @param line 需要分词的句子
     * @return
     */
    public static Map<String, String> sentiFly(SentimentProcess sentimentProcess, Integer parserType, String line) {
        // 返回的map
        Map<String, String> resultMap = new HashMap<>(32);
        Map<String, List<String>> nlp;
        if(parserType == 0){
             nlp = JSONObject.parseObject(line, Map.class);
        }else{
            // 预处理
            line = PreProcess.process(line);
            // 自然语言处理，1-native，2-web
            nlp = NlpProcess.parser(line, parserType);
        }

        if (null == nlp || nlp.size() == 0) {
            return resultMap;
        }
        List<String> seqs = new ArrayList<>();
        // final phrases
        List<String> finalPhrases = new ArrayList<>();
        List<String> sentiWord = new ArrayList<>();
        List<String> seged = nlp.get("seged");
        List<String> posed = nlp.get("posed");
        List<String> parsed = nlp.get("parsed");
        for (int i = 0; i < seged.size(); i++) {
            List<String> phrases = sentimentProcess.findPhrase(posed.get(i), parsed.get(i));
            List<String> finalPh = sentimentProcess.filterPhrase(phrases);
            finalPhrases.add(StringUtil.listToString(finalPh, " ,"));
            String phraseNumberSeqs = sentimentProcess.calAll(finalPh);
            if (StringUtils.isNotEmpty(phraseNumberSeqs)) {
                seqs.add(phraseNumberSeqs);
            }
        }

        double sentiScore = 0.0, positiveProb = 0.0, negativeProb = 0.0;
        if (sentimentProcess instanceof GoopSentimentProcess) {
            sentiScore = Evaluate.statistics(StringUtil.listToString(seqs, "|"), 1);
        } else if (sentimentProcess instanceof CompSentimentProcess) {
            sentiScore = Evaluate.statistics(StringUtil.listToString(seqs, "|"), 2);
            Map<String, Double> prob = Evaluate.sentiProb(StringUtil.listToString(seqs, "|"));
            positiveProb = prob.get("positiveProb");
            negativeProb = prob.get("negativeProb");
        }
        String segedStr = StringUtil.listToString(seged, " ");
        String posedStr = StringUtil.listToString(posed, " ");
        String parsedStr = StringUtil.listToString(parsed, " ");

        sentiWord = StringUtil.getWord(finalPhrases);
        resultMap.put("input", line);
        resultMap.put("seged", segedStr);
        resultMap.put("posed", posedStr);
        resultMap.put("parsed", parsedStr);
        resultMap.put("finalPhrases", StringUtil.listToString(finalPhrases, " ,"));
        resultMap.put("sentiWord", StringUtil.listToString(sentiWord, " ,"));
        resultMap.put("seqs", StringUtil.listToString(seqs, "|"));
        resultMap.put("positiveProb", String.valueOf(positiveProb));
        resultMap.put("negativeProb", String.valueOf(negativeProb));
        resultMap.put("score", String.valueOf(sentiScore));
        return resultMap;
    }
}
