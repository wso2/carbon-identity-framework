## **HOW TO RUN**

**>> oracle-tokencleanup.sql**


**Compile the Stored Procedure**

First - Compile the stored procedure using a Oracle client. Following is a sample for CLI based Oracle client.
Make sure to create the procedure in the identity database schema only.

```
sqlplus> @\<path>\oracle-token-cleanup.sql;
```

**Execute the Stored Procedure.**

Then execute the compiled store procedure by using the exec function in the Oracle client. Following is a sample for CLI based Oracle client.

```
sqlplus> exec WSO2_TOKEN_CLEANUP_SP();
```

! Starting from IS 5.7.0 following indexs should be inplace to avoid deadlock situation cause by the foreign keys
```
CREATE INDEX IDX_OROR_TID ON IDN_OIDC_REQ_OBJECT_REFERENCE(TOKEN_ID);
CREATE INDEX IDX_OROR_CID ON IDN_OIDC_REQ_OBJECT_REFERENCE(CODE_ID);
CREATE INDEX IDX_OROC_ROID ON IDN_OIDC_REQ_OBJECT_CLAIMS(REQ_OBJECT_ID);
CREATE INDEX IDX_OROCV_ROCID ON IDN_OIDC_REQ_OBJ_CLAIM_VALUES(REQ_OBJECT_CLAIMS_ID);
```
