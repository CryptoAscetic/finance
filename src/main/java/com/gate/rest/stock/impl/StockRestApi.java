package com.gate.rest.stock.impl;

import com.gate.rest.base.GateConfig;
import com.gate.rest.stock.IStockRestApi;
import com.gate.rest.util.HttpUtilManager;
import com.gate.rest.util.StringUtil;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author huang_fengge
 * @Description //接口文档https://www.gate.tv/cn/api2
 * @Date 下午2:00 2021/12/29
 * @Param
 * @return
**/
public class StockRestApi implements IStockRestApi{


	private String url_pre;



	public StockRestApi(String url_prex){
		this.url_pre = url_prex;
	}



	public final String PAIRS_URL = "/api2/1/pairs";


	public final String MARKETINFO_URL = "/api2/1/marketinfo";


	public final String MARKETLIST_URL = "/api2/1/marketlist";


	public final String TICKERS_URL = "/api2/1/tickers";

	
	public final String TICKER_URL = "/api2/1/ticker";

	
	public final String ORDERBOOK_URL = "/api2/1/orderBook";


	public final String BALANCE_URL = "/api2/1/private/balances";

	
	private final String DEPOSITADDRESS_URL = "/api2/1/private/depositAddress";

	
	private final String DEPOSITESWITHDRAWALS_URL = "/api2/1/private/depositsWithdrawals";

	
	private final String BUY_URL = "/api2/1/private/buy";

	
	private final String SELL_URL = "/api2/1/private/sell";


	private final String CANCELORDER_URL = "/api2/1/private/cancelOrder";

	
	private final String CANCELALLORDERS_URL = "/api2/1/private/cancelAllOrders";

	
	private final String GETORDER_URL = "/api2/1/private/getOrder";


	private final String OPENORDERS_URL = "/api2/1/private/openOrders";

	
	private final String TRADEHISTORY_URL = "/api2/1/tradeHistory";


	private final String WITHDRAW_URL = "/api2/1/private/withdraw";
	

	private final String MYTRADEHISTORY_URL = "/api2/1/private/tradeHistory";


	@Override
	public String pairs() throws HttpException, IOException {
		HttpUtilManager httpUtil = HttpUtilManager.getInstance();
		String param = "";
		String result = httpUtil.requestHttpGet(url_pre, PAIRS_URL, param);
	    return result;
	}

	@Override
	public String marketInfo() throws HttpException, IOException {
		HttpUtilManager httpUtil = HttpUtilManager.getInstance();
		String param = "";
		String result = httpUtil.requestHttpGet(url_pre, MARKETINFO_URL, param);
		return result;
	}

	@Override
	public String marketList() throws HttpException, IOException {
		HttpUtilManager httpUtil = HttpUtilManager.getInstance();
		String param = "";
		String result = httpUtil.requestHttpGet(url_pre, MARKETLIST_URL, param);
		return result;
	}

	@Override
	public String tickers() throws HttpException, IOException {
		HttpUtilManager httpUtil = HttpUtilManager.getInstance();
		String param = "";
		String result = httpUtil.requestHttpGet(url_pre, TICKERS_URL, param);
		return result;
	}

	@Override
	public String ticker(String symbol) throws HttpException, IOException {
		HttpUtilManager httpUtil = HttpUtilManager.getInstance();
		String param = "";
		
		param += "/"+symbol;
		
		String result = httpUtil.requestHttpGet(url_pre, TICKER_URL + param, "");
		return result;
	}

	@Override
	public String orderBook(String symbol) throws HttpException, IOException {
		HttpUtilManager httpUtil = HttpUtilManager.getInstance();
		String param = "";
		if(!StringUtil.isEmpty(symbol)) {
			if(param.equals("")) {
				param += "/";
			}
			param += symbol;
		}
		String result = httpUtil.requestHttpGet(url_pre, ORDERBOOK_URL + param, param);
		return result;
	}

	@Override
	public String tradeHistory(String symbol) throws HttpException, IOException {
		HttpUtilManager httpUtil = HttpUtilManager.getInstance();
		String param = "";
		if(!StringUtil.isEmpty(symbol )) {
			if(param.equals("")) {
				param += "/";
			}
			param += symbol;
		}
		System.out.println(TRADEHISTORY_URL+param);
		String result = httpUtil.requestHttpGet(url_pre, TRADEHISTORY_URL + param, "");
		return result;
	}

