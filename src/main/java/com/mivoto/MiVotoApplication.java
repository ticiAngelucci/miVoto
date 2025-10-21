package com.mivoto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableRetry
public class MiVotoApplication {

  public static void main(String[] args) {
    SpringApplication.run(MiVotoApplication.class, args);
  }
}
