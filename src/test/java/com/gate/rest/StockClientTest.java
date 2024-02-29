package com.gate.rest;

import com.gate.rest.base.GateConfig;
import com.gate.rest.entity.Price;
import com.gate.rest.stock.IStockRestApi;
import com.gate.rest.stock.impl.StockRestApi;
import com.gate.rest.util.JsonTool;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author ：huang_fengge
 * @date ：Created in 2021/12/29 上午9:11
 * @description：
 * @modified By：
 * @version: $
 */
@SpringBootTest
@Log4j2
class StockClientTest {
    @Autowired
    GateConfig gateConfig;
    //请求查询的地址
    public static final String QUERY_URL = "https://data.gateapi.io";
    //请求交易地址
    public static final String TRADE_URL = "https://data.gateapi.io";


    /**
     * 获取交易对
     *
     * @throws HttpException
     * @throws IOException
     */
    @Test
    public void getPairsTest() throws HttpException, IOException {

        IStockRestApi stockGet = new StockRestApi(QUERY_URL);

        // All trading Pairs
        String pairs = stockGet.pairs();
        log.info(pairs);


        // Place order buy
        // String buy = stockPost.buy("ltc_btc", "999","123");
        // log.info(buy);

        // Place order sell
        // String sell = stockPost.sell("ltc_btc", "999","123");
        // log.info(sell);

        // Cancel order
        // String cancelOrder = stockPost.cancelOrder("123456789", "ltc_btc");
        // log.info(cancelOrder);

        // Cancel all orders
        // String cancelAllOrders = stockPost.cancelAllOrders("1", "ltc_btc");
        // log.info(cancelAllOrders);

        // Get order status
        // String getOrder = stockPost.getOrder("123456789", "ltc_btc");
        // log.info(getOrder);

        // Get my open order list
        // String openOrders = stockPost.openOrders();
        // log.info(openOrders);

        // Get my last 24h trades
        // String myTradeHistory = stockPost.myTradeHistory("eth_btc","123456789");
        // log.info(myTradeHistory);

        // withdrawal
        // String withdraw = stockPost.withdraw("btc","99","your addr");
        // log.info(withdraw);

    }


    /**
     * 返回所有系统支持的交易市场的详细行情和币种信息，包括币种名，市值，供应量，最新价格，涨跌趋势，价格曲线等。
     *
     * @throws HttpException
     * @throws IOException
     */
    @Test
    public void getMarketList() throws HttpException, IOException {

        IStockRestApi stockGet = new StockRestApi(QUERY_URL);
        // Market Details
        String marketList = stockGet.marketList();
        log.info(marketList);
    }

    /**
     * @throws HttpException
     * @throws IOException
     */
    @Test
    public void getMarketInfo() throws HttpException, IOException {

        IStockRestApi stockGet = new StockRestApi(QUERY_URL);
        // Market Details
        String marketInfo = stockGet.marketInfo();
        log.info(marketInfo);
    }

    /**
     * 交易对查询
     *
     * @throws HttpException
     * @throws IOException
     */
    @Test
    public void getTicker() throws HttpException, IOException {
        IStockRestApi stockGet = new StockRestApi(QUERY_URL);
        String supeSymbol = "supe_usdt";
        String lionSymbol = "lion_usdt";
        String coinInfo = stockGet.ticker(supeSymbol);
        log.info("Supe价格：{}", coinInfo);
        String lionInfo = stockGet.ticker(lionSymbol);
        log.info("Lion价格：{}", lionInfo);
    }

    /**
     * 返回当前市场深度（委托挂单），其中 asks 是委卖单, bids 是委买单。
     *
     * @throws HttpException
     * @throws IOException
     */
    @Test
    public void getOrderBook() throws HttpException, IOException {
        IStockRestApi stockGet = new StockRestApi(QUERY_URL);
        // Depth
        String orderBook = stockGet.orderBook("supe_usdt");
        log.info(orderBook);
    }

    /**
     * 返回最新80条历史成交记录
     *
     * @throws HttpException
     * @throws IOException
     */
    @Test
    public void getTradeHistory() throws HttpException, IOException {
        IStockRestApi stockGet = new StockRestApi(QUERY_URL);
        // Depth
        String orderBook = stockGet.tradeHistory("supe_usdt");
        log.info(orderBook);
    }


