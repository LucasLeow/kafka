package com.lucas.kafka_intro;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemo {

    private static final Logger log = LoggerFactory.getLogger(ProducerDemo.class.getSimpleName());
    public static void main(String[] args) {
        // Create Producer Properties (config for Producer)
        Properties prod_props = getProperties();

        String topic_name = "purchases";
        String key = "key1";
        String value = "hello, world!";

        // try block to auto close
        // create Producer <key String, value String>
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(prod_props)) {

            // create Producer record (to send to Kafka)
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic_name, key, value);

            // send data to kafka
            producer.send(producerRecord, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception e) {
                    // executes everytime a record successfully sent or exception thrown
                    if (e == null) {
                        // record successfully sent
                        log.info("Received new metadata \n" +
                                "Topic: " + metadata.topic() + "\n" +
                                "Partition: " + metadata.partition() + "\n" +
                                "Offset: " + metadata.offset() + "\n" +
                                "Timestamp: " + metadata.timestamp() + "\n");
                    } else {
                        log.error("Error while producing", e);
                    }
                }
            });

            // flush producer (send all data & block until sending done)
            producer.flush();

            // close producer (will flush once more)
            producer.close();
        }
    }

    private static Properties getProperties() {
        Properties prod_props = new Properties();

        // for localhost connection
        prod_props.setProperty("bootstrap.servers", "127.0.0.1:9092");

        // for cloud kafka connection (get values from cloud platform UI)
        prod_props.setProperty("security.protocol", "SASL_SSL");
        prod_props.setProperty("sasl.jaas.config", "<value>");
        prod_props.setProperty("sasl.mechanism", "PLAIN");
        prod_props.setProperty("bootstrap.servers", "<clusterURL:port>");

        // Serialize data before sending to apache kafka (if other type, must use corresponding type)
        prod_props.setProperty("key.serializer", StringSerializer.class.getName());
        prod_props.setProperty("value.serializer", StringSerializer.class.getName());
        return prod_props;
    }
}
