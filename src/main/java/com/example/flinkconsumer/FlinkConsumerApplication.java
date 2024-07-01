package com.example.flinkconsumer;

import com.example.flinkconsumer.service.FlinkJobService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class FlinkConsumerApplication{

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(FlinkConsumerApplication.class, args);

//		FlinkJobService flinkJobService = context.getBean(FlinkJobService.class);
//		try {
//			flinkJobService.runDataToOpensearchJob();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
}
