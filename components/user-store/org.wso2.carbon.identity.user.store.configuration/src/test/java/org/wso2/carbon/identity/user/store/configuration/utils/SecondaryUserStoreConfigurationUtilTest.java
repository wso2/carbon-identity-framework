/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.store.configuration.utils;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.store.configuration.dao.AbstractUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.dao.UserStoreDAO;
import org.wso2.carbon.identity.user.store.configuration.dao.impl.DatabaseBasedUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.dao.impl.DatabaseBasedUserStoreDAOImpl;
import org.wso2.carbon.identity.user.store.configuration.dao.impl.FileBasedUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.dao.impl.FileBasedUserStoreDAOImpl;
import org.wso2.carbon.identity.user.store.configuration.internal.UserStoreConfigListenersHolder;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.Map;

import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertThrows;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Test class for the repository selection in {@link SecondaryUserStoreConfigurationUtil}.
 */
public class SecondaryUserStoreConfigurationUtilTest {

    private static final String PERSISTENCE_TYPE = "UserStorePersistenceType";
    private static final String MIGRATE_FILE_BASED = "MigrateFileBasedUserStoreConfigurations";

    private Map<String, AbstractUserStoreDAOFactory> factories;

    @BeforeMethod
    public void setUp() {

        factories = UserStoreConfigListenersHolder.getInstance().getUserStoreDAOFactories();
        factories.clear();
        factories.put(FileBasedUserStoreDAOFactory.class.getName(), new FileBasedUserStoreDAOFactory());
        factories.put(DatabaseBasedUserStoreDAOFactory.class.getName(), new DatabaseBasedUserStoreDAOFactory());
    }

    @AfterMethod
    public void tearDown() {

        factories.clear();
    }

    @Test
    public void testDatabasePersistenceIsOffUnlessSelected() {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty(PERSISTENCE_TYPE)).thenReturn(null);
            assertFalse("An absent persistence type must leave the file system in charge.",
                    SecondaryUserStoreConfigurationUtil.isDatabaseBasedUserStorePersistenceEnabled());

            identityUtil.when(() -> IdentityUtil.getProperty(PERSISTENCE_TYPE)).thenReturn("file");
            assertFalse("Any value other than the database one must leave the file system in charge.",
                    SecondaryUserStoreConfigurationUtil.isDatabaseBasedUserStorePersistenceEnabled());

            identityUtil.when(() -> IdentityUtil.getProperty(PERSISTENCE_TYPE)).thenReturn("DataBase");
            assertTrue("The persistence type must be read regardless of case.",
                    SecondaryUserStoreConfigurationUtil.isDatabaseBasedUserStorePersistenceEnabled());
        }
    }

    @Test
    public void testMigrationNeedsBothTheDatabaseAndItsOwnFlag() {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty(PERSISTENCE_TYPE)).thenReturn("file");
            identityUtil.when(() -> IdentityUtil.getProperty(MIGRATE_FILE_BASED)).thenReturn("true");
            assertFalse("Migration must not run while the file system is the repository.",
                    SecondaryUserStoreConfigurationUtil.isFileBasedUserStoreMigrationEnabled());

            identityUtil.when(() -> IdentityUtil.getProperty(PERSISTENCE_TYPE)).thenReturn("database");
            identityUtil.when(() -> IdentityUtil.getProperty(MIGRATE_FILE_BASED)).thenReturn(null);
            assertFalse("Migration must be off unless it is turned on explicitly.",
                    SecondaryUserStoreConfigurationUtil.isFileBasedUserStoreMigrationEnabled());

            identityUtil.when(() -> IdentityUtil.getProperty(MIGRATE_FILE_BASED)).thenReturn("true");
            assertTrue("Migration runs once both are on.",
                    SecondaryUserStoreConfigurationUtil.isFileBasedUserStoreMigrationEnabled());
        }
    }

    @Test
    public void testConfiguredRepositoryDecidesTheDao() throws Exception {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty(PERSISTENCE_TYPE)).thenReturn(null);
            UserStoreDAO fileBased = SecondaryUserStoreConfigurationUtil.getUserStoreDAO();
            assertTrue("The file system is the default repository.",
                    fileBased instanceof FileBasedUserStoreDAOImpl);

            identityUtil.when(() -> IdentityUtil.getProperty(PERSISTENCE_TYPE)).thenReturn("database");
            UserStoreDAO databaseBased = SecondaryUserStoreConfigurationUtil.getUserStoreDAO();
            assertTrue("Selecting the database must hand back the database DAO.",
                    databaseBased instanceof DatabaseBasedUserStoreDAOImpl);
        }
    }

    @Test
    public void testAnUnregisteredDatabaseFactoryIsReported() {

        factories.remove(DatabaseBasedUserStoreDAOFactory.class.getName());
        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty(PERSISTENCE_TYPE)).thenReturn("database");
            // Without this the unregistered factory would surface as a NullPointerException.
            assertThrows(UserStoreException.class, SecondaryUserStoreConfigurationUtil::getUserStoreDAO);
        }
    }
}
