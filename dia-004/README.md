# Altere o seu produtor para enviar um POJO usando um serializador JSON que você mesmo escreveu. Use [Jackson](https://www.devmedia.com.br/introducao-ao-jackson-objectmapper/43174)

Diferente do que fizemos no [Dia 002](/dia-002/README.md), dessa vez não vamos ler um texto. Eu tomei dessa decisão porque foi muito difícil imaginar casos de uso com processamento de texto. Agora vamos processar dados de clima.

Então antes de começar é preciso criar uma conta no [OpenWeather](https://openweathermap.org/api) e crie uma chave de API. Essa API permite fazer até 60 chamadas por minutos, o que é mais que suficiente para os nossos testes. Ao conseguir a chave, recomendo que você use o Postman para verificar o resultado da chamada `GET https://api.openweathermap.org/data/2.5/weather?lat=35&lon=139&appid=${CHAVE_API}`. Se você conseguiu ver o resultado, imagina que essa pode ser uma chamada qualquer do sistema que você está implementando. Ela vai retornar um JSON que pode ser transformada um POJO/Record que pode ser também enviado diretamente para o produtor Kafka.

Agora o próximo passo é mapear esse JSON como um DTO. **Já fica o aviso que sou absolutamente contra usar a sigla DTO como sufixo**, prefira usar um nome significativo para o domínio do negócio. No nosso caso não vamos nos preocupar muito com nomenclatura, nosso objeto se chamará `Clima` e os objetos internos vão usar o mesmo nome da propriedade. Para mapear o objeto eu criei um Record com todos os campos disponíveis na [documentação da API](https://openweathermap.org/current).

```java
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
```

Agora o próximo passo é requerer os dados através de um cliente HTTP. Seu eu estivesse usando o Java 8, poderia fazer isso usando o [Apache HTTPClient](https://hc.apache.org/httpcomponents-client-5.1.x/), mas na JDK 11 foi adicionado um [HTTP Client](https://openjdk.java.net/groups/net/httpclient/intro.html) similar muito fácil de usar. Com essa biblioteca podemos requerer o corpo da mensagem de várias formas usando a classe [`BodyHandlers`](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpResponse.BodyHandlers.html). No exemplo abaixo usamos o `BodyHandlers.ofString()` porque desejamos a resposta como String, mas vamos também usar o `BodyHandlers.ofInputStream()`.

```java
var client = HttpClient.newHttpClient();
var request = HttpRequest.newBuilder()
                      .uri(URI.create(String.format("https://api.openweathermap.org/data/2.5/weather?lat=%.6f&lon=%.6f&appid=%s", latitude, longitude, appId))
                      .build();
var response = client.send(request, BodyHandlers.ofString());
if (response.statusCode() == 200) {
    System.out.println("Leitura finalizada! resp: " + response.body());
} else {
    System.err.println("Não foi possível consultar o clima!");
}
```

Já temos a resposta e agora precisamos transformar ela em um POJO/Record. Para fazer isso vamos usar a biblioteca [Jackson data-bind](https://github.com/FasterXML/jackson-databind) ela é uma das mais comumente usada e mais performática (_a última vez que fiz um teste de performance nela foi muito tempo atrás_). Com ela podemos facilmente transformar String ou InputStream em objetos complexos, veja que no código baixo transformamos um InputStream em Clima.

```java
var objectMapper = new ObjectMapper();
var client = HttpClient.newHttpClient();
var request = HttpRequest.newBuilder()
                      .uri(URI.create(String.format("https://api.openweathermap.org/data/2.5/weather?lat=%.6f&lon=%.6f&appid=%s", latitude, longitude, appId))
                      .build();
var response = client.send(request, BodyHandlers.ofInputStream());
if (response.statusCode() == 200) {
    var clima = objectMapper.readValue(response.body(), Clima.class);
    System.out.println("Leitura finalizada! resp: " + clima);
} else {
    System.err.println("Não foi possível consultar o clima!");
}
```

Agora precisamos criar o nosso próprio `JsonSerializer`! O Kafka não distribui classes de serialização para objetos complexos, apenas para tipos primitivos e outras classes que fazem parte da biblioteca do Java ou do próprio Kafka. Então para criar um serializador precisamos implementar a interface [`Serializer<T>`](https://kafka.apache.org/32/javadoc/org/apache/kafka/common/serialization/Serializer.html). Nela temos apenas um método que devemos obrigatoriamente implementar que é o `byte[] serialize(String topic, T data)`, além dele temos o `configure` que pode ser usado para acessar configurações do `KafkaProducer`, o `close` que pode ser usado para liberar recursos e o `byte[] serialize(String topic, Headers headers, T data)` que deve ser usado se [`Headers`](https://kafka.apache.org/32/javadoc/org/apache/kafka/common/header/Headers.html) devem ser serializado juntamente com a mensagem. Veremos `Headers` depois, eles podem ser usados para enviar metadados da mensagem.

A nossa implementação de um serializador JSON deve ser implementada usando o [`ObjectMapper`](https://fasterxml.github.io/jackson-databind/javadoc/2.13/com/fasterxml/jackson/databind/ObjectMapper.html). Para tratar as exceções podemos lançar qualquer exceção que estende `KafkaException`, pois elas são [tratadas no produtor](https://github.com/apache/kafka/blob/3.2.0/clients/src/main/java/org/apache/kafka/clients/producer/KafkaProducer.java#L1032).

```java
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
```

Por fim precisamos adicionar alguns parâmetros no nosso código, como as coordenadas da localidade que desejamos as informações do clima. a chave da API e o tempo entre cada consulta. Para que esse programa funcione como um programa de linha de comando (_cli_, ou _**c**ommand **l**ine **i**nterface_ ou interface de linha de comando em tradução livre) vamos usar o framework [**picocli**](https://picocli.info/) construindo a seguinte interface de linha de comando.

```bash
$ java -jar target/produtor.jar --help
Usage: consulta-clima [-hV] [--appId=<appId>] [-lat=<latitude>]
                      [-lon=<longitude>] [-t=<timeout>]
Consulta informações do clima e envia para cluster Apache Kafka.
      --appId=<appId>       OpenWeather APP Id. Faça o cadastro em https://home.
                              openweathermap.org/.
  -h, --help                Show this help message and exit.
      -lat, --latitude=<latitude>
                            Latitude desejada
      -lon, --longitude=<longitude>
                            Longitude desejada
  -t, --timeout=<timeout>   Tempo de espera entre as requisições em segundos.
  -V, --version             Print version information and exit.
```

Para que esse programa seja disponível em apenas um arquivo JAR, sem dependências, adicionamos alguns plugins na build maven. Para compilar, na pasta `dia-004/produtor` execute o comando abaixo e o jar estará disponível em `dia-004/produtor/target/produtor.jar`.

```bash
mvn clean package
```

Abaixo temos um exemplo de execução omitindo o APP_ID que foi usado como variável de ambiente que é exportada na primeira lina do comando:

```
$ export APP_ID=<APP_ID>

$ java -jar target/produtor.jar -lat -22.909938 -lon -47.062633 --appId $APP_ID
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Iniciando Serializador! configs={bootstrap.servers=localhost:29092, value.serializer=class io.vepo.kafka100dias.clima.serializer.JsonSerializer, key.serializer=class io.vep
o.kafka100dias.clima.serializer.JsonSerializer, client.id=producer-1} chave=true
Iniciando Serializador! configs={bootstrap.servers=localhost:29092, value.serializer=class io.vepo.kafka100dias.clima.serializer.JsonSerializer, key.serializer=class io.vep
o.kafka100dias.clima.serializer.JsonSerializer, client.id=producer-1} chave=false
Leitura finalizada! resp: Clima[coord=Coordenada[lon=-47.0626, lat=-22.9099], weather=[Weather[id=800, main=Clear, description=clear sky, icon=01n]], base=stations, main=Ma
in[temp=293.91, feelsLike=294.21, tempMin=293.09, tempMax=293.91, pressure=1016.0, humidity=83.0, seaLevel=0.0, groundLevel=0.0], visibility=10000, wind=Wind[speed=1.54, de
g=150.0, gust=0.0], clouds=Clouds[all=0], rain=null, snow=null, dt=1654125188, sys=SysInfo[type=1, id=8393, message=0.0, country=BR, sunrise=1654076506, sunset=1654115444],
 timezone=-10800, id=3467865, name=Campinas, cod=200]
Metadados: topic=clima partition=0 offset=10 timestamp=1654125470414
Leitura finalizada! resp: Clima[coord=Coordenada[lon=-47.0626, lat=-22.9099], weather=[Weather[id=800, main=Clear, description=clear sky, icon=01n]], base=stations, main=Ma
in[temp=293.91, feelsLike=294.21, tempMin=293.09, tempMax=293.91, pressure=1016.0, humidity=83.0, seaLevel=0.0, groundLevel=0.0], visibility=10000, wind=Wind[speed=1.54, de
g=150.0, gust=0.0], clouds=Clouds[all=0], rain=null, snow=null, dt=1654125188, sys=SysInfo[type=1, id=8393, message=0.0, country=BR, sunrise=1654076506, sunset=1654115444],
 timezone=-10800, id=3467865, name=Campinas, cod=200]
Metadados: topic=clima partition=0 offset=11 timestamp=1654125475753
Produtor finalizado!

$
```

Caso você deseje chamar o programa direto usando o maven, use o comando abaixo do diretório `dia-004/produtor`:

```bash
mvn clean compile exec:java
```