## **HOW TO RUN**

**>> mysql-tokencleanup.sql**


**Compile the Stored Procedure**

First - Compile the stored procedure using a mysql client. Following is a sample for CLI based mysql client.
Make sure to create the procedure in the identity database schema only.

```
mysql> source \<path>\mysql-tokencleanup.sql;
```
or
```
bash:~$ mysql -u yourusername -p yourpassword yourdatabase < \<path>\mysql-tokencleanup.sql
```

**Execute the Stored Procedure.**

Then execute the compiled store procedure by using the call function in the mysql client. Following is a sample for CLI based mysql client.

```
mysql> call WSO2_TOKEN_CLEANUP_SP();
```
