package com.skyeai.jarvis.repository;

import com.skyeai.jarvis.model.PriceChangeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 价格变动信息Repository
 */
@Repository
public interface PriceChangeInfoRepository extends JpaRepository<PriceChangeInfo, Long> {
    /**
     * 根据股票代码获取最近N条价格变动信息，按时间戳降序排列
     * @param stockSymbol 股票代码
     * @param limit 限制数量
     * @return 价格变动信息列表
     */
    List<PriceChangeInfo> findByStockSymbolOrderByTimestampDesc(String stockSymbol, org.springframework.data.domain.Pageable pageable);
}