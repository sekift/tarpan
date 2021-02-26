package com.tarpan.www;

import com.alibaba.fastjson.JSONObject;
import com.tarpan.www.util.HttpUtil;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 百度的情感倾向AI结果
 *
 * @author sekift
 */
public class BaiduSentimentDemo {
    private static final String BAIDU_SENTI_URL = "https://ai.baidu.com/aidemo";

    public static void main(String[] args) {
        String str = "之前06年的时候入住感觉非常好，服务、硬件都不错。";
//        System.out.println(str.length());
//        System.out.println(parserByBaidu(str));
        parserFromFile("F:\\workspace\\data\\test\\negall-two.txt",
                "F:\\workspace\\data\\test\\negall-baidu-result.txt");

    }

    /**
     * 文件处理
     *
     * @param inPath
     * @param outPath
     */
    public static void parserFromFile(String inPath, String outPath) {
        try {
            int i = 1448;
            LineIterator lines = FileUtils.lineIterator(new File(inPath), Charsets.UTF_8.toString());
            while (lines.hasNext()) {
                String line = lines.next().trim();
                if(line.length()>=1050){
                    FileUtils.writeStringToFile(new File(outPath),
                            i + " ; 0.0 ; 0.0 ; 0.0====TOO LONG"  + "\n", Charsets.UTF_8, true);
                }else {
                    String result = parserByBaidu(line);
                    FileUtils.writeStringToFile(new File(outPath),
                            i + " ; " + result + "\n", Charsets.UTF_8, true);
                }
                i++;
                Thread.sleep(2097);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String parserByBaidu(String line) {
        Map<String, String> headers = new HashMap<>(16);
        headers.put("Cookie", "BAIDUID=6ADB4187D81FE4330E2AB4B1C9105686:FG=1; BIDUPSID=BD341C956AA10AA990CBD328398E1095; PSTM=1605505074; Hm_lvt_8b973192450250dd85b9011320b455ba=1613956027,1613956223,1614149678,1614215312; __yjs_duid=1_b365b229f33902719c70339ed077ad7b1611712018783; BDORZ=FFFB88E999055A3F8A630C64834BD6D0; BDSFRCVID=WvKOJeC629IatZOeXUJfhbGMY220KcrTH6aoCTG05-J49vHb8Do8EG0Pef8g0Ku-8Uu8ogKK3gOTH4DF_2uxOjjg8UtVJeC6EG0Ptf8g0M5; H_BDCLCKID_SF=tbCHoK-2JDI3qK_kKPnhKP40KUKX5-CsQmKL2hcH0KLKoCPCe5u5KxKdjhJ35fbubJriaM7YaMb1MRjvXx--2-F…nZLZTJrVnBnSUFBQUFBJCQAAAAAAAAAAAEAAADGAFQIc2VraWZ0AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAALYEM2C2BDNgc; delPer=0; PSINO=6; BDRCVFR[gltLrB7qNCt]=mk3SLVN4HKm; BA_HECTOR=agah8h00alag8k8gfs1g3ek0e0r; ab_sr=1.0.0_ZjhkMWVkNWJjOGIwOTI1ODljOWE4YTUzMmRlZWFiYjg5NjZjMTY2ZmU4Mjg0NGQ3OThjMDlkZmYwMTZiYjQ0MDkzMjYzODUzYTI3MmQxYzA5NWQ2NjI5M2E4ZmUwZWI4; __yjsv5_shitong=1.0_7_7622ea2a7fda8e75eea40e54e4bb0a135364_300_1614237925629_113.109.249.139_4421d1ac; BDRCVFR[Fc9oatPmwxn]=mk3SLVN4HKm");
        Map<String, String> params = new HashMap<>(16);
        params.put("apiType", "nlp");
        params.put("type", "sentimentClassify");
        params.put("t1", line);
        String response = HttpUtil.post(BAIDU_SENTI_URL, params, headers, 10 * 3600, 10 * 3600, "utf-8");

        Map<String, Object> map = JSONObject.parseObject(response);
        System.out.println(map);
        map = JSONObject.parseObject(map.get("data").toString());
        List<Object> list = JSONObject.parseArray(map.get("items").toString());
        Map<String, Object> result = JSONObject.parseObject(list.get(0).toString());
        return result.get("positive_prob") + " ; " + result.get("negative_prob") + " ; "
                + result.get("confidence") + "====" + map.get("text");
    }

}
