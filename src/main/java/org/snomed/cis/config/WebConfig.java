package org.snomed.cis.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.security.Principal;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

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

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return Authentication.class.isAssignableFrom(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                Principal principal = webRequest.getUserPrincipal();
                if (principal instanceof Authentication auth && parameter.getParameterType().isInstance(auth)) {
                    return auth;
                }
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && parameter.getParameterType().isInstance(auth)) {
                    return auth;
                }
                return null;
            }
        });
    }
}
