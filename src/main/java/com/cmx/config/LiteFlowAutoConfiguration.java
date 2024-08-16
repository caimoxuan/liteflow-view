package com.cmx.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "liteflow.enable", havingValue = "true")
@ComponentScan(basePackages = {"com.cmx.web"})
public class LiteFlowAutoConfiguration {

    @PostConstruct
    public void init() {
        log.info("[LiteFlowAutoConfiguration] init");
    }

}
