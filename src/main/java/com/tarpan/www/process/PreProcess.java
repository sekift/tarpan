package com.tarpan.www.process;

import com.tarpan.www.Constants;
import com.tarpan.www.util.FileUtil;
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

    /**
     * 文件的预处理
     *
     * @param inPath
     * @param outPath
     */
    public static void preProcess(String inPath, String outPath) {
        List<String> emojiList = FileUtil.file2List(FileUtil.getDataPath(Constants.EMOJI_FILE));
        List<String> ivList = FileUtil.file2List(FileUtil.getDataPath(Constants.IV_FILE));
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
                        for (int j = 0; j < ivList.size(); j++) {
                            if (copyArray[i].contains(ivList.get(j))) {
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
