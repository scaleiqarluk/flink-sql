package com.example.demo;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.api.common.serialization.SimpleStringSchema;

import java.util.Properties;

public class FlinkKafkaConsumerExample {

    public static void main(String[] args) throws Exception {
        // Set up the execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Kafka properties
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "flink-consumer-group");

        // Add the Kafka source
        FlinkKafkaConsumer<String> kafkaConsumer = new FlinkKafkaConsumer<>("IngestorDataStreamDemo", new SimpleStringSchema(), properties);

        // Set up the Kafka consumer
        DataStream<String> kafkaStream = env.addSource(kafkaConsumer);

        // Process the Kafka stream
        DataStream<String> processedStream = kafkaStream.map(new MapFunction<String, String>() {
            @Override
            public String map(String value) throws Exception {
                // Perform transformation here if needed
                System.out.println("processed: "+ value);
                return "Processed: " + value;
            }
        });

        // Print the processed stream to the console
        processedStream.print();

        // Execute Flink job
        env.execute("Kafka Consumer Example");
    }
}
