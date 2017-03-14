/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.gateway.test.module.util;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.service.jndi.JNDIContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.jdbc.JdbcTemplate;
import org.wso2.carbon.identity.gateway.api.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.context.SessionContext;
import org.wso2.carbon.identity.gateway.dao.AsyncGatewayContextDAO;
import org.wso2.carbon.identity.gateway.dao.AsyncSessionDAO;
import org.wso2.carbon.identity.gateway.dao.CacheBackedGatewaySessionDAO;
import org.wso2.carbon.identity.gateway.dao.CacheBackedSessionDAO;
import org.wso2.carbon.identity.gateway.dao.GatewayContextDAO;
import org.wso2.carbon.identity.gateway.dao.SessionDAO;
import org.wso2.carbon.identity.gateway.dao.jdbc.JDBCGatewayContextDAO;
import org.wso2.carbon.identity.gateway.dao.jdbc.JDBCSessionDAO;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.nio.file.Paths;
import java.util.List;


@Listeners(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)

public class GatewayDAOTests {

    private static final Logger log = LoggerFactory.getLogger(GatewayDAOTests.class);
    private final String RANDOM_KEY_1 = "randomKey1";
    private final String RANDOM_KEY_2 = "randomKey2";
    private final String RANDOM_KEY_3 = "randomKey3";
    private final String DATA_SOURCE_NAME = "java:comp/env/jdbc/WSO2CARBON_DB";

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;


    @Configuration
    public Option[] createConfiguration() {

        List<Option> optionList = GatewayOSGiTestUtils.getDefaultSecurityPAXOptions();

        optionList.add(CoreOptions.systemProperty("java.security.auth.login.config")
                .value(Paths.get(GatewayOSGiTestUtils.getCarbonHome(), "conf", "security", "carbon-jaas.config")
                        .toString()));

        return optionList.toArray(new Option[optionList.size()]);
    }

    /**
     * Initialize DAO by adding templates
     */
    private void initDAO() {
        JNDIContextManager jndiContextManager = this.bundleContext.getService(bundleContext
                .getServiceReference(JNDIContextManager.class));
        try {
            Context ctx = jndiContextManager.newInitialContext();
            DataSource dsObject = (DataSource) ctx.lookup(DATA_SOURCE_NAME);
            if (dsObject != null) {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dsObject);
                initializeDao(jdbcTemplate);
            } else {
                log.error("Could not find WSO2CarbonDB");
            }
        } catch (NamingException e) {
            log.error("Error occurred while looking up the Datasource", e);
        }

    }

    /**
     * Add and remove sessions from session data store
     */
    @Test
    public void testAddRemoveSession() {
        Assert.assertNull(JDBCSessionDAO.getInstance().get(RANDOM_KEY_1));
        JDBCSessionDAO.getInstance().put(RANDOM_KEY_2, new SessionContext());
        Assert.assertNotNull(JDBCSessionDAO.getInstance().get(RANDOM_KEY_2));
        JDBCSessionDAO.getInstance().remove(RANDOM_KEY_2);
        Assert.assertNull(JDBCSessionDAO.getInstance().get(RANDOM_KEY_2));
    }

    /**
     * Tests for AsyncIdentityContextStore
     *
     * @throws InterruptedException
     */
    @Test
    public void testAsyncIdentityContextStore() throws InterruptedException {
        GatewayContextDAO asyncGatewayContextDAO = AsyncGatewayContextDAO.getInstance();
        Assert.assertNull(asyncGatewayContextDAO.get(RANDOM_KEY_3));
        asyncGatewayContextDAO.put(RANDOM_KEY_3, new GatewayMessageContext(null));
        // Waiting since this is an async operation
        Thread.sleep(100);
        Assert.assertNotNull(asyncGatewayContextDAO.get(RANDOM_KEY_3));
        asyncGatewayContextDAO.remove(RANDOM_KEY_3);
        Thread.sleep(100);
        Assert.assertNull(asyncGatewayContextDAO.get(RANDOM_KEY_3));

    }

    /**
     * Tests for Async Session Context store
     *
     * @throws InterruptedException
     */
    @Test
    public void testAsyncSessionContextStore() throws InterruptedException {
        SessionDAO asyncSessionDAO = AsyncSessionDAO.getInstance();
        Assert.assertNull(asyncSessionDAO.get(RANDOM_KEY_3));
        asyncSessionDAO.put(RANDOM_KEY_3, new SessionContext());
        Thread.sleep(100);
        Assert.assertNotNull(asyncSessionDAO.get(RANDOM_KEY_3));
        asyncSessionDAO.remove(RANDOM_KEY_3);
        Thread.sleep(100);
        Assert.assertNull(asyncSessionDAO.get(RANDOM_KEY_3));

    }

    /**
     * Tests for Cache Backed Identity Session DAO
     *
     * @throws InterruptedException
     */
    @Test
    public void testCacheBackedIdentitySessionDAO() throws InterruptedException {
        GatewayContextDAO gatewayContextDAO = CacheBackedGatewaySessionDAO.getInstance();
        Assert.assertNull(gatewayContextDAO.get(RANDOM_KEY_3));
        gatewayContextDAO.put(RANDOM_KEY_3, new GatewayMessageContext(null));
        Thread.sleep(100);
        Assert.assertNotNull(gatewayContextDAO.get(RANDOM_KEY_3));
        gatewayContextDAO.remove(RANDOM_KEY_3);
        Thread.sleep(100);
        Assert.assertNull(gatewayContextDAO.get(RANDOM_KEY_3));

    }

    /**
     * Tests for Cache Backed Session DAO
     *
     * @throws InterruptedException
     */
    @Test
    public void testCacheBackedSessionDAO() throws InterruptedException {
        SessionDAO asyncSessionDAO = CacheBackedSessionDAO.getInstance();
        Assert.assertNull(asyncSessionDAO.get(RANDOM_KEY_3));
        asyncSessionDAO.put(RANDOM_KEY_3, new SessionContext());
        Thread.sleep(100);
        Assert.assertNotNull(asyncSessionDAO.get(RANDOM_KEY_3));
        asyncSessionDAO.remove(RANDOM_KEY_3);
        Thread.sleep(100);
        Assert.assertNull(asyncSessionDAO.get(RANDOM_KEY_3));

    }

    /**
     * initialize DAOs by adding template
     *
     * @param jdbcTemplate
     */
    private void initializeDao(JdbcTemplate jdbcTemplate) {
        JDBCSessionDAO.getInstance().setJdbcTemplate(jdbcTemplate);
        JDBCGatewayContextDAO.getInstance().setJdbcTemplate(jdbcTemplate);
    }
}
