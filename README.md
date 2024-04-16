# activemqstudy

Para criar um projeto em Quarkus que demonstre o uso do ActiveMQ Artemis como broker de mensagens, siga os passos detalhados abaixo. Este exemplo abrangerá desde a configuração de um container Docker com o Artemis MQ até a implementação de um produtor e consumidor de mensagens no Quarkus.

### a) Criação de um Container do Artemis MQ

Vamos usar o Docker para rodar uma instância do ActiveMQ Artemis. Você pode usar a imagem oficial ou qualquer outra que prefira. Aqui está um exemplo de comando Docker para iniciar o Artemis MQ com usuário e senha definidos:

```bash
docker run -d --name artemis \
  -e ARTEMIS_USERNAME=admin \
  -e ARTEMIS_PASSWORD=admin \
  -p 8161:8161 \
  -p 61616:61616 \
  vromero/activemq-artemis
```

- `ARTEMIS_USERNAME` e `ARTEMIS_PASSWORD` são as variáveis de ambiente para definir o usuário e a senha do serviço.
- Porta `8161` é usada para o console de gerenciamento web.
- Porta `61616` é a porta padrão para conexões JMS.

### b) Dependências Necessárias no `pom.xml`

Para utilizar o ActiveMQ Artemis no Quarkus, adicione as seguintes dependências ao seu `pom.xml`:

```xml
<dependencies>
    <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest</artifactId>
    </dependency>
    <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-messaging-amqp</artifactId>
    </dependency>
    <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest-jackson</artifactId>
    </dependency>
</dependencies>
```

- **quarkus-smallrye-reactive-messaging-amqp**: Esta dependência permite o uso do AMQP 1.0, que é compatível com o Artemis.

### c) Uso do Produtor e do Consumidor de Mensagens

Vamos criar um produtor e um consumidor simples no Quarkus.

**Produtor**:

```java
package org.example;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class MessageProducer {

    @Channel("messages-out")
    Emitter<String> emitter;

    public void send(String message) {
        emitter.send(message);
    }
}
```

**Consumidor**:

```java
package org.example;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class MessageConsumer {

    @Incoming("messages-in")
    public void receive(String message) {
        System.out.println("Received message: " + message);
    }
}
```

### d) Configurações do `application.properties`

Inclua as configurações necessárias para conectar ao Artemis:

```properties
# Configurações AMQP para conectar ao Artemis para todos os canais
amqp-host=localhost
amqp-port=61616
amqp-username=admin
amqp-password=admin

# Configurações de canais
#OUTGOING
mp.messaging.outgoing.messages-out.connector=smallrye-amqp
mp.messaging.outgoing.messages-out.address=minhaqueue

#INCOMING
mp.messaging.incoming.messages-in.connector=smallrye-amqp
mp.messaging.incoming.messages-in.address=minhaqueue

#Opcionalmente configurar o AMQP especificamente para um canal OUTGOING
#mp.messaging.outgoing.messages-out.host=localhost
#mp.messaging.outgoing.messages-out.port=61616
#mp.messaging.outgoing.messages-out.username=admin
#mp.messaging.outgoing.messages-out.password=admin

#Opcionalmente configurar o AMQP especificamente para um canal INCOMING
#mp.messaging.incoming.messages-in.host=localhost
#mp.messaging.incoming.messages-in.port=61616
#mp.messaging.incoming.messages-in.username=admin
#mp.messaging.incoming.messages-in.password=admineue
```

- **connection-url**: URL de conexão com as credenciais e endereço do Artemis.
- **mp.messaging.outgoing.messages-out**: Define o canal de saída para envio de mensagens.
- **mp.messaging.incoming.messages-in**: Define o canal de entrada para recebimento de mensagens.
- O connector é um valor fixo a depender do serviço (ActiveMQ RabbitMq KafkaMQ)
- O Address é o nome da queue que existe no brokercomo 
  
  

As configurações especificadas no `application.properties` para `mp.messaging.outgoing.messages-out` e `mp.messaging.incoming.messages-in` têm uma relação direta com as anotações `@Channel("messages-out")` e `@Incoming("messages-in")` nas classes `MessageProducer` e `MessageConsumer`, respectivamente, no contexto de uma aplicação Quarkus utilizando o SmallRye Reactive Messaging para AMQP (ActiveMQ Artemis). Vamos entender como essas partes interagem:

### Configurações no application.properties

1. **mp.messaging.outgoing.messages-out.connector=smallrye-amqp**: Esta linha define qual conector será usado para o canal de saída nomeado `messages-out`. O valor `smallrye-amqp` indica que o conector AMQP do SmallRye é usado, o que é adequado para integrar com sistemas de mensagens compatíveis com AMQP, como o ActiveMQ Artemis.

