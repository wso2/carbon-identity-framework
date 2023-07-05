# Why stored procedures?

Based on the use cases sometimes the data in the database might grow fast and cleaning them up from the product itself becomes expensive. These stored procedures help us to offload the expensive data cleanups to the database server.

There are stored procedures available for OAuth2 token and authorization code cleanup, session data cleanup and confirmation code cleanup.

**NOTE: Some of the scripts might not exist based on the availability**

## **Usage**

**>> tokencleanup.sql**


This is an optimized token cleanup script with batch wise delete, which will have a very minimal impact the database on its execution for other queries. This procedure includes the cleanup of Tokens , Scopes and the Authorization Codes from the respective tables of *idn_oauth2_access_token*, *idn_oauth2_access_token_scope* and *idn_oauth2_authorization_code* 

*- Logical condition used for token delete.*

> EXPIRED, INACTIVE, REVOKED all the tokens will get DELETED
> ACTIVE tokens will check following condition before DELETED
> TOKENS: **(createdTime+validityPeriod)** should be less than **(currentTime-2)** hours
> AND
> REFRESH TOKEN: **(refreshcreatedTime+refreshvalidityPeriod)** should be less than **(currentTime-2)** hours

*- Logical condition used for code delete.*

> DELETE everything on authorization code if the **TOKEN_ID does not exist** in the **IDN_OAUTH2_ACCESS_TOKEN** table and the (**createdtime** + **validityPeriod**) is less than the (**currentTime-2**) hours.

---
>\** VALIDITY PERIOD: should not be a **negative value** or **max integer** (ex : non expiry tokens)


**CONFIGURABLE ATTRIBUTES** 

This is where you can have customized configurations. following are some of the important variables you need to consider.

**batchSize* - This variable defines how many records will be deleted per batch for one iteration.

**chunkSize* - If you have millions of data in the table, this variable allows to handle them chunk wise , which is a larger set to the batch where the batch process each of the chunk (ex if you have 20 million data in the particular table, the chunk will initially take half million of such data and provided it into the batch delete as 10000 records per batch. Once that chunk is completed it will get another half million and proceed.)

**checkCount* - If the token were kept expiring while the cleanup scripts run, it will be stuck in an endless loop. Hence this defines a safe margin for the cleanup script to complete its job if the eligible tokens for delete are less than checkCount.

**backupTables* - enable the backup table to restore later at a later stage. (Please note this backup tables will be overwritten every time you run the cleanup script.). Also, the this would not capture the tokens which were created in between the backup task and the cleanup iteration, hence if tokens created after the backup task will not be able to restore if its get deleted from the cleanup iteration.

**sleepTime* - Used to defined the wait time for each iteration of the batch deletes to avoid table locks.

**safePeriod* - Active tokens are eligible to delete if the token (createdTime - validityPeriod) is less than the currentTime. In addition to that, the safe period will ensure the even such eligible token will keep in the database for the number of hours defined as safePeriod.

**enableLog* - Parameter use for enable or disable the logs.

**logLevel* - Parameter used to set the log levels.

**enableAudit* - By setting this parameter to true,  will log the each of deleted tokens in the auditlog_idn_oauth2_access_token_cleanup and the authorization codes in the auditlog_idn_oauth2_authorization_code_cleanup tables for track them in a later time. 

 **Functions**

**BACKUP IDN_OAUTH2_ACCESS_TOKEN TABLE* - This section acts to back up all the required table in case of restoration to be performed.

**CREATING AUDITLOG TABLES FOR DELETING TOKENS* - This section creates the initial audit logs table for persisting the deleted tokens and the authorization codes.

**DELETE IDN_OAUTH2_ACCESS_TOKEN CALCULATION* - This section used prints the breakdown of the tokens which should delete and retain.

**BATCH DELETE IDN_OAUTH2_ACCESS_TOKEN* - This section does the chunk and batch-wise delete for the access tokens.

**DELETE IDN_OAUTH2_AUTHORIZATION_CODE CALCULATION* - This section used prints the breakdown of the authorization codes which should delete and retain.

**BATCH DELETE IDN_OAUTH2_AUTHORIZATION_CODE* - This section does the chunk and batch-wise delete for the authorization codes.

**REBUILDING INDEXES* - As an extra step to optimize the database, this can perform an index rebuilding task for improving the performance, However, it's not recommended to perform on a live system unless you have downtime. Hence this could lock down the whole table. 

**ANALYSING TABLES*  - As an extra step you can perform a table analyze for gather the statistics for the tables which had the delete operation. This is also to improve the performance of the database. However, it's also not recommended to perform on a live system unless you have downtime. Please refer to documentation [2] for more information.

**>> tokencleanup-restore.sql**

This is the stored procedure used to restore the deleted tokens from the WSO2_TOKEN_CLEANUP_SP procedure. The restoration can be only done if the backupTables property is set to true in the WSO2_TOKEN_CLEANUP_SP procedure.

This is only an immediate restoration script for the WSO2_TOKEN_CLEANUP_SP procedure, hence each execution of the WSO2_TOKEN_CLEANUP_SP procedure will replace the backup tables.

Please note that it is highly recommended to have a complete backup of the production environment before modifying any data, in case of any restoration would be required.

**>> confirmation-code-cleanup.sql**

This scripts used to delete the confirmation codes from the registry tables.

**>> session-data-cleanup.sql**

This scripts used to delete the session data from the IDN_AUTH_SESSION_STORE table.

**>> jti-cleanup.sql**

This is an optimized token cleanup script with batch wise delete, which will have a very minimal impact the database on its execution for other queries. This procedure includes the cleanup of JWT IDs from the table *idn_oidc_jti*.

If preventTokenReuse config in deployment.toml file is set to True, WSO2_JTI_CLEANUP_SP procedure should be run to clean the table *idn_oidc_jti*.

Please note that it is highly recommended to have a complete backup of the production environment before modifying any data, in case of any restoration would be required.

**>> jti-cleanup-restore.sql**

This is the stored procedure used to restore the deleted JWT IDs from the WSO2_JTI_CLEANUP_SP procedure. The restoration can be only done if the backupTables property is set to true in the WSO2_JTI_CLEANUP_SP procedure.

This is only an immediate restoration script for the WSO2_JTI_CLEANUP_SP procedure, hence each execution of the WSO2_JTI_CLEANUP_SP procedure will replace the backup tables.
