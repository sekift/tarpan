package com.tarpan.www;

import com.tarpan.www.process.AnalyseProcess;
import com.tarpan.www.process.SentimentProcess;
import com.tarpan.www.process.impl.CompSentimentProcess;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.util.Map;

/**
 * 一个分析的例子
 *
 * @author sekift
 */
public class AnalyseDemo {

    public static void main(String args[]) {
        SentimentProcess sentimentProcess = new CompSentimentProcess();
        // 单句分析
        System.out.println(AnalyseProcess.sentiFly(sentimentProcess, 1,
                "酒店比较旧，不符合四星，出行不是很方便。"));
        // 文件分析
//        parserFromFile(sentimentProcess, 0,"F:\\workspace\\data\\test\\negall-parti.txt",
//                "F:\\workspace\\data\\test\\negall-comp-result-1.txt");
    }

    /**
     * 文件处理
     *
     * @param sentimentProcess
     * @param inPath
     * @param outPath
     */
    public static void parserFromFile(SentimentProcess sentimentProcess, Integer parserType, String inPath, String outPath) {
        try {
            int i = 1;
            LineIterator lines = FileUtils.lineIterator(new File(inPath), Charsets.UTF_8.toString());
            while (lines.hasNext()) {
                String line = lines.next().trim();
                Map<String, String> result = AnalyseProcess.sentiFly(sentimentProcess, parserType, line);
                FileUtils.writeStringToFile(new File(outPath),
                        i + " ; " + result.get("positiveProb") + " ; " + result.get("negativeProb") + " ; " + result.get("score")
                                + " ; " + result.get("seqs") + " ; " + result.get("sentiWord")
                                + " ; " + result.get("finalPhrases") + "\n",
                        Charsets.UTF_8, true);
                i++;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
