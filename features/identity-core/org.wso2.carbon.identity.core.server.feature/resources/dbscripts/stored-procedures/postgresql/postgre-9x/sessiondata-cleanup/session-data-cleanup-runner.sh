#!/bin/bash
set -e
set -u

username='postgres'
password='postgres'
database='CARBON_DB'
host='localhost'
schema='schemaname'

export PGPASSWORD=$password


# ----------------------------------------------------
# TABLE BACKUP
# ----------------------------------------------------

batchStatus=$(psql \
  -X \
  -h $host \
  -U $username \
  --echo-all \
   -d $database \
    -c "select $schema.WSO2_SESSION_DATA_CLEANUP_SP(0)")
echo ''


# ----------------------------------------------------
# CALCULATE RECORDS IDN_AUTH_SESSION_STORE
# ----------------------------------------------------

batchStatus=$(psql \
  -X \
  -h $host \
  -U $username \
  --echo-all \
   -d $database \
    -c "select $schema.WSO2_SESSION_DATA_CLEANUP_SP(1)")
echo ''

# ----------------------------------------------------
# BATCH DELETE IDN_AUTH_SESSION_STORE
# ----------------------------------------------------

while true
do
batchStatus=$(psql \
  -X \
  -h $host \
  -U $username \
  --echo-all \
   -d $database \
    -c "select $schema.WSO2_SESSION_DATA_CLEANUP_SP(2)")
echo ''
if [[ $batchStatus == *"FINISHED"* ]]; then
  break;
fi
done

# ----------------------------------------------------
# CALCULATE OPERATIONAL RECORDS IDN_AUTH_SESSION_STORE
# ----------------------------------------------------

batchStatus=$(psql \
  -X \
  -h $host \
  -U $username \
  --echo-all \
   -d $database \
    -c "select $schema.WSO2_SESSION_DATA_CLEANUP_SP(3)")
echo ''


# ----------------------------------------------------
# BATCH DELETE OPERATIONAL RECORDS ON IDN_AUTH_SESSION_STORE
# ----------------------------------------------------

while true
do
batchStatus=$(psql \
  -X \
  -h $host \
  -U $username \
  --echo-all \
   -d $database \
    -c "select $schema.WSO2_SESSION_DATA_CLEANUP_SP(4)")
echo ''
if [[ $batchStatus == *"FINISHED"* ]]; then
  break;
fi
done

echo "WSO2 WSO2_SESSION_DATA_CLEANUP_SP SCRIPT EXECUTION COMPLETED !"
exit 0
