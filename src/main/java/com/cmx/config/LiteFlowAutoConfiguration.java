package com.cmx.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "liteflow.enable", havingValue = "true")
@ComponentScan(basePackages = {"com.cmx.web"})
public class LiteFlowAutoConfiguration {
}
