package com.tarpan.www.process;

import java.util.List;

/**
 *
 */
public interface SentimentProcess {

    List<String> findPhrase(String posedStr, String parsedStr);

    List<String> filterPhrase(List<String> phrases);

    String calAll(List<String> finalPh);
}
