package io.vepo.kafka100dias.clima.modelo;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Rain(@JsonProperty("1h") double oneHour, @JsonProperty("3h") double threeHour) {

}
