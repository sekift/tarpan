#Tarpan
European Wild Horse(Tarpan)，欧洲野马于1877年灭绝。 在这里是自主实现的中文细粒度情感分析程序，能够给出词语、句子、段落的具体得分。<br />

## 使用
现在用在歌词情感分析上，参考项目：https://github.com/sekift/quelili 。<br />
 
## 参考以下资料
宾州树《汉语词性标注规范》<br />
词性标记	英文名称	中文名称	例子<br />
AD	adverbs	副词	“还”<br />
AS	Aspect marker	体标记	了，着，过<br />
BA	in ba-const	把/将	把，将<br />
CC	Coordinating conjunction	并列连词	“和”，“与”，“或”，“或者”<br />
CD	Cardinal numbers	数词	“一百”<br />
CS	Subordinating conj	从属连词	若，如果，如<br />
DEC	for relative-clause etc	标句词，关系从句“的”	我买“的”书<br />
DEG	Associative	所有格/联结作用“的”	我“的”书<br />
DER	in V-de construction,and V-de-R	V得，表示结果补语的“得”	跑“得”气喘吁吁<br />
DEV	before VP	表示方式状语的“地”	高兴/VA 地/DEV 说/VV<br />
DT	Determiner	限定词	这<br />
ETC	Tag for words in coordination phrase	"等”，“等等”	科技文教 等/ETC 领域<br />
FW	Foreign words	外语词	ISO<br />
IJ	interjection	感叹词	啊<br />
JJ	Noun-modifier other than nouns	其他名词修饰语	共同/JJ 的/DEG 目的/NN 她/PN 是/VC 女/JJ 的/DEG<br />
LB	in long bei-construction	长“被”	“被”他打了<br />
LC	Localizer	方位词	桌子“上”<br />
M	Measure word	量词	一“间”房子<br />
MSP	Some particles	其他结构助词	他“所”需要的 所，而，以<br />
NN	Common nouns	其他名词，普通名词	桌子<br />
NR	Proper nouns	专有名词	北京<br />
NT	Temporal nouns	时间名词	一月，汉朝<br />
OD	Ordinal numbers	序数词	第一<br />
ON	Onomatopoeia	拟声词	“哗啦啦”<br />
P	Prepositions	介词	“在”<br />
PN	pronouns	代词	“你”，“我”，“他”<br />
PU	Punctuations	标点	，。<br />
SB	in short bei-construction	短“被”	他“被”训了一顿<br />
SP	Sentence-final particle	句末助词	他好 吧/SP<br />
VA	Predicative adjective	谓语形容词	花很 红/VA 红彤彤 雪白 丰富<br />
VC	Copula	系动词	“是”，“为”，“非”<br />
VE	as the main verb	“有”作为主要动词	“有”，“无”<br />
VV	Other verbs	其他动词，普通动词	走，可能，喜欢<br />

## 分词使用斯坦福NLP
CoreNLP：https://github.com/stanfordnlp/CoreNLP/<br />
在线依存关系：http://nlp.stanford.edu:8080/parser/index.jsp<br />
也有集成的，在线的分词，例如例子中使用的，但是性能不怎么样。<br />
