package org.snomed.cis.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer){
        configurer.setUseTrailingSlashMatch(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/info/js/*.ttf", "/info/js/*.woff2", "/info/js/*.woff")
                .addResourceLocations("classpath:/static/info/fonts/roboto/");

        registry.addResourceHandler("/info/index.html/js/**")
                .addResourceLocations("classpath:/static/info/js/");

        registry.addResourceHandler("/info/index.html/css/**")
                .addResourceLocations("classpath:/static/info/css/");

        registry.addResourceHandler("/info/index.html/fonts/roboto/**")
                .addResourceLocations("classpath:/static/info/fonts/roboto/");
    }

}
