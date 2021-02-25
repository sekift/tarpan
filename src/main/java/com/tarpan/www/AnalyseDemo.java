package com.tarpan.www;

import com.alibaba.fastjson.JSONObject;
import com.tarpan.www.nlp.NlpProcess;
import com.tarpan.www.pre.PreProcess;
import com.tarpan.www.pro.Evaluate;
import com.tarpan.www.process.AnalyseProcess;
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
 * 一个分析的例子
 *
 * @author sekift
 */
public class AnalyseDemo {

    public static void main(String args[]) {
        SentimentProcess sentimentProcess = new CompSentimentProcess();
        // 单句分析
        System.out.println(AnalyseProcess.sentiFly(sentimentProcess, 1,
                "行政豪华间有点偏贵，房间面积小了！服务总体还可以，就是房间清扫人员不太注意礼貌，有时候刚敲门马上就擅自进来！酒店位置不错！"));
        // 文件分析
//        parserFromFile(sentimentProcess, 1,"F:\\workspace\\data\\test\\posall-parti.txt",
//                "F:\\workspace\\data\\test\\posall-comp-result-vv.txt");
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
                                + " ; " + result.get("seqs") + " ; " + result.get("fph") + "\n",
                        Charsets.UTF_8, true);
                i++;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
