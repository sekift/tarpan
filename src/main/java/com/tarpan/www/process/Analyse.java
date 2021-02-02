package com.tarpan.www.process;

import com.tarpan.www.nlp.NlpProcess;
import com.tarpan.www.util.LogUtils;
import com.tarpan.www.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * the example to process a text by the sentiment analysis proposed
 *
 * @author sekift
 */
public class Analyse {

    public static List<String> sentiFly(String line) {
        // 返回的list
        List<String> resultList = new ArrayList<>();
        // nature language processing
        Map<String, List<String>> nlp = NlpProcess.parser(line);
        if (null == nlp || nlp.size() == 0) {
            return resultList;
        }

        // opinion phrases and compute the sentiment strength
        List<String> seqs = new ArrayList<>();
        // final phrases
        List<String> fph = new ArrayList<>();
        List<String> seged = nlp.get("seged");
        List<String> posed = nlp.get("posed");
        List<String> parsed = nlp.get("parsed");
        for (int i = 0; i < seged.size(); i++) {
            LogUtils.logInfo("seged: " + seged + "posed: " + posed + "parsed: " + parsed);
            List<String> phrases = SentiProcess.findPhrase(LoadFile.getNegAndPos(),
                    LoadFile.getSentiNN(), LoadFile.getSentiVV(), LoadFile.getSentiAD(), LoadFile.getSummary(),
                    LoadFile.getAspect(), LoadFile.getAmbiguity(), posed.get(i), parsed.get(i));
            LogUtils.logInfo("phrases: " + phrases);
            List<String> finalPh = SentiProcess.filterPhrase(phrases);
            LogUtils.logInfo("finalPh: " + finalPh);
            fph.add(StringUtil.listToString(finalPh, " ,"));
            String phraseNumberSeqs = SentiProcess.calAll(LoadFile.getSentiment(), LoadFile.getNonLinear(),
                    LoadFile.getAdvxxx(), finalPh);
            seqs.add(phraseNumberSeqs);
        }

        double sentiScore = SentiProcess.statistics(StringUtil.listToString(seqs, "|"));
        String segedStr = StringUtil.listToString(seged, " ");
        String posedStr = StringUtil.listToString(posed, " ");
        String parsedStr = StringUtil.listToString(parsed, " ");
        resultList.add(segedStr);
        resultList.add(posedStr);
        resultList.add(parsedStr);
        resultList.add(StringUtil.listToString(fph, " ,"));
        resultList.add(StringUtil.listToString(seqs, "|"));
        resultList.add(String.valueOf(sentiScore));
        LogUtils.logInfo("part " + StringUtil.listToString(seqs, "|"));
        LogUtils.logInfo("sent " + sentiScore);
        return resultList;
    }

    public static void main(String args[]) {
        System.out.println(sentiFly("酒店实在差，房间又小又脏，卫生间环境太差，没有餐厅。"));
    }

}
