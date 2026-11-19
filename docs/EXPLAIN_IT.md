# How to explain this framework in an interview

Say this in your own words. It is written the way you would talk, and it is honest about what this project is.

## The 60-second version

"On the side I built an API automation framework in Java, using REST Assured and TestNG with Maven. At Equiniti I test APIs with Postman and Swagger and check the data in MySQL, so I wanted to see the same thing done in code.

It has a layer that sends the requests, and the tests only do the assertions. I check the status code, the body, the JSON schema, the response time, and I chain calls where one response feeds the next. It also tests SOAP, which is just an XML message over HTTP, including a fault.

One thing I like is the service virtualization. I use WireMock as a stand-in service, so I can test a 500 error, a 401 or a slow response, which are hard to get from a real environment. And there's a test that calls the API and then runs SQL to confirm the stored data matches.

It runs from an Azure Pipelines file: build, run the tests, publish the results. And for Parasoft, I prepared an OpenAPI file and a WSDL I can import, and I wrote down how each part maps to SOAtest."

## Be straight about two things
- **It is a personal project**, not something you built at Equiniti. Say "I built" and "on the side".
- **You have not used Parasoft SOAtest on a project** (unless you have). Say what you prepared and what you understand. Do not claim more.

## Likely follow-up questions

**Why REST Assured?**
"It's Java, it reads almost like plain English, and it fits TestNG and Maven. The given, when, then style makes tests easy for anyone on the team to follow."

**What's the difference between a request spec and a response spec?**
"A request spec holds things I always send, like the base URL and headers. A response spec holds checks I always want, like the response time. That way I don't repeat them in every test."

**How do you test a SOAP service?**
"It's an HTTP POST with an XML envelope and sometimes a SOAPAction header. I send the envelope, then check the XML in the response, for example the result value, or the fault message for a bad request. I can generate the requests from the WSDL."

**What is service virtualization and why use it?**
"It's a fake service that behaves like the real one. I use it when the real service isn't ready, is expensive, or can't be made to fail on demand. Then I can test errors, delays and edge cases, and the tests give the same result every time."

**How do you validate data in the database?**
"After the API call I run a SQL query for that record, using the transaction ID, and compare the fields with the response: status, amount and currency. I also check for duplicates with GROUP BY and HAVING."

**How would you run this in Azure Pipelines?**
"The YAML has a trigger on the main branch and pull requests. It builds and runs the Maven tests, publishes the test results so the team can see them in Azure DevOps, and the build fails if a test fails. The fast offline tests run on every pull request."

**How do you handle secrets like passwords or tokens?**
"Not in the code. I'd keep them in pipeline variables or a secure library and read them as environment variables."

**What would you improve?**
"Add Allure reporting, handle authentication flows like OAuth tokens, and add contract testing."
