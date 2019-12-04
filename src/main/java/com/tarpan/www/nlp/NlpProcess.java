package com.tarpan.www.nlp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.tarpan.www.util.HttpUtil;

/**
 * 斯坦福分词处理
 * @author sekift
 *
 */
public class NlpProcess {
	
	//分词，ner，pos，依赖什么的
	
	public static Map<String, String> parse(String text){
		Map<String, String> map = new HashMap<String, String>();
		text = "中国科幻扛鼎之作。前两部给我的感觉是精彩，到第三部就是震撼了！值得细细读，慢慢读，反复读。";
		//分析，作为例子，下面是结果
		String seged = "中国 科幻 扛鼎之作 。 前 两 部 给我 的 感觉 是 精彩 ， 到 第三 部 就 是 震撼 了 ！ 值得 细细 读 ， 慢慢 读 ， 反复 读 。";
		String posed = "中国#NR 科幻#JJ 扛鼎之作#NN 。#PU 前#DT 两#CD 部#M 给我#NN 的#DEG 感觉#NN 是#VC 精彩#VA ，#PU 到#P 第三#OD 部#M 就#AD 是#VC 震撼#VV 了#AS ！#PU 值得#VV 细细#AD 读#VV ，#PU 慢慢#AD 读#VV ，#PU 反复#AD 读#VV 。#PU";
		String parsed = "nmod(扛鼎之作-3, 中国-1)   amod(扛鼎之作-3, 科幻-2)   nmod:topic(精彩-12, 扛鼎之作-3)   punct(精彩-12, 。-4)   det(给我-8, 前-5)   dep(前-5, 两-6)   mark:clf(两-6, 部-7)   nmod:assmod(感觉-10, 给我-8)   case(给我-8, 的-9)   nsubj(精彩-12, 感觉-10)   cop(精彩-12, 是-11)   root(ROOT-0, 精彩-12)   punct(精彩-12, ，-13)   case(第三-15, 到-14)   nmod:prep(震撼-19, 第三-15)   mark:clf(第三-15, 部-16)   advmod(震撼-19, 就-17)   cop(震撼-19, 是-18)   conj(精彩-12, 震撼-19)   aux:asp(震撼-19, 了-20)   punct(精彩-12, ！-21)   conj(精彩-12, 值得-22)   advmod(读-24, 细细-23)   ccomp(值得-22, 读-24)   punct(读-24, ，-25)   advmod(读-27, 慢慢-26)   conj(读-24, 读-27)   punct(读-24, ，-28)   advmod(读-30, 反复-29)   conj(读-24, 读-30)   punct(精彩-12, 。-31)";
		map.put("seged", seged);
		map.put("posed", posed);
		map.put("parsed", parsed);
		return map;
	}
	
	/**
	 * 从http://nlp.stanford.edu:8080/parser/index.jsp获取分词结果
	 */
	public static Map<String, String> parseFromWeb(String text){
		Map<String, String> map = new HashMap<String, String>();
		if(text.length()>72){
			System.out.println("句子长度为：" + text.length()+" ,已经大于web分词的72个字符限制。");
			return map;
		}
		
		String url = "http://nlp.stanford.edu:8080/parser/index.jsp";
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("chineseParseButton","剖析 (Parse)");
		params.put("query",text);
		params.put("parserSelect","Chinese");
		params.put("parse","剖析 (Parse)");
		String response = HttpUtil.post(url.toString(), params, null, 10 * 3600, 10 * 3600, "utf-8");
		Document doc = Jsoup.parse(response);
		// 第一个是原句，第二个是
		Elements parserOutputMonospace = doc.getElementsByClass("parserOutputMonospace");
		Elements spacingFree = doc.getElementsByClass("spacingFree");
	
		map.put("seged", parserOutputMonospace.get(0).text());
		map.put("posed", parserOutputMonospace.get(1).text().replaceAll("/", "#"));
		String parse = spacingFree.get(1).text();
		parse = parse.replaceAll("\\)", "\\)   ").trim();
		map.put("parsed", parse);
		return map;
	}
	
	public static void main(String args[]){
		System.out.println(parseFromWeb("太差了。"));
	}
	
	/**
	 * 看另外一个项目
	 * 数据包括：
	 * 	seged = []
        posed = []
        parsed = []
        
	 * 返回的数据是：
	 * 1、seged: [My, dog, also, likes, eating, sausage, .]
	 * 2、posed: [My#PRP$, dog#NN, also#RB, likes#VBZ, eating#JJ, sausage#NN, .#PU]
	 * 3、parsed: [root(ROOT-0, likes-4), nmod:poss(dog-2, My-1), nsubj(likes-4, dog-2), advmod(likes-4, also-3), dobj(likes-4, sausage-6), punct(likes-4, .-7), amod(sausage-6, eating-5)]
	 * 经过转换后的数据为：
	 * 1、seged: My dog also likes eating sausage .
	 * 2、posed: My#PRP$ dog#NN also#RB likes#VBZ eating#JJ sausage#NN .#PU
	 * 3、parsed: root(ROOT-0, likes-4)   nmod:poss(dog-2, My-1)   nsubj(likes-4, dog-2)   advmod(likes-4, also-3)   dobj(likes-4, sausage-6)   punct(likes-4, .-7)   amod(sausage-6, eating-5)
	 *
	 */
	public static Map<String, List<String>> parser(String text){
		Map<String, String> result = parseFromWeb(text); //
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		if(result == null || result.size() == 0){
			return map;
		}
		List<String> seList = new ArrayList<String>();
		List<String> poList = new ArrayList<String>();
		List<String> paList = new ArrayList<String>();
		
		seList.add(result.get("seged"));
		poList.add(result.get("posed"));
		paList.add(result.get("parsed"));
		map.put("seged", seList);
		map.put("posed", poList);
		map.put("parsed", paList);
		return map;
	}

	public static Map<String, List<String>> parser1(String text){
		return parser(text);
	}
}
