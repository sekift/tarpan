package com.tarpan.www.nlp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.tarpan.www.util.HttpUtil;
import com.tarpan.www.util.StringUtil;

/**
 * 斯坦福分词处理
 * @author sekift
 *
 */
public class NlpProcess {
	
	//分词，ner，pos，依赖什么的
	
	public static Map<String, String> parse(String text){
		Map<String, String> map = new HashMap<String, String>();
		text = "联想的视频软件挺烂,那个噪音吓死人~关声音就没了~勉强接受~";
		//分析，作为例子，下面是结果
		String seged = "联想 的 视频 软件 挺 烂  那个 噪音 吓死 人 ~ 关 声音 就 没 了 ~ 勉强 接受 ~";
		String posed = "联想#NR 的#DEG 视频#NN 软件#NN 挺#AD 烂#VA #PU 那个#DT 噪音#NN 吓死#VV 人#NN ~#PU 关#VV 声音#NN 就#AD 没#VE 了#AS ~#PU 勉强#AD 接受#VV ~#PU";
		String parsed = "nmod:assmod(软件-4 联想-1)   case(联想-1 的-2)   compound:nn(软件-4 视频-3)   nsubj(烂-6 软件-4)   advmod(烂-6 挺-5)   root(ROOT-0 烂-6)   punct(烂-6 -7)   det(噪音-9 那个-8)   nsubj(吓-10 噪音-9)   conj(烂-6 吓-10)   nsubj(接受-19 死人-11)   punct(死人-11 ~-12)   parataxis:prnmod(死人-11 关-13)   dobj(关-13 声音-14)   advmod(没了-16 就-15)   conj(关-13 没了-16)   punct(死人-11 ~-17)   advmod(接受-19 勉强-18)   ccomp(吓-10 接受-19)   punct(烂-6 ~-20)";
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
		Map<String, String> result = parseFromWeb(text);
		Map<String, List<String>> map = new HashMap<String, List<String>>();
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
