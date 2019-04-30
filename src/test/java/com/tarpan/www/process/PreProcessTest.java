package com.tarpan.www.process;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.tarpan.www.util.FileUtil;
import com.tarpan.www.util.StringUtil;

public class PreProcessTest {
	String str = "文字#NN";
	Map<String, Integer> dict = PreProcess.sentiment();
	Set<String> nnSet = FileUtil.file2Set(FileUtil.getDataPath("/data/sentiNN.txt"));
	Set<String> vvSet = FileUtil.file2Set(FileUtil.getDataPath("/data/sentiVV.txt"));
	Set<String> adSet = FileUtil.file2Set(FileUtil.getDataPath("/data/sentiAD.txt"));
	List<String> sumList = FileUtil.file2List(FileUtil.getDataPath("/data/summary.txt"));
	List<String> am = FileUtil.file2List(FileUtil.getDataPath("/data/ambiguity.txt"));
	Map<String, String> aspect = FileUtil.file2DicStr(FileUtil.getDataPath("/data/aspectDICT.txt"));
	
	
	@Test
	public void sentimentLoadTest(){
		int i = 0;
		for (String key : dict.keySet()) {
			i++;
			if (i > 10) {
				break;
			}
		}
	}
	
	@Test
	public void findPhraseTest(){
		List<String> result = PreProcess.findPhrase(dict, nnSet, vvSet, adSet, sumList, aspect, am, 
				"一#CD 个#M 不#AD 看#VV 101#CD 的#DEC 人#NN ，#PU 也#AD 忍#VV 不#AD 住#VV 发微#VV 博为#NR"
				+ " 杨超越#NR 说#VV 几#CD 句#M 话#NN ：#PU 你#PN 恨#VV 一#CD 个#M 人#NN ，#PU 就#AD 在#P 她#PN"
				+ " 的#DEG 性别#NN 里#LC 用#P 最#AD 肮脏#JJ 的#DEG 东西#NN 羞辱#VV 她#PN ，#PU 这样#AD 真的#AD"
				+ " 很#AD 糟糕#VA 。#PU 令#VV 人#NN 恶心#VV 。#PU 而#AD 最#AD 令#VV 人#NN 恶心#VA 的#DEC 是#VC"
				+ " ：#PU 女性#NN 因为#P 性别#NN 而#MSP 产生#VV 的#DEC 肮脏#JJ 咒骂#NN 就#AD 是#VC 比#P 男性#NN"
				+ " 多#AD 很多#CD 。#PU","一#CD   个#M   不#AD   看#VV   101#CD   的#DEC   人#NN   ，#PU   也#AD   忍#VV   不#AD   住#VV   发微#VV   博为#NR   杨超越#NR   说#VV   几#CD   句#M   话#NN   ：#PU   你#PN   恨#VV   一#CD   个#M   人#NN   ，#PU   就#AD   在#P   她#PN   的#DEG   性别#NN   里#LC   用#P   最#AD   肮脏#JJ   的#DEG   东西#NN   羞辱#VV   她#PN   ，#PU   这样#AD   真的#AD   很#AD   糟糕#VA   。#PU   令#VV   人#NN   恶心#VV   。#PU   而#AD   最#AD   令#VV   人#NN   恶心#VA   的#DEC   是#VC   ：#PU   女性#NN   因为#P   性别#NN   而#MSP   产生#VV   的#DEC   肮脏#JJ   咒骂#NN   就#AD   是#VC   比#P   男性#NN   多#AD   很多#CD   。#PU");
		result = PreProcess.filterPhrase(result);
		double sum = 0.0;
		System.out.println(result);
		for(String str : result){
			str = str.trim();
			if(!StringUtil.isNullOrBlank(str)){
				String[] li = str.split("   ");
				for(int i=0;i<li.length;i++){
					double k = 0.0;
					if(dict.containsKey(li[i])){
						double d = dict.get(li[i]);
						System.out.println(li[i]+" "+ d);
					}
				}
			}
		}
		
	    System.out.println(result);
	}
	
	@Test
	public void getWordTest(){
		assertTrue(PreProcess.getWord(str).equals("文字"));
	}
	
	@Test
	public void getLabelTest(){
		assertTrue(PreProcess.getLabel(str).equals("NN"));
	}

	
}
