package com.tarpan.www.process;

import java.util.List;

/**
 *
 */
public interface SentimentProcess {

    /**
     * 词性与情感得分
     * @param posedStr
     * @param parsedStr
     * @return
     */
    List<String> findPhrase(String posedStr, String parsedStr);

    /**
     * 分词性打分，去掉不必要的词语
     * @param phrases
     * @return
     */
    List<String> filterPhrase(List<String> phrases);

    /**
     * 最终得分字符串
     * @param finalPh
     * @return
     */
    String calAll(List<String> finalPh);
}
