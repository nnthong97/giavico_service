package com.giavico;

import com.giavico.beverage.config.OllamaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(OllamaProperties.class)
public class GiavicoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GiavicoServiceApplication.class, args);
    }
}
