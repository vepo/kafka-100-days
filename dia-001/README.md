# Instale o Apache Kafka (escreva um Dockerfile, ou instale ele localmente)

Kafka depende do Zookeeper, logo é preciso um docker-compose.yaml com os dois serviços.


Eu criei o docker-compose.yaml contendo a última versão da [imagem da Confluent](https://hub.docker.com/r/confluentinc/cp-kafka).
Além da Kafka e do Zookeeper adicionei um Schema Registry que será usado mais a frente.

Ao configurar o Apache Kafka, eu adicionei dois listeners, ver propriedade [listener.security.protocol.map](https://kafka.apache.org/documentation/#brokerconfigs_listener.security.protocol.map). O primeiro listener aponta para a porta `9092` e associado com ele tem o hostname `kafka-01`, vou explicar isso mais a frente. Com o outro listener estão associados a porta `29092` e `localhost`.

Porque é importante associar um hostname a um listener? Porque nem sempre o cliente irá se conectar ao broker para requerer informações do cluster,
ao obter essa informação o próprio cliente irá decidir em qual broker ele irá se comunicar (_ver [Partitioning and bootstrapping](https://kafka.apache.org/protocol.html#protocol_partitioning) na documentação_). Isso significa que ao decidir em qual broker o cliente irá se comunicar, ele usará esse hostname que no nosso caso pode ser tanto `localhost` quanto `kafka-01`. 

Por isso que o endereço de conexão é chamado de `bootstrap-server`, porque ele não precisa conter todos os brokers do cluster, essa informação será descoberta em durante a conexão. **MAS** ele deve conter um número de brokers suficiente para que ao menos um deles esteja sempre disponível.

Para executar:

```bash
docker-compose up -d
```

Para verificar se ele está em execução:

```bash
$ docker ps -a
CONTAINER ID   IMAGE                                   COMMAND                  CREATED              STATUS              PORTS                                                             NAMES
24c0015a1fbc   confluentinc/cp-kafka:6.2.4             "/etc/confluent/dock…"   About a minute ago   Up About a minute   9092/tcp, 0.0.0.0:29092->29092/tcp, :::29092->29092/tcp           dia-001_kafka-01_1
0b9f4b2efb3f   confluentinc/cp-zookeeper:6.2.4         "/etc/confluent/dock…"   About a minute ago   Up About a minute   2888/tcp, 3888/tcp, 0.0.0.0:22181->2181/tcp, :::22181->2181/tcp   dia-001_zookeeper_1
a97709d3efa2   confluentinc/cp-schema-registry:6.2.4   "/etc/confluent/dock…"   About a minute ago   Up About a minute   0.0.0.0:8081->8081/tcp, :::8081->8081/tcp                         dia-001_schema-registry_1

```

No container `dia-001_kafka-01_1` é possível acessar a linha de comando para ver os scripts que ajudarão a fazer a manutenção do cluster. Nos comandos abaixo eu listo todos os scripts e depois uso o `kafka-configs` para listar todos as configurações do broker identificado pelo id **1**. Essas configurações podem ser encontradas na documentação: [Broker Configs](https://kafka.apache.org/documentation/#brokerconfigs).

```bash
$ docker exec -it dia-001_kafka-01_1 bash
[appuser@24c0015a1fbc ~]$ ls /usr/bin/kafka-*
/usr/bin/kafka-acls                 /usr/bin/kafka-console-consumer    /usr/bin/kafka-delegation-tokens  /usr/bin/kafka-leader-election  /usr/bin/kafka-preferred-replica-election  /usr/bin/kafka-run-class     /usr/bin/kafka-streams-application-reset
/usr/bin/kafka-broker-api-versions  /usr/bin/kafka-console-producer    /usr/bin/kafka-delete-records     /usr/bin/kafka-log-dirs         /usr/bin/kafka-producer-perf-test          /usr/bin/kafka-server-start  /usr/bin/kafka-topics
/usr/bin/kafka-cluster              /usr/bin/kafka-consumer-groups     /usr/bin/kafka-dump-log           /usr/bin/kafka-metadata-shell   /usr/bin/kafka-reassign-partitions         /usr/bin/kafka-server-stop   /usr/bin/kafka-verifiable-consumer
/usr/bin/kafka-configs              /usr/bin/kafka-consumer-perf-test  /usr/bin/kafka-features           /usr/bin/kafka-mirror-maker     /usr/bin/kafka-replica-verification        /usr/bin/kafka-storage       /usr/bin/kafka-verifiable-producer
[appuser@24c0015a1fbc ~]$ kafka-configs --bootstrap-server localhost:9092 --all --describe --broker 1
All configs for broker 1 are:
  log.cleaner.min.compaction.lag.ms=0 sensitive=false synonyms={DEFAULT_CONFIG:log.cleaner.min.compaction.lag.ms=0}
  offsets.topic.num.partitions=50 sensitive=false synonyms={DEFAULT_CONFIG:offsets.topic.num.partitions=50}
  log.flush.interval.messages=9223372036854775807 sensitive=false synonyms={DEFAULT_CONFIG:log.flush.interval.messages=9223372036854775807}
  controller.socket.timeout.ms=30000 sensitive=false synonyms={DEFAULT_CONFIG:controller.socket.timeout.ms=30000}
  principal.builder.class=null sensitive=false synonyms={}
  log.flush.interval.ms=null sensitive=false synonyms={}
  min.insync.replicas=1 sensitive=false synonyms={DEFAULT_CONFIG:min.insync.replicas=1}
  num.recovery.threads.per.data.dir=1 sensitive=false synonyms={DEFAULT_CONFIG:num.recovery.threads.per.data.dir=1}
  ssl.keystore.type=JKS sensitive=false synonyms={DEFAULT_CONFIG:ssl.keystore.type=JKS}
  zookeeper.ssl.protocol=TLSv1.2 sensitive=false synonyms={DEFAULT_CONFIG:zookeeper.ssl.protocol=TLSv1.2}
  sasl.mechanism.inter.broker.protocol=GSSAPI sensitive=false synonyms={DEFAULT_CONFIG:sasl.mechanism.inter.broker.protocol=GSSAPI}
  fetch.purgatory.purge.interval.requests=1000 sensitive=false synonyms={DEFAULT_CONFIG:fetch.purgatory.purge.interval.requests=1000}
  ssl.endpoint.identification.algorithm=https sensitive=false synonyms={DEFAULT_CONFIG:ssl.endpoint.identification.algorithm=https}
  zookeeper.ssl.keystore.location=null sensitive=false synonyms={}
  replica.socket.timeout.ms=30000 sensitive=false synonyms={DEFAULT_CONFIG:replica.socket.timeout.ms=30000}
  message.max.bytes=1048588 sensitive=false synonyms={DEFAULT_CONFIG:message.max.bytes=1048588}
  max.connection.creation.rate=2147483647 sensitive=false synonyms={DEFAULT_CONFIG:max.connection.creation.rate=2147483647}
  connections.max.reauth.ms=0 sensitive=false synonyms={DEFAULT_CONFIG:connections.max.reauth.ms=0}
  log.flush.offset.checkpoint.interval.ms=60000 sensitive=false synonyms={DEFAULT_CONFIG:log.flush.offset.checkpoint.interval.ms=60000}
  zookeeper.clientCnxnSocket=null sensitive=false synonyms={}
  zookeeper.ssl.client.enable=false sensitive=false synonyms={DEFAULT_CONFIG:zookeeper.ssl.client.enable=false}
  quota.window.num=11 sensitive=false synonyms={DEFAULT_CONFIG:quota.window.num=11}
  zookeeper.connect=zookeeper:2181 sensitive=false synonyms={STATIC_BROKER_CONFIG:zookeeper.connect=zookeeper:2181}
  authorizer.class.name= sensitive=false synonyms={DEFAULT_CONFIG:authorizer.class.name=}
  password.encoder.secret=null sensitive=true synonyms={}
  num.replica.fetchers=1 sensitive=false synonyms={DEFAULT_CONFIG:num.replica.fetchers=1}
  alter.log.dirs.replication.quota.window.size.seconds=1 sensitive=false synonyms={DEFAULT_CONFIG:alter.log.dirs.replication.quota.window.size.seconds=1}
  log.roll.jitter.hours=0 sensitive=false synonyms={DEFAULT_CONFIG:log.roll.jitter.hours=0}
  password.encoder.old.secret=null sensitive=true synonyms={}
  log.cleaner.delete.retention.ms=86400000 sensitive=false synonyms={DEFAULT_CONFIG:log.cleaner.delete.retention.ms=86400000}
  queued.max.requests=500 sensitive=false synonyms={DEFAULT_CONFIG:queued.max.requests=500}
  log.cleaner.threads=1 sensitive=false synonyms={DEFAULT_CONFIG:log.cleaner.threads=1}
  sasl.kerberos.service.name=null sensitive=false synonyms={}
  socket.request.max.bytes=104857600 sensitive=false synonyms={DEFAULT_CONFIG:socket.request.max.bytes=104857600}
  log.message.timestamp.type=CreateTime sensitive=false synonyms={DEFAULT_CONFIG:log.message.timestamp.type=CreateTime}
  zookeeper.ssl.keystore.type=null sensitive=false synonyms={}
  connections.max.idle.ms=600000 sensitive=false synonyms={DEFAULT_CONFIG:connections.max.idle.ms=600000}
  zookeeper.set.acl=false sensitive=false synonyms={DEFAULT_CONFIG:zookeeper.set.acl=false}
  delegation.token.expiry.time.ms=86400000 sensitive=false synonyms={DEFAULT_CONFIG:delegation.token.expiry.time.ms=86400000}
  max.connections=2147483647 sensitive=false synonyms={DEFAULT_CONFIG:max.connections=2147483647}
  transaction.state.log.num.partitions=50 sensitive=false synonyms={DEFAULT_CONFIG:transaction.state.log.num.partitions=50}
  listener.security.protocol.map=PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT sensitive=false synonyms={STATIC_BROKER_CONFIG:listener.security.protocol.map=PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT, DEFAULT_CONFIG:listener.security.protocol.map=PLAINTEXT:PLAINTEXT,SS
L:SSL,SASL_PLAINTEXT:SASL_PLAINTEXT,SASL_SSL:SASL_SSL}
  log.retention.hours=168 sensitive=false synonyms={DEFAULT_CONFIG:log.retention.hours=168}
  client.quota.callback.class=null sensitive=false synonyms={}
  ssl.provider=null sensitive=false synonyms={}
  delete.records.purgatory.purge.interval.requests=1 sensitive=false synonyms={DEFAULT_CONFIG:delete.records.purgatory.purge.interval.requests=1}
  log.roll.ms=null sensitive=false synonyms={}
  ssl.cipher.suites= sensitive=false synonyms={DEFAULT_CONFIG:ssl.cipher.suites=}
  zookeeper.ssl.keystore.password=null sensitive=true synonyms={}
  security.inter.broker.protocol=PLAINTEXT sensitive=false synonyms={DEFAULT_CONFIG:security.inter.broker.protocol=PLAINTEXT}
  delegation.token.secret.key=null sensitive=true synonyms={}
  replica.high.watermark.checkpoint.interval.ms=5000 sensitive=false synonyms={DEFAULT_CONFIG:replica.high.watermark.checkpoint.interval.ms=5000}
  replication.quota.window.size.seconds=1 sensitive=false synonyms={DEFAULT_CONFIG:replication.quota.window.size.seconds=1}
  sasl.kerberos.ticket.renew.window.factor=0.8 sensitive=false synonyms={DEFAULT_CONFIG:sasl.kerberos.ticket.renew.window.factor=0.8}
  zookeeper.connection.timeout.ms=null sensitive=false synonyms={}
  metrics.recording.level=INFO sensitive=false synonyms={DEFAULT_CONFIG:metrics.recording.level=INFO}
  password.encoder.cipher.algorithm=AES/CBC/PKCS5Padding sensitive=false synonyms={DEFAULT_CONFIG:password.encoder.cipher.algorithm=AES/CBC/PKCS5Padding}
  ssl.principal.mapping.rules=DEFAULT sensitive=false synonyms={DEFAULT_CONFIG:ssl.principal.mapping.rules=DEFAULT}
  replica.selector.class=null sensitive=false synonyms={}
  max.connections.per.ip=2147483647 sensitive=false synonyms={DEFAULT_CONFIG:max.connections.per.ip=2147483647}
  background.threads=10 sensitive=false synonyms={DEFAULT_CONFIG:background.threads=10}
  quota.consumer.default=9223372036854775807 sensitive=false synonyms={DEFAULT_CONFIG:quota.consumer.default=9223372036854775807}
  request.timeout.ms=30000 sensitive=false synonyms={DEFAULT_CONFIG:request.timeout.ms=30000}
  log.message.format.version=2.8-IV1 sensitive=false synonyms={DEFAULT_CONFIG:log.message.format.version=2.8-IV1}
  sasl.login.class=null sensitive=false synonyms={}
  log.dir=/tmp/kafka-logs sensitive=false synonyms={DEFAULT_CONFIG:log.dir=/tmp/kafka-logs}
  log.segment.bytes=1073741824 sensitive=false synonyms={DEFAULT_CONFIG:log.segment.bytes=1073741824}
  replica.fetch.response.max.bytes=10485760 sensitive=false synonyms={DEFAULT_CONFIG:replica.fetch.response.max.bytes=10485760}
  group.max.session.timeout.ms=1800000 sensitive=false synonyms={DEFAULT_CONFIG:group.max.session.timeout.ms=1800000}
  port=9092 sensitive=false synonyms={DEFAULT_CONFIG:port=9092}
  log.segment.delete.delay.ms=60000 sensitive=false synonyms={DEFAULT_CONFIG:log.segment.delete.delay.ms=60000}
  log.retention.minutes=null sensitive=false synonyms={}
  log.dirs=/var/lib/kafka/data sensitive=false synonyms={STATIC_BROKER_CONFIG:log.dirs=/var/lib/kafka/data}
  controlled.shutdown.enable=true sensitive=false synonyms={DEFAULT_CONFIG:controlled.shutdown.enable=true}
  socket.connection.setup.timeout.max.ms=30000 sensitive=false synonyms={DEFAULT_CONFIG:socket.connection.setup.timeout.max.ms=30000}
  log.message.timestamp.difference.max.ms=9223372036854775807 sensitive=false synonyms={DEFAULT_CONFIG:log.message.timestamp.difference.max.ms=9223372036854775807}
  password.encoder.key.length=128 sensitive=false synonyms={DEFAULT_CONFIG:password.encoder.key.length=128}
  sasl.login.refresh.min.period.seconds=60 sensitive=false synonyms={DEFAULT_CONFIG:sasl.login.refresh.min.period.seconds=60}
  transaction.abort.timed.out.transaction.cleanup.interval.ms=10000 sensitive=false synonyms={DEFAULT_CONFIG:transaction.abort.timed.out.transaction.cleanup.interval.ms=10000}
  sasl.kerberos.kinit.cmd=/usr/bin/kinit sensitive=false synonyms={DEFAULT_CONFIG:sasl.kerberos.kinit.cmd=/usr/bin/kinit}
  log.cleaner.io.max.bytes.per.second=1.7976931348623157E308 sensitive=false synonyms={DEFAULT_CONFIG:log.cleaner.io.max.bytes.per.second=1.7976931348623157E308}
  auto.leader.rebalance.enable=true sensitive=false synonyms={DEFAULT_CONFIG:auto.leader.rebalance.enable=true}
  leader.imbalance.check.interval.seconds=300 sensitive=false synonyms={DEFAULT_CONFIG:leader.imbalance.check.interval.seconds=300}
  log.cleaner.min.cleanable.ratio=0.5 sensitive=false synonyms={DEFAULT_CONFIG:log.cleaner.min.cleanable.ratio=0.5}
  replica.lag.time.max.ms=30000 sensitive=false synonyms={DEFAULT_CONFIG:replica.lag.time.max.ms=30000}
  num.network.threads=3 sensitive=false synonyms={DEFAULT_CONFIG:num.network.threads=3}
  ssl.keystore.key=null sensitive=true synonyms={}
  sasl.client.callback.handler.class=null sensitive=false synonyms={}
  metrics.num.samples=2 sensitive=false synonyms={DEFAULT_CONFIG:metrics.num.samples=2}
  socket.send.buffer.bytes=102400 sensitive=false synonyms={DEFAULT_CONFIG:socket.send.buffer.bytes=102400}
  password.encoder.keyfactory.algorithm=null sensitive=false synonyms={}
  socket.receive.buffer.bytes=102400 sensitive=false synonyms={DEFAULT_CONFIG:socket.receive.buffer.bytes=102400}
  replica.fetch.min.bytes=1 sensitive=false synonyms={DEFAULT_CONFIG:replica.fetch.min.bytes=1}
  broker.rack=null sensitive=false synonyms={}
  zookeeper.ssl.truststore.password=null sensitive=true synonyms={}
  unclean.leader.election.enable=false sensitive=false synonyms={DEFAULT_CONFIG:unclean.leader.election.enable=false}
  offsets.retention.check.interval.ms=600000 sensitive=false synonyms={DEFAULT_CONFIG:offsets.retention.check.interval.ms=600000}
  producer.purgatory.purge.interval.requests=1000 sensitive=false synonyms={DEFAULT_CONFIG:producer.purgatory.purge.interval.requests=1000}
  metrics.sample.window.ms=30000 sensitive=false synonyms={DEFAULT_CONFIG:metrics.sample.window.ms=30000}
  log.retention.check.interval.ms=300000 sensitive=false synonyms={DEFAULT_CONFIG:log.retention.check.interval.ms=300000}
  sasl.login.refresh.window.jitter=0.05 sensitive=false synonyms={DEFAULT_CONFIG:sasl.login.refresh.window.jitter=0.05}
  leader.imbalance.per.broker.percentage=10 sensitive=false synonyms={DEFAULT_CONFIG:leader.imbalance.per.broker.percentage=10}
  controller.quota.window.num=11 sensitive=false synonyms={DEFAULT_CONFIG:controller.quota.window.num=11}
  advertised.host.name=null sensitive=false synonyms={}
  metric.reporters= sensitive=false synonyms={DEFAULT_CONFIG:metric.reporters=}
  quota.producer.default=9223372036854775807 sensitive=false synonyms={DEFAULT_CONFIG:quota.producer.default=9223372036854775807}
  auto.create.topics.enable=true sensitive=false synonyms={DEFAULT_CONFIG:auto.create.topics.enable=true}
  ssl.engine.factory.class=null sensitive=false synonyms={}
  replica.socket.receive.buffer.bytes=65536 sensitive=false synonyms={DEFAULT_CONFIG:replica.socket.receive.buffer.bytes=65536}
  zookeeper.ssl.truststore.location=null sensitive=false synonyms={}
  replica.fetch.wait.max.ms=500 sensitive=false synonyms={DEFAULT_CONFIG:replica.fetch.wait.max.ms=500}
  password.encoder.iterations=4096 sensitive=false synonyms={DEFAULT_CONFIG:password.encoder.iterations=4096}
  default.replication.factor=1 sensitive=false synonyms={DEFAULT_CONFIG:default.replication.factor=1}
  ssl.truststore.password=null sensitive=true synonyms={}
  sasl.kerberos.principal.to.local.rules=DEFAULT sensitive=false synonyms={DEFAULT_CONFIG:sasl.kerberos.principal.to.local.rules=DEFAULT}
  log.preallocate=false sensitive=false synonyms={DEFAULT_CONFIG:log.preallocate=false}
  transactional.id.expiration.ms=604800000 sensitive=false synonyms={DEFAULT_CONFIG:transactional.id.expiration.ms=604800000}
  control.plane.listener.name=null sensitive=false synonyms={}
  transaction.state.log.replication.factor=3 sensitive=false synonyms={DEFAULT_CONFIG:transaction.state.log.replication.factor=3}
  num.io.threads=8 sensitive=false synonyms={DEFAULT_CONFIG:num.io.threads=8}
  sasl.login.refresh.buffer.seconds=300 sensitive=false synonyms={DEFAULT_CONFIG:sasl.login.refresh.buffer.seconds=300}
  offsets.commit.required.acks=-1 sensitive=false synonyms={DEFAULT_CONFIG:offsets.commit.required.acks=-1}
  connection.failed.authentication.delay.ms=100 sensitive=false synonyms={DEFAULT_CONFIG:connection.failed.authentication.delay.ms=100}
  delete.topic.enable=true sensitive=false synonyms={DEFAULT_CONFIG:delete.topic.enable=true}
  quota.window.size.seconds=1 sensitive=false synonyms={DEFAULT_CONFIG:quota.window.size.seconds=1}
  ssl.truststore.type=JKS sensitive=false synonyms={DEFAULT_CONFIG:ssl.truststore.type=JKS}
  offsets.commit.timeout.ms=5000 sensitive=false synonyms={DEFAULT_CONFIG:offsets.commit.timeout.ms=5000}
  zookeeper.ssl.ocsp.enable=false sensitive=false synonyms={DEFAULT_CONFIG:zookeeper.ssl.ocsp.enable=false}
  log.cleaner.max.compaction.lag.ms=9223372036854775807 sensitive=false synonyms={DEFAULT_CONFIG:log.cleaner.max.compaction.lag.ms=9223372036854775807}
  zookeeper.ssl.enabled.protocols=null sensitive=false synonyms={}
  log.retention.ms=null sensitive=false synonyms={}
  alter.log.dirs.replication.quota.window.num=11 sensitive=false synonyms={DEFAULT_CONFIG:alter.log.dirs.replication.quota.window.num=11}
  log.cleaner.enable=true sensitive=false synonyms={DEFAULT_CONFIG:log.cleaner.enable=true}
  offsets.load.buffer.size=5242880 sensitive=false synonyms={DEFAULT_CONFIG:offsets.load.buffer.size=5242880}
  ssl.client.auth=none sensitive=false synonyms={DEFAULT_CONFIG:ssl.client.auth=none}
  controlled.shutdown.max.retries=3 sensitive=false synonyms={DEFAULT_CONFIG:controlled.shutdown.max.retries=3}
  offsets.topic.replication.factor=1 sensitive=false synonyms={STATIC_BROKER_CONFIG:offsets.topic.replication.factor=1, DEFAULT_CONFIG:offsets.topic.replication.factor=3}
  zookeeper.ssl.truststore.type=null sensitive=false synonyms={}
  transaction.state.log.min.isr=2 sensitive=false synonyms={DEFAULT_CONFIG:transaction.state.log.min.isr=2}
  ssl.secure.random.implementation=null sensitive=false synonyms={}
  sasl.kerberos.ticket.renew.jitter=0.05 sensitive=false synonyms={DEFAULT_CONFIG:sasl.kerberos.ticket.renew.jitter=0.05}
  ssl.trustmanager.algorithm=PKIX sensitive=false synonyms={DEFAULT_CONFIG:ssl.trustmanager.algorithm=PKIX}
  zookeeper.session.timeout.ms=18000 sensitive=false synonyms={DEFAULT_CONFIG:zookeeper.session.timeout.ms=18000}
  log.retention.bytes=-1 sensitive=false synonyms={DEFAULT_CONFIG:log.retention.bytes=-1}
  controller.quota.window.size.seconds=1 sensitive=false synonyms={DEFAULT_CONFIG:controller.quota.window.size.seconds=1}
  sasl.jaas.config=null sensitive=true synonyms={}
  sasl.kerberos.min.time.before.relogin=60000 sensitive=false synonyms={DEFAULT_CONFIG:sasl.kerberos.min.time.before.relogin=60000}
  offsets.retention.minutes=10080 sensitive=false synonyms={DEFAULT_CONFIG:offsets.retention.minutes=10080}
  replica.fetch.backoff.ms=1000 sensitive=false synonyms={DEFAULT_CONFIG:replica.fetch.backoff.ms=1000}
  inter.broker.protocol.version=2.8-IV1 sensitive=false synonyms={DEFAULT_CONFIG:inter.broker.protocol.version=2.8-IV1}
  kafka.metrics.reporters= sensitive=false synonyms={DEFAULT_CONFIG:kafka.metrics.reporters=}
  num.partitions=1 sensitive=false synonyms={DEFAULT_CONFIG:num.partitions=1}
  ssl.keystore.certificate.chain=null sensitive=true synonyms={}
  socket.connection.setup.timeout.ms=10000 sensitive=false synonyms={DEFAULT_CONFIG:socket.connection.setup.timeout.ms=10000}
  broker.id.generation.enable=true sensitive=false synonyms={DEFAULT_CONFIG:broker.id.generation.enable=true}
  listeners=PLAINTEXT://0.0.0.0:9092,PLAINTEXT_HOST://0.0.0.0:29092 sensitive=false synonyms={STATIC_BROKER_CONFIG:listeners=PLAINTEXT://0.0.0.0:9092,PLAINTEXT_HOST://0.0.0.0:29092}
  ssl.enabled.protocols=TLSv1.2,TLSv1.3 sensitive=false synonyms={DEFAULT_CONFIG:ssl.enabled.protocols=TLSv1.2,TLSv1.3}
  inter.broker.listener.name=PLAINTEXT sensitive=false synonyms={STATIC_BROKER_CONFIG:inter.broker.listener.name=PLAINTEXT}
  alter.config.policy.class.name=null sensitive=false synonyms={}
  delegation.token.expiry.check.interval.ms=3600000 sensitive=false synonyms={DEFAULT_CONFIG:delegation.token.expiry.check.interval.ms=3600000}
  log.flush.scheduler.interval.ms=9223372036854775807 sensitive=false synonyms={DEFAULT_CONFIG:log.flush.scheduler.interval.ms=9223372036854775807}
  zookeeper.max.in.flight.requests=10 sensitive=false synonyms={DEFAULT_CONFIG:zookeeper.max.in.flight.requests=10}
  log.index.size.max.bytes=10485760 sensitive=false synonyms={DEFAULT_CONFIG:log.index.size.max.bytes=10485760}
  ssl.keymanager.algorithm=SunX509 sensitive=false synonyms={DEFAULT_CONFIG:ssl.keymanager.algorithm=SunX509}
  sasl.login.callback.handler.class=null sensitive=false synonyms={}
  replica.fetch.max.bytes=1048576 sensitive=false synonyms={DEFAULT_CONFIG:replica.fetch.max.bytes=1048576}
  zookeeper.ssl.crl.enable=false sensitive=false synonyms={DEFAULT_CONFIG:zookeeper.ssl.crl.enable=false}
  sasl.server.callback.handler.class=null sensitive=false synonyms={}
  log.cleaner.dedupe.buffer.size=134217728 sensitive=false synonyms={DEFAULT_CONFIG:log.cleaner.dedupe.buffer.size=134217728}
  advertised.port=null sensitive=false synonyms={}
  log.cleaner.io.buffer.size=524288 sensitive=false synonyms={DEFAULT_CONFIG:log.cleaner.io.buffer.size=524288}
  create.topic.policy.class.name=null sensitive=false synonyms={}
  ssl.truststore.certificates=null sensitive=true synonyms={}
  controlled.shutdown.retry.backoff.ms=5000 sensitive=false synonyms={DEFAULT_CONFIG:controlled.shutdown.retry.backoff.ms=5000}
  security.providers=null sensitive=false synonyms={}
  log.roll.hours=168 sensitive=false synonyms={DEFAULT_CONFIG:log.roll.hours=168}
  log.cleanup.policy=delete sensitive=false synonyms={DEFAULT_CONFIG:log.cleanup.policy=delete}
  log.flush.start.offset.checkpoint.interval.ms=60000 sensitive=false synonyms={DEFAULT_CONFIG:log.flush.start.offset.checkpoint.interval.ms=60000}
  host.name= sensitive=false synonyms={DEFAULT_CONFIG:host.name=}
  log.roll.jitter.ms=null sensitive=false synonyms={}
  transaction.state.log.segment.bytes=104857600 sensitive=false synonyms={DEFAULT_CONFIG:transaction.state.log.segment.bytes=104857600}
  offsets.topic.segment.bytes=104857600 sensitive=false synonyms={DEFAULT_CONFIG:offsets.topic.segment.bytes=104857600}
  group.initial.rebalance.delay.ms=3000 sensitive=false synonyms={DEFAULT_CONFIG:group.initial.rebalance.delay.ms=3000}
  log.index.interval.bytes=4096 sensitive=false synonyms={DEFAULT_CONFIG:log.index.interval.bytes=4096}
  log.cleaner.backoff.ms=15000 sensitive=false synonyms={DEFAULT_CONFIG:log.cleaner.backoff.ms=15000}
  ssl.truststore.location=null sensitive=false synonyms={}
  offset.metadata.max.bytes=4096 sensitive=false synonyms={DEFAULT_CONFIG:offset.metadata.max.bytes=4096}
  ssl.keystore.password=null sensitive=true synonyms={}
  zookeeper.sync.time.ms=2000 sensitive=false synonyms={DEFAULT_CONFIG:zookeeper.sync.time.ms=2000}
  fetch.max.bytes=57671680 sensitive=false synonyms={DEFAULT_CONFIG:fetch.max.bytes=57671680}
  compression.type=producer sensitive=false synonyms={DEFAULT_CONFIG:compression.type=producer}
  max.connections.per.ip.overrides= sensitive=false synonyms={DEFAULT_CONFIG:max.connections.per.ip.overrides=}
  sasl.login.refresh.window.factor=0.8 sensitive=false synonyms={DEFAULT_CONFIG:sasl.login.refresh.window.factor=0.8}
  kafka.metrics.polling.interval.secs=10 sensitive=false synonyms={DEFAULT_CONFIG:kafka.metrics.polling.interval.secs=10}
  max.incremental.fetch.session.cache.slots=1000 sensitive=false synonyms={DEFAULT_CONFIG:max.incremental.fetch.session.cache.slots=1000}
  delegation.token.master.key=null sensitive=true synonyms={}
  ssl.key.password=null sensitive=true synonyms={}
  reserved.broker.max.id=1000 sensitive=false synonyms={DEFAULT_CONFIG:reserved.broker.max.id=1000}
  transaction.remove.expired.transaction.cleanup.interval.ms=3600000 sensitive=false synonyms={DEFAULT_CONFIG:transaction.remove.expired.transaction.cleanup.interval.ms=3600000}
  log.message.downconversion.enable=true sensitive=false synonyms={DEFAULT_CONFIG:log.message.downconversion.enable=true}
  ssl.protocol=TLSv1.3 sensitive=false synonyms={DEFAULT_CONFIG:ssl.protocol=TLSv1.3}
  transaction.state.log.load.buffer.size=5242880 sensitive=false synonyms={DEFAULT_CONFIG:transaction.state.log.load.buffer.size=5242880}
  ssl.keystore.location=null sensitive=false synonyms={}
  sasl.enabled.mechanisms=GSSAPI sensitive=false synonyms={DEFAULT_CONFIG:sasl.enabled.mechanisms=GSSAPI}
  num.replica.alter.log.dirs.threads=null sensitive=false synonyms={}
  zookeeper.ssl.cipher.suites=null sensitive=false synonyms={}
  group.min.session.timeout.ms=6000 sensitive=false synonyms={DEFAULT_CONFIG:group.min.session.timeout.ms=6000}
  log.cleaner.io.buffer.load.factor=0.9 sensitive=false synonyms={DEFAULT_CONFIG:log.cleaner.io.buffer.load.factor=0.9}
  transaction.max.timeout.ms=900000 sensitive=false synonyms={DEFAULT_CONFIG:transaction.max.timeout.ms=900000}
  group.max.size=2147483647 sensitive=false synonyms={DEFAULT_CONFIG:group.max.size=2147483647}
  delegation.token.max.lifetime.ms=604800000 sensitive=false synonyms={DEFAULT_CONFIG:delegation.token.max.lifetime.ms=604800000}
  broker.id=1 sensitive=false synonyms={STATIC_BROKER_CONFIG:broker.id=1, DEFAULT_CONFIG:broker.id=-1}
  offsets.topic.compression.codec=0 sensitive=false synonyms={DEFAULT_CONFIG:offsets.topic.compression.codec=0}
  zookeeper.ssl.endpoint.identification.algorithm=HTTPS sensitive=false synonyms={DEFAULT_CONFIG:zookeeper.ssl.endpoint.identification.algorithm=HTTPS}
  replication.quota.window.num=11 sensitive=false synonyms={DEFAULT_CONFIG:replication.quota.window.num=11}
  advertised.listeners=PLAINTEXT://kafka-01:9092,PLAINTEXT_HOST://localhost:29092 sensitive=false synonyms={STATIC_BROKER_CONFIG:advertised.listeners=PLAINTEXT://kafka-01:9092,PLAINTEXT_HOST://localhost:29092}
  queued.max.request.bytes=-1 sensitive=false synonyms={DEFAULT_CONFIG:queued.max.request.bytes=-1}
```
