## **HOW TO RUN**

**>> mssql-tokencleanup.sql**

**Compile the Stored Procedure**

First, Compile the stored procedure using a MSSQL client by selecting the whole script at once and execute from the MSSQL client.
Make sure to create the procedure in the identity database schema only.

**Execute the Stored Procedure.**

Then execute the compiled store procedure by using the EXEC function in the MSSQL client. Following is a sample.

```
EXEC WSO2_TOKEN_CLEANUP_SP;
```

**>> mssql-tokencleanup-restore.sql**

**Compile the Stored Procedure**

First, Compile the stored procedure using a MSSQL client by selecting the whole script at once and execute from the MSSQL client.
Make sure to create the procedure in the identity database schema only.

**Execute the Stored Procedure.**

Whenever you need to restore the cleaned tokens from the backup, execute the compiled store procedure by using the EXEC function in the MSSQL client. Following is a sample.

```
EXEC WSO2_TOKEN_CLEANUP_DATA_RESTORE_SP;
```

**>> mssql-jti-cleanup.sql**

**Compile the Stored Procedure**

First, Compile the stored procedure using a MSSQL client by selecting the whole script at once and execute from the MSSQL client.
Make sure to create the procedure in the identity database schema only.

**Execute the Stored Procedure.**

Then execute the compiled store procedure by using the EXEC function in the MSSQL client. Following is a sample.

```
EXEC WSO2_JTI_CLEANUP_SP;
```

**>> mssql-jti-cleanup-restore.sql**

**Compile the Stored Procedure**

First, Compile the stored procedure using a MSSQL client by selecting the whole script at once and execute from the MSSQL client.
Make sure to create the procedure in the identity database schema only.

**Execute the Stored Procedure.**

Whenever you need to restore the cleaned JWT IDs from the backup, execute the compiled store procedure by using the EXEC function in the MSSQL client. Following is a sample.

```
EXEC WSO2_JTI_CLEANUP_DATA_RESTORE_SP;
```