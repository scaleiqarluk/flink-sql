<style>
    .highlight1{
        background-color:#501717;
    }
</style>

# Consume Kafka messages using Apache Flink and push in Opensearch


## 1. Install Java 11 and set environment
```sh
# Download JDK 11

# edit ~/.bashrc(ubuntu) or /etc/profile(manjaro)
# --------
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
export PATH=$PATH:$JAVA_HOME/bin
```
<br>

## 2. Maven 3.8 install and set environment
```sh
# Download Maven 3.8.x

# edit ~/.bashrc(ubuntu) or /etc/profile(manjaro)
# --------
export MAVEN_HOME=/opt/apache-maven-3.8.x
export PATH=$PATH:$MAVEN_HOME/bin
```
<br>

## 3. Create SSL certificate

```sh
# Go to this folder and then execute all these commands
cd opensearch-config
# Please note: Hierarchy listed at the bottom of this file for reference

# Step 1: Generate a New Private Key
openssl genrsa -out localhost.key 2048

# Step 2: Create a New Certificate Signing Request (CSR)
openssl req -new -key localhost.key -out localhost.csr -subj "/CN=localhost" -addext "subjectAltName = DNS:localhost"

# Step 3: Sign the CSR with a CA Certificate
openssl x509 -req -in localhost.csr -out localhost.crt -signkey localhost.key -days 365

# Step 4: Add Certificate to Java Truststore:
keytool -import -file localhost.crt -alias localhost -keystore $JAVA_HOME/lib/security/cacerts

# List certificates
keytool -list -keystore $JAVA_HOME/lib/security/cacerts 
```
<br>

## 4. Create custom docker image of Logstash to support opensearch output
```sh
FROM docker.elastic.co/logstash/logstash:8.13.4

# Install logstash-output-opensearch plugin
RUN logstash-plugin install logstash-output-opensearch
```
Build the custom images
```sh
docker build -t my-logstash-with-opensearch . 
```
<br>

## 5. Steps to start the Docker containers
```sh
# Update aws accesskey and secret in this file 
logstash-config/logstash.conf

# Place the localhost.crt in this folder (this file created in Step 3.Create SSL certificate)
logstash-config
```

1. Start docker compose
```sh
docker compose up -d

# At this step, Opensearch should be working in browser 
http://localhost:5601

# Credentials
admin/myPass2403
```
2. Compile and run consumer
```sh
mvn clean install package

# Start Flink-Kafka-consumer
java -jar target/flink-kafka-consumer-1.0-SNAPSHOT-jar-with-dependencies.jar 

# This is the main class
com.example.demo.DemoApplication
```
<br>

## 6. Start the ingestor<br>
https://github.com/scaleiqarluk/scaleiQingestor_new

```sh
# clone the git repo outside of "Flink-Kafka-consumer"
git clone https://github.com/scaleiqarluk/scaleiQingestor_new

# get inside the cloned folder
java -jar build/libs/ingestor-1.0-SNAPSHOT.jar
```
<br>

### 7. Query opensearch data using SQL
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
<br>

### Files hierarchy of project - Flink-Kafka-consumer

```sh
Flink-Kafka-consumer
├── docker-compose.yml
├── Dockerfile
├── empty-settings
├── flink-conf.yaml
├── flink-kafka-consumer.iml
├── Flink-Kafka-consumer.iml
├── HELP.md
├── logstash-config
│   ├── localhost.crt
│   └── logstash.conf
├── logstash_custom_image
│   └── Dockerfile
├── mvnw
├── mvnw.cmd
├── opensearch-config
│   ├── localhost.crt
│   ├── localhost.csr
│   └── localhost.key
├── opensearch-snapshots
├── pom.xml
├── Readme.md
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── example
│   │   │           └── demo
│   │   │               ├── DemoApplication.java
│   │   │               └── FlinkSQLOpensearch.java
│   │   └── resources
│   │       ├── application.properties
│   │       └── log4j.properties
├── steps.md
└── target
    ├── flink-kafka-consumer-1.0-SNAPSHOT.jar
    ├── flink-kafka-consumer-1.0-SNAPSHOT-jar-with-dependencies.jar
```