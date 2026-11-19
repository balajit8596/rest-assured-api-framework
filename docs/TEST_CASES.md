# Test Cases

| ID | Area | Title | Expected result | Group | Class |
|---|---|---|---|---|---|
| TC-100 | REST | List posts | 200 and a non-empty list | live, smoke | `PostsApiTest` |
| TC-101 | REST | Single post matches schema | 200, schema valid, id = 1 | live, smoke | `PostsApiTest` |
| TC-102 | REST | Create a post | 201, data echoed, id present | live | `PostsApiTest` |
| TC-103 | REST | Update a post | 200 with the new title | live | `PostsApiTest` |
| TC-104 | REST | Delete a post | 200 | live | `PostsApiTest` |
| TC-105 | REST | Unknown resource | 404 | live | `PostsApiTest` |
| TC-106 | REST | Filter by query parameter | Every item has the requested userId | live | `PostsApiTest` |
| TC-107 | REST | Response time | Within the configured limit | live | `PostsApiTest` |
| TC-108 | REST | Chained calls | Value from call 1 works in call 2 | live | `PostsApiTest` |
| TC-109 | REST | Data driven ids 1 to 5 | 200 and matching id for each | live | `PostsApiTest` |
| TC-110 | Virtual REST | Happy path from stub | 200 with stubbed data | virtual, smoke | `VirtualizedRestTest` |
| TC-111 | Virtual REST | Unauthorized | 401 and error message | virtual | `VirtualizedRestTest` |
| TC-112 | Virtual REST | Server error | 500 and error message | virtual | `VirtualizedRestTest` |
| TC-113 | Virtual REST | Slow dependency | 200 after at least the simulated delay | virtual | `VirtualizedRestTest` |
| TC-120 | SOAP | Add | `AddResult` = 5 | virtual, smoke | `SoapServiceTest` |
| TC-121 | SOAP | Divide by zero | 500 and fault "Division by zero" | virtual | `SoapServiceTest` |
| TC-130 | DB | API matches database | Status, currency and amount equal | virtual, db | `ApiVsDatabaseTest` |
| TC-131 | DB | Duplicate detection | SQL finds the duplicate transaction | virtual, db | `ApiVsDatabaseTest` |
