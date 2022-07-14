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
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import static org.mockito.ArgumentMatchers.anyString;

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
        }).when(context).lookup(anyString());

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
     *
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
    public static BasicDataSource initializeDatasource(String datasourceName, Class clazz, String[] files) throws
            TestCreationException {

        Map<String, Object> jndiObjectsMap = jndiContextData.get();
        if (jndiObjectsMap != null) {
            BasicDataSource basicDataSource = (BasicDataSource) jndiObjectsMap.get(datasourceName);
            if (basicDataSource != null && !basicDataSource.isClosed()) {
                return basicDataSource;
            }
        }
        BasicDataSource dataSource = createDb(datasourceName, clazz, files);
        addContextLookup(datasourceName, dataSource);
        return dataSource;
    }

    private static BasicDataSource createDb(String dbName, Class clazz, String[] files) throws
            TestCreationException {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + dbName);
        try (Connection connection = dataSource.getConnection()) {
            for (String f : files) {
                File fileFromClasspathResource = getClasspathAccessibleFile(f, clazz);
                String scriptPath = null;
                File tempFile = null;
                if (fileFromClasspathResource != null && fileFromClasspathResource.exists()) {
                    scriptPath = fileFromClasspathResource.getAbsolutePath();
                } else {
                    //This may be from jar.
                    tempFile = copyTempFile(f, clazz);
                    scriptPath = tempFile.getAbsolutePath();
                }
                if (scriptPath != null) {
                    try (Statement statement = connection.createStatement()) {
                        statement.executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");   //NOSONAR
                    } catch (SQLException e) {
                        throw new TestCreationException(
                                "Error while loading data to the in-memory H2 Database located from resource : " +
                                        fileFromClasspathResource + "\nabsolute path : " + scriptPath, e);
                    }
                }
                if (tempFile != null) {
                    tempFile.delete();
                }
            }
            return dataSource;
        } catch (SQLException e) {
            throw new TestCreationException(
                    "Error while creating the in-memory H2 Database : ", e);
        }
    }

    /**
     * Returns a file in current classpath in a directory.
     * Returns null if the resource is within a jar.
     *
     * @param relativeFilePath
     * @param clazz
     * @return
     */
    private static File getClasspathAccessibleFile(String relativeFilePath, Class clazz) {

        URL url = clazz.getClassLoader().getResource(relativeFilePath);
        if(url == null) {
            return null;
        }
        File fileInClassloader = new File(url.getPath());
        if (fileInClassloader.isFile()) {
            return fileInClassloader;
        }
        return null;
    }

    /**
     * Copies a resource inside a jar to external file within a directory.
     * Then returns the created file.
     *
     * @param relativeFilePath
     * @param clazz
     * @return
     * @throws TestCreationException
     */
    private static File copyTempFile(String relativeFilePath, Class clazz) throws TestCreationException {

        URL url = clazz.getClassLoader().getResource(relativeFilePath);
        if(url == null) {
            throw new TestCreationException("Could not find a resource on the classpath : " + relativeFilePath);
        }
        InputStream inputStream;
        try {
            inputStream = url.openStream();
            ReadableByteChannel inputChannel = Channels.newChannel(inputStream);
            File tempFile = File.createTempFile("tmp_", "_registry.sql");
            FileOutputStream fos = new FileOutputStream(tempFile);
            WritableByteChannel targetChannel = fos.getChannel();
            //Transfer data from input channel to output channel
            ((FileChannel) targetChannel).transferFrom(inputChannel, 0, Short.MAX_VALUE);
            inputStream.close();
            targetChannel.close();
            fos.close();
            return tempFile;
        } catch (IOException e) {
            throw new TestCreationException("Could not copy the file content to temp file from : " + relativeFilePath);
        }
    }
}



