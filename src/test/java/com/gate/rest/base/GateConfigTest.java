package com.gate.rest.base;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author ：huang_fengge
 * @date ：Created in 2021/12/29 下午1:57
 * @description：
 * @modified By：
 * @version: $
 */
@SpringBootTest
@Log4j2
class GateConfigTest {
    @Autowired
    GateConfig gateConfig;

    @Test
    public void gateConfigTest(){
        log.info(gateConfig.getSecret() + "," + gateConfig.getKey());
    }

}