package com.tarpan.www.pre;

import com.tarpan.www.Constants;
import com.tarpan.www.util.LanguageUtil;
import com.tarpan.www.util.LogUtils;
import com.tarpan.www.util.StringUtil;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.util.List;

/**
 * 数据预处理
 *
 * @author sekift
 */
public class PreProcess {
    public static void main(String[] args) {
        String str = "最近考虑做些英文词语词干化的工作，听说coreNLP这个工具;不错，就'拿来用了。，。core、NLP是斯坦福大！%￥&*学开发；、的一套关于自然语言处理的工具(toolbox)，使用简单功能强大，有;命名实体识别、词性标注、词语词干化、语句语法树的构造还有指代关系等功能，使用起来比较方便。";
        System.out.println(process(str));
    }

    /**
     * 句子的预处理
     *
     * @param input
     * @return
     */
    public static String process(String input) {
        List<String> punctList = LoadFile.getPunct();
        try {
            // 中文繁转简
            input = LanguageUtil.convertToGB2(input);
            // 去掉特殊字符，方便分词
            for(String punct : punctList){
                input = input.replace(punct, ",");
            }

            // TODO 是否作不确定词性过滤
            // TODO 是否作一些分词有问题的进行替换

        } catch (Exception e) {
            LogUtils.logError("[情感分析]文件的预处理出错，", e);
            e.printStackTrace();
        }
        return input.trim();
    }

    /**
     * 文件的预处理
     *
     * @param inPath
     * @param outPath
     */
    public static void process(String inPath, String outPath) {
        List<String> emojiList = LoadFile.getEmoji();
        List<String> unsureList = LoadFile.getUnsure();
        try {
            LineIterator lines = FileUtils.lineIterator(new File(inPath), Charsets.UTF_8.toString());
            while (lines.hasNext()) {
                String line = lines.next().trim();
                if (!StringUtil.isNullOrBlank(line)) {
                    // 中文繁转简
                    line = LanguageUtil.convertToGB2(line);
                    for (String emoji : emojiList) {
                        // 表情转汉字含义
                        String[] arr = emoji.split(Constants.EQUAL_SEP);
                        line = line.replaceAll(arr[1], arr[0]);
                    }
                    //remove intensional verb and something unsure
                    // TODO 是否作IV词性过滤
                    String lineCopy = line;
                    lineCopy = lineCopy.replace("。", "\n").replace("，", "\n").replace(",", "\n");
                    String[] copyArray = lineCopy.split("\n");
                    for (int i = 0; i < copyArray.length; i++) {
                        for (int j = 0; j < unsureList.size(); j++) {
                            if (copyArray[i].contains(unsureList.get(j))) {
                                line = line.replace(copyArray[i], " ");
                            }
                        }
                    }
                    // TODO 是否作一些分词有问题的进行替换
                }
                FileUtils.writeStringToFile(new File(outPath), line,
                        Charsets.UTF_8, true);
            }
        } catch (Exception e) {
            LogUtils.logError("[情感分析]文件的预处理出错，", e);
            e.printStackTrace();
        }
    }
}
