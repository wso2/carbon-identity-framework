/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.flow.mgt;

import java.nio.file.Paths;
import java.sql.Connection;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;

/**
 * Helper methods for tests.
 */
public class TestHelperMethods {

    /**
     * Initiate H2 database.
     *
     * @param scriptPath Path to the database script.
     * @throws Exception Error when initiating H2 database.
     */
    public static BasicDataSource initiateH2Database(String scriptPath, String databaseName) throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + databaseName);
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("select 1");
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
        }
        return dataSource;
    }

    /**
     * Get the path to the database script.
     *
     * @return Path to the database script.
     */
    public static String getFilePath(String filename) {

        if (StringUtils.isNotBlank(filename)) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", filename).toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }

    /**
     * Close H2 database.
     *
     * @throws Exception Error when closing H2 database.
     */
    public static void closeH2Database(BasicDataSource dataSource) throws Exception {

        if (dataSource != null) {
            dataSource.close();
        }
    }
}
