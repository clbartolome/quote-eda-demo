Quote Demo
============================

This application is based on a Quarkus sample available at https://quarkus.io/guides/amqp. 

A small enhancement has been introduced to the backend service to simulate a variable response time resulting in out-of-order responses:
[QuoteProcessor.java](quote-processor/src/main/java/org/acme/amqp/processor/QuoteProcessor.java)

It was originally designed to use the AMQP protocol, but in this branch the configuration is changed to sit on top of Kafka: comparing it with the main branch you'll notice that thanks to Quarkus and Microprofile the required changes are minimal.

### Run Locally

In a first terminal, run:

```sh
podman compose up -d 

mvn -f quote-producer quarkus:dev
```

In a second terminal, run:

```sh
mvn -f quote-processor quarkus:dev -DdebugPort=5006
```

Then, open your browser to `http://localhost:8080/`, and click on the "Request Quote" button.

Optionally, you can launch the analytics µService:

```sh
mvn -f quote-analytics quarkus:dev -DdebugPort=5007
```

### Run In OpenShift

Install ArgoCD and Streams for Apache Kafka operators.

Create a namespace `my-kafka` and a Kafka instance with default configuration (do not change anything).


In a terminal, create the quotes application in ArgoCD:

```sh
oc new-project quotes
oc adm policy add-cluster-role-to-user cluster-admin  system:serviceaccount:openshift-gitops:openshift-gitops-argocd-application-controller -n quotes

oc apply -f argocd/quote-app.yaml
```

Go to ArgoCD and review created resources.

Then, open your browser and open producer route in `quotes` namespace.

### Build applications images

```sh
mvn -f quote-processor package -Pnative -Dquarkus.native.container-build=true
cd quote-processor
podman build -f src/main/docker/Dockerfile.native -t quote-processor .
podman push quay.io/calopezb/quote-processor:1.0.0

cd ..

mvn -f quote-producer package -Pnative  -Dquarkus.native.container-build=true
cd quote-producer
podman build -f src/main/docker/Dockerfile.native -t quote-producer .
podman push quay.io/calopezb/quote-producer:1.0.0
```