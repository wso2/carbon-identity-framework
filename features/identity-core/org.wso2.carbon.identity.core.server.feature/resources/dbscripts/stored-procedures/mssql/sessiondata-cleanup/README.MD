## **HOW TO RUN**

**Compile the Stored Procedure**

First - Compile the stored procedure [mssql-session-data-cleanup.sql](mssql-session-data-cleanup.sql) using a MSSQL client by selecting the whole script at once and execute from the MSSQL client.
Make sure to create the procedure in the identity database schema only.

**Execute the Stored Procedure.**

Then execute the compiled store procedure by using the EXEC function in the MSSQL client. Following is a sample.

```
EXEC CLEANUP_SESSION_DATA;
```

**Schedule to run the stored procedure automatically after a defined period of time.**

Run the [mssql-session-data-cleanup-scheduler.sql](mssql-session-data-cleanup-scheduler.sql) after modifying the parameters to match your configurations.

Below is a description of each section of the scheduler script.

**sp_add_job** : Creates a job named `SessionCleanUpTask` for session data clean up task.
(More info : [https://docs.microsoft.com/en-us/sql/relational-databases/system-stored-procedures/sp-add-job-transact-sql?view=sql-server-ver15](https://docs.microsoft.com/en-us/sql/relational-databases/system-stored-procedures/sp-add-job-transact-sql?view=sql-server-ver15))

| Argument | Description | Sample Value |
| ------ | ------ | ------ | 
| job_name |  | SessionCleanUpTask |

**sp_add_jobstep** : Add a job step named `SessionDataCleanUpStep` for the `SessionCleanUpTask` job.
(More info : [https://docs.microsoft.com/en-us/sql/relational-databases/system-stored-procedures/sp-add-jobstep-transact-sql?view=sql-server-ver15](https://docs.microsoft.com/en-us/sql/relational-databases/system-stored-procedures/sp-add-jobstep-transact-sql?view=sql-server-ver15))

| Argument | Description | Sample Value |
| ------ | ------ | ------ | 
| job_name |  | SessionDataCleanUpStep |
| subsystem |  | N'TSQL' |
| command |  | N'EXEC CLEANUP_SESSION_DATA' |
| database_name |  | N'WSO2_IDN_DB' |
| retry_attempts |  | 1 |
| retry_interval |  | 15 |
| flags |  | 8 |

**sp_add_schedule** : Creates a schedule named `SessionCleanUpScheduler`.
(More info : [https://docs.microsoft.com/en-us/sql/relational-databases/system-stored-procedures/sp-add-schedule-transact-sql?view=sql-server-ver15](https://docs.microsoft.com/en-us/sql/relational-databases/system-stored-procedures/sp-add-schedule-transact-sql?view=sql-server-ver15))

| Argument | Description | Sample Value |
| ------ | ------ | ------ | 
| schedule_name |  | N'SessionCleanUpScheduler' |
| freq_type |  | 4 |
| freq_interval |  | 1 |
| active_start_time |  | 013000 |

**sp_attach_schedule** : Attaches the `SessionCleanUpScheduler` schedule to `SessionCleanUpTask` job.
(More info : [https://docs.microsoft.com/en-us/sql/relational-databases/system-stored-procedures/sp-attach-schedule-transact-sql?view=sql-server-ver15](https://docs.microsoft.com/en-us/sql/relational-databases/system-stored-procedures/sp-attach-schedule-transact-sql?view=sql-server-ver15))

| Argument | Description | Sample Value |
| ------ | ------ | ------ | 
| job_name |  | N'SessionCleanUpTask' |
| schedule_name |  | N'SessionCleanUpScheduler' |

**sp_add_jobserver** : Targets the specified job at the specified server. Bu default, this targets the job to the local server. 
`[ @server_name = ] 'server'` can be used to define the name of the server at which to target the job.
(More info : [https://docs.microsoft.com/en-us/sql/relational-databases/system-stored-procedures/sp-add-jobserver-transact-sql?view=sql-server-ver15](https://docs.microsoft.com/en-us/sql/relational-databases/system-stored-procedures/sp-add-jobserver-transact-sql?view=sql-server-ver15))

Below is a sample script which schedules the session data clean up job to run every day, when the time in the server is 01.30. 
The name of the database used in the sample is `WSO2_IDN_DB`.

```
USE msdb ;
GO
-- Create a job named SessionCleanUpTask for session data clean up task.
EXEC sp_add_job
    @job_name = N'SessionCleanUpTask' ;
GO
-- Add a job step named SessionDataCleanUpStep for the SessionCleanUpTask job.
-- Set the database and stored procedure execution command.
EXEC sp_add_jobstep
    @job_name = N'SessionCleanUpTask',
    @step_name = N'SessionDataCleanUpStep',
    @subsystem = N'TSQL',
    @command = N'EXEC CLEANUP_SESSION_DATA',
    @database_name = N'WSO2_IDN_DB',
    @retry_attempts = 1,
    @retry_interval = 15,
    @flags = 8;
GO
-- Create a schedule named SessionCleanUpScheduler.
-- Jobs that use this schedule execute every day when the time on the server is 01.30.
EXEC sp_add_schedule
    @schedule_name = N'SessionCleanUpScheduler',
    @freq_type = 4,
    @freq_interval = 1,
    @active_start_time = 013000 ;
GO
-- Attach the SessionCleanUpScheduler schedule to SessionCleanUpTask job.
EXEC sp_attach_schedule
   @job_name = N'SessionCleanUpTask',
   @schedule_name = N'SessionCleanUpScheduler';
GO
-- Add the SessionCleanUpTask job to local server.
EXEC sp_add_jobserver
    @job_name = N'SessionCleanUpTask';
GO
```