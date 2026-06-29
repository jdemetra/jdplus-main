# AGENTS.md — JDemetra+

## Overview

**JDemetra+** (v3) is a **seasonal adjustment and time series analysis** Java library and application suite,
developed by the **National Bank of Belgium** in collaboration with the Deutsche Bundesbank, INSEE and Eurostat.
It is officially recommended by the European Statistical System (ESS) for seasonal and calendar adjustment of
official statistics.

The project implements two leading seasonal adjustment methods:

- **TRAMO/SEATS** – Time series Regression with ARIMA noise, Missing values and Outliers / Signal Extraction in
  ARIMA Time Series (Bank of Spain)
- **X-13ARIMA-SEATS** – extension of X-12ARIMA with SEATS (U.S. Census Bureau)

Beyond seasonal adjustment, JDemetra+ provides tools for outlier detection, nowcasting, temporal disaggregation,
benchmarking, and high-frequency data analysis.

The software is Free and Open Source Software (FOSS) licensed under the **EUPL**. All platform-independent
libraries are published to **Maven Central** under the group `eu.europa.ec.joinup.sat`.

## Architecture

### Naming convention

All Maven `artifactId`s, JPMS module names, and Java package names follow the pattern:

```
jdplus-TOPIC-STEREOTYPE[-CLASSIFIER]
```

- **TOPIC**: `toolkit`, `sa`, `tramoseats`, `x13`, `spreadsheet`, `sql`, `text`
- **STEREOTYPE**: `base`, `cli`, `desktop`, `bom`, `archetype`
- **CLASSIFIER**: `api`, `core`, `xml`, `protobuf`, `information`, `workspace`, `tsp`, `tspbridge`, `r`, `bin`,
  `design`, `branding`

### Top-level Maven aggregators

| Module                  | Description                                                      |
|-------------------------|------------------------------------------------------------------|
| `jdplus-main-base`      | Platform-independent Java libraries — published to Maven Central |
| `jdplus-main-cli`       | Command-line interface plugins and standalone binary             |
| `jdplus-main-desktop`   | NetBeans Platform RCP plugins and desktop application            |
| `jdplus-main-bom`       | Bill of Materials for dependency management                      |
| `jdplus-main-archetype` | Maven archetype for bootstrapping extension projects             |

### Topics (domain areas)

Each topic has its own Maven parent inside `jdplus-main-base`:

| Topic         | Key classes / SPIs                                               | Description                                                                           |
|---------------|------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| `toolkit`     | `TsProvider`, `TsData`, `TsPeriod`, `InformationExtractor`       | Core math, time series, ARIMA, State Space Framework (SSF), regression, TSP framework |
| `sa`          | `SaProcessingFactory`, `SaDiagnosticsFactory`, `SaOutputFactory` | Generic seasonal adjustment API shared by all SA methods                              |
| `tramoseats`  | `Tramo.Processor`, `Seats.Processor`, `TramoSeats.Processor`     | TRAMO/SEATS seasonal adjustment method                                                |
| `x13`         | `RegArima.Processor`, `X11.Processor`, `X13.Processor`           | X-13ARIMA-SEATS seasonal adjustment method                                            |
| `spreadsheet` | `TsProvider` impl                                                | Spreadsheet time series provider (Excel, ODS via `spreadsheet4j`)                     |
| `sql`         | `TsProvider` impl                                                | SQL database time series provider                                                     |
| `text`        | `TsProvider` impl                                                | Text/CSV time series provider                                                         |

### Module classifiers (within each topic in `base`)

| Classifier    | JPMS module suffix | Description                                                                |
|---------------|--------------------|----------------------------------------------------------------------------|
| `api`         | `.api`             | Public API: immutable DTOs, enums, SPI interfaces; declares `uses` entries |
| `core`        | `.core`            | Algorithm implementations; provides `api` SPIs                             |
| `xml`         | `.xml`             | JAXB-based XML serialization (legacy JD2 workspace format)                 |
| `protobuf`    | `.protobuf`        | Protocol Buffers serialization (generated from `.proto` sources)           |
| `information` | `.information`     | `InformationSet` key-value mapping for result extraction                   |
| `workspace`   | `.workspace`       | File-based workspace handlers (`FamilyHandler` SPI)                        |
| `tsp`         | `.tsp`             | Time series provider framework: cube, stream, grid abstractions            |
| `tspbridge`   | `.tspbridge`       | Bridge adapting JD2 (v2) TSP providers to the v3 API                       |
| `r`           | `.r`               | R-ready facades for rJava/RProtoBuf integration                            |

### Deployment stereotypes

#### `base` — Platform-independent libraries

The core dependency graph (simplified):

```
toolkit.base.api
       ↑
sa.base.api ← tramoseats.base.api
sa.base.api ← x13.base.api
       ↑
*.base.core  (algorithm implementations)
       ↑
*.base.xml | *.base.protobuf | *.base.information | *.base.workspace | *.base.r
```

#### `desktop` — NetBeans Platform application

Built with `nbm-maven-plugin`; branding token `nbdemetra`. Each topic contributes one NBM plugin.
The `jdplus-main-desktop-design` module provides shared design-time annotations:
`@GlobalService`, `@SwingComponent`, `@SwingAction`, `@SwingProperty`, `@SwingEditorAttribute`.
The `jdplus-toolkit-desktop-plugin` provides the foundational UI services:
`TsManager`, `WorkspaceRepository`, `WorkspaceItemManager`, `DocumentUIServices`, and JFreeChart integration.

#### `cli` — Command-line interface

Built with `appassembler-maven-plugin`. Each topic contributes a CLI plugin.
`jdplus-main-cli-design` holds the shared `GAV` annotation.

