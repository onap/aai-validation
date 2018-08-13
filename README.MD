# Validation microservice

This project has mixed Java and Groovy source code.

[Maven](https://maven.apache.org/) 3+ is required.

#### To build and run tests:
`mvn clean verify`

Eclipse users should install [Groovy Eclipse plugins](https://github.com/groovy/groovy-eclipse/wiki).

#### To generate jars (including apidocs):
`mvn package`

#### To run on localhost:
`mvn spring-boot:run`

### HTTPS interfaces

[Validation Service](https://localhost:9501/services/validation-service/v1/app/validate)

[Info Service](https://localhost:9501/services/validation-service/v1/core/core-service/info)
