# Crie um produtor simples que envia um arquivo texto para um tópico, cada linha deve ser uma mensagem

## 1. Criando projeto

O primeiro passo para criar um produtor é criar um projeto Java simples. Para isso vou criar uma estrutura para usar o Maven como sistema de build. Para isso eu preciso:

```
.
├── src
│   └── main
│       └── java
│           └── io
│               └── vepo
│                   └── kafka100dias
│                       └── LeitorTexto.java
└── pom.xml
```

Para quem não conhece o Maven (_dê uma olhada nesse [simples tutorial](https://github.com/dev-roadmap/backend-roadmap/blob/main/caso-de-uso-00-configurando-um-projeto-quarkus.md)_), ele serve para gerenciar a build de projetos Java. O arquivo `pom.xml` vai conter as informações básicas do projeto e as dependências. Você pode achar estranha a estrutura de diretórios, mas ela é bastante útil para evitar configurações. O Maven atua por um padrão chamado [Convenção sobre configuração](https://pt.wikipedia.org/wiki/Conven%C3%A7%C3%A3o_sobre_configura%C3%A7%C3%A3o), ao invês de colocar todas as configurações do projeto, basta seguir essa regrinha básica de estrutura de diretórios.

## 1. Adicionando as dependências

Para encontrar dependências Maven, é possível procurar no mvnrepository.com. Cada dependência é definida pelas coordenadas `groupId`, `artifactId` e `version` e elas podem ser encontradas diretamente no mvnrepository.com, como é o caso do Kafka Clientes [mvnrepository.com/artifact/org.apache.kafka/kafka-clients](https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients). Observe o padrão da URL, `mvnrepository.com/artifact/{groupId}/{artifactId}`. É possível também adicionar a versão na URL `mvnrepository.com/artifact/{groupId}/{artifactId}/{version}`.

Acessando o mvnrepository.com, podemos pegar adicionar no `pom.xml`.

```xml
<!-- https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients -->
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>3.1.0</version>
</dependency>
```

Para facilitar na execução, já estou colocando o plugin `org.codehaus.mojo:exec-maven-plugin` corretamente configurado para apontar para a classe `io.vepo.kafka100dias.leitortexto.LeitorTexto`, assim para executar basta usar `mvn clean compile exec:java`.

## 2. Iniciar Produer

Para iniciar um producer é preciso criar um mapa com todas as propriedades necessárias. O mínimo necessário é definir quem é o `bootstrap.servers`, `key.serializer` e `value.serializer`. A documentação dessas configurações pode ser encontrada em [kafka.apache.org](https://kafka.apache.org/documentation/#producerconfigs) e é recomendável ler para se conhecer quais são as opções existente. MAS todas essa documentação também está disponível por código na classe [`org.apache.kafka.clients.producer.ProducerConfig`](https://kafka.apache.org/31/javadoc/org/apache/kafka/clients/producer/ProducerConfig.html).

Outro detalhe importante é que é preciso especificar quais classes farão a serialização das mensagens. O Kafka não conhece o valor das mensagens e nem a estrutura, toda mensagem é tratada como bites. Isso é uma escolha arquitetural, visto que o foco aqui é na agilidade de entrega, cabe aos clientes saberem (ou podem ignorar) o conteúdo das mensagens.

Assim o código para istanciar é:

```java
var props = new Properties();
props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

try (var producer = new KafkaProducer<String, String>(props)) {
    // [código]
}
```

## 3. Enviando mensagens

Para enviar mensagens é preciso conhecer o corpo, a chave e o tópico. Todas os metadados da mensagem podem ser especificados, mas não é uma prática comum, é preferivel que deixe para o KafkaProducer decidir. Assim vamos criar um ProducerRecord com o tópico, a chave e o conteúdo. Ao enviar a mensagem o médoto send irá retornar um Future com as informações de envio, pois o método [KafkaProducer.send](https://kafka.apache.org/31/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html#send(org.apache.kafka.clients.producer.ProducerRecord)) é assíncrono.

```java
producer.send(new ProducerRecord<>("texto", arquivo, line));
```

Os metadados da mensagem vão contar o timestamp, a partição e o offset. Como nosso tópico só tem uma partição, os offset serão sequenciais. Mas a partição vai depender da chave escolhida. No nosso caso é o nome do arquivo, isso garante que todo o arquivo seja processado sequencialmente.

O resultado completo pode ser encontrado na classe [LeitorTexto](/dia-002/produtor/src/main/java/io/vepo/kafka100dias/leitortexto/LeitorTexto.java).