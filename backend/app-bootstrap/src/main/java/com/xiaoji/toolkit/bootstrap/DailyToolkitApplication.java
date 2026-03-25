package com.xiaoji.toolkit.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.xiaoji.toolkit")
public class DailyToolkitApplication {
    public static void main(String[] args) {
        SpringApplication.run(DailyToolkitApplication.class, args);
    }
}
