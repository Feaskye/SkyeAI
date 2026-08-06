package com.skyeai.jarvis.rag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * RRF融合算法实现
 * Reciprocal Rank Fusion用于融合多个检索结果
 */
@Slf4j
@Component
public class RrfFusion {

    /**
     * RRF常数K
     * 用于调整融合时的衰减速度
     */
    private static final int RRF_CONSTANT_K = 60;

    /**
     * 融合多个检索结果
     * @param resultLists 多个检索结果列表
     * @param <T> 结果类型
     * @return 融合后的结果列表
     */
    public <T> List<RrfResult<T>> fuse(List<? extends List<? extends RankedItem<T>>> resultLists) {
        if (resultLists == null || resultLists.isEmpty()) {
            return Collections.emptyList();
        }

        // 如果只有一个结果列表，直接返回
        if (resultLists.size() == 1) {
            return resultLists.get(0).stream()
                    .map(item -> new RrfResult<>(item.getItem(), item.getScore(), 1))
                    .toList();
        }

        // RRF分数映射
        Map<T, Double> rrfScores = new HashMap<>();
        Map<T, Integer> hitCount = new HashMap<>();
        Map<T, Set<Integer>> sourceMap = new HashMap<>();

        // 对每个结果列表进行RRF计算
        for (int listIndex = 0; listIndex < resultLists.size(); listIndex++) {
            List<? extends RankedItem<T>> list = resultLists.get(listIndex);

            for (int rank = 0; rank < list.size(); rank++) {
                T item = list.get(rank).getItem();
                double originalScore = list.get(rank).getScore();

                // RRF公式: score += 1.0 / (k + rank)
                double rrfScore = 1.0 / (RRF_CONSTANT_K + rank + 1);
                rrfScores.merge(item, rrfScore, Double::sum);

                // 记录命中次数
                hitCount.merge(item, 1, Integer::sum);

                // 记录来源列表
                sourceMap.computeIfAbsent(item, k -> new HashSet<>()).add(listIndex);
            }
        }

        // 排序并构建结果
        List<RrfResult<T>> results = new ArrayList<>();
        rrfScores.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .forEach(entry -> {
                    T item = entry.getKey();
                    double rrfScore = entry.getValue();
                    double avgScore = rrfScore / resultLists.size();
                    int hits = hitCount.getOrDefault(item, 0);
                    Set<Integer> sources = sourceMap.getOrDefault(item, Collections.emptySet());

                    results.add(new RrfResult<>(item, avgScore, hits, sources.size(), sources));
                });

        return results;
    }

    /**
     * 简单融合（只用排名信息）
     */
    public <T> List<T> simpleFuse(List<? extends List<T>> resultLists) {
        Map<T, Double> rrfScores = new HashMap<>();

        for (List<T> list : resultLists) {
            for (int rank = 0; rank < list.size(); rank++) {
                T item = list.get(rank);
                double rrfScore = 1.0 / (RRF_CONSTANT_K + rank + 1);
                rrfScores.merge(item, rrfScore, Double::sum);
            }
        }

        List<T> results = new ArrayList<>();
        rrfScores.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .forEach(entry -> results.add(entry.getKey()));

        return results;
    }

    /**
     * 获取RRF常数K
     */
    public static int getConstantK() {
        return RRF_CONSTANT_K;
    }

    /**
     * 排名项接口
     */
    public interface RankedItem<T> {
        T getItem();
        double getScore();
    }

    /**
     * RRF结果
     */
    @lombok.Data
    public static class RrfResult<T> {
        /**
         * 结果项
         */
        private T item;

        /**
         * RRF平均分数
         */
        private double rrfScore;

        /**
         * 命中次数
         */
        private int hitCount;

        /**
         * 来源数量
         */
        private int sourceCount;

        /**
         * 来源列表索引
         */
        private Set<Integer> sources;

        /**
         * 全参数构造函数
         */
        public RrfResult(T item, double rrfScore, int hitCount, int sourceCount, Set<Integer> sources) {
            this.item = item;
            this.rrfScore = rrfScore;
            this.hitCount = hitCount;
            this.sourceCount = sourceCount;
            this.sources = sources;
        }

        /**
         * 简化构造函数
         */
        public RrfResult(T item, double rrfScore, int hitCount) {
            this.item = item;
            this.rrfScore = rrfScore;
            this.hitCount = hitCount;
            this.sourceCount = 1;
            this.sources = new HashSet<>();
        }
    }
}