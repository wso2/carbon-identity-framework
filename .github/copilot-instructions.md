## Comments
- All public methods should have a docstring.
- Comments should start with a capital letter.
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
