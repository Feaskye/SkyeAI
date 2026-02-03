package com.skyeai.jarvis.repository;

import com.skyeai.jarvis.model.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 股票价格Repository
 */
@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {

    /**
     * 获取最近的股票价格
     * @param stockSymbol 股票代码或名称
     * @return 最近的股票价格
     */
    @Query("SELECT sp FROM StockPrice sp WHERE sp.stockSymbol = ?1 ORDER BY sp.timestamp DESC LIMIT 1")
    Optional<StockPrice> findLatestByStockSymbol(String stockSymbol);
}