    /**
     * 获取金币
     * <p>
     * {
     * "result": "true",
     * "available": {
     * "BNB": "0.00000018",
     * "POINT": "0.0000437",
     * "BABYDOGE": "0",
     * "UNQ": "3.55033827",
     * "TRX": "0",
     * "SNTR": "5.72412705",
     * "DAO": "0",
     * "MDX": "0.00000095",
     * "DOG": "0",
     * "ICP": "0.0012789",
     * "USDT": "0.00255092",
     * "ALTB": "78.57814295",
     * "FROG": "65.00582458",
     * "AART": "21.1262439",
     * "GT": "0.00000076",
     * "IZI": "2.516664",
     * "LION": "0.00000027",
     * "SQUID": "0.00000045",
     * "HIGH": "0.02714634",
     * "SUPE": "182.95879695",
     * "USDG": "0.00000053",
     * "GTC": "0.00000042"
     * },
     * "locked": {
     * "BNB": "0",
     * "POINT": "0",
     * "BABYDOGE": "0",
     * "UNQ": "0",
     * "TRX": "0",
     * "SNTR": "0",
     * "DAO": "0",
     * "MDX": "0",
     * "DOG": "0",
     * "ICP": "0",
     * "USDT": "0",
     * "ALTB": "0",
     * "FROG": "346.41",
     * "AART": "0",
     * "GT": "0",
     * "IZI": "0",
     * "LION": "0",
     * "SQUID": "0",
     * "HIGH": "0",
     * "SUPE": "504.01",
     * "USDG": "0",
     * "GTC": "0"
     * }
     * }
     *
     * @throws HttpException
     * @throws IOException
     */
    @Test
    public void getBalance() throws Exception {
        while (true){
            IStockRestApi stockGet = new StockRestApi(QUERY_URL);
            // balance
            String balance = stockGet.balance(gateConfig);
            log.info(balance);
            Thread.sleep(1000);
        }
    }

    /**
     * {
     * "result": "true",
     * "addr": "0x5465999365923BE083095D1FD9785F77E4cAbA8d",
     * "multichain_addresses": [
     * {
     * "chain": "BSC",
     * "address": "0x5465999365923BE083095D1FD9785F77E4cAbA8d",
     * "payment_id": "",
     * "payment_name": "",
     * "obtain_failed": 0
     * },
     * {
     * "chain": "GTEVM",
     * "address": "0x5465999365923BE083095D1FD9785F77E4cAbA8d",
     * "payment_id": "",
     * "payment_name": "",
     * "obtain_failed": 0
     * }
     * ],
     * "message": "Sucess",
     * "code": 0
     * }
     * 获取充值地址的api
     *
     * @throws HttpException
     * @throws IOException
     */
    @Test
    public void depositAddress() throws HttpException, IOException {
        IStockRestApi stockPost = new StockRestApi(TRADE_URL);
        // get deposit address
        String depositAddress = stockPost.depositAddress(gateConfig, "supe");
        log.info(depositAddress);
    }

    /**
     * 获取充值提现地址
     *
     * @throws HttpException
     * @throws IOException   参数名	参数类型	必填	描述
     *                       start	String	否	起始UNIX时间(如 1469092370)
     *                       end	String	否	终止UNIX时间(如 1469713981)
     *                       sortType	String	否	排序顺序（"ASC":升序,"DESC":降序）
     *                       page	String	否	页码，取值从1开始
     */
    @Test
    public void depositsWithdrawals() throws HttpException, IOException {
        IStockRestApi stockPost = new StockRestApi(TRADE_URL);
        // get deposit address
        String depositAddress = stockPost.depositsWithdrawals(gateConfig, "1469092370", "1469713981");
        log.info(depositAddress);
    }

    /**
     * 购买
     *
     * @throws HttpException
     * @throws IOException   参数名	参数类型	必填	描述	示例:
     *                       currencyPair	String	是	交易币种对(如btc_usdt,ltc_btc)	ltc_btc
     *                       rate	String	是	价格	1000
     *                       amount	String	是	交易量	800
     *                       orderType	String	否	订单类型("gtc"：普通订单（默认）；“ioc”：立即执行否则取消订单（Immediate-Or-Cancel，IOC）；"poc":被动委托（只挂单，不吃单）（Pending-Or-Cancelled，POC）)	ioc
     *                       text	String	否	用户自定义订单标识，必须以固定前缀 "t-"开头，不计算前缀的情况下，长度限制为 16 字节，范围 [0-9a-zA-Z-_.]。	t-1128392
     */
    @Test
    public void buy() throws HttpException, IOException {
        //获取价格
        IStockRestApi stockGet = new StockRestApi(QUERY_URL);
        //交易对
        String supeSymbol = "supe_usdt";
        //单价
        String rate = "8.4";
        //数量
        String amount = "1";
        String coinInfo = stockGet.ticker(supeSymbol);
        log.info(coinInfo);
        Price price = JsonTool.jsonToObject(coinInfo, Price.class);
        log.info(price);
        BigDecimal lastPrice = new BigDecimal(price.getLast());
        if(lastPrice.doubleValue()<8.5){
        IStockRestApi stockPost = new StockRestApi(TRADE_URL);
        // Place order buy
        String buy = stockPost.buy(gateConfig, supeSymbol, rate, amount);
        log.info(buy);
        }

    }

}