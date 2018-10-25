package org.wso2.carbon.identity.functions.library.mgt;

/**
 * This class contains default SQL queries.
 * */
public class FunctionLibMgtDBQueries {

    // STORE Queries
    public static final String STORE_FUNCTIONLIB_INFO =
            "INSERT INTO IDN_FUNCTION_LIBRARY (NAME, DESCRIPTION, TYPE, TENANT_ID, DATA) VALUES (?,?,?,?,?)";

    // EDIT Queries
    public static final String UPDATE_FUNCTIONLIB_INFO =
            "UPDATE IDN_FUNCTION_LIBRARY SET NAME= ?, DESCRIPTION= ?, DATA= ? WHERE TENANT_ID= ? AND NAME = ?";

    // LOAD Queries
    public static final String LOAD_FUNCTIONLIB_FROM_TENANTID =
            "SELECT NAME,DESCRIPTION,TYPE,DATA FROM IDN_FUNCTION_LIBRARY WHERE TENANT_ID = ?";

    public static final String LOAD_FUNCTIONLIB_FROM_TENANTID_AND_NAME =
            "SELECT NAME,DESCRIPTION,TYPE,DATA FROM IDN_FUNCTION_LIBRARY WHERE TENANT_ID = ? AND NAME= ?";


    //DELETE Queries
    public static final String REMOVE_FUNCTIONLIB =
            "DELETE FROM IDN_FUNCTION_LIBRARY WHERE TENANT_ID = ? AND NAME= ?";
}
