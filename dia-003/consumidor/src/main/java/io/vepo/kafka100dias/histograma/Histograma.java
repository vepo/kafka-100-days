package io.vepo.kafka100dias.histograma;

import java.util.*;
import java.util.stream.Collectors;

public class Histograma {

    private final int top;
    private final Map<String, Integer> contadores;

    public Histograma(int top) {
        this.top = top;
        this.contadores = new HashMap<>();
    }

    public void serve(String palavra) {
        this.contadores.compute(palavra, (__, contador) -> Objects.isNull(contador) ? 1 : contador + 1);
    }

    public List<String> top() {
        return this.contadores.entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()))
                .limit(top)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
