package com.cmx.web.config;

import com.cmx.extension.loader.ExtensionLoaderFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;


@Configuration
public class BeanConfig {

    @Resource
    private ApplicationContext applicationContext;

    @PostConstruct
    public void initFactory() {
        ExtensionLoaderFactory.setApplicationContext(applicationContext);
    }


}
