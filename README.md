# #100DaysOfCode com Apache Kafka

Essa é uma iniciativa que não vou conseguir fazer todo dia, mas será o desafio que coloco para quem quiser. Vou preparar um roteiro e quem for seguindo pode me marcar e marcar [@dev_roadmap](https://twitter.com/dev_roadmap/) para acompanhar o progresso. 

## Regras

1. Escrever código ao menos por 1 hora nos próximos 100 dias
2. Tweetar seu progresso usando as tags #100DaysOfCode e #ApachaKafka

A primeira regra eu vou suavizar porque eu mesmo não consigo (quem tem filhos entende) reservar 1h por dia todo dia.

## Aceite o desafio publicamente

Ao aceitar o desafio, compartilha no [Twitter](https://twitter.com/intent/tweet?text=Aceitei%20o%20desafio%20de%20participar%20do%20%23100DaysOfCode%20com%20%23ApacheKafka%20iniciando%20hoje%21%20Me%20segue%20a%C3%AD%20pra%20aprender...%20%20Oi%20%40vepo%20e%20%40dev_roadmap&url=https://github.com/vepo/kafka-100-days&hashtags=100DaysOfCode,ApacheKafka).

## Roteiro

- [ ] Dia 001 - Instale o Apache Kafka (escreva um Dockerfile, ou instale ele localmente) [[1]](https://vepo.github.io/posts/rodando-o-apache-kafka-localmente)
- [ ] Dia 002 - Crie um prodor simples que envia um arquivo texto para um tópico, cada linha deve ser uma mensagem [[2]](https://vepo.github.io/posts/enviando-mensagens)
- [ ] Dia 003 - Crie um consumidor simples que lê as mensagens de um tópico [[3]](https://vepo.github.io/posts/recebendo-mensagens)
- [ ] Dia 004 - Altere o seu produtor para enviar um POJO usando um serializador JSON que você mesmo escreveu. Use [Jackson](https://www.devmedia.com.br/introducao-ao-jackson-objectmapper/43174)
- [ ] Dia 005 - Altere seu consumidor para receber um POJO usando um desserializador JSON que você mesmo escreveu. Use [Jackson](https://www.devmedia.com.br/introducao-ao-jackson-objectmapper/43174)
- [ ] Dia 006 - Crie um nome consumidor com um novo [group.id](https://kafka.apache.org/documentation/#consumerconfigs_group.id) e veja como os dois consumidores funcionam em paralelo.
- [ ] Dia 007 - Adicione ao menos mais uma instância do Kafka e tente conectar no cluster, não somente em um broker. 
- [ ] Dia 008 - Explore os scripts `kafka-topics` e `kafka-consumer-groups`. Se você usa docker eles estão dentro do seu container, se usa local estão na pasta `bin` do Kafka.
- [ ] Dia 009 - Altere as configurações do seu tópico, adicione mais partições e mude o fator de replicação. Verifique as possiblidades. [[4]](https://vepo.github.io/posts/anatomia-de-um-topico)
- [ ] Dia 010 - Rode mais de uma instância do mesmo consumidor para um tópico com mais de uma partição e veja como funciona.
- [ ] (...)
