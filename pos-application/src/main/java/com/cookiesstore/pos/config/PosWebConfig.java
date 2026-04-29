package com.cookiesstore.pos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PosWebConfig implements WebMvcConfigurer {

    private final PosSourceAccessInterceptor sourceAccessInterceptor;

    public PosWebConfig(PosSourceAccessInterceptor sourceAccessInterceptor) {
        this.sourceAccessInterceptor = sourceAccessInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sourceAccessInterceptor)
            .addPathPatterns("/**");
    }
}