2. **mp.messaging.outgoing.messages-out.address=queue**: Especifica o endereço (ou destino) para onde as mensagens enviadas através deste canal serão encaminhadas. Neste caso, é uma fila chamada `queue`. Isso significa que as mensagens produzidas pelo canal `messages-out` serão postadas nesta fila.

3. **mp.messaging.incoming.messages-in.connector=smallrye-amqp**: Similarmente ao canal de saída, essa configuração define que o canal de entrada `messages-in` usará o conector AMQP do SmallRye para receber mensagens.

4. **mp.messaging.incoming.messages-in.address=queue**: Indica o endereço de onde as mensagens serão recebidas para o canal `messages-in`. As mensagens colocadas na fila `queue` serão consumidas por este canal.

### Anotações nas Classes do Produtor e Consumidor

- **@Channel("messages-out") na classe MessageProducer**: Esta anotação é usada para injetar um objeto `Emitter<T>` que é conectado ao canal de saída chamado `messages-out`. Quando o método `send` é chamado no produtor, ele usa esse `Emitter` para enviar mensagens para o canal configurado no `application.properties`, que por sua vez encaminha essas mensagens para a fila `queue` no broker de mensagens.
  
  ```java
  @Channel("messages-out")
  Emitter<String> emitter;
  ```

- **@Incoming("messages-in") na classe MessageConsumer**: Esta anotação é usada para marcar o método que deverá ser invocado quando uma mensagem for recebida no canal de entrada chamado `messages-in`. O método `receive` desta classe será chamado automaticamente sempre que uma nova mensagem chegar na fila `queue`, conforme configurado.
  
  ```java
  @Incoming("messages-in")
  public void receive(String message) {
      System.out.println("Received message: " + message);
  }
  ```

### 

Portanto, as configurações no `application.properties` definem os detalhes técnicos de como os canais `messages-out` e `messages-in` devem se comportar em termos de conexão e roteamento de mensagens, enquanto as anotações `@Channel` e `@Incoming` no código Java conectam essas configurações aos componentes de software que efetivamente enviam e recebem as mensagens. Esta abordagem desacopla a configuração da infraestrutura de mensagens do código da lógica de negócio, permitindo que cada parte seja alterada de forma independente.



O valor do parâmetro `connector` nas configurações de `mp.messaging.outgoing` e `mp.messaging.incoming` no `application.properties` de uma aplicação Quarkus não é arbitrário; ele deve corresponder a um conector de mensagens específico configurado e suportado pelo Quarkus e pelo SmallRye Reactive Messaging.

### Explicação do Conector

- **smallrye-amqp**: Este é o nome do conector para o AMQP 1.0 provido pelo SmallRye, que é uma implementação do MicroProfile Reactive Messaging. Ele é especificamente projetado para trabalhar com brokers de mensagens que suportam o protocolo AMQP 1.0, como o ActiveMQ Artemis, RabbitMQ, e outros.

Quando você especifica `connector=smallrye-amqp` nas configurações de `mp.messaging.*`, você está instruindo o Quarkus a utilizar o conector AMQP do SmallRye para lidar com as mensagens enviadas ou recebidas por esse canal específico. Isso envolve serialização/desserialização de mensagens, gerenciamento de conexão e sessão, tratamento de erros, entre outras funções.

### Outros Conectores Possíveis

Quarkus suporta diversos outros conectores através da integração com SmallRye, incluindo:

- **smallrye-kafka**: Para integração com Apache Kafka.
- **smallrye-mqtt**: Para integração com MQTT.
- **smallrye-reactive-messaging-http**: Para integração com serviços via HTTP em um modelo reativo.
- **smallrye-jms**: Para integração com brokers que suportam JMS (Java Message Service).

Cada conector é projetado para ser usado com um protocolo ou broker de mensagens específico e não pode ser simplesmente substituído por outro sem considerar as compatibilidades e configurações necessárias. Por exemplo, se seu ambiente utiliza Kafka ao invés de Artemis, você deveria usar `smallrye-kafka` em vez de `smallrye-amqp`.

### Conclusão

Portanto, não, o valor do `connector` não pode ser qualquer nome; ele precisa ser um identificador válido de um conector suportado pela configuração do Quarkus e pelo SmallRye Reactive Messaging. A escolha do conector deve alinhar-se com o sistema de mensageria e protocolos que você está usando em sua aplicação. Mudar este valor aleatoriamente sem as configurações apropriadas resultará em falhas na aplicação.



