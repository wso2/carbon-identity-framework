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
    -c "select $schema.WSO2_REG_LOG_CLEANUP(0)")
echo ''


# ----------------------------------------------------
# CALCULATE RECORDS REG_LOG
# ----------------------------------------------------

batchStatus=$(psql \
  -X \
  -h $host \
  -U $username \
  --echo-all \
   -d $database \
    -c "select $schema.WSO2_REG_LOG_CLEANUP(1)")
echo ''

# ----------------------------------------------------
# BATCH DELETE REG_RESOURCE
# ----------------------------------------------------

while true
do
batchStatus=$(psql \
  -X \
  -h $host \
  -U $username \
  --echo-all \
   -d $database \
    -c "select $schema.WSO2_REG_LOG_CLEANUP(2)")
echo ''
if [[ $batchStatus == *"FINISHED"* ]]; then
  break;
fi
done

# ------------------------------------------------------
# CLEANUP ANY EXISTING TEMP TABLES
# ------------------------------------------------------

batchStatus=$(psql \
  -X \
  -h $host \
  -U $username \
  --echo-all \
   -d $database \
    -c "select $schema.WSO2_REG_LOG_CLEANUP(3)")
echo ''


# ----------------------------------------------------
# CALCULATE RECORDS REG_LOG AFTER DELETE
# ----------------------------------------------------

batchStatus=$(psql \
  -X \
  -h $host \
  -U $username \
  --echo-all \
   -d $database \
    -c "select $schema.WSO2_REG_LOG_CLEANUP(4)")
echo ''


echo "WSO2 WSO2_REG_LOG_CLEANUP SCRIPT EXECUTION COMPLETED !"
exit 0
