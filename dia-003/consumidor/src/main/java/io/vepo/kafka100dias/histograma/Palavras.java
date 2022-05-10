package io.vepo.kafka100dias.histograma;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface Palavras {
    static Pattern PALAVRA_COM_PONTUACAO = Pattern.compile("([^.,?!;]+)[.,?!;]+");

    public static List<String> quebrar(String texto) {
        return Arrays.asList(texto.replaceAll("\r", "")
                        .split("\s"))
                .stream()
                .map(Palavras::removePontuacao)
                .collect(Collectors.toList());
    }

    static String removePontuacao(String palavra) {
        var matcher = PALAVRA_COM_PONTUACAO.matcher(palavra);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return palavra;
    }
}
