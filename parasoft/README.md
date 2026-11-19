# Parasoft SOAtest alignment

Parasoft SOAtest is a commercial tool for API and service testing (REST and SOAP) that lets you build tests with little or no code. It is licensed software, so it is **not** run in this repository's CI and **no SOAtest project files (`.tst`) are included**. Those files are proprietary and are created inside the tool.

This folder contains what you need to **import into SOAtest** and rebuild the same checks there, plus a guide to how the code framework maps to SOAtest concepts.

## What is here
| File | Use |
|---|---|
| `openapi/posts-api.yaml` | OpenAPI definition of the posts REST API. Import it to generate REST client tests. |
| `soap/calculator.wsdl` | WSDL of the calculator SOAP service. Import it to generate SOAP client tests. |

The Java tests in `src/test/java` check the same services, so you can compare results between the two approaches.

## Concept mapping: this framework and SOAtest
The names below are the general SOAtest concepts. Menu names and wizard steps differ between versions, so check them against the version your team uses.

| In this framework (REST Assured / Java) | Typical SOAtest equivalent |
|---|---|
| `PostsApi` request methods | REST Client or SOAP Client tests, generated from OpenAPI or WSDL |
| `.statusCode(200)`, `.body(...)` assertions | Response assertions (status code, JSON/XML value, schema) |
| `matchesJsonSchemaInClasspath(...)` | Schema validation assertion |
| `@DataProvider` (data-driven) | Data source (CSV, spreadsheet or database) bound to test parameters |
| Chained calls (`extract().path(...)` into the next request) | Extracting a value from a response and using it in a later test |
| `Config` / `-Dapi.base.url` | Environments, with different endpoints per environment |
| WireMock stubs (virtualized services) | Parasoft Virtualize (service virtualization) |
| `DbValidator` SQL checks | Database tools and assertions |
| Maven and TestNG groups in the pipeline | Running a test suite from the command line or from the CI plugin |
| Surefire XML reports | SOAtest reports, which can be published to CI |

## How to rebuild the same tests in SOAtest
1. Create a new project and **create tests from the OpenAPI file** (`posts-api.yaml`). You should get a test per operation.
2. Add assertions: status code 200 for GET, 201 for POST, 404 for an unknown id, and a schema or value check on the body.
3. For the data-driven case, create a data source with ids 1 to 5 and bind it to the `{id}` parameter.
4. For chaining, extract the first post's `id` from the `userId=1` response and use it in the next call.
5. For SOAP, **create tests from `calculator.wsdl`**, then add an assertion on `AddResult` and a negative test for the divide-by-zero fault. Point the endpoint at your virtualized or real service.
6. Put the tests in a suite, then run it from the command line or the CI integration so it can run in the pipeline.

## How to describe this honestly in an interview
- Say you build and run the API tests in code (REST Assured), and that you understand how the same checks are built in SOAtest.
- If you have not used SOAtest on a real project, say so. Describe the mapping above and that you prepared importable OpenAPI and WSDL assets.
- Do not claim SOAtest project experience you do not have.
