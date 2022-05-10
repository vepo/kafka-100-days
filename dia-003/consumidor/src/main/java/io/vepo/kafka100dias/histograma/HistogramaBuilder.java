package io.vepo.kafka100dias.histograma;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class HistogramaBuilder {

    private final int tamanho;
    private Map<String, Histograma> histogramas;

    public HistogramaBuilder(int tamanho) {
        this.histogramas = new HashMap<>();
        this.tamanho = tamanho;
    }

    public static void main(String[] args) {
        var props = new Properties();
        var running = new AtomicBoolean(true);
        var finalizado = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running.set(false);
            try {
                finalizado.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "histograma");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        System.out.println("Iniciando consumidor...");
        try (var consumer = new KafkaConsumer<String, String>(props)) {
            consumer.subscribe(Arrays.asList("texto"));
            var builder = new HistogramaBuilder(15);
            while (running.get()) {
                consumer.poll(Duration.ofSeconds(1L))
                        .forEach(record -> builder.serve(record.key(), record.value()));
            }
        }

        System.out.println("Consumidor finalizado!");
        finalizado.countDown();
    }

    public void serve(String arquivo, String texto) {
        var histograma = histogramas.computeIfAbsent(arquivo, __ -> new Histograma(tamanho));
        Palavras.quebrar(texto).forEach(palavra -> histograma
                .serve(palavra));
        System.out.println("Arquivo: " + arquivo + " histograma: " + histograma.top());
    }
}