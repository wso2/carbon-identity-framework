USE msdb ;
GO
-- Create a job named TokenCleanUpTask for token data clean up task.
EXEC sp_add_job
    @job_name = N'TokenCleanUpTask' ;
GO
-- Add a job step named TokenDataCleanUpStep for the TokenCleanUpTask job.
-- Set the database and stored procedure execution command.
EXEC sp_add_jobstep
    @job_name = N'TokenCleanUpTask',
    @step_name = N'TokenDataCleanUpStep',
    @subsystem = N'TSQL',
    @command = N'EXEC WSO2_TOKEN_CLEANUP_SP',
    @database_name = N'<database_name>',
    @retry_attempts = <number_of_retry_attempts>,
    @retry_interval = <retry_interval>,
    @flags = <output_control_option>;
GO
-- Create a schedule named TokenCleanUpScheduler.
EXEC sp_add_schedule
    @schedule_name = N'TokenCleanUpScheduler',
    @freq_type = <integer_indicating_frequency_type>,
    @freq_interval = <integer_indicating_frequency_interval>,
    @active_start_time = <start_time_HHMMSS> ;
GO
-- Attach the TokenCleanUpScheduler schedule to TokenCleanUpTask job.
EXEC sp_attach_schedule
   @job_name = N'TokenCleanUpTask',
   @schedule_name = N'TokenCleanUpScheduler';
GO
-- Targets the TokenCleanUpTask job at the local server.
EXEC sp_add_jobserver
    @job_name = N'TokenCleanUpTask';
GO