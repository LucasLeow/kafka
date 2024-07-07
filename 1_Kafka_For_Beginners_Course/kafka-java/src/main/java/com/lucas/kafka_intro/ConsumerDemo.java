package com.lucas.kafka_intro;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
public class ConsumerDemo {
    private static final Logger log = LoggerFactory.getLogger(ProducerDemo.class.getSimpleName());
    public static void main(String[] args) {
        log.info("I am a Kafka Consumer");
        Properties consumer_props = getProperties();
        String topic_name = "purchases";

        // create consumer <Key: String, Value: String>
        try(KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumer_props)) {

            // get reference to main thread (for proper shutdown of consumer)
            final Thread mainThread = Thread.currentThread();

            // add shutdown hook (thread to execute on shutdown)
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    log.info("shutdown initiated, exit by consumer.wakeup()...");
                    consumer.wakeup(); // throws wakeup exception on next poll

                    // join main thread to allow execution of code in main thread
                    try {
                        mainThread.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            try {
                // subscribe to topic
                consumer.subscribe(List.of(topic_name));

                // poll data from kafka broker
                while (true) {
                    log.info("Polling for data");

                    // Retrieve all records in topic (with delay to prevent overloading of kafka servers)
                    ConsumerRecords<String, String> cr = consumer.poll(Duration.ofMillis(1000)); // wait 1s to receive data from kafka

                    for (ConsumerRecord<String, String> r: cr) {
                        log.info("Key: " + r.key() + ", Value: " + r.value());
                        log.info("Partition: " + r.partition() + ", Offset: " + r.offset());
                    }
                }

            } catch (WakeupException we) {
                log.info("Consumer is shutting down" + we);
            } catch (Exception e) {
                log.error("Unexpected error occurred", e);
            } finally {
                consumer.close();
                log.info("Consumer shutdown successfully");
            }
        }
    }

    private static Properties getProperties() {
        Properties consumer_props = new Properties();
        String groupId = "my-java-app";

        // for localhost connection
        consumer_props.setProperty("bootstrap.servers", "127.0.0.1:9092");

        // for cloud kafka connection (get values from cloud platform UI)
        consumer_props.setProperty("security.protocol", "SASL_SSL");
        consumer_props.setProperty("sasl.jaas.config", "<value>");
        consumer_props.setProperty("sasl.mechanism", "PLAIN");
        consumer_props.setProperty("bootstrap.servers", "<clusterURL:port>");

        // Deserialize data from broker (if other type, must use corresponding type)
        consumer_props.setProperty("key.deserializer", StringDeserializer.class.getName());
        consumer_props.setProperty("value.deserializer", StringDeserializer.class.getName());

        consumer_props.setProperty("group.id", groupId);
        consumer_props.setProperty("auto.offset.reset", "earliest");
        // none -> if no existing consumer group, fail. must set group
        // earliest -> read from beginning of topic (read all msges)
        // latest -> read from end of topic (get only latest)

        return consumer_props;
    }

}
