package com.cmx.web.config;

import com.cmx.extension.loader.ExtensionLoaderFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Configuration
public class BeanConfig {

    @Resource
    private ApplicationContext applicationContext;

    @PostConstruct
    public void initFactory() {
        ExtensionLoaderFactory.setApplicationContext(applicationContext);
    }


}
