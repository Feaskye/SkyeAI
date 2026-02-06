package model

import "time"

// StockData 股票完整数据
type StockData struct {
	StockCode           string             `json:"stockCode"`           // 股票代码
	StockName           string             `json:"stockName"`           // 股票名称
	CurrentPrice        float64            `json:"currentPrice"`        // 当前价格
	OpenPrice           float64            `json:"openPrice"`           // 开盘价
	HighPrice           float64            `json:"highPrice"`           // 最高价
	LowPrice            float64            `json:"lowPrice"`            // 最低价
	PreviousClose       float64            `json:"previousClose"`       // 前收盘价
	ChangePercent       float64            `json:"changePercent"`       // 涨跌幅
	Volume              int64              `json:"volume"`              // 成交量
	Amount              float64            `json:"amount"`              // 成交额
	MarketCap           float64            `json:"marketCap"`           // 市值
	Timestamp           time.Time          `json:"timestamp"`           // 时间戳
	TechnicalIndicators map[string]float64 `json:"technicalIndicators"` // 技术指标
	Sector              string             `json:"sector"`              // 所属版块
	Status              string             `json:"status"`              // 股票状态（正常、停牌等）
}

// MarketData 大盘数据
type MarketData struct {
	MarketName    string    `json:"marketName"`    // 市场名称
	IndexCode     string    `json:"indexCode"`     // 指数代码
	IndexName     string    `json:"indexName"`     // 指数名称
	CurrentPrice  float64   `json:"currentPrice"`  // 当前价格
	OpenPrice     float64   `json:"openPrice"`     // 开盘价
	HighPrice     float64   `json:"highPrice"`     // 最高价
	LowPrice      float64   `json:"lowPrice"`      // 最低价
	PreviousClose float64   `json:"previousClose"` // 前收盘价
	ChangePercent float64   `json:"changePercent"` // 涨跌幅
	Volume        int64     `json:"volume"`        // 成交量
	Amount        float64   `json:"amount"`        // 成交额
	Timestamp     time.Time `json:"timestamp"`     // 时间戳
	Status        string    `json:"status"`        // 市场状态（开盘、收盘等）
	HotSectors    []string  `json:"hotSectors"`    // 热点版块
}

// NewsItem 新闻条目
type NewsItem struct {
	ID             string    `json:"id"`             // 新闻ID
	Title          string    `json:"title"`          // 标题
	Content        string    `json:"content"`        // 内容
	Source         string    `json:"source"`         // 来源
	PublishTime    time.Time `json:"publishTime"`    // 发布时间
	RelatedStocks  []string  `json:"relatedStocks"`  // 相关股票
	RelatedSectors []string  `json:"relatedSectors"` // 相关版块
	Sentiment      string    `json:"sentiment"`      // 情感分析结果（正面、负面、中性）
	Importance     int       `json:"importance"`     // 重要性（1-5）
	URL            string    `json:"url"`            // 新闻链接
}

// DecisionResult 决策结果
type DecisionResult struct {
	Action         string    `json:"action"`         // 决策动作（BUY、SELL、HOLD）
	Confidence     float64   `json:"confidence"`     // 信心度（0-1）
	Reason         string    `json:"reason"`         // 决策理由
	Timestamp      time.Time `json:"timestamp"`      // 时间戳
	InputData      string    `json:"inputData"`      // 输入数据
	MarketTrend    string    `json:"marketTrend"`    // 市场趋势
	RiskLevel      string    `json:"riskLevel"`      // 风险等级
	ExpectedReturn float64   `json:"expectedReturn"` // 预期收益
}

// MarketDecision 大盘决策
type MarketDecision struct {
	MarketStatus    string    `json:"marketStatus"`    // 市场状态
	Trend           string    `json:"trend"`           // 趋势（UP、DOWN、SIDEWAYS）
	Confidence      float64   `json:"confidence"`      // 信心度
	Recommendation  string    `json:"recommendation"`  // 建议
	Reason          string    `json:"reason"`          // 理由
	Timestamp       time.Time `json:"timestamp"`       // 时间戳
	InputData       string    `json:"inputData"`       // 输入数据
	SupportLevel    float64   `json:"supportLevel"`    // 支撑位
	ResistanceLevel float64   `json:"resistanceLevel"` // 阻力位
}

// Notification 通知信息
type Notification struct {
	Type           string          `json:"type"`                     // 通知类型
	StockCode      string          `json:"stockCode,omitempty"`      // 股票代码
	SectorName     string          `json:"sectorName,omitempty"`     // 版块名称
	Title          string          `json:"title"`                    // 标题
	Content        string          `json:"content"`                  // 内容
	Action         string          `json:"action,omitempty"`         // 动作
	AlertType      string          `json:"alertType,omitempty"`      // 告警类型
	Confidence     float64         `json:"confidence,omitempty"`     // 信心度
	SuccessRate    float64         `json:"successRate,omitempty"`    // 成功率
	Timestamp      time.Time       `json:"timestamp"`                // 时间戳
	Decision       *DecisionResult `json:"decision,omitempty"`       // 决策结果
	MarketDecision *MarketDecision `json:"marketDecision,omitempty"` // 大盘决策
	BacktestResult *BacktestResult `json:"backtestResult,omitempty"` // 回测结果
	Alert          *Alert          `json:"alert,omitempty"`          // 告警信息
}
