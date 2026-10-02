# kafka avro consumer


### Run locally
```shell
docker compose -f docker/docker-compose.yml up -d
./mvnw spring-boot:run
```

The consumer uses PostgreSQL. The defaults connect to the local Compose database.
For deployment, set `PERSON_DB_URL`, `PERSON_DB_USERNAME`, and `PERSON_DB_PASSWORD`
to the same shared database on every consumer instance; the bundled credentials
are for local development. Liquibase creates the schema and the unique constraint
on `person_entity.event_id`, and Hibernate validates the schema at startup.
The migration targets a new database; migrate any existing data before switching
an existing deployment to this database.

### Useful Links
* Swagger UI: http://localhost:8085/swagger-ui.html
* Actuator Endpoint: http://localhost:8085/actuator
* Schema Registry : http://localhost:8081/subjects/persons-value/versions?normalize=false
