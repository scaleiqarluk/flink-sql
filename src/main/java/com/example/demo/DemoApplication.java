package com.example.demo;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.opensearch.sink.OpensearchSink;
import org.apache.flink.connector.opensearch.sink.OpensearchSinkBuilder;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.http.HttpHost;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.Requests;

public class DemoApplication {

    // Read from Kafka topic "IngestorDataStreamDemo" -> Filter data -> insert in Kafka topic "target_topic"

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        String kafkaBroker = "127.0.0.1:9092";
        String topic = "IngestorDataStreamDemo";
        String targetTopic = "target_topic";

        // kafka consumer
        KafkaSource<String> kafkaSource = KafkaSource
                .<String>builder()
                .setBootstrapServers(kafkaBroker)
                .setTopics(topic)
                .setGroupId("my-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStream<String> kafkaStream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source");
        kafkaStream.print();
        OpensearchSink<String> sink = new OpensearchSinkBuilder<String>()
                .setHosts(new HttpHost("localhost", 9200, "https"))
                .setEmitter(
                        (element, context, indexer) -> {
                            // Create index request
                            IndexRequest indexRequest = Requests.indexRequest()
                                    .index("soundarya_index") // Specify your index name
                                    .source("data", element);

                            // Add index request to bulk processor
                            indexer.add(indexRequest);
                        }
                )
                .setConnectionUsername("admin")
                .setConnectionPassword("myPass2403")
                .setAllowInsecure(true)
                .setBulkFlushMaxActions(1)
                .build();

        kafkaStream.sinkTo(sink);
        env.execute("Kafka Consumer Example");
    }

}
