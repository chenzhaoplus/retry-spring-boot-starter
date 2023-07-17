package com.smcaiot.retry.starter.app;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.smcaiot.retry.starter")
@EnableConfigurationProperties({RetryProperties.class})
public class RetryAutoConfiguration {

}
