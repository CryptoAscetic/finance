package com.gate.rest.base;

import com.gate.rest.util.HttpUtilManager;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpException;
import org.apache.http.ParseException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author ：huang_fengge
 * @date ：Created in 2024/3/8 上午10:48
 * @description：
 * @modified By：
 * @version: $
 */
public class SolTest {

    @Test
    public void getPrice() throws HttpException, IOException {
        HttpUtilManager httpUtil = HttpUtilManager.getInstance();
        Header header = new Header() {
            @Override
            public HeaderElement[] getElements() throws ParseException {
                return new HeaderElement[0];
            }

            @Override
            public String getName() {
                return "X-API-Key";
            }

            @Override
            public String getValue() {
                return "";
            }
        };
        String result = httpUtil.HttpGet(
                "https://solana-gateway.moralis.io/token/mainnet/SRMuApVNdxXokk5GT7XD5cUUgXMBCoAz2LHeuAoKWRt/price"
                , "", "", header);

        System.out.println(result);
    }
}
