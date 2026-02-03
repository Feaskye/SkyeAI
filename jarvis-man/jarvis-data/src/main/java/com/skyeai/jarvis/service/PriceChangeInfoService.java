package com.skyeai.jarvis.service;

import com.skyeai.jarvis.model.PriceChangeInfo;
import com.skyeai.jarvis.repository.PriceChangeInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 价格变动信息服务
 */
@Service
public class PriceChangeInfoService {

    private final PriceChangeInfoRepository priceChangeInfoRepository;

    @Autowired
    public PriceChangeInfoService(PriceChangeInfoRepository priceChangeInfoRepository) {
        this.priceChangeInfoRepository = priceChangeInfoRepository;
    }

    /**
     * 保存价格变动信息
     * @param priceChangeInfo 价格变动信息对象
     * @return 保存后的价格变动信息
     */
    public PriceChangeInfo savePriceChangeInfo(PriceChangeInfo priceChangeInfo) {
        if (priceChangeInfo.getTimestamp() == null) {
            priceChangeInfo.setTimestamp(LocalDateTime.now());
        }
        return priceChangeInfoRepository.save(priceChangeInfo);
    }

    /**
     * 根据股票代码获取最近的价格变动信息
     * @param stockSymbol 股票代码
     * @param limit 限制数量
     * @return 价格变动信息列表
     */
    public List<PriceChangeInfo> getRecentPriceChangeInfo(String stockSymbol, int limit) {
        return priceChangeInfoRepository.findByStockSymbolOrderByTimestampDesc(stockSymbol, PageRequest.of(0, limit));
    }

    /**
     * 获取所有价格变动信息
     * @return 价格变动信息列表
     */
    public List<PriceChangeInfo> getAllPriceChangeInfo() {
        return priceChangeInfoRepository.findAll();
    }
}
