package io.github.mightguy.microserivce.config;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * The class {@code SwaggerConfig} is responsible for the swagger configration, It is used for
 * creating docket & creating metadata for swagger API
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

  /**
   *
   * @return
   */
  @Bean
  public Docket productApi() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select().apis(RequestHandlerSelectors.basePackage("io.github.mightguy.microserivce"))
        .paths(PathSelectors.regex("/.*"))
        .build()
        .apiInfo(metaData());
  }

  private ApiInfo metaData() {
    return new ApiInfo(
        "Spring Boot REST API",
        "Spring Boot REST API for Spellchecker",
        "1.0",
        "Terms of service",
        new Contact("Lucky Sharma", "", "iamluckysharma.0910@gmail.com"),
        "",
        "",
        new ArrayList<>());
  }
}

