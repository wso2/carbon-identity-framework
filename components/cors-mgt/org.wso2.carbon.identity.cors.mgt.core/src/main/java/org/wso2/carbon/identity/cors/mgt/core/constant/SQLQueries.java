package org.wso2.carbon.identity.cors.mgt.core.constant;

/**
 * SQL queries related to CORS operations.
 */
public class SQLQueries {

    public static final String GET_CORS_ORIGINS_BY_TENANT_ID =
            "SELECT ID, ORIGIN, UUID " +
            "FROM IDN_CORS_ORIGIN " +
            "WHERE TENANT_ID = :TENANT_ID; " +
            "ORDER BY ID ASC";

    public static final String GET_CORS_ORIGINS_BY_APPLICATION_ID =
            "SELECT ID, ORIGIN, UUID " +
            "FROM IDN_CORS_ORIGIN " +
            "INNER JOIN IDN_CORS_ASSOCIATION ON IDN_CORS_ORIGIN.ID = IDN_CORS_ASSOCIATION.IDN_CORS_ORIGIN_ID " +
            "WHERE IDN_CORS_ORIGIN.TENANT_ID = :TENANT_ID; AND IDN_CORS_ASSOCIATION.SP_APP_ID = :SP_APP_ID; " +
            "ORDER BY ID ASC";

    public static final String GET_CORS_ORIGIN_ID =
            "SELECT ID FROM IDN_CORS_ORIGIN " +
            "WHERE TENANT_ID = :TENANT_ID; AND ORIGIN = :ORIGIN;";

    public static final String GET_CORS_ORIGIN_ID_BY_UUID =
            "SELECT ID " +
            "FROM IDN_CORS_ORIGIN " +
            "WHERE UUID = :UUID;";

    public static final String INSERT_CORS_ORIGIN =
            "INSERT INTO IDN_CORS_ORIGIN (TENANT_ID, ORIGIN, UUID) " +
            "VALUES (?, ?, ?)";

    public static final String INSERT_CORS_ASSOCIATION =
            "INSERT INTO IDN_CORS_ASSOCIATION (IDN_CORS_ORIGIN_ID, SP_APP_ID) " +
            "VALUES (:IDN_CORS_ORIGIN_ID;, :SP_APP_ID;)";

    public static final String DELETE_ORIGIN =
            "DELETE " +
            "FROM IDN_CORS_ORIGIN " +
            "WHERE ID = :ID;";

    public static final String GET_CORS_APPLICATION_IDS_BY_CORS_ORIGIN_ID =
            "SELECT SP_APP_ID " +
            "FROM IDN_CORS_ASSOCIATION " +
            "WHERE IDN_CORS_ORIGIN_ID = :IDN_CORS_ORIGIN_ID;";

    public static final String GET_CORS_APPLICATIONS_BY_CORS_ORIGIN_ID =
            "SELECT SP_APP.UUID, SP_APP.APP_NAME " +
            "FROM SP_APP " +
            "INNER JOIN IDN_CORS_ASSOCIATION ON IDN_CORS_ASSOCIATION.SP_APP_ID = SP_APP.ID " +
            "INNER JOIN IDN_CORS_ORIGIN ON IDN_CORS_ASSOCIATION.IDN_CORS_ORIGIN_ID = IDN_CORS_ORIGIN.ID " +
            "WHERE IDN_CORS_ORIGIN.UUID = :UUID;";

    public static final String DELETE_CORS_APPLICATION_ASSOCIATION =
            "DELETE " +
            "FROM IDN_CORS_ASSOCIATION " +
            "WHERE IDN_CORS_ORIGIN_ID = :IDN_CORS_ORIGIN_ID; AND SP_APP_ID = :SP_APP_ID;";
}
