import pandas as pd
import jieba
import re
import json
from collections import Counter

# 使用 GB18030 编码读取数据
file_path = '四川省6.0级以上历史地震评论数据.csv'
data = pd.read_csv(file_path, encoding='utf-8')

# 提取评论列并进行空值过滤，并保留原始索引
comments = data['评论'].dropna()

# 加载停用词表
stopwords = set()
with open('cn_stopwords.txt', 'r', encoding='utf-8') as f:
    for line in f:
        stopwords.add(line.strip())

# 加载情感词典
positive_words = set()
with open('positive_submit.txt', 'r', encoding='utf-8') as f:
    for line in f:
        positive_words.add(line.strip())

negative_words = set()
with open('negative_submit.txt', 'r', encoding='utf-8') as f:
    for line in f:
        negative_words.add(line.strip())


# 清洗文本
def clean_text(text):
    text = re.sub(r'[^\w\s]', '', text)
    irrelevant_patterns = ['捂脸', '笑哭', '点赞', '哈哈哈', '赞', '表情',
                           '[九转大肠]', '[比心]', '[玫瑰]', '[看]', '[感谢]', '[发怒]',
                           '给你点一百个赞', '强烈建议恢复八抬大轿', '抠鼻', '呲牙']
    for pattern in irrelevant_patterns:
        text = text.replace(pattern, '')
    words = jieba.cut(text)
    return [word for word in words if word not in stopwords and word.strip() != '']


# 分词处理
tokens = [clean_text(comment) for comment in comments]


# 情感分析
def sentiment_analysis(token_list):
    pos_count = sum(1 for word in token_list if word in positive_words)
    neg_count = sum(1 for word in token_list if word in negative_words)
    if pos_count > neg_count:
        return '正面'
    elif neg_count > pos_count:
        return '负面'
    else:
        return '中性'


sentiments = [sentiment_analysis(token) for token in tokens]
data.loc[comments.index, '情感倾向'] = sentiments
sentiment_counts = data['情感倾向'].value_counts().to_dict()

# 高频词统计
flat_tokens = [word for sublist in tokens for word in sublist]
word_freq = Counter(flat_tokens)
top_50_words = word_freq.most_common(50)
top_50_words_list = [{'word': word, 'count': count} for word, count in top_50_words]

# 共现词对
co_occurrence = Counter()
for token_list in tokens:
    for i in range(len(token_list)):
        for j in range(i + 1, len(token_list)):
            if token_list[i] != token_list[j]:
                pair = tuple(sorted([token_list[i], token_list[j]]))
                co_occurrence[pair] += 1

top_20_pairs = co_occurrence.most_common(20)
top_20_pairs_list = [{'pair': list(pair), 'count': count} for pair, count in top_20_pairs]

# 组合为 JSON
result = {
    'sentiment_analysis': sentiment_counts,
    'top_words': top_50_words_list,
    'top_co_occurrence_pairs': top_20_pairs_list
}

# 打印 JSON 给 Java 后端接收
print(json.dumps(result, ensure_ascii=False, indent=2))
