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
    -c "select $schema.WSO2_CONFIRMATION_CODE_CLEANUP_SP(0)")
echo ''


# ----------------------------------------------------
# CALCULATE RECORDS REG_RESOURCE
# ----------------------------------------------------

batchStatus=$(psql \
  -X \
  -h $host \
  -U $username \
  --echo-all \
   -d $database \
    -c "select $schema.WSO2_CONFIRMATION_CODE_CLEANUP_SP(1)")
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
    -c "select $schema.WSO2_CONFIRMATION_CODE_CLEANUP_SP(2)")
echo ''
if [[ $batchStatus == *"FINISHED"* ]]; then
  break;
fi
done

# ----------------------------------------------------
# CALCULATE RECORDS REG_RESOURCE_PROPERTY
# ----------------------------------------------------

batchStatus=$(psql \
  -X \
  -h $host \
  -U $username \
  --echo-all \
   -d $database \
    -c "select $schema.WSO2_CONFIRMATION_CODE_CLEANUP_SP(3)")
echo ''


# ----------------------------------------------------
# BATCH DELETE REG_RESOURCE_PROPERTY
# ----------------------------------------------------

while true
do
batchStatus=$(psql \
  -X \
  -h $host \
  -U $username \
  --echo-all \
   -d $database \
    -c "select $schema.WSO2_CONFIRMATION_CODE_CLEANUP_SP(4)")
echo ''
if [[ $batchStatus == *"FINISHED"* ]]; then
  break;
fi
done


# ----------------------------------------------------
# CALCULATE RECORDS REG_PROPERTY
# ----------------------------------------------------

batchStatus=$(psql \
  -X \
  -h $host \
  -U $username \
  --echo-all \
   -d $database \
    -c "select $schema.WSO2_CONFIRMATION_CODE_CLEANUP_SP(5)")
echo ''


# ----------------------------------------------------
# BATCH DELETE REG_PROPERTY
# ----------------------------------------------------

while true
do
batchStatus=$(psql \
  -X \
  -h $host \
  -U $username \
  --echo-all \
   -d $database \
    -c "select $schema.WSO2_CONFIRMATION_CODE_CLEANUP_SP(6)")
echo ''
if [[ $batchStatus == *"FINISHED"* ]]; then
  break;
fi
done


echo "WSO2 WSO2_CONFIRMATION_CODE_CLEANUP_SP SCRIPT EXECUTION COMPLETED !"
exit 0
