[![Build Status](https://travis-ci.com/IgorNB/chatty.svg?branch=master)](https://travis-ci.com/IgorNB/chatty)
[![SonarCloud Quality](https://sonarcloud.io/api/project_badges/measure?project=com.lig%3Achatty&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.lig%3Achatty)
[![SonarCloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.lig%3Achatty&metric=coverage)](https://sonarcloud.io/dashboard?id=com.lig%3Achatty)
[![SonarCloud CodeSmell](https://sonarcloud.io/api/project_badges/measure?project=com.lig%3Achatty&metric=code_smells)](https://sonarcloud.io/dashboard?id=com.lig%3Achatty)
[![SonarCloud Bug](https://sonarcloud.io/api/project_badges/measure?project=com.lig%3Achatty&metric=bugs)](https://sonarcloud.io/dashboard?id=com.lig%3Achatty)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
# chatty Pet-Project
See demo on [youtube]().

(Chat) Sample project which illustrates JWT secured statless rest-api & websocket (through RabbitMQ) in Spring Boot.

## Prerequisites
* [Node.js](https://nodejs.org/en/)
* java 8
* [git](https://git-scm.com/)

## Getting Started
* get source code
```
git clone https://github.com/IgorNB/chatty.git
```

* install front-end dependencies
```
npm install
```

* Start development environment (with not needed to be installed in-memory H2 dataBase and in-memory MQ). Run the following commands in two separate terminals to create a blissful development experience where your browser auto-refreshes when files change on your hard drive
You will get (relative to host):
1.  api: localhost:8090
2.  api swagger ui: localhost:8090/swagger-ui.html
3.  spring actuator: localhost:8090/actuator
4.  front-end ui sample: localhost:8080
```
./mvnw
npm start
```
* Alternative is to start full environment in separate docker containers (1 nginx loadbalancer, >1 Spring backend services, 1 Postgres DB database, 1 RabbitMQ MQ). Please wait - it takes some time after container is up. 
You will get (relative to host):
1.  api: localhost:8090
2.  api swagger ui: localhost:8090/swagger-ui.html
3.  spring actuator (per server): localhost:8090/actuator
4.  rabbitmq ui: localhost:15672 (guest/guest)
5.  front-end ui sample: localhost:8080
 
```
mvnw clean install -DskipTests
docker-compose build
docker-compose up -d --remove-orphans
npm start
```
and to remove 
```
docker-compose stop
docker-compose rm
```
 
### Installing

No installation available. See "Getting started"

## Running the tests
 * tests 
```
./mvnw clean test
```
 * tests coverage (with Jacoco. Min line coverage is auto-checked 70%) 
```
./mvnw clean verify
```


### And coding style tests

#### General rules 
checked with Sonar Quility Gate


## Development
#### Controller
Regular Spring `@RestController` are used.
#### Dto, Mapper
Regular `POJO`'s are used as Dto's. [MapStruct](http://mapstruct.org/) is used as Mapper library. 
#### Service
Regular Spring `@Service`'s are used 
#### Entity, Repository
Regular `JPA` entities and Spring Data JPA repositories are used.
#### Migrations
Entity changes are applyed to DB on application start (Liquibase is used). Liquibase changesets are used to illustrate production ready development lifecycle (instead of Hibernate Hbm2dll which is [deprecated](http://docs.jboss.org/tools/4.1.0.Final/en/hibernatetools/html/ant.html#d0e4651) for usage in production).

* To speed up development changeSet should be generated automatically, then reviewed by developer and included in commit. Use next command to auto-generate changeset (note that liquibase.contexts=gen is used to skip test data load, which should be marked with "context: test" in changeSet files):

``` 
mvn clean compile liquibase:clearCheckSums liquibase:dropAll liquibase:update -Dliquibase.contexts=gen liquibase:diff
```

* Review auto-generataed changeSet file, make changes if needed and run next command to check your test data can be loaded in your new DB schema (or just start application and this will be done automatically):

``` 
mvn clean compile liquibase:clearCheckSums liquibase:dropAll liquibase:update liquibase:diff
```

* Changeset will be also automatically applied on start up, but if you want to see DB without starting application run next command:
``` 
mvn liquibase:update
```

* Changeset can be auto-generated in xml, yaml or SQL (database specific) format. To change it, please, change value of the next property in .POM file:
```
<liquibase.changeLogFile.format>
```

## Built With

* [Spring](https://docs.spring.io/spring/docs/5.1.4) - web framework
* [MapStruct](http://mapstruct.org/) - mapper library
* [Apache Commons Lang 3](https://commons.apache.org/proper/commons-lang/download_lang.cgi) - helper utilities for the java.lang API
* [Maven](https://maven.apache.org/) - dependency Management
* [Junit5](https://junit.org/junit5/) - testing framework
* [Mockito](https://site.mockito.org/) -  mocking framework for unit tests
* [AssertJ](http://joel-costigliola.github.io/assertj/) - fluent assertions library
* [Jacoco](https://www.eclemma.org/jacoco/) - code coverage library
* [Sonar Cube](https://sonarcloud.io) - Continuous Code Quality provider
* [Travis CI](https://travis-ci.org) - Continuous Integration provider
## Contributing

Not available 


## Authors

* **IgorNB**

## License

This project is licensed under the Apache License - see the [LICENSE.txt](LICENSE.txt) file for details

