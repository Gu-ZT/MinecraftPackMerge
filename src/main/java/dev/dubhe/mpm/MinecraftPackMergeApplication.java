package dev.dubhe.mpm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MinecraftPackMergeApplication {
    public static void main(String[] args) {
        SpringApplication.run(MinecraftPackMergeApplication.class, args);
    }
}
