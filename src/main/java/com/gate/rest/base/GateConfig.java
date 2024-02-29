package com.gate.rest.base;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author ：huang_fengge
 * @date ：Created in 2021/12/29 下午1:37
 * @description：配置信息
 * @modified By：
 * @version: v1.0.0$
 */
@Data
@ConfigurationProperties(prefix = "gate.config")
@Component
public class GateConfig {
    String secret;

    String key;
}
