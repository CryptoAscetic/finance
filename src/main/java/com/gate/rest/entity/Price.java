package com.gate.rest.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ：huang_fengge
 * @date ：Created in 2021/12/31 下午3:22
 * @description：
 * @modified By：
 * @version: $
 * {
 *   "quoteVolume": "1711.56084969",
 *   "baseVolume": "15089.268588503",
 *   "highestBid": "8.8001",
 *   "high24hr": "9.2096",
 *   "last": "8.8148",
 *   "lowestAsk": "8.8148",
 *   "elapsed": "7ms",
 *   "result": "true",
 *   "low24hr": "8.6229",
 *   "percentChange": "-1.08"
 * }
 *
 */
@NoArgsConstructor
@Data
public class Price {

    @JsonProperty("quoteVolume")
    private String quoteVolume;
    @JsonProperty("baseVolume")
    private String baseVolume;
    @JsonProperty("highestBid")
    private String highestBid;
    @JsonProperty("high24hr")
    private String high24hr;
    @JsonProperty("last")
    private String last;
    @JsonProperty("lowestAsk")
    private String lowestAsk;
    @JsonProperty("elapsed")
    private String elapsed;
    @JsonProperty("result")
    private String result;
    @JsonProperty("low24hr")
    private String low24hr;
    @JsonProperty("percentChange")
    private String percentChange;
}
