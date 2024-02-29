package com.gate.rest.stock;

import com.gate.rest.base.GateConfig;
import org.apache.http.HttpException;

import java.io.IOException;


/**
 * @author hfg
 */
public interface IStockRestApi {
	/**
	 * 获取交易对
	 *
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	String pairs() throws HttpException, IOException;

	/**
	 * 获取信息
	 *
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	String marketInfo() throws HttpException, IOException;

	/**
	 * 返回所有系统支持的交易市场的详细行情和币种信息，包括币种名，市值，供应量，最新价格，涨跌趋势，价格曲线等。
	 *
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	String marketList() throws HttpException, IOException;

	/**
	 * 返回系统支持的所有交易对的 最新，最高，最低 交易行情和交易量，每20秒钟更新:
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	String tickers() throws HttpException, IOException;

	/**
	 * 交易对
	 *
	 * @param symbol
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	String ticker(String symbol) throws HttpException, IOException;

	/**
	 * 当前市场深度 API
	 * 返回当前市场深度（委托挂单），其中 asks 是委卖单, bids 是委买单。
	 * @param symbol
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	String orderBook(String symbol) throws HttpException, IOException;

	/**
	 * 返回最新80条历史成交记录：
	 * <p>
	 * URL: https://data.gateapi.io/api2/1/tradeHistory/[CURR_A]_[CURR_B]
	 *
	 * @param symbol
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	String tradeHistory(String symbol) throws HttpException, IOException;

	/**
	 * 获取钱包
	 *
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	String balance(GateConfig gateConfig) throws HttpException, IOException;

	/**
	 * @param gateConfig
	 * @param symbol
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	String depositAddress(GateConfig gateConfig, String symbol) throws HttpException, IOException;

	/**
	 * 获取充值提现历史
	 *
	 * @param startTime
	 * @param endTime
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	String depositsWithdrawals(GateConfig gateConfig, String startTime, String endTime) throws HttpException, IOException;

	/**
	 * 购买
	 *
	 * @param currencyPair
	 * @param rate
	 * @param amount
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 */
	String buy(GateConfig gateConfig, String currencyPair, String rate, String amount) throws HttpException, IOException;

	String sell(GateConfig gateConfig, String currencyPair, String rate, String amount) throws HttpException, IOException;

	String cancelOrder(String orderNumber, String currencyPair) throws HttpException, IOException;

	String cancelAllOrders(String type, String currencyPair) throws HttpException, IOException;

	String getOrder(String orderNumber, String currencyPair) throws HttpException, IOException;

	String openOrders() throws HttpException, IOException;

	String myTradeHistory(String currencyPair, String orderNumber) throws HttpException, IOException;

	String withdraw(String currency, String amount, String address) throws HttpException, IOException;

}
