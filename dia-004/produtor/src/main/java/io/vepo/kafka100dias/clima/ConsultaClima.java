package io.vepo.kafka100dias.clima;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vepo.kafka100dias.clima.modelo.Clima;
import io.vepo.kafka100dias.clima.modelo.Coordenada;
import io.vepo.kafka100dias.clima.serializer.JsonSerializer;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "consulta-clima", mixinStandardHelpOptions = true, version = "consulta-clima 4.0", description = "Consulta informações do clima e envia para cluster Apache Kafka.")
public class ConsultaClima implements Callable<Integer> {

    @Option(names = { "-lat", "--latitude" }, description = "Latitude desejada")
    private double latitude;

    @Option(names = { "-lon", "--longitude" }, description = "Longitude desejada")
    private double longitude;

    @Option(names = {
            "--appId" }, description = "OpenWeather APP Id. Faça o cadastro em https://home.openweathermap.org/.")
    private String appId;

    @Option(names = { "-t",
            "--timeout" }, description = "Tempo de espera entre as requisições em segundos.", defaultValue = "5")
    private int timeout;

    public static void main(String[] args) throws IOException, InterruptedException {
        int exitCode = new CommandLine(new ConsultaClima()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        var running = new AtomicBoolean(true);
        var finalizado = new CountDownLatch(1);
        Thread shutdownHook = new Thread(() -> {
            running.set(false);
            try {
                finalizado.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        try {
            Runtime.getRuntime().addShutdownHook(shutdownHook);

            var objectMapper = new ObjectMapper();
            var props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

            var client = HttpClient.newHttpClient();
            try (var producer = new KafkaProducer<Coordenada, Clima>(props)) {
                while (running.get()) {
                    var request = HttpRequest.newBuilder()
                            .uri(URI.create(
                                    String.format(
                                            "https://api.openweathermap.org/data/2.5/weather?lat=%.6f&lon=%.6f&appid=%s",
                                            latitude, longitude, appId)))
                            .build();
                    var response = client.send(request, BodyHandlers.ofInputStream());
                    if (response.statusCode() == 200) {
                        var clima = objectMapper.readValue(response.body(), Clima.class);
                        System.out.println("Leitura finalizada! resp: " + clima);
                        
                        var resultado = producer.send(new ProducerRecord<Coordenada, Clima>("clima", clima.coord(), clima));
                        var metadados = resultado.get();
                        System.out.println("Metadados: topic=" + metadados.topic() +
                                                " partition=" + metadados.partition() +
                                                    " offset=" + metadados.offset() +
                                                " timestamp=" + metadados.timestamp());
                    } else {
                        System.err.println("Não foi possível consultar o clima!");
                    }
                    Thread.sleep(timeout * 1_000);
                }
            }
            System.out.println("Produtor finalizado!");
            finalizado.countDown();
            return 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
            return 1;
        }
    }
}