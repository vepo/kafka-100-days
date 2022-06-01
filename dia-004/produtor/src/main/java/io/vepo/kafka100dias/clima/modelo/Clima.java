package io.vepo.kafka100dias.clima.modelo;

import java.util.List;

public record Clima(Coordenada coord,
        List<Weather> weather,
        String base,
        Main main,
        int visibility,
        Wind wind,
        Clouds clouds,
        Rain rain,
        Snow snow,
        int dt,
        SysInfo sys,
        long timezone,
        long id,
        String name,
        int cod) {
}