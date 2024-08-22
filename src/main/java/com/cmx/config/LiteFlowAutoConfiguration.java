package com.cmx.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
@AutoConfiguration
@ConditionalOnProperty(name = "liteflow.enable", havingValue = "true")
@ComponentScan(basePackages = {"com.cmx.web"})
public class LiteFlowAutoConfiguration {

    @PostConstruct
    public void init() {
        log.info("[LiteFlowViewAutoConfiguration] init");
    }

}