# Acknowledgment

Na maioria dos sistemas de mensageria que usam o padrão AMQP, como o ActiveMQ Artemis, o processo de consumir uma mensagem e retirá-la da fila envolve um conceito conhecido como "acknowledgment" (confirmação de recebimento). O modo como isso é tratado pode variar dependendo do sistema de mensagens e da configuração, mas a ideia básica é que o consumidor precisa informar ao broker de mensagens que a mensagem foi processada com sucesso antes que ela seja removida da fila. Aqui estão os passos gerais e algumas considerações específicas para configurar esse comportamento em um sistema que utiliza o Quarkus com o SmallRye Reactive Messaging para AMQP:

### Configuração Básica de Acknowledgment

#### 1. **Ack Automático**

No modo automático, que é muitas vezes o padrão, o sistema de mensageria automaticamente considera a mensagem como "acknowledged" assim que ela é entregue ao consumidor. Isso significa que a mensagem será removida da fila imediatamente após a entrega, independentemente de ter sido processada com sucesso ou não.

#### 2. **Ack Manual**

No modo manual, o consumidor deve explicitamente enviar uma confirmação de que a mensagem foi processada com sucesso. Isso oferece mais controle, pois permite que o consumidor processe a mensagem e, dependendo do resultado do processamento, decida se deve ou não confirmar o recebimento.

### Uso do SmallRye e Quarkus

Para utilizar acknowledgment manual em um projeto Quarkus que utiliza SmallRye Reactive Messaging, você pode configurar seu consumidor para fazer o ack manualmente. Aqui está um exemplo de como isso pode ser configurado:

```java
package org.example;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import io.smallrye.reactive.messaging.annotations.Blocking;
import org.eclipse.microprofile.reactive.messaging.Message;

@ApplicationScoped
public class MessageConsumer {

    @Incoming("messages-in")
    @Blocking
    public CompletionStage<Void> receive(Message<String> message) {
        try {
            // Processa a mensagem aqui
            String payload = message.getPayload();
            System.out.println("Received message: " + payload);
            // Confirma manualmente que a mensagem foi processada com sucesso
            return message.ack();
        } catch (Exception e) {
            // Lógica de tratamento de erros
            return CompletableFuture.completedFuture(null);
        }
    }
}
```

### Configurações Adicionais

- **No arquivo `application.properties`**, você pode precisar configurar o comportamento de acknowledgment dependendo do broker e da biblioteca utilizada:

```properties
# Configuração de acknowledgment para o canal 'messages-in'
mp.messaging.incoming.messages-in.ack="manual"
```

- **Garantindo Robustez**: Ao usar ack manual, é importante implementar uma lógica de tratamento de erros robusta. Se ocorrer um erro no processamento da mensagem, você deve decidir se a mensagem deve ser rejeitada, descartada ou reenfileirada.

### Considerações Finais

Ao implementar o ack manual, você ganha o controle sobre quando uma mensagem é removida da fila, o que é útil em processamentos onde a falha em uma operação deve impedir a remoção da mensagem, permitindo sua reprocessamento ou análise posterior. Essa abordagem é especialmente importante em sistemas onde a integridade e a confiabilidade dos dados são críticas. Por fim, certifique-se de revisar a documentação específica do seu sistema de mensageria e do framework utilizado para garantir que as configurações estejam corretas e otimizadas para suas necessidades.



### e) Endpoints para Testar

Crie um `Resource` no Quarkus para enviar e receber mensagens via HTTP:

```java
package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.consumer.MessageConsumer;
import org.acme.produtor.MessageProducer;

@Path("/message")
public class MessageResource {
    @Inject
    MessageProducer messageProducer;

    @Inject
    MessageConsumer messageConsumer;

    @GET
    @Path("/send")
    @Produces(MediaType.TEXT_PLAIN)
    public String sendMessage(){
        messageProducer.send("Hello! Im from message producer!!");
        return "Message Sent";
    }
    @POST
    @Path("/send")
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendCustomMessage(String message){
        messageProducer.send(message);
        return Response.ok("Mensagem Sent!").build();
    }
// O método de recebimento é automático pelo Consumer
}


```

### Conclusão

Com este setup, você tem um sistema básico usando Quarkus para produzir e consumir mensagens através do ActiveMQ Artemis. Para testar, inicie seu ambiente Docker, execute sua aplicação Quarkus e acesse

-----



This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:

```shell
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 

```shell
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 

```shell
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/activemqstudy-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- Messaging - AMQP Connector ([guide](https://quarkus.io/guides/amqp)): Connect to AMQP with Reactive Messaging
- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it
