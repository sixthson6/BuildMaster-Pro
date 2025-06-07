package com.tech.auditlog;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "auditExecutor")
    public Executor auditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("audit-");
        executor.setKeepAliveSeconds(60);

        // Better rejection handler for audit logs
        executor.setRejectedExecutionHandler((runnable, threadPoolExecutor) -> {
            if (!threadPoolExecutor.isShutdown()) {
                try {
                    // Try to execute immediately in caller thread as fallback
                    runnable.run();
                } catch (Exception e) {
                    // Log the error but don't fail the main operation
                    System.err.println("Failed to execute audit log: " + e.getMessage());
                }
            }
        });

        executor.initialize();
        return executor;
    }
}
