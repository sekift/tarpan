package com.tarpan.www.feature;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.util.List;

public class FileProcess {
    private static final String negFilePathInput = "F:/workspace/data/test/ChnSentiCorp情感分析酒店评论/负面/";
    private static final String posFilePathInput = "F:/workspace/data/test/ChnSentiCorp情感分析酒店评论/正面/";

    private static final String negFilePathOutput = "F:/workspace/data/test/负面/";
    private static final String posFilePathOutput = "F:/workspace/data/test/正面/";

    /**
     * 将文件内容的多行合并成一行
     */
    private static void mergeFilesLine() {
        String negPre = "neg.", posPre = "pos.", pro = ".txt";
        try {
            for (int i = 0; i < 2000; i++) {
                String inPath = posFilePathInput + posPre + i + pro;
                String outPath = posFilePathOutput + posPre + i + pro;
                LineIterator lines = FileUtils.lineIterator(new File(inPath), Charsets.UTF_8.toString());
                StringBuilder sb = new StringBuilder();
                while (lines.hasNext()) {
                    String line = lines.next().trim();
                    sb.append(line);
                }
                FileUtils.writeStringToFile(new File(outPath), sb.toString() + "\n",
                        Charsets.UTF_8, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FileProcess.mergeFilesLine();
    }
}
