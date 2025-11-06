package org.dplay.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class DPlayServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DPlayServerApplication.class, args);
    }

}

