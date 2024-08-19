package com.cmx.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {


//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/liteflow").addResourceLocations("classpath:/META-INF/resources/webjars/liteflow/index.html");
//        registry.addResourceHandler("/liteflow/**").addResourceLocations("classpath:/META-INF/resources/webjars/liteflow/");
//    }
}
