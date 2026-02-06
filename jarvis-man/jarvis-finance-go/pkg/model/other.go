package model

import "time"

// NewsArticle 新闻文章
type NewsArticle struct {
	Title         string    `json:"title"`         // 标题
	Content       string    `json:"content"`       // 内容
	Source        string    `json:"source"`        // 来源
	PublishTime   time.Time `json:"publishTime"`   // 发布时间
	RelatedStocks []string  `json:"relatedStocks"` // 相关股票
	Sentiment     string    `json:"sentiment"`     // 情感分析结果（正面、负面、中性）
}

// TechnicalIndicator 技术指标
type TechnicalIndicator struct {
	StockCode     string                 `json:"stockCode"`     // 股票代码
	IndicatorType string                 `json:"indicatorType"` // 指标类型（MACD、KDJ、RSI等）
	Value         float64                `json:"value"`         // 指标值
	Timestamp     time.Time              `json:"timestamp"`     // 时间戳
	Parameters    map[string]interface{} `json:"parameters"`    // 指标参数
}

// BacktestResult 回测结果
type BacktestResult struct {
	StockCode        string                 `json:"stockCode"`        // 股票代码
	IndicatorType    string                 `json:"indicatorType"`    // 指标类型
	SuccessRate      float64                `json:"successRate"`      // 成功率
	TotalTrades      int                    `json:"totalTrades"`      // 总交易次数
	ProfitableTrades int                    `json:"profitableTrades"` // 盈利交易次数
	StartDate        time.Time              `json:"startDate"`        // 开始日期
	EndDate          time.Time              `json:"endDate"`          // 结束日期
	Parameters       map[string]interface{} `json:"parameters"`       // 回测参数
}

// Alert 告警信息
type Alert struct {
	AlertType string    `json:"alertType"` // 告警类型
	StockCode string    `json:"stockCode"` // 股票代码
	Message   string    `json:"message"`   // 告警消息
	Timestamp time.Time `json:"timestamp"` // 时间戳
	Severity  string    `json:"severity"`  // 严重程度（高、中、低）
	IsRead    bool      `json:"isRead"`    // 是否已读
}

// Recommendation 投资建议
type Recommendation struct {
	StockCode  string    `json:"stockCode"`  // 股票代码
	Suggestion string    `json:"suggestion"` // 建议（买入、卖出、持有）
	Reason     string    `json:"reason"`     // 理由
	RiskLevel  string    `json:"riskLevel"`  // 风险等级
	Timestamp  time.Time `json:"timestamp"`  // 时间戳
	Confidence float64   `json:"confidence"` // 置信度
}

// UserConfig 用户配置
type UserConfig struct {
	UserID                  string             `json:"userId"`                  // 用户ID
	WatchedStocks           []string           `json:"watchedStocks"`           // 关注的股票
	Thresholds              map[string]float64 `json:"thresholds"`              // 监控阈值
	NotificationPreferences map[string]bool    `json:"notificationPreferences"` // 通知偏好
}
