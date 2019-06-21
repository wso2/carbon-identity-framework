## **HOW TO RUN**

**>> oracle-tokencleanup.sql**


**Compile the Stored Procedure**

First - Compile the stored procedure using a Oracle client. Following is a sample for CLI based Oracle client.
Make sure to create the procedure in the identity database schema only.

```
sqlplus> @\<path>\oracle-tokencleanup.sql;
```

**Execute the Stored Procedure.**

Then execute the compiled store procedure by using the exec function in the Oracle client. Following is a sample for CLI based Oracle client.

```
sqlplus> exec WSO2_TOKEN_CLEANUP_SP();
```
