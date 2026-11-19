# REST Assured API Automation Framework

A Java API test automation framework for **REST and SOAP services**, built with **REST Assured, TestNG and Maven**. It also covers **backend validation with SQL**, **service virtualization** with WireMock, CI on **Azure Pipelines** (and GitHub Actions), and **Parasoft SOAtest assets** (OpenAPI and WSDL) with a guide to how the code maps to SOAtest.

## What is in it
- **REST tests** with a service-object layer, shared request/response specs, JSON schema validation, data-driven tests, chained calls and response-time checks
- **SOAP tests**: an XML envelope sent over HTTP and checked with XPath-style paths, including a SOAP fault case
- **Service virtualization**: WireMock stands in for real services, so error codes, slow responses and SOAP faults can be tested offline and repeatably
- **API vs database validation**: after an API call, SQL confirms the stored data matches (an in-memory H2 database stands in for MySQL)
- **Configuration** from system properties, environment variables or `config.properties`
- **CI**: `azure-pipelines.yml` and `.github/workflows/ci.yml`
- **Parasoft SOAtest**: importable `parasoft/openapi/posts-api.yaml` and `parasoft/soap/calculator.wsdl`, plus `parasoft/README.md`

## Project structure
```
src/main/java/com/qa/framework/
  config/Config.java          settings (system property, env var, properties file)
  specs/RequestSpecs.java     reusable request and response specs
  api/PostsApi.java           service object: sends requests, returns the Response
  models/Post.java            request/response model
  data/TestDataFactory.java   unique test data
  db/DbValidator.java         JDBC helper for SQL validation (parameterized)
src/test/java/com/qa/framework/tests/
  BaseApiTest.java            common setup, logs request/response on failure
  PostsApiTest.java           REST tests on a live API         (group: live)
  VirtualizedRestTest.java    REST error/slow scenarios        (group: virtual)
  SoapServiceTest.java        SOAP request and fault           (group: virtual)
  ApiVsDatabaseTest.java      API vs SQL validation            (groups: virtual, db)
src/test/resources/schemas/   JSON schemas
parasoft/                     SOAtest import files and guide
docs/TEST_CASES.md            test case list mapped to classes
azure-pipelines.yml           Azure DevOps pipeline
```

Design points:
- **Tests only assert.** `PostsApi` sends the request and returns a `Response`, so the same method serves positive and negative tests.
- **Specs avoid repetition**: base URI and headers are set once.
- **Virtualization** makes tests deterministic. Real services cannot easily return a 500, a SOAP fault or a 1.5 second delay on demand.
- **SQL uses prepared statements**, so values are bound as parameters.

## Prerequisites
Java 17 or newer and Maven 3.9 or newer.

## Run
```bash
mvn test                       # everything (live tests need internet)
mvn test -Dgroups=virtual      # offline tests only: virtualized REST, SOAP, database
mvn test -Dgroups=live         # live API tests only
mvn test -Dgroups=smoke        # smoke subset
mvn test -Dapi.base.url=https://other-host.example.com   # point at another environment
```
Reports are written to `target/surefire-reports/`.

## CI
- **Azure Pipelines** (`azure-pipelines.yml`): triggers on pushes and pull requests to `main`, runs the virtualized tests, publishes results, then runs the live smoke tests without blocking the pipeline on a network problem.
- **GitHub Actions** (`.github/workflows/ci.yml`): the same flow, with reports uploaded as an artifact.

## Parasoft SOAtest
SOAtest is licensed software and is not run here. `parasoft/` contains the OpenAPI and WSDL files to import into it, and a guide mapping each part of this framework to the SOAtest concept. See `parasoft/README.md`.

## Limits
- The live tests use the public JSONPlaceholder API, which does not persist data. Create, update and delete return realistic responses but nothing is stored.
- The SOAP service is virtualized for repeatability. The WSDL can be pointed at a real service.
- The database check uses H2 in memory. Use your own JDBC URL and credentials for MySQL.

## Roadmap
Reporting with Allure, contract testing, authentication flows (OAuth tokens), and running in parallel.
