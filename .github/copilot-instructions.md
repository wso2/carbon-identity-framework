## Comments
- All public methods should have a docstring.
- Comments should start with a capital letter.
- Comments should always end with a period.
  
(Fix the code if possible, otherwise mention it in the comment.)

## Logs
### Debug
- If there's a string concatenation, then having `if (LOG.isDebugEnabled())` is mandatory.
    - Make sure to not use `LOG.debug` if the string concatenation is not used.

  (Fix the code if possible, otherwise mention it in the comment.)

## DAO Layer
- All database queries should support the following database types:
    - DB2
    - H2
    - MS SQL Server
    - MySQL
    - Oracle
    - PostgreSQL

  If a query is not supported by one of the above databases, then it should be fixed or mentioned in the comment.
