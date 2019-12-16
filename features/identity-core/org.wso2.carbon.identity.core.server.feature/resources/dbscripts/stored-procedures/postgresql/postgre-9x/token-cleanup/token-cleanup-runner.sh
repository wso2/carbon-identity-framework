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
# TOKEN BACKUP
# ----------------------------------------------------

batchStatus=$(psql \
  -X \
  -h $host \
  -U $username \
  --echo-all \
   -d $database \
    -c "select $schema.WSO2_TOKEN_CLEANUP_SP(0)")
echo ''


# ----------------------------------------------------
# CALCULATE TOKEN TYPES
# ----------------------------------------------------

batchStatus=$(psql \
  -X \
  -h $host \
  -U $username \
  --echo-all \
   -d $database \
    -c "select $schema.WSO2_TOKEN_CLEANUP_SP(1)")
echo ''

# ----------------------------------------------------
# BATCH DELETE IDN_OAUTH2_ACCESS_TOKEN
# ----------------------------------------------------

while true
do
batchStatus=$(psql \
  -X \
  -h $host \
  -U $username \
  --echo-all \
   -d $database \
    -c "select $schema.WSO2_TOKEN_CLEANUP_SP(2)")
echo ''
if [[ $batchStatus == *"FINISHED"* ]]; then
  break;
fi
done

# ----------------------------------------------------
# CALCULATE AUTHORIZATION_CODE TYPES
# ----------------------------------------------------

batchStatus=$(psql \
  -X \
  -h $host \
  -U $username \
  --echo-all \
   -d $database \
    -c "select $schema.WSO2_TOKEN_CLEANUP_SP(3)")
echo ''


# ----------------------------------------------------
# BATCH DELETE IDN_OAUTH2_AUTHORIZATION_CODE
# ----------------------------------------------------

while true
do
batchStatus=$(psql \
  -X \
  -h $host \
  -U $username \
  --echo-all \
   -d $database \
    -c "select $schema.WSO2_TOKEN_CLEANUP_SP(4)")
echo ''
if [[ $batchStatus == *"FINISHED"* ]]; then
  break;
fi
done

# ----------------------------------------------------
# OPTIMIZE DATAASE TABLES AFTER DELETE
# ----------------------------------------------------

batchStatus=$(psql \
  -X \
  -h $host \
  -U $username \
  --echo-all \
   -d $database \
    -c "select $schema.WSO2_TOKEN_CLEANUP_SP(5)")
echo ''


echo "WSO2 TOKEN_CLEANUP SCRIPT EXECUTION COMPLETED !"
exit 0
