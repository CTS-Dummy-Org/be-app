package com.cognizant.quickstart;

import com.cognizant.quickstart.config.ConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@SpringBootApplication
public class QuickstartTemplateSpringbootHelloworldApiApplication {

  @Autowired
  ConfigProperties configProperties;

  public static void main(String[] args) {
    log.info("Hello World");
    log.debug("Debug Initiated");
    SpringApplication.run(QuickstartTemplateSpringbootHelloworldApiApplication.class, args);
  }

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**").allowedOrigins(configProperties.getCorsUrl())
            .allowedMethods("*");
      }
    };
  }
}
