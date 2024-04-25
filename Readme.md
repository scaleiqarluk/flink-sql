<style>
    .highlight1{
        background-color:#501717;
    }
</style>

# Steps to start
1. Start docker compose
2. Start the ingestor located at path:<br>
<b><i><span class="highlight1">/home/soundarya/cubescale/cubescale-repos_new/scaleiQingestor/</span></i></b>
3. Start Flink-Kafka-consumer, flink requires Java 11
```sh
/usr/lib/jvm/jdk-11/bin/java -jar target/flink-kafka-consumer-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Read messages
```sh
docker exec -it kafka kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic target_topic
```

### Read messages from beginning
```sh
docker exec -it kafka kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic target_topic  --from-beginning
```