/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.store;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.framework.testutil.powermock.PowerMockIdentityBaseTest;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test class implementing methods to manage data source connection.
 */
public class DataStoreBaseTest extends PowerMockIdentityBaseTest {

    private static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();

    public static Connection getConnection(String datasourceName) throws SQLException {

        if (dataSourceMap.get(datasourceName) != null) {
            return dataSourceMap.get(datasourceName).getConnection();
        }
        throw new RuntimeException("No datasource initiated for database: " + datasourceName);
    }

    public static String getDatabaseScriptFilePath(String fileName) {

        if (StringUtils.isNotBlank(fileName)) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbScripts", fileName)
                    .toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }

    public static BasicDataSource getDatasource(String datasourceName) {

        if (dataSourceMap.get(datasourceName) != null) {
            return dataSourceMap.get(datasourceName);
        }
        throw new RuntimeException("No datasource initiated for database: " + datasourceName);
    }

    protected void initH2DB(String databaseName, String scriptPath) throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + databaseName);
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
        }
        dataSourceMap.put(databaseName, dataSource);
    }

    protected void closeH2DB(String databaseName) throws Exception {

        BasicDataSource dataSource = dataSourceMap.get(databaseName);
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
