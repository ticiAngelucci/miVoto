package com.example.mivoto.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    private final CorsProperties corsProperties;

    public CorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        if (!CollectionUtils.isEmpty(corsProperties.getAllowedOrigins())) {
            corsProperties.getAllowedOrigins().forEach(config::addAllowedOrigin);
        }
        if (!CollectionUtils.isEmpty(corsProperties.getAllowedOriginPatterns())) {
            corsProperties.getAllowedOriginPatterns().forEach(config::addAllowedOriginPattern);
        }
        config.addAllowedHeader("*");
        if (!CollectionUtils.isEmpty(corsProperties.getAllowedMethods())) {
            corsProperties.getAllowedMethods().forEach(config::addAllowedMethod);
        }
        config.setAllowCredentials(true);
        config.setMaxAge(corsProperties.getMaxAgeSeconds());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
