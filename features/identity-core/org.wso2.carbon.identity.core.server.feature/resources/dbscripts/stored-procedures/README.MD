# Why stored procedures ?

Based on the use cases sometimes the data in the database might grow fast and cleaning them up from the product itself becomes expensive. These stored procedures helps us to offload the expensive data cleanups to the database server.

There are stored procedures available for OAuth2 token and authorization code cleanup, session data cleanup and confirmation code cleanup.

Below is the directory structure of the stored procedure script files.

**NOTE : Some of script might not exist based on the availability**

```
--stored-procedures
  |--mysql
     |--confirmation-code-cleanup.sql
     |--oauth2-token-cleanup.sql
     |--session-data-cleanup.sql
  |--oracle
```
