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
    @database_name = N'<database_name>',
    @retry_attempts = <number_of_retry_attempts>,
    @retry_interval = <retry_interval>,
    @flags = <output_control_option>;
GO
-- Create a schedule named SessionCleanUpScheduler.
EXEC sp_add_schedule
    @schedule_name = N'SessionCleanUpScheduler',
    @freq_type = <integer_indicating_frequency_type>,
    @freq_interval = <integer_indicating_frequency_interval>,
    @active_start_time = <start_time_HHMMSS> ;
GO
-- Attach the SessionCleanUpScheduler schedule to SessionCleanUpTask job.
EXEC sp_attach_schedule
   @job_name = N'SessionCleanUpTask',
   @schedule_name = N'SessionCleanUpScheduler';
GO
-- Targets the SessionCleanUpTask job at the local server.
EXEC sp_add_jobserver
    @job_name = N'SessionCleanUpTask';
GO