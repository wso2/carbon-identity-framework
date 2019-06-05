## **HOW TO RUN**

**>> postgresql-tokencleanup.sql**


**Compile the Stored Procedure**

First - Compile the stored procedure using a PostgreSQL client. Following is a sample for CLI based PostgreSQL client.
Make sure to create the procedure in the identity database schema only.

Connect to the database pointed for WSO2_SERVER. (change the WSO2_CARBON_DB in below command with your actual database name according to your environment) 
```
pgsql>\c WSO2_CARBON_DB
```
Execute the below command to compile the script to create the stored procedure postgresql-tokencleanup.sql.txt. (change the path to the token-cleanup.sql accordingly)
```
pgsql>\i postgresql-tokencleanup.sql
```

**Execute the Stored Procedure.**

configure the database name, username, password, hostname in the script token-cleanup-runner.sh according to your environment configurations.

```
username='postgres'
password='postgres'
database='CARBON_DB'
host='localhost'
```

Execute the token-cleanup-runner.sh script in the bash
```
bash:$./token-clean-runner.sh
```
