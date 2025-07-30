package com.xiaomi.auto.midemo.service;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.nlp.tokenizers.ChineseWordTokenizer;
import com.kennycason.kumo.palette.ColorPalette;
import com.xiaomi.auto.midemo.dto.SocialMediaPost;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.style.Styler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VisualizationService {
    @Value("${visualization.wordcloud.path}")
    private String wordCloudPath;

    @Value("${visualization.chart.path}")
    private String chartPath;

    // 初始化字体和目录
    public VisualizationService() throws IOException {
        // 创建可视化目录（如果不存在）
        Files.createDirectories(Paths.get(wordCloudPath));
        Files.createDirectories(Paths.get(chartPath));
    }

    /**
     * 生成关键词云
     * @param posts 社交媒体帖子列表
     * @param title 关键词云标题
     * @return 生成的图片文件路径
     */
    public String generateKeywordCloud(List<SocialMediaPost> posts, String title) throws IOException, FontFormatException {
        // 提取所有帖子内容
        String allContent = posts.stream()
                .flatMap(post -> Stream.of(post.getContent(), post.getVideoText()))
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));

        // 分析词频
        FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
        frequencyAnalyzer.setWordTokenizer(new ChineseWordTokenizer());
        frequencyAnalyzer.setMinWordLength(2);
        List<WordFrequency> wordFrequencies = frequencyAnalyzer.load(allContent);

        // 创建关键词云
        Dimension dimension = new Dimension(600, 600);
        WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
        wordCloud.setPadding(2);
        wordCloud.setBackground(new CircleBackground(300));
        wordCloud.setColorPalette(new ColorPalette(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE));
        wordCloud.setFontScalar(new LinearFontScalar(12, 48));
        wordCloud.setKumoFont(new KumoFont(Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/fonts/simhei.ttf"))));
        wordCloud.build(wordFrequencies);

        // 保存关键词云图片
        String fileName = title.replaceAll("\\s+", "_") + "_" + System.currentTimeMillis() + ".png";
        String filePath = wordCloudPath + fileName;
        wordCloud.writeToFile(filePath);

        return filePath;
    }

    /**
     * 生成情感时间线图表
     * @param sentimentResults 带有情感分析结果的帖子列表
     * @return 生成的图片文件路径
     */
    public String generateSentimentTimeline(List<SocialMediaPost> sentimentResults) throws IOException {
        // 按日期分组统计情感数量
        Map<String, Map<String, Long>> dateSentimentCounts = sentimentResults.stream()
                .collect(Collectors.groupingBy(
                        post -> parseDate(post.getPublishTime()),
                        Collectors.groupingBy(
                                SocialMediaPost::getSentiment,
                                Collectors.counting()
                        )
                ));

        // 准备图表数据
        List<String> dates = new ArrayList<>(dateSentimentCounts.keySet());
        List<Integer> positiveCounts = dates.stream()
                .map(date -> dateSentimentCounts.get(date).getOrDefault("正面", 0L).intValue())
                .collect(Collectors.toList());
        List<Integer> negativeCounts = dates.stream()
                .map(date -> dateSentimentCounts.get(date).getOrDefault("负面", 0L).intValue())
                .collect(Collectors.toList());
        List<Integer> neutralCounts = dates.stream()
                .map(date -> dateSentimentCounts.get(date).getOrDefault("中性", 0L).intValue())
                .collect(Collectors.toList());

        // 创建图表
        String[] xAxisData = dates.toArray(new String[0]);
        int[] positiveData = positiveCounts.stream().mapToInt(Integer::intValue).toArray();
        int[] negativeData = negativeCounts.stream().mapToInt(Integer::intValue).toArray();
        int[] neutralData = neutralCounts.stream().mapToInt(Integer::intValue).toArray();

        XYChart chart = QuickChart.getChart("情感时间线分析", "日期", "数量", 
                new String[]{"正面", "负面", "中性"}, xAxisData, 
                new int[][]{positiveData, negativeData, neutralData});

        // 图表样式设置
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setDatePattern("yyyy-MM-dd");
        chart.getStyler().setPlotGridLinesVisible(true);

        // 保存图表图片
        String fileName = "sentiment_timeline_" + System.currentTimeMillis() + ".png";
        String filePath = chartPath + fileName;
        new SwingWrapper(chart).saveAsPNG(filePath, 800, 600);

        return filePath;
    }

    /**
     * 解析日期字符串（适配不同平台的日期格式）
     */
    private String parseDate(String dateString) {
        try {
            // 尝试解析常见日期格式
            DateTimeFormatter[] formatters = {
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
            };

            for (DateTimeFormatter formatter : formatters) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
                    return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (Exception e) {
            // 如果解析失败，返回未知日期
            return "未知日期";
        }
        return "未知日期";
    }
}