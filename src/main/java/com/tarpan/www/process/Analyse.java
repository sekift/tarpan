package com.tarpan.www.process;

import com.tarpan.www.Constants;
import com.tarpan.www.nlp.NlpProcess;
import com.tarpan.www.util.FileUtil;
import com.tarpan.www.util.LogUtils;
import com.tarpan.www.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * the example to process a text by the sentiment analysis proposed
 * 
 * @author sekift
 *
 */
public class Analyse {
	// 引入文件
	private static Map<String, Double> sentiDict;
	private static Map<String, Double> nonLinear;
	private static Map<String, String> aspect;
	private static Map<String, Integer> dict;
	private static Set<String> nnSet;
	private static Set<String> vvSet;
	private static Set<String> adSet;
	private static List<String> sumList;
	private static List<String> am;

	static {
		sentiDict = Senti.loadSenti(FileUtil.getDataPath(Constants.SENTI_FILE));
		nonLinear = Senti.loadLexicon(FileUtil.getDataPath(Constants.NONLI_FILE));
		aspect = Senti.loadAspectSenti(FileUtil.getDataPath(Constants.ASPECT_FILE));
		dict = PreProcess.sentiment();
		nnSet = FileUtil.file2Set(FileUtil.getDataPath(Constants.SENTINN_FILE));
		vvSet = FileUtil.file2Set(FileUtil.getDataPath(Constants.SENTIVV_FILE));
		adSet = FileUtil.file2Set(FileUtil.getDataPath(Constants.SENTIAD_FILE));
		sumList = FileUtil.file2List(FileUtil.getDataPath(Constants.SUMMARY_FILE));
		am = FileUtil.file2List(FileUtil.getDataPath(Constants.AM_FILE));
	}

	public static List<String> sentiFly(String line) {
		// 返回的list
		List<String> resultList = new ArrayList<>();
		// nature language processing
		Map<String, List<String>> nlp = NlpProcess.parser(line);
		if(null == nlp || nlp.size() ==0){
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
			LogUtils.logInfo("seged: " + seged+"posed: " + posed+"parsed: " + parsed);
			List<String> phrases = PreProcess.findPhrase(dict, nnSet, vvSet, adSet, sumList, aspect, am, posed.get(i),
					parsed.get(i));
			LogUtils.logInfo("phrases: "+phrases);
			List<String> finalPh = PreProcess.filterPhrase(phrases);
			LogUtils.logInfo("finalPh: "+finalPh);
			fph.add(StringUtil.listToString(finalPh, " ,"));
			String phraseNumberSeqs = Senti.calAll(sentiDict, nonLinear, FileUtil.getDataPath(Constants.ADV_FILE),
					finalPh);
			seqs.add(phraseNumberSeqs);
		}
		
		double senti = Senti.statistics(StringUtil.listToString(seqs, "|"));
		String segedStr = StringUtil.listToString(seged, " ");
		String posedStr = StringUtil.listToString(posed, " ");
		String parsedStr = StringUtil.listToString(parsed, " ");
		resultList.add(segedStr);
		resultList.add(posedStr);
		resultList.add(parsedStr);
		resultList.add(StringUtil.listToString(fph, " ,"));
		resultList.add(StringUtil.listToString(seqs, "|"));
		resultList.add(String.valueOf(senti));
		LogUtils.logInfo("ciyu " + StringUtil.listToString(fph, " ,"));
		LogUtils.logInfo("part " + StringUtil.listToString(seqs, "|"));
		LogUtils.logInfo("sent "+senti);
		return resultList;
	}
	
	public static void main(String args[]){
		System.out.println(sentiFly("周围环境差,房间设施陈旧.房间小.服务员懒散.去餐厅很不方便,路线复杂.早餐贵,而且没有什么可以选择的种类." ));
		System.out.println(sentiFly("上网收费巨高,房间卫生差,找不到服务员,床都坏了没人管.实在太烂了,谁去住就是SB" ));
	}

}
