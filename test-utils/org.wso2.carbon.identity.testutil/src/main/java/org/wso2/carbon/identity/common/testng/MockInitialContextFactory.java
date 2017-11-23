/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.common.testng;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * Mock initial context factory to be used to supply Datasource, etc.
 */
public class MockInitialContextFactory implements InitialContextFactory {

    private static ThreadLocal<Map<String, Object>> jndiContextData = new ThreadLocal<>();
    private static Log log = LogFactory.getLog(MockInitialContextFactory.class);

    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        Context context = Mockito.mock(Context.class);
        Mockito.doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                String name = (String) invocationOnMock.getArguments()[0];
                return getDatasource(name);
            }
        }).when(context).lookup(Matchers.anyString());

        return context;
    }

    /**
     * Destroy the initial context.
     */
    public static void destroy() {
        Map<String, Object> jndiObjectsMap = jndiContextData.get();
        if (jndiObjectsMap != null) {
            for (Map.Entry entry : jndiObjectsMap.entrySet()) {
                Object value = entry.getValue();
                if (value != null && value instanceof BasicDataSource) {
                    try {
                        ((BasicDataSource) value).close();
                    } catch (SQLException e) {
                        //Just Ignore for now.
                    }
                }
            }
            jndiContextData.remove();
        }
    }

    private static BasicDataSource getDatasource(String name) {
        Map context = jndiContextData.get();
        if (context == null) {
            return null;
        }
        return (BasicDataSource) context.get(name);
    }

    /**
     * Closes the datasource, given the JNDI name.
     * @param name
     */
    public static void closeDatasource(String name) {
        Map context = jndiContextData.get();
        if (context == null) {
            return;
        }
        Object old = context.get(name);
        if (old instanceof BasicDataSource) {
            try {
                ((BasicDataSource) old).close();
            } catch (Exception e) {
                log.error("Error while closing the in-memory H2 Database.", e);
            }
        }
    }

    private static void addContextLookup(String name, BasicDataSource object) {
        Map context = jndiContextData.get();
        if (context == null) {
            context = new HashMap();
            jndiContextData.set(context);
        }
        Object old = context.get(name);
        if (old instanceof BasicDataSource) {
            try {
                ((BasicDataSource) old).close();
            } catch (Exception e) {
                log.error("Error while closing the in-memory H2 Database.", e);
            }
        }
        context.put(name, object);
    }

    /**
     * Initializes the datasource given JNDI name and files.
     *
     * @param datasourceName
     * @param clazz
     * @param files
     */
    public static BasicDataSource initializeDatasource(String datasourceName, Class clazz, String[] files) {
        Map<String, Object> jndiObjectsMap = jndiContextData.get();
        if (jndiObjectsMap != null) {
            BasicDataSource basicDataSource = (BasicDataSource) jndiObjectsMap.get(datasourceName);
            if (basicDataSource != null && !basicDataSource.isClosed()) {
                return basicDataSource;
            }
        }
        String basePath = clazz.getResource("/").getFile();
        BasicDataSource dataSource = createDb(datasourceName, basePath, files);
        addContextLookup(datasourceName, dataSource);
        return dataSource;
    }

    private static BasicDataSource createDb(String dbName, String basePath, String[] files) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + dbName);
        try (Connection connection = dataSource.getConnection()) {
            for (String f : files) {
                String scriptPath = Paths.get(basePath, f).toString();
                try (Statement statement = connection.createStatement()  ) {
                    statement.executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");   //NOSONAR
                }
            }
        } catch (SQLException e) {
            log.error("Error while creating the in-memory H2 Database.", e);
        }
        return dataSource;
    }
}


