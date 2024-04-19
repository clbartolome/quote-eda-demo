AMQ Broker Demo
============================

The purpose of this demo is to:

- Deploy AMQ Broker in OpenShift
- Prove the AMQ Broker failover
- Deploy a simple Quote application which leverages AMQ Broker for asynchronous backend interactions

## AMQ Broker installation

Install the AMQ Broker Operator through the OperatorHub

Create a project:

```sh
oc new-project amq-broker
```

Deploy the broker:

```sh
oc apply -f k8s/01-ActiveMQArtemis-CRD.yaml
```

## AMQ Broker failover test

Launch the producer pod:

```sh
oc run producer -ti --image=registry.redhat.io/amq7/amq-broker-rhel8:7.11.3 --rm=true --restart=Never -- \
    /opt/amq/bin/artemis producer --user admin --password redhat1! --url "tcp://amq-broker-acceptor-std-0-svc:5672?failoverAttempts=10&useTopologyForLoadBalancing=true" --message-count 10000 --sleep 100 --verbose
```
In another terminal launch the consumer pod:

```sh
oc run producer -ti --image=registry.redhat.io/amq7/amq-broker-rhel8:7.11.3 --rm=true --restart=Never -- \
    /opt/amq/bin/artemis consumer --user admin --password redhat1! --url "tcp://amq-broker-acceptor-std-0-svc:5672?failoverAttempts=10&useTopologyForLoadBalancing=true" --message-count 10000 --verbose
```

Open another terminal and check where the producer and consumer are connected:

```sh
oc rsh amq-broker-ss-0
```

Inside the container issue the following commands to check the first instance and repeat for others:

```sh
amq-broker/bin/artemis queue stat --url tcp://amq-broker-acceptor-std-0-svc:5672
```

From the outcoming table, you should spot an active consumer.

Kill broker with the attached consumer, e.g.:

```sh
oc delete pod amq-broker-ss-0
```

You should notice that both consumer and producer stop for a couple of seconds and then continue (failback) on the other broker.

## Quarkus Quote Demo

This application is based on a Quarkus sample available at https://quarkus.io/guides/amqp.
Check there detailed information about the application which use the AMQP 1.0 protocol.
It was introduced a small enhancement in the backend service to simulate a varying response time leading to unordered replies:
[QuoteProcessor.java](amqp-quickstart-processor/src/main/java/org/acme/amqp/processor/QuoteProcessor.java)

### Start the application in dev mode

In a first terminal, run:

```sh
mvn -f amqp-quickstart-producer quarkus:dev
```

In a second terminal, run:

```sh
mvn -f amqp-quickstart-processor quarkus:dev
```  

Then, open your browser to `http://localhost:8080/`, and click on the "Request Quote" button.

### Build the application in JVM mode

To build the applications, run:

```sh
mvn -f amqp-quickstart-producer package
mvn -f amqp-quickstart-processor package
```

Because we are running in _prod_ mode, we need to provide an AMQP 1.0 broker.
The [docker-compose.yml](docker-compose.yml) file starts the broker and your application.

Start the broker and the applications using:

```sh
docker compose up --build
```

Then, open your browser to `http://localhost:8080/`, and click on the "Request Quote" button.

Alternatively, you can use **podman**:

1. Build the images:

    ```sh
    podman build amqp-quickstart-processor -f amqp-quickstart-processor/src/main/docker/Dockerfile.jvm -t quarkus-quickstarts/amqp-quickstart-processor:1.0-jvm
    podman build amqp-quickstart-producer -f amqp-quickstart-producer/src/main/docker/Dockerfile.jvm -t quarkus-quickstarts/amqp-quickstart-producer:1.0-jvm
    ```

2. Run all the containers:

    ```sh
    podman kube play amqp-quickstart-kube.yaml
    ```

Finally, to launch a container at a time:

```sh
podman network create amq-broker
podman run -d --rm --name artemis --net=amq-broker -e AMQ_USER=quarkus -e AMQ_PASSWORD=quarkus -e AMQ_EXTRA_ARGS="--relax-jolokia" quay.io/artemiscloud/activemq-artemis-broker:latest
podman run -d --rm --name processor --net=amq-broker -e AMQP_HOST=artemis -e AMQP_PORT=5672 amqp-quickstart-processor:1.0-jvm
podman run -d --rm --name producer --net=amq-broker -e AMQP_HOST=artemis -e AMQP_PORT=5672 -p 8080:8080 amqp-quickstart-producer:1.0-jvm
```

### Build the application in native mode

To build the applications into native executables, run:

```sh
mvn -f amqp-quickstart-producer package -Pnative  -Dquarkus.native.container-build=true
mvn -f amqp-quickstart-processor package -Pnative -Dquarkus.native.container-build=true
```

The `-Dquarkus.native.container-build=true` instructs Quarkus to build Linux 64bits native executables, who can run inside containers.  

Then, start the system using:

```sh
export QUARKUS_MODE=native
docker compose up
```
Then, open your browser to `http://localhost:8080/`, and click on the "Request Quote" button.

### Openshift Deployment

Deploy the processor:

```sh
./mvnw -f amqp-quickstart-processor package -DskipTests -Dquarkus.kubernetes.deploy=true
```

Deploy the producer:
```sh
./mvnw -f amqp-quickstart-producer package -DskipTests -Dquarkus.kubernetes.deploy=true
```