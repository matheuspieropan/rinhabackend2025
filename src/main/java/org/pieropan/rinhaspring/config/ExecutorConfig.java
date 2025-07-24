package org.pieropan.rinhaspring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ExecutorConfig {

    @Bean //quanto mais aumenta o número de threads + sobe a média do response time e + inconcistencia
    public ExecutorService executorService() {
        return new ThreadPoolExecutor(30, 30, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2000), Thread.ofVirtual().factory());
    }
}