## **HOW TO RUN**

**>> mssql-tokencleanup.sql**

**Compile the Stored Procedure**

First - Compile the stored procedure using a MSSQL client by selecting the whole script at once and execute from the MSSQL client.
Make sure to create the procedure in the identity database schema only.

**Execute the Stored Procedure.**

Then execute the compiled store procedure by using the EXEC function in the MSSQL client. Following is a sample.

```
EXEC WSO2_TOKEN_CLEANUP_SP;
```
