package com.example.mivoto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication(scanBasePackages = "com.example.mivoto")
public class MivotoApplication {
  public static void main(String[] args) {
    SpringApplication.run(MivotoApplication.class, args);
  }

  // Dump de endpoints al arrancar (útil para ver qué mapeó)
  @Bean
  public Object logMappings(RequestMappingHandlerMapping mapping) {
    mapping.getHandlerMethods().forEach((req, handler) ->
        System.out.println(">> Mapped " + req + " -> " + handler.getMethod().getName()));
    return new Object();
  }
}
