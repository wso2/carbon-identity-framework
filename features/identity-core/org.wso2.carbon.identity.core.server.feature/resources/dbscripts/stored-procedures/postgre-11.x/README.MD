## **HOW TO RUN**

**>> postgresql_11-tokencleanup.sql**


**Compile the Stored Procedure**

First - Compile the stored procedure using a PostgreSQL client. Following is a sample for CLI based PostgreSQL client.
Make sure to create the procedure in the identity database schema only.

Connect to the database pointed for WSO2_SERVER. (change the WSO2_CARBON_DB in below command with your actual database name according to your environment) 
```
pgsql>\c WSO2_CARBON_DB
```
Execute the below command to compile the script to create the stored procedure postgresql-tokencleanup.sql.txt. (change the path to the token-cleanup.sql accordingly)
```
pgsql>\i postgresql_11-tokencleanup.sql
```

**Execute the Stored Procedure.**

Then execute the compiled stored procedure by using the call function in the PostgreSQL client. Following is a sample for CLI based PostgreSQL client.

```
pgsql> CALL WSO2_TOKEN_CLEANUP_SP ();
```
