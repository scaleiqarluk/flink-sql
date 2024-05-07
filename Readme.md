<style>
    .highlight1{
        background-color:#501717;
    }
</style>

# Consume Kafka messages using Apache Flink and push in Opensearch

## Steps to start the Consumer
1. Start docker compose
2. Compile jar
```sh
mvn clean install package
```
3. Start the ingestor located at path:<br>
<b><i><span class="highlight1">/home/soundarya/cubescale/cubescale-repos_new/scaleiQingestor/</span></i></b>
```sh
java -jar build/libs/ingestor-1.0-SNAPSHOT.jar
```


4. Start Flink-Kafka-consumer, flink requires Java 11
```sh
java -jar target/flink-kafka-consumer-1.0-SNAPSHOT-jar-with-dependencies.jar 

# This is the main class
com.example.demo.DemoApplication
```

### Access Opensearch using browser
```sh
# Go to this URL
http://localhost:5601/

# Use credentials
admin/myPass2403
```

### Query opensearch data using SQL
```sh
# Use this file
com.example.demo.FlinkSQLOpensearch

# This file runs properly using IntelliJ IDE, but I am not able to run using command line yet. It gives an error

Exception in thread "main" org.apache.flink.table.api.ValidationException: Could not find any factories that implement 'org.apache.flink.table.factories.CatalogStoreFactory' in the classpath.
        at org.apache.flink.table.factories.FactoryUtil.discoverFactory(FactoryUtil.java:596)
        at org.apache.flink.table.factories.TableFactoryUtil.findAndCreateCatalogStoreFactory(TableFactoryUtil.java:221)
        at org.apache.flink.table.api.bridge.java.internal.StreamTableEnvironmentImpl.create(StreamTableEnvironmentImpl.java:121)
        at org.apache.flink.table.api.bridge.java.StreamTableEnvironment.create(StreamTableEnvironment.java:122)
        at com.example.demo.FlinkSQLOpensearch.main(FlinkSQLOpensearch.java:42)
```