package io.vepo.kafka100dias.clima.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Main(double temp,
        @JsonProperty("feels_like") double feelsLike,
        @JsonProperty("temp_min") double tempMin,
        @JsonProperty("temp_max") double tempMax,
        double pressure,
        double humidity,
        @JsonProperty("sea_level") double seaLevel,
        @JsonProperty("grnd_level") double groundLevel) {

}
