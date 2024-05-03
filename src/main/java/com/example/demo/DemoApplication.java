package com.example.demo;

import com.google.gson.JsonParser;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.opensearch.sink.FailureHandler;
import org.apache.flink.connector.opensearch.sink.FlushBackoffType;
import org.apache.flink.connector.opensearch.sink.OpensearchSink;
import org.apache.flink.connector.opensearch.sink.OpensearchSinkBuilder;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.http.HttpHost;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.Requests;
import org.opensearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemoApplication {

    public static Logger log = LoggerFactory.getLogger(DemoApplication.class);
    private static class MyActionRequestFailureHandler implements FailureHandler {
        @Override
        public void onFailure(Throwable throwable) {
            log.error("Failed to process action request: {}", throwable.getMessage());
        }
    }
    private static MyActionRequestFailureHandler my1 = new MyActionRequestFailureHandler();


    // Read from Kafka topic "IngestorDataStreamDemo" -> Filter data -> insert in Kafka topic "target_topic"

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        String kafkaBroker = "127.0.0.1:9092";
        String topic = "IngestorDataStreamDemo";

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
//        kafkaStream.print();

        OpensearchSink<String> sink = new OpensearchSinkBuilder<String>()
                .setHosts(new HttpHost("localhost", 9200, "https"))
                .setEmitter(
                        (element, context, indexer) -> {
                            String id = extractId(element);

                            // Create index request
                            IndexRequest indexRequest =  Requests.indexRequest()
                                    .index("heya2_index") // Specify your index name
                                    .source(element, XContentType.JSON);
                            // Add index request to bulk processor
                            indexer.add(indexRequest);
                        }
                )
                .setFailureHandler(my1)
                .setConnectionUsername("admin")
                .setConnectionPassword("myPass2403")
                .setAllowInsecure(true)
                .setBulkFlushMaxActions(10)
                .setBulkFlushBackoffStrategy(FlushBackoffType.EXPONENTIAL, 5, 10000)
                .build();

        kafkaStream.sinkTo(sink);
        env.execute("Kafka Consumer Example");
    }

    private static String extractId(String json){
        return JsonParser.parseString(json)
            .getAsJsonObject()
            .get("meta")
            .getAsJsonObject()
            .get("id")
            .getAsString();
    }



}

