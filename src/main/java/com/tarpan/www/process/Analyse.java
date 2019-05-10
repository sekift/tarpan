package com.tarpan.www.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tarpan.www.Constants;
import com.tarpan.www.nlp.NlpProcess;
import com.tarpan.www.util.FileUtil;
import com.tarpan.www.util.StringUtil;

/**
 * the example to process a text by the sentiment analysis proposed
 * 
 * @author sekift
 *
 */
public class Analyse {
	private static Map<String, Double> sentiDict = new HashMap<String, Double>();
	private static Map<String, Double> nonLinear = new HashMap<String, Double>();
	private static Map<String, Integer> dict = new HashMap<String, Integer>();
	private static Map<String, String> aspect = new HashMap<String, String>();
	private static Set<String> nnSet = new HashSet<String>();
	private static Set<String> vvSet = new HashSet<String>();
	private static Set<String> adSet = new HashSet<String>();
	private static List<String> sumList = new ArrayList<String>();
	private static List<String> am = new ArrayList<String>();

	static {
		sentiDict = Senti.loadSenti(FileUtil.getDataPath(Constants.SENTI_FILE));
		nonLinear = Senti.loadLexicon(FileUtil.getDataPath(Constants.NONLI_FILE));
		dict = PreProcess.sentiment();
		nnSet = FileUtil.file2Set(FileUtil.getDataPath(Constants.SENTINN_FILE));
		vvSet = FileUtil.file2Set(FileUtil.getDataPath(Constants.SENTIVV_FILE));
		adSet = FileUtil.file2Set(FileUtil.getDataPath(Constants.SENTIAD_FILE));
		sumList = FileUtil.file2List(FileUtil.getDataPath(Constants.SUMMARY_FILE));
		aspect = Check.loadAspectsenti(FileUtil.getDataPath(Constants.ASPECT_FILE));
		am = FileUtil.file2List(FileUtil.getDataPath(Constants.AM_FILE));
	}

	public static List<String> sentiFly(String line) {
		List<String> li = new ArrayList<String>();
		// opinion phrases and compute the sentiment strength
		List<String> seqs = new ArrayList<String>();
		List<String> fph = new ArrayList<String>(); // final phrases

		// nature language processing
		Map<String, List<String>> nlp = NlpProcess.parser1(line);
		if(null == nlp || nlp.size() ==0){
			return li;
		}
		List<String> seged = nlp.get("seged");
		List<String> posed = nlp.get("posed");
		List<String> parsed = nlp.get("parsed");
		for (int i = 0; i < seged.size(); i++) {
			System.out.println("seged: " + seged+"posed: " + posed+"parsed: " + parsed);
			List<String> phrases = PreProcess.findPhrase(dict, nnSet, vvSet, adSet, sumList, aspect, am, posed.get(i),
					parsed.get(i));
			System.out.println("phrases: "+phrases);
			List<String> finalPh = PreProcess.filterPhrase(phrases);
			System.out.println("finalPh: "+finalPh);
			fph.add(StringUtil.listToString(finalPh, " ,"));
			String phraseNUMBERseqs = Senti.calAll(sentiDict, nonLinear, FileUtil.getDataPath(Constants.ADV_FILE),
					finalPh);
			//System.out.println("phraseNUMBERseqs: " + phraseNUMBERseqs);
			seqs.add(phraseNUMBERseqs);
		}
		
		double senti = Senti.statistics(StringUtil.listToString(seqs, "|"));
		String segedStr = StringUtil.listToString(seged, " ");
		String posedStr = StringUtil.listToString(posed, " ");
		String parsedStr = StringUtil.listToString(parsed, " ");
		li.add(segedStr);
		li.add(posedStr);
		li.add(parsedStr);
		li.add(StringUtil.listToString(fph, " ,"));
		li.add(StringUtil.listToString(seqs, "|"));
		li.add(String.valueOf(senti));
		System.out.println("ciyu " + StringUtil.listToString(fph, " ,"));
		System.out.println("part " + StringUtil.listToString(seqs, "|"));
		System.out.println("sent "+senti);
		return li;
	}
	
	public static void main(String args[]){
		System.out.println(sentiFly("看上去挺好，实际上并不算高端。"));
	}

}
