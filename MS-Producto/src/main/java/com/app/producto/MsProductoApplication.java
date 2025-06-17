package com.app.producto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MsProductoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsProductoApplication.class, args);
    }

}
