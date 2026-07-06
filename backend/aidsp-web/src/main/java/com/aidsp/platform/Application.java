package com.aidsp.platform;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI Decision Support Platform 启动入口。
 */
@SpringBootApplication(scanBasePackages = "com.aidsp.platform")
@EnableDubbo(scanBasePackages = "com.aidsp.platform")
@MapperScan(basePackages = {
        "com.aidsp.platform.sys.repository",
        "com.aidsp.platform.company.repository",
        "com.aidsp.platform.industry.repository",
        "com.aidsp.platform.report.repository",
        "com.aidsp.platform.agent.repository",
        "com.aidsp.platform.knowledge.repository",
        "com.aidsp.platform.rag.repository"
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