	@Override
	public String balance(GateConfig gateConfig) throws HttpException, IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.putAll(Map.of("key", gateConfig.getKey(), "secret", gateConfig.getSecret()));
		HttpUtilManager httpUtil = HttpUtilManager.getInstance();

		String result = httpUtil.doRequest("data", "post", url_pre + BALANCE_URL, params);
		return result;
	}


	@Override
	public String depositAddress(GateConfig gateConfig, String symbol) throws HttpException, IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("currency", symbol);
		params.putAll(Map.of("key", gateConfig.getKey(), "secret", gateConfig.getSecret()));

		HttpUtilManager httpUtil = HttpUtilManager.getInstance();
		String result = httpUtil.doRequest("data", "post", url_pre + DEPOSITADDRESS_URL, params);
		return result;
	}


	@Override
	public String depositsWithdrawals(GateConfig gateConfig, String startTime, String endTime) throws HttpException, IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("start", startTime);
		params.put("end", endTime);
		params.putAll(Map.of("key", gateConfig.getKey(), "secret", gateConfig.getSecret()));
		HttpUtilManager httpUtil = HttpUtilManager.getInstance();
		String result = httpUtil.doRequest("data", "post", url_pre + DEPOSITESWITHDRAWALS_URL, params);
		return result;
	}

	@Override
	public String buy(GateConfig gateConfig, String currencyPair, String rate, String amount) throws HttpException, IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("currencyPair", currencyPair);
		params.put("rate", rate);
		params.put("amount", amount);
		params.putAll(Map.of("key", gateConfig.getKey(), "secret", gateConfig.getSecret()));

		HttpUtilManager httpUtil = HttpUtilManager.getInstance();
		String result = httpUtil.doRequest("data", "post", url_pre + BUY_URL, params);
		return result;
	}

	@Override
	public String sell(GateConfig gateConfig, String currencyPair, String rate, String amount) throws HttpException, IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("currencyPair", currencyPair);
		params.put("rate", rate);
		params.put("amount", amount);
		params.putAll(Map.of("key", gateConfig.getKey(), "secret", gateConfig.getSecret()));

		HttpUtilManager httpUtil = HttpUtilManager.getInstance();
		String result = httpUtil.doRequest("data", "post", url_pre + SELL_URL, params);
		return result;
	}
	
	
	@Override
	public String cancelOrder(String orderNumber,String currencyPair) throws HttpException, IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("orderNumber", orderNumber);
		params.put("currencyPair", currencyPair);

		HttpUtilManager httpUtil = HttpUtilManager.getInstance();
		String result = httpUtil.doRequest( "data", "post", url_pre + CANCELORDER_URL, params );
		return result;
	}
	
	@Override
	public String cancelAllOrders(String type,String currencyPair) throws HttpException, IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("type", type);
		params.put("currencyPair", currencyPair);

		HttpUtilManager httpUtil = HttpUtilManager.getInstance();
		String result = httpUtil.doRequest( "data", "post", url_pre + CANCELALLORDERS_URL, params );
		return result;
	}
	
	@Override
	public String getOrder(String orderNumber,String currencyPair) throws HttpException, IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("orderNumber", orderNumber);
		params.put("currencyPair", currencyPair);

		HttpUtilManager httpUtil = HttpUtilManager.getInstance();
		String result = httpUtil.doRequest( "data", "post", url_pre + GETORDER_URL, params );
		return result;
	}
	
	
	@Override
	public String openOrders() throws HttpException, IOException {
		Map<String, String> params = new HashMap<String, String>();


		HttpUtilManager httpUtil = HttpUtilManager.getInstance();
		String result = httpUtil.doRequest( "data", "post", url_pre + OPENORDERS_URL, params );
		return result;
	}
	
	
	@Override
	public String myTradeHistory(String currencyPair,String orderNumber) throws HttpException, IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("currencyPair", currencyPair);
		params.put("orderNumber", orderNumber);

		HttpUtilManager httpUtil = HttpUtilManager.getInstance();
		String result = httpUtil.doRequest( "data", "post", url_pre + MYTRADEHISTORY_URL, params );
		return result;
	}

	
	@Override
	public String withdraw(String currency,String amount, String address) throws HttpException, IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("currency", currency);
		params.put("amount", amount);
		params.put("address", address);

		HttpUtilManager httpUtil = HttpUtilManager.getInstance();
		String result = httpUtil.doRequest( "data", "post", url_pre + WITHDRAW_URL, params );
		return result;
	}






	public String getUrl_pre() {
		return url_pre;
	}

	public void setUrl_pre(String url_pre) {
		this.url_pre = url_pre;
	}

}
