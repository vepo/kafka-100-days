package io.vepo.kafka100dias.leitortexto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class LeitorTexto {
    public static void main(String[] args) {
        var props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        try (var producer = new KafkaProducer<String, String>(props)) {
            var recursos = Paths.get(".", "recursos");
            for (var arquivo : recursos.toFile().list()) {
                try {
                    Files.lines(recursos.resolve(arquivo))
                         .forEachOrdered(line -> {
                             try {
                                 var metadadosFuture = producer.send(new ProducerRecord<>("texto", arquivo, line));
                                 var metadados = metadadosFuture.get();
                                 System.out.println("Metadados: topic=" + metadados.topic() +
                                                          " partition=" + metadados.partition() +
                                                             " offset=" + metadados.offset() +
                                                          " timestamp=" + metadados.timestamp());
                             } catch (InterruptedException | ExecutionException ei) {
                                 Thread.currentThread().interrupt();
                             }
                         });
                    
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            System.out.println("Leitura finalizada!");
        }
    }
}