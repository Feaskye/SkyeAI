package model

import "time"

// StockInfo 股票基础信息
type StockInfo struct {
	StockCode   string    `json:"stockCode"`   // 股票代码
	StockName   string    `json:"stockName"`   // 股票名称
	Sector      string    `json:"sector"`      // 所属版块
	Market      string    `json:"market"`      // 市场（上证、深证等）
	ListingDate time.Time `json:"listingDate"` // 上市日期
}

// StockPrice 股票价格数据
type StockPrice struct {
	StockCode     string    `json:"stockCode"`     // 股票代码
	Price         float64   `json:"price"`         // 价格
	Volume        int64     `json:"volume"`        // 成交量
	Timestamp     time.Time `json:"timestamp"`     // 时间戳
	ChangePercent float64   `json:"changePercent"` // 涨跌幅
}

// IndexData 指数数据
type IndexData struct {
	IndexCode     string    `json:"indexCode"`     // 指数代码
	IndexName     string    `json:"indexName"`     // 指数名称
	Price         float64   `json:"price"`         // 价格
	Volume        int64     `json:"volume"`        // 成交量
	Timestamp     time.Time `json:"timestamp"`     // 时间戳
	ChangePercent float64   `json:"changePercent"` // 涨跌幅
}

// SectorData 版块数据
type SectorData struct {
	SectorCode    string    `json:"sectorCode"`    // 版块代码
	SectorName    string    `json:"sectorName"`    // 版块名称
	Price         float64   `json:"price"`         // 价格
	ChangePercent float64   `json:"changePercent"` // 涨跌幅
	Timestamp     time.Time `json:"timestamp"`     // 时间戳
}
