package io.vepo.kafka100dias.histograma;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class HistogramaTest {

    @Test
    void criarHistogramaTest() {
        var histograma = new Histograma(10);
        histograma.serve("oi");
        assertEquals(Arrays.asList("oi"), histograma.top());
    }

    @Test
    void injetarTextoTest() {
        var histograma = new Histograma(10);
        Palavras.quebrar("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum")
                .forEach(histograma::serve);
        assertEquals(Arrays.asList("in", "ut", "dolor", "dolore", "mollit", "culpa", "incididunt", "tempor", "est", "adipiscing"), histograma.top());
    }

    @Test
    void validarContagemTest() {
        var histograma = new Histograma(10);
        var numeros = Arrays.asList("um", "dois", "três", "quatro", "cinco", "seis", "sete", "oito", "nove", "dez", "onze", "doze");
        Palavras.quebrar(IntStream.range(0, numeros.size())
                        .mapToObj(index -> IntStream.range(0, index + 1)
                                .mapToObj(__ -> numeros.get(index))
                                .collect(Collectors.toList()))
                        .flatMap(List::stream)
                        .collect(Collectors.joining(" ")))
                .forEach(histograma::serve);
        assertEquals(Arrays.asList("doze", "onze", "dez", "nove", "oito", "sete", "seis", "cinco", "quatro", "três"), histograma.top());
    }
}