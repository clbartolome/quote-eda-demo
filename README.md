Quarkus AMQP 1.0 Quickstart
============================

This project illustrates how you can interact with AMQP 1.0 (Apache Artemis in this quickstart) using MicroProfile Reactive Messaging.
The complete instructions are available on https://quarkus.io/guides/amqp.

## Start the application in dev mode

In a first terminal, run:

```bash
> mvn -f amqp-quickstart-producer quarkus:dev
```

In a second terminal, run:

```bash
> mvn -f amqp-quickstart-processor quarkus:dev
```  

Then, open your browser to `http://localhost:8080/quotes.html`, and click on the "Request Quote" button.

## Build the application in JVM mode

To build the applications, run:

```bash
> mvn -f amqp-quickstart-producer package
> mvn -f amqp-quickstart-processor package
```

Because we are running in _prod_ mode, we need to provide an AMQP 1.0 broker.
The [docker-compose.yml](docker-compose.yml) file starts the broker and your application.

Start the broker and the applications using:

```bash
> docker compose up --build
```

Then, open your browser to `http://localhost:8080/quotes.html`, and click on the "Request Quote" button.
 

## Build the application in native mode

To build the applications into native executables, run:

```bash
> mvn -f amqp-quickstart-producer package -Pnative  -Dquarkus.native.container-build=true
> mvn -f amqp-quickstart-processor package -Pnative -Dquarkus.native.container-build=true
```

The `-Dquarkus.native.container-build=true` instructs Quarkus to build Linux 64bits native executables, who can run inside containers.  

Then, start the system using:

```bash
> export QUARKUS_MODE=native
> docker compose up
```
Then, open your browser to `http://localhost:8080/quotes.html`, and click on the "Request Quote" button.

## Openshift Deployment

Install the AMQ Broker Operator

Deploy the broker:

```sh
oc apply -f k8s/01-ActiveMQArtemis-CRD.yaml
```

Deploy the processor:

```sh
./mvnw -f amqp-quickstart-processor package -DskipTests -Dquarkus.kubernetes.deploy=true
```

Deploy the producer:
```sh
./mvnw -f amqp-quickstart-producer package -DskipTests -Dquarkus.kubernetes.deploy=true
```
## Broker Test

Import AMQ Broker image:

```sh
oc import-image amq7/amq-broker-rhel8:7.11.3-1.1698106824 --from=registry.redhat.io/amq7/amq-broker-rhel8:7.11.3-1.1698106824 --confirm
```

Launch the producer job and show the logs:
```sh
oc create -f k8s/02-producer.yaml
oc logs -f -l job-name=producer
```

Launch the consumer job and show the logs:
```sh
oc create -f k8s/03-consumer.yaml
oc logs -f -l job-name=consumer
```

Remove the jobs:
```sh
oc delete job consumer producer
```
