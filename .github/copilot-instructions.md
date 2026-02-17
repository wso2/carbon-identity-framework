## License Header
- Ensure that all java files contains the appropriate license header at the top.
- The copyright year must be the current year or a range ending in the current year (e.g., 2018-2026).
- If the header is missing or incorrect, suggest this version:

```
/*
* Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
*
* WSO2 LLC. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
```

## Comments
- All public methods should have a docstring.
- Comments should start with a space and first letter capitalized.
- Comments should always end with a period.
  
(Fix the code if possible; otherwise, mention the specific issue—such as 'missing docstring', 'comment does not start with a capital letter', or 'comment does not end with a period'—in the comment.)

## Logs
### Debug
- If there's a string concatenation, then having `if (LOG.isDebugEnabled())` is mandatory.
- If the log message involves string concatenation or any expensive computation, always wrap the log statement with `if (LOG.isDebugEnabled())` to avoid unnecessary computation.
    - For simple log messages (e.g., static strings or simple variable interpolation), you can use `LOG.debug` directly without the debug check.

  (If you cannot fix the logging issue, mention the performance concern in the comment.)

## DAO Layer
- All database queries should support the following database types:
    - DB2
    - H2
    - MS SQL Server
    - MySQL
    - Oracle
    - PostgreSQL

  If a query is not supported by one of the above databases, then it should be fixed or mentioned in the comment.

## Primary Security Checklist:
- Injection Flaws: Scrutinize all user-controlled input for potential SQL Injection, Cross-Site Scripting (XSS), or Command Injection.

- Hardcoded Secrets: Search for any exposed secrets like API keys, passwords, or private tokens.

- Broken Access Control: Verify that any new endpoints or data access functions have proper authorization and permission checks.

- Sensitive Data Exposure: Ensure that no sensitive user data (e.g., PII, credentials) is being logged or sent in error messages.

- Insecure Dependencies: Check if any newly added dependencies have known vulnerabilities.

### Reporting Format:
For each issue you find, provide a concise, inline review comment that includes:

- Vulnerability: A brief title for the issue (e.g., "Potential XSS").

- Risk: A one-sentence explanation of the potential impact.

- Suggestion: A clear, actionable recommendation for a fix.