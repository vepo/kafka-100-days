package io.vepo.kafka100dias.clima.serializer;

import java.util.Map;

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerializer implements Serializer<Object> {

    private ObjectMapper objectMapper;

    public JsonSerializer() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        System.out.println("Iniciando Serializador! configs=" + configs + " chave=" + isKey);
    }

    @Override
    public byte[] serialize(String topic, Object data) {
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw new KafkaException("Não foi possível serializar o objeto!", e);
        }
    }

}
