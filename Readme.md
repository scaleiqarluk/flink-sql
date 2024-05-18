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
sudo keytool -import -file localhost.crt -alias localhost -keystore $JAVA_HOME/lib/security/cacerts

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
cd logstash_custom_image
docker build -t my-logstash-with-opensearch . 
```
<br>

## 5. Update logstash files
```sh
# Update aws accesskey and secret in this file 
logstash-config/logstash.conf

# Place the localhost.crt in this folder (this file created in Step 3.Create SSL certificate)
logstash-config
```
<br>

## 6. Start the Docker containers
```sh
docker compose up -d

# At this step, Opensearch should be working in browser 
http://localhost:5601

# Credentials
admin/myPass2403
```
<br>

## 7. Start DataStream Consumer(Using Apache Flink)
```sh
mvn clean install package

# Start Flink-Kafka-consumer
java -jar target/flink-kafka-consumer-1.0-SNAPSHOT-jar-with-dependencies.jar 

# This is the main class
com.example.demo.DemoApplication
```
<br>

## 8. Start the ingestor (produces data to Kafka)
https://github.com/scaleiqarluk/scaleiQingestor_new

```sh
# clone the git repo outside of "Flink-Kafka-consumer"
git clone https://github.com/scaleiqarluk/scaleiQingestor_new

# get inside the cloned folder and compile
./gradlew clean build 

# run the built jar
java -jar build/libs/ingestor-1.0-SNAPSHOT.jar
```
<br>

### 9. Submit Flink job
Code snippet of pom.xml showing java 11 usage compulsory for Flink apps compilation. It can run on Java 11 or later.
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
		 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>your.group.id</groupId>
	<artifactId>flink-kafka-consumer</artifactId>
	<version>1.0-SNAPSHOT</version>

	<properties>
		<java.version>11</java.version>
```
My system's Java and Maven details
```sh
java --version
# openjdk 17.0.11 2024-04-16
# OpenJDK Runtime Environment (build 17.0.11+9)
# OpenJDK 64-Bit Server VM (build 17.0.11+9, mixed mode, sharing)

mvn -v
# Apache Maven 3.8.8 (4c87b05d9aedce574290d1acc98575ed5eb6cd39)
# Maven home: /opt/mvn8
# Java version: 17.0.11, vendor: N/A, runtime: /usr/lib/jvm/java-17-openjdk
# Default locale: en_IN, platform encoding: UTF-8
# OS name: "linux", version: "6.9.0-1-manjaro", arch: "amd64", family: "unix"
```

```sh
# compile project
mvn clean install package

# Copy the jar in the contaner
docker cp target/flink-kafka-consumer-1.0-SNAPSHOT-jar-with-dependencies.jar jobmanager:/flink-consumer-0.0.1-SNAPSHOT.jar

# Subject Flink Job
docker exec -it jobmanager ./bin/flink run -c com.example.demo.FlinkSQLOpensearch /flink-consumer-0.0.1-SNAPSHOT.jar
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