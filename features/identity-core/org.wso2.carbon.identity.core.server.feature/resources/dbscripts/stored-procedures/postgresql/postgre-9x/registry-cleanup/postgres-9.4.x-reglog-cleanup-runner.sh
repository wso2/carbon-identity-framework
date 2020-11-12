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
    -c "select $schema.WSO2_REG_LOG_CLEANUP_9_4_x(0)")
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
    -c "select $schema.WSO2_REG_LOG_CLEANUP_9_4_x(1)")
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
    -c "select $schema.WSO2_REG_LOG_CLEANUP_9_4_x(2)")
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
    -c "select $schema.WSO2_REG_LOG_CLEANUP_9_4_x(3)")
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
    -c "select $schema.WSO2_REG_LOG_CLEANUP_9_4_x(4)")
echo ''


echo "WSO2 WSO2_REG_LOG_CLEANUP_9_4_x SCRIPT EXECUTION COMPLETED !"
exit 0
