package org.wso2.carbon.identity.functions.library.mgt.dao.util;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DAOUtils {
    private static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();

    public static void initializeDataSource(String databaseName, String scriptPath) throws Exception {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:" + databaseName);

        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
        }
        dataSourceMap.put(databaseName, dataSource);
    }

    public static Connection getConnection(String database) throws SQLException {
        if (dataSourceMap.get(database) != null) {
            return dataSourceMap.get(database).getConnection();
        }
        throw new RuntimeException("Invalid datasource.");
    }

    public static String getFilePath(String fileName) {
        if (StringUtils.isNotBlank(fileName)) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbScripts", fileName)
                    .toString();
        }
        return null;
    }
}
