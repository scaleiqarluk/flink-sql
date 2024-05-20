package com.example.flinkconsumer.config;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.JobManagerOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class FlinkConfig {

    @Bean
    public Configuration flinkConfiguration() {
        Configuration config = new Configuration();
        config.setString(JobManagerOptions.ADDRESS, "localhost");
        config.setInteger(JobManagerOptions.PORT, 6123);
        return config;
    }

    @Bean
    public StreamExecutionEnvironment flinkStreamExecutionEnvironment() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
        env.getConfig().setGlobalJobParameters(flinkConfiguration());
        // You can further configure the execution environment if needed
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, 10000)); // Example restart strategy
        return env;
    }
}
