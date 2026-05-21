# JDemetra+ v3 – Web Service

> **⚠ Warning: this project is a proof of concept and is not yet ready for production use.**

This module exposes the JDemetra+ v3 engine as a service through **gRPC** and a **REST API**.  
gRPC achieves the best raw performance while the REST API reaches the broadest set of clients.

- gRPC server reflection is enabled, making it easy to explore the API with tools like `grpcurl`.
- The REST API is exposed via JSON transcoding on top of the gRPC service.
- An **OpenAPI schema and Swagger UI** are generated automatically for the REST API.

Internally the project is built on [Quarkus](https://quarkus.io/).

---

## Table of contents

- [Ports](#ports)
- [Available operations](#available-operations)
- [Calling the service](#calling-the-service)
  - [gRPC (grpcurl)](#grpc-grpcurl)
  - [REST (curl)](#rest-curl)
- [Running in dev mode](#running-in-dev-mode)
- [Packaging and running](#packaging-and-running)
- [Docker](#docker)
- [Creating a native executable](#creating-a-native-executable)
- [Configuration](#configuration)

---

## Ports

| Mode     | gRPC   | HTTP (REST / OpenAPI) | Dev UI                           |
|----------|--------|-----------------------|----------------------------------|
| **dev**  | `4566` | `4568`                | `http://127.0.0.1:4568/q/dev-ui` |
| **prod** | `4567` | `4569`                | –                                |

OpenAPI / Swagger UI (prod): <http://localhost:4569/q/swagger-ui>

---

## Available operations

All operations belong to the `jdplus.main.ws.TsFunctions` gRPC service.  
Every operation is also available as a streaming variant (`*Stream`).

| RPC                | Input                   | Output                   | Description                                                          |
|--------------------|-------------------------|--------------------------|----------------------------------------------------------------------|
| `Normalize`        | `TsFunctionInput`       | `TsFunctionOutput`       | Standardizes the series: `(y − mean) / stdev`                        |
| `Statistics`       | `TsFunctionInput`       | `DescriptiveStatistics`  | Basic descriptive statistics (n, min, max, mean, stdev, quartiles)   |
| `Pct`              | `PctInput`              | `TsFunctionOutput`       | Percentage change: `(y[t] / y[t−lag] − 1) × 100`                     |
| `Delta`            | `DeltaInput`            | `TsFunctionOutput`       | Difference operator applied *power* times: `y[t] − y[t−lag]`         |
| `Aggregate`        | `AggregationInput`      | `TsFunctionOutput`       | Frequency conversion (sum, average, first, last, min, max)           |
| `HodrickPrescott`  | `HodrickPrescottInput`  | `HodrickPrescottOutput`  | Hodrick–Prescott filter (returns trend and noise components)         |
| `BuildTsData`      | `BuildTsDataInput`      | `TsFunctionOutput`       | Converts a list of dated observations into a regular `TsData` series |
| `BuildTsDataTable` | `BuildTsDataTableInput` | `BuildTsDataTableOutput` | Aligns a collection of `TsData` series into a matrix                 |

Supported frequencies: `FREQ_YEARLY`, `FREQ_HALF_YEARLY`, `FREQ_QUADRI_MONTHLY`, `FREQ_QUARTERLY`, `FREQ_BI_MONTHLY`, `FREQ_MONTHLY`, `FREQ_DAILY`.

---

## Calling the service

### gRPC (grpcurl)

Install [grpcurl](https://github.com/fullstorydev/grpcurl), then:

```shell
# List available services
grpcurl -plaintext localhost:4567 list

# List methods of TsFunctions
grpcurl -plaintext localhost:4567 list jdplus.main.ws.TsFunctions

# Describe the Normalize method
grpcurl -plaintext localhost:4567 describe jdplus.main.ws.TsFunctions.Normalize

# Normalize a yearly series
grpcurl -plaintext -d '{"id":"abc","series":{"start":{"frequency":"FREQ_YEARLY","year":2010},"values":[3.0,4.0,5.0]}}' \
  localhost:4567 jdplus.main.ws.TsFunctions.Normalize

# Descriptive statistics
grpcurl -plaintext -d '{"id":"abc","series":{"start":{"frequency":"FREQ_MONTHLY","year":2020,"pos":0},"values":[1,2,3,4,5,6,7,8,9,10,11,12]}}' \
  localhost:4567 jdplus.main.ws.TsFunctions.Statistics

# Hodrick-Prescott filter (lambda=1600)
grpcurl -plaintext -d '{"id":"hp","series":{"start":{"frequency":"FREQ_QUARTERLY","year":2000},"values":[100,102,101,103]},"lambda":1600}' \
  localhost:4567 jdplus.main.ws.TsFunctions.HodrickPrescott
```

### REST (curl)

```shell
# Normalize
curl -s -X POST -H "Content-Type: application/json" \
  localhost:4569/hello/normalize \
  --data '{"id":"abc","series":{"start":{"frequency":"FREQ_YEARLY","year":2010},"values":[3.0,4.0,5.0]}}'

# Descriptive statistics
curl -s -X POST -H "Content-Type: application/json" \
  localhost:4569/hello/statistics \
  --data '{"id":"abc","series":{"start":{"frequency":"FREQ_MONTHLY","year":2020,"pos":0},"values":[1,2,3,4,5,6]}}'
```

> **CORS** is pre-configured to accept requests from `http://localhost:*` and `http://127.0.0.1:*`.  
> To allow all origins during development, set `quarkus.http.cors.origins=*` in `application.properties`.

---

## Running in dev mode

Dev mode enables live coding and hot reload:

```shell
mvn compile quarkus:dev
```

- gRPC → `localhost:4566`
- REST / OpenAPI → `localhost:4568`
- Dev UI → <http://127.0.0.1:4568/q/dev-ui>

---

## Packaging and running

Build an über-jar:

```shell
mvn package
java -jar target/*-runner.jar
```

---

## Docker

Four Dockerfiles are available under `src/main/docker/`:

| File                      | Description                                      |
|---------------------------|--------------------------------------------------|
| `Dockerfile.jvm`          | Standard JVM image                               |
| `Dockerfile.legacy-jar`   | JVM image using the legacy (non-über) jar layout |
| `Dockerfile.native`       | Native executable image                          |
| `Dockerfile.native-micro` | Minimal native image (micro base image)          |

Build and run the JVM image:

```shell
mvn package
docker build -f src/main/docker/Dockerfile.jvm -t jdplus-main-ws:latest .
docker run -p 4567:4567 -p 4569:4569 jdplus-main-ws:latest
```

---

## Creating a native executable

Native packaging requires [GraalVM Community Edition](https://github.com/graalvm/graalvm-ce-builds/releases).

```shell
# With GraalVM installed locally
mvn package -Dnative

# Without GraalVM – build inside a container
mvn package -Pnative -Dquarkus.native.container-build=true

# Run the resulting binary
./target/jdplus-main-ws-*-runner
```

---

## Configuration

Quarkus supports [multiple configuration sources](https://quarkus.io/guides/config-reference).  
The simplest approach is to pass system properties on the command line:

```shell
# Change the gRPC server port
java -Dquarkus.grpc.server.port=5000 -jar target/*-runner.jar
```

Key properties (see `src/main/resources/application.properties`):

| Property                                        | Dev default | Prod default | Description             |
|-------------------------------------------------|-------------|--------------|-------------------------|
| `quarkus.grpc.server.port`                      | `4566`      | `4567`       | gRPC server port        |
| `quarkus.http.port`                             | `4568`      | `4569`       | HTTP (REST) server port |
| `quarkus.grpc.server.enable-reflection-service` | `true`      | `true`       | Enable gRPC reflection  |
| `quarkus.http.cors.enabled`                     | `true`      | `true`       | Enable CORS             |

Full list of gRPC configuration properties: <https://quarkus.io/guides/grpc-service-implementation#server-configuration>
