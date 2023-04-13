# JDemetra+ v3

**JDemetra+ is a tool for seasonal adjustment (SA)** developed by the National Bank of Belgium (NBB) in cooperation with the Deutsche Bundesbank and Eurostat in accordance with the Guidelines of the European Statistical System (ESS).

## Installing / Getting started

JDemetra+ v3 runs on any operating system that supports **Java 17 or later** such as Microsoft **Windows**, **Solaris OS**, Apple **macOS**, **Ubuntu** and other various **Linux** distributions.

The project is still in development, but you can find **daily builds** and install instructions at https://github.com/nbbrd/jdemetra-app-snapshot.

Its main **documentation** is available at https://jdemetra-new-documentation.netlify.app.

## Developing

This project is written in Java and uses [Apache Maven](https://maven.apache.org/) as a build tool.  
It requires [Java 17 as minimum version](https://whichjdk.com/) and all its dependencies are hosted on [Maven Central](https://search.maven.org/).

The code can be build using any IDE or by just type-in the following commands in a terminal:
```shell
git clone https://github.com/jdemetra/jdplus-main.git
cd jdplus-main
mvn clean install
```

### Structure

JDemetra+ code is **divided into topics** (toolkit, x13, ...) and is **grouped by lifecycle** (main, experimental, ...).  
Each group is hosted in a separate Git repository while each topic has its own Maven module.

For example, the x13 topic can be found in the Maven module `jdplus-x13-base-parent` of the repository `jdplus-main`.

Here is the schema of all the groups and their topics: 

```mermaid
flowchart BT 
    subgraph main
        mainx[toolkit, tramoseats, x13, sa, spreadsheet, sql, text]
    end

    subgraph benchmarking
        benchmarkingx[benchmarking]
    end
    benchmarking --> main
    click benchmarkingx https://github.com/jdemetra/jdplus-benchmarking

    subgraph incubator
        incubatorx[sts\nstl\nhighfreq\nadvancedsa]
    end
    incubator --> main
    click incubatorx https://github.com/jdemetra/jdplus-incubator

    subgraph experimental
        experimentalx[experimentalsa\nbusinesscycle\ncalendars]
    end
    experimental --> main
    click experimentalx https://github.com/jdemetra/jdplus-experimental

    subgraph revisions
        revisionsx[revisions]
    end
    revisions --> main
    click revisionsx https://github.com/jdemetra/jdplus-revisions

    subgraph nowcasting
        nowcastingx[dfm]
    end
    nowcasting --> main
    click nowcastingx https://github.com/jdemetra/jdplus-nowcasting
```

### Naming

Git repositories names, Maven modules artifactId, Java modules names and Java packages names follow this naming convention:  
`PREFIX-TOPIC[-STEREOTYPE[-CLASSIFIER]]` 

This naming convention is enforced by the following regex pattern:
```regexp
^(jdplus)-(\w+)(?:-(base|cli|desktop)(?:-(\w+))?)?$
```

## Contributing

Any contribution is welcome and should be done through pull requests and/or issues.

## Licensing

The code of this project is licensed under the [European Union Public Licence (EUPL)](https://joinup.ec.europa.eu/page/eupl-text-11-12).
 