package com.example.demo;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import java.util.Properties;

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

        String filterCriteria = "New Jersey";

        SingleOutputStreamOperator filteredStream = kafkaStream.filter(new FilterFunction() {
            @Override
            public boolean filter(Object o) throws Exception {
                return o.toString().contains(filterCriteria);
            }
        });
        filteredStream.print();

        // kafka producer
        Properties sinkProps = new Properties();
        sinkProps.put("bootstrap.servers", kafkaBroker);

        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                .setBootstrapServers(kafkaBroker)
                .setRecordSerializer(KafkaRecordSerializationSchema
                        .builder()
                        .setTopic(targetTopic)
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                )
                .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();
        filteredStream.sinkTo(kafkaSink);

        env.execute("Kafka Consumer Example");
    }

}
