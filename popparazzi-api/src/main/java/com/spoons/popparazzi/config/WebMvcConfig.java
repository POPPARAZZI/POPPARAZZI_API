package com.spoons.popparazzi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.local.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.local.public-prefix:/files}")
    private String publicPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String dir = Paths.get(uploadDir).toAbsolutePath().normalize().toString();
        String location = "file:" + (dir.endsWith("/") ? dir : dir + "/");

        String urlPattern = publicPrefix.endsWith("/")
                ? publicPrefix + "**"
                : publicPrefix + "/**";

        registry.addResourceHandler(urlPattern)
                .addResourceLocations(location);
    }
}