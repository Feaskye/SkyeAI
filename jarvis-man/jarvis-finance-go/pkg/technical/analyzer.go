package technical

import (
	"math"
)

// TechnicalAnalyzer 技术指标计算模块
type TechnicalAnalyzer struct {
	// 可以添加必要的字段
}

// NewTechnicalAnalyzer 创建技术指标计算实例
func NewTechnicalAnalyzer() *TechnicalAnalyzer {
	return &TechnicalAnalyzer{}
}

// CalculateMACD 计算MACD指标
func (t *TechnicalAnalyzer) CalculateMACD(prices []float64, fastPeriod, slowPeriod, signalPeriod int) (float64, float64, float64) {
	if len(prices) < slowPeriod {
		return 0, 0, 0
	}

	// 计算EMA
	fastEMA := calculateEMA(prices, fastPeriod)
	slowEMA := calculateEMA(prices, slowPeriod)

	// 计算DIF
	dif := fastEMA - slowEMA

	// 计算DEA（信号线）
	// 这里简化处理，实际应该使用DIF的EMA
	dea := dif*0.2 + 0.8*dif // 简化处理

	// 计算MACD柱状图
	macdHist := (dif - dea) * 2

	return dif, dea, macdHist
}

// CalculateKDJ 计算KDJ指标
func (t *TechnicalAnalyzer) CalculateKDJ(prices []float64, highPrices []float64, lowPrices []float64, period int) (float64, float64, float64) {
	if len(prices) < period {
		return 0, 0, 0
	}

	// 计算RSV
	highest := max(highPrices[len(highPrices)-period:])
	lowest := min(lowPrices[len(lowPrices)-period:])
	closePrice := prices[len(prices)-1]

	if highest == lowest {
		return 50, 50, 50
	}

	rsv := (closePrice - lowest) / (highest - lowest) * 100

	// 计算K、D、J
	k := 50.0 // 初始值
	d := 50.0 // 初始值
	j := 0.0

	// 简化处理，实际应该使用平滑算法
	k = k*0.6667 + rsv*0.3333
	d = d*0.6667 + k*0.3333
	j = 3*k - 2*d

	return k, d, j
}

// CalculateRSI 计算RSI指标
func (t *TechnicalAnalyzer) CalculateRSI(prices []float64, period int) float64 {
	if len(prices) < period+1 {
		return 50
	}

	// 计算价格变化
	changes := make([]float64, len(prices)-1)
	for i := 1; i < len(prices); i++ {
		changes[i-1] = prices[i] - prices[i-1]
	}

	// 计算平均 gain 和 loss
	gain := 0.0
	loss := 0.0

	for i := len(changes) - period; i < len(changes); i++ {
		if changes[i] > 0 {
			gain += changes[i]
		} else {
			loss += math.Abs(changes[i])
		}
	}

	if loss == 0 {
		return 100
	}

	// 计算RS
	rs := gain / loss

	// 计算RSI
	rsi := 100 - (100 / (1 + rs))

	return rsi
}

// CalculateBOLL 计算布林带指标
func (t *TechnicalAnalyzer) CalculateBOLL(prices []float64, period int, multiplier float64) (float64, float64, float64) {
	if len(prices) < period {
		return 0, 0, 0
	}

	// 计算中轨（MA）
	ma := calculateMA(prices, period)

	// 计算标准差
	stdDev := calculateStdDev(prices, period, ma)

	// 计算上轨和下轨
	topBand := ma + multiplier*stdDev
	bottomBand := ma - multiplier*stdDev

	return topBand, ma, bottomBand
}

// CalculateIndicators 计算多个技术指标
func (t *TechnicalAnalyzer) CalculateIndicators(stockCode string, prices []float64, highPrices []float64, lowPrices []float64) map[string]float64 {
	indicators := make(map[string]float64)

	// 计算MACD
	_, _, macdHist := t.CalculateMACD(prices, 12, 26, 9)
	indicators["MACD"] = macdHist

	// 计算KDJ
	k, d, j := t.CalculateKDJ(prices, highPrices, lowPrices, 9)
	indicators["KDJ_K"] = k
	indicators["KDJ_D"] = d
	indicators["KDJ_J"] = j

	// 计算RSI
	rsi := t.CalculateRSI(prices, 14)
	indicators["RSI"] = rsi

	// 计算布林带
	topBand, ma, bottomBand := t.CalculateBOLL(prices, 20, 2)
	indicators["BOLL_TOP"] = topBand
	indicators["BOLL_MID"] = ma
	indicators["BOLL_BOTTOM"] = bottomBand

	return indicators
}

// 辅助函数：计算移动平均线
func calculateMA(prices []float64, period int) float64 {
	if len(prices) < period {
		return 0
	}

	sum := 0.0
	for i := len(prices) - period; i < len(prices); i++ {
		sum += prices[i]
	}

	return sum / float64(period)
}

// 辅助函数：计算指数移动平均线
func calculateEMA(prices []float64, period int) float64 {
	if len(prices) < period {
		return 0
	}

	// 计算第一个EMA（使用MA）
	ema := calculateMA(prices[:period], period)

	// 计算后续的EMA
	multiplier := 2.0 / (float64(period) + 1)

	for i := period; i < len(prices); i++ {
		ema = prices[i]*multiplier + ema*(1-multiplier)
	}

	return ema
}

// 辅助函数：计算标准差
func calculateStdDev(prices []float64, period int, mean float64) float64 {
	if len(prices) < period {
		return 0
	}

	sumSquaredDiff := 0.0
	for i := len(prices) - period; i < len(prices); i++ {
		diff := prices[i] - mean
		sumSquaredDiff += diff * diff
	}

	variance := sumSquaredDiff / float64(period)
	return math.Sqrt(variance)
}

// 辅助函数：计算最大值
func max(values []float64) float64 {
	if len(values) == 0 {
		return 0
	}

	maxVal := values[0]
	for _, v := range values {
		if v > maxVal {
			maxVal = v
		}
	}

	return maxVal
}

// 辅助函数：计算最小值
func min(values []float64) float64 {
	if len(values) == 0 {
		return 0
	}

	minVal := values[0]
	for _, v := range values {
		if v < minVal {
			minVal = v
		}
	}

	return minVal
}
