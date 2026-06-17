package com.giavico.beverage;

import com.giavico.beverage.config.OllamaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(OllamaProperties.class)
public class FormulaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FormulaServiceApplication.class, args);
    }
}
