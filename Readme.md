# Read messages
docker exec -it 1c8c0585991f kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic target_topic

# Read messages from beginning
docker exec -it 1c8c0585991f kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic target_topic  --from-beginning