#### `ws` — Web service (PoC)

A **Quarkus** application exposing the JDemetra+ engine over **gRPC** (port 4567) and **REST/JSON**
(port 4569, with OpenAPI/Swagger UI). Serialization uses Protocol Buffers.
Not yet production-ready.

### Service Provider Interface (SPI) pattern

The JPMS `ServiceLoader` mechanism is used throughout (`uses` / `provides … with` in `module-info.java`).
The `java-service-util` annotation processor (`@ServiceProvider`) simplifies registration.

Key extension points:

| SPI                                         | Declaring module         | Description                      |
|---------------------------------------------|--------------------------|----------------------------------|
| `TsProvider`                                | `toolkit.base.api`       | Time series data source provider |
| `SaProcessingFactory`                       | `sa.base.api`            | SA algorithm factory             |
| `SaDiagnosticsFactory`                      | `sa.base.api`            | SA diagnostics factory           |
| `SaOutputFactory`                           | `sa.base.api`            | SA output formatter              |
| `InformationExtractor`                      | `toolkit.base.api`       | Result dictionary mapping        |
| `FamilyHandler`                             | `toolkit.base.workspace` | Workspace file-family handler    |
| `TramoSeats.Processor`, `X13.Processor`, …  | method `api` modules     | Algorithm-specific processors    |
| `ArmaFilter`, `AutoRegressiveEstimation`, … | `toolkit.base.core`      | Internal algorithm SPIs          |

### Serialization formats

| Format           | Classifier     | Usage                                                           |
|------------------|----------------|-----------------------------------------------------------------|
| XML (JAXB)       | `-xml`         | Legacy JD2 workspace interchange, human-readable specifications |
| Protocol Buffers | `-protobuf`    | R integration (`RProtoBuf`), web service transport              |
| Information-set  | `-information` | Generic key-value result extraction and reporting               |

### Key external dependencies

| Dependency                          | Purpose                                                              |
|-------------------------------------|----------------------------------------------------------------------|
| NetBeans Platform                   | RCP application framework (desktop only)                             |
| `com.google.protobuf`               | Binary serialization                                                 |
| `spreadsheet4j`                     | Spreadsheet I/O (Excel, ODS, …)                                      |
| `nbbrd/java-io-util` (`nbbrd.io.*`) | I/O utilities, XML-JAXB helpers                                      |
| `nbbrd/java-sql-util`               | SQL tooling                                                          |
| `com.github.benmanes.caffeine`      | Caching in TSP provider layer                                        |
| Quarkus                             | Web service runtime (`ws` module only)                               |
| Lombok                              | Boilerplate reduction (all modules)                                  |
| JSpecify                            | Nullability annotations                                              |
| `nbbrd/java-service-util`           | `ServiceLoader` annotation processor                                 |
| `nbbrd/java-design-util`            | Design annotations (`@VisibleForTesting`, `@StaticFactoryMethod`, …) |

### Extension ecosystem

`jdplus-main` is the **root lifecycle**. Downstream repositories extend it by depending on the
`jdplus-main-bom` and following the same naming convention:

| Repository            | Topics added                             |
|-----------------------|------------------------------------------|
| `jdplus-benchmarking` | Benchmarking                             |
| `jdplus-incubator`    | STS, STL, HighFreq, AdvancedSA           |
| `jdplus-experimental` | ExperimentalSA, BusinessCycle, Calendars |
| `jdplus-revisions`    | Revisions analysis                       |
| `jdplus-nowcasting`   | Dynamic Factor Models (DFM)              |


## Build & Test

```shell
mvn clean install                 # full build + tests + enforcer checks
mvn clean install -Pyolo          # skip all checks (fast local iteration)
mvn test -pl <module-name> -Pyolo # fast test a single module
mvn test -pl <module-name> -am    # full test a single module
```

- **Java 21 target**
- **JUnit 5** with parallel execution enabled (`junit.jupiter.execution.parallel.enabled=true`); **AssertJ** for assertions

## Key Conventions

- **Lombok**: use lombok annotations when possible. Config in `lombok.config`: `addNullAnnotations=jspecify`, `builder.className=Builder`
- **Nullability**: `@org.jspecify.annotations.Nullable` for nullable; `@lombok.NonNull` for non-null parameters. Return types use `@Nullable` or the `OrNull` suffix (e.g., `getThingOrNull`)
- **Design annotations** use annotations from `java-design-util` such as `@VisibleForTesting`, `@StaticFactoryMethod`, `@DirectImpl`, `@MightBeGenerated`, `@MightBePromoted`
- **Internal packages**: `internal.<project>.*` are implementation details; public API lives in the root and `spi` packages
- **Static analysis**: `forbiddenapis` (no `jdk-unsafe`, `jdk-deprecated`, `jdk-internal`, `jdk-non-portable`, `jdk-reflection`), `modernizer`
- **Reproducible builds**: `project.build.outputTimestamp` is set in the root POM
- **Formatting/style**:
  - Use IntelliJ IDEA default code style for Java
  - Follow existing formatting and match naming conventions exactly
  - Follow the principles of "Effective Java"
  - Follow the principles of "Clean Code"
- **Java/JVM**:
  - Target version defined in root POM properties; some modules may require higher versions
  - Use modern Java feature compatible with defined version

## Agent behavior

- Do respect existing architecture, coding style, and conventions
- Do prefer minimal, reviewable changes
- Do preserve backward compatibility
- Do not introduce new dependencies without justification
- Do not rewrite large sections for cleanliness
- Do not reformat code
- Do not propose additional features or changes beyond the scope of the task
