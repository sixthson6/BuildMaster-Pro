package com.tech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class BuildmasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuildmasterApplication.class, args);
    }

}
