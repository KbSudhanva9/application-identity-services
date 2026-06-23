package com.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.auth.interceptor.LoggingInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoggingInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

//        registry.addInterceptor(interceptor);
    	
    	registry.addInterceptor(interceptor).addPathPatterns("/**");
    	
    }
}
