package com.skyeai.jarvis.service;

import com.skyeai.jarvis.model.StockPrice;
import com.skyeai.jarvis.repository.StockPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 股票价格服务
 */
@Service
public class StockPriceService {

    private final StockPriceRepository stockPriceRepository;

    @Autowired
    public StockPriceService(StockPriceRepository stockPriceRepository) {
        this.stockPriceRepository = stockPriceRepository;
    }

    /**
     * 保存股票价格
     * @param stockPrice 股票价格对象
     * @return 保存后的股票价格
     */
    public StockPrice saveStockPrice(StockPrice stockPrice) {
        if (stockPrice.getTimestamp() == null) {
            stockPrice.setTimestamp(LocalDateTime.now());
        }
        return stockPriceRepository.save(stockPrice);
    }

    /**
     * 根据股票代码获取最近的股票价格
     * @param stockSymbol 股票代码
     * @return 最近的股票价格对象
     */
    public Optional<StockPrice> getRecentStockPriceBySymbol(String stockSymbol) {
        return stockPriceRepository.findLatestByStockSymbol(stockSymbol);
    }

    /**
     * 根据ID获取股票价格
     * @param id 股票价格ID
     * @return 股票价格对象
     */
    public Optional<StockPrice> getStockPriceById(Long id) {
        return stockPriceRepository.findById(id);
    }
}
