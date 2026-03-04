#set( $h1 = '#' )
#set( $h2 = '##' )
#set( $h3 = '###' )
$h1 ${topic} extensions for JDemetra+ v3

This repository has been created by a Maven archetype and contains the source code of some JDemetra+ v3 extensions.

The parameters used to generate this project are the following:

| Parameter | Value |
|------------|------------|
| topic | `${topic}` |
| forgeUrl | `${forgeUrl}` |
| devName | `${devName}` |
| devEmail | `${devEmail}` |
| devOrg | `${devOrg}` |
| devOrgUrl | `${devOrgUrl}` |
| artifactId | `${artifactId}` |
| version | `${version}` |

$h2 Developing

This project is written in Java and uses [Apache Maven](https://maven.apache.org/) as a build tool.  
It requires [Java @maven.compiler.release@ as minimum version](https://whichjdk.com/) and all its dependencies are hosted on [Maven Central](https://search.maven.org/).

The code can be built using any IDE or by just type-in the following commands in a terminal:
```shell
git clone ${forgeUrl}.git
cd jdplus-${topic}
mvn clean install
```

$h3 Structure

```
${artifactId}/
├── pom.xml                                 $h1 Root POM (parent of all modules)
├── ${artifactId}-base/
│   ├── pom.xml                             $h1 Base aggregator
│   └── ${artifactId}-base-parent/
│       ├── pom.xml                         $h1 Base parent POM
│       └── ${artifactId}-base-api/
│           ├── pom.xml                     $h1 Base API module
│           └── src/
│               ├── main/java/
│               └── test/java/
├── ${artifactId}-bom/
│   └── pom.xml                             $h1 Bill of Materials
├── ${artifactId}-cli/
│   ├── pom.xml                             $h1 CLI aggregator
│   └── ${artifactId}-cli-plugin/
│       └── pom.xml                         $h1 CLI plugin module
└── ${artifactId}-desktop/
    ├── pom.xml                             $h1 Desktop aggregator
    └── ${artifactId}-desktop-plugin/
        ├── pom.xml                         $h1 Desktop plugin module
        └── src/
            ├── main/
            │   ├── java/
            │   ├── javadoc/
            │   ├── nbm/
            │   └── resources/
            └── test/java/
```

$h3 Naming

Git repositories names, Maven modules artifactId, Java modules names and Java packages names follow this naming convention:
`PREFIX-TOPIC[-STEREOTYPE[-CLASSIFIER]]`

This naming convention is enforced by the following regex pattern:
```regexp
^(jdplus)-(\w+)(?:-(base|cli|desktop|bom)(?:-(\w+))?)?$
```

$h2 Licensing

The code of this project is licensed under the [European Union Public Licence (EUPL)](https://joinup.ec.europa.eu/page/eupl-text-11-12).
 
