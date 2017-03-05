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
import org.wso2.carbon.identity.gateway.dao.AsyncIdentityContextDAO;
import org.wso2.carbon.identity.gateway.dao.AsyncSessionDAO;
import org.wso2.carbon.identity.gateway.dao.CacheBackedIdentitySessionDAO;
import org.wso2.carbon.identity.gateway.dao.CacheBackedSessionDAO;
import org.wso2.carbon.identity.gateway.dao.IdentityContextDAO;
import org.wso2.carbon.identity.gateway.dao.SessionDAO;
import org.wso2.carbon.identity.gateway.dao.jdbc.JDBCIdentityContextDAO;
import org.wso2.carbon.identity.gateway.dao.jdbc.JDBCSessionDAO;
import org.wso2.carbon.identity.mgt.event.IdentityMgtMessageContext;
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


    private void initDAO() {
        JNDIContextManager jndiContextManager = this.bundleContext.getService(bundleContext
                .getServiceReference(JNDIContextManager.class));
        try {
            Context ctx = jndiContextManager.newInitialContext();
            DataSource dsObject = (DataSource) ctx.lookup("java:comp/env/jdbc/WSO2CARBON_DB");
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

    @Test
    public void testAddRemoveSession() {
        Assert.assertNull(JDBCSessionDAO.getInstance().get("randomkey1"));
        JDBCSessionDAO.getInstance().put("randomKey2", new SessionContext());
        Assert.assertNotNull(JDBCSessionDAO.getInstance().get("randomKey2"));
        JDBCSessionDAO.getInstance().remove("randomKey2");
        Assert.assertNull(JDBCSessionDAO.getInstance().get("randomKey2"));
    }

    @Test
    public void testAsyncIdentityContextStore() throws InterruptedException {
        IdentityContextDAO asyncIdentityContextDAO = AsyncIdentityContextDAO.getInstance();
        Assert.assertNull(asyncIdentityContextDAO.get("randomkey3"));
        asyncIdentityContextDAO.put("randomkey3" , new GatewayMessageContext(null));
        Thread.sleep(100);
        Assert.assertNotNull(asyncIdentityContextDAO.get("randomkey3"));
        asyncIdentityContextDAO.remove("randomkey3");
        Thread.sleep(100);
        Assert.assertNull(asyncIdentityContextDAO.get("randomkey3"));

    }

    @Test
    public void testAsyncSessionContextStore() throws InterruptedException {
        SessionDAO asyncSessionDAO = AsyncSessionDAO.getInstance();
        Assert.assertNull(asyncSessionDAO.get("randomkey3"));
        asyncSessionDAO.put("randomkey3" , new SessionContext());
        Thread.sleep(100);
        Assert.assertNotNull(asyncSessionDAO.get("randomkey3"));
        asyncSessionDAO.remove("randomkey3");
        Thread.sleep(100);
        Assert.assertNull(asyncSessionDAO.get("randomkey3"));

    }

    @Test
    public void testCacheBackedIdentitySessionDAO() throws InterruptedException {
        IdentityContextDAO identityContextDAO = CacheBackedIdentitySessionDAO.getInstance();
        Assert.assertNull(identityContextDAO.get("randomkey3"));
        identityContextDAO.put("randomkey3", new GatewayMessageContext(null));
        Thread.sleep(100);
        Assert.assertNotNull(identityContextDAO.get("randomkey3"));
        identityContextDAO.remove("randomkey3");
        Thread.sleep(100);
        Assert.assertNull(identityContextDAO.get("randomkey3"));

    }

    @Test
    public void testCacheBackedSessionDAO() throws InterruptedException {
        SessionDAO asyncSessionDAO = CacheBackedSessionDAO.getInstance();
        Assert.assertNull(asyncSessionDAO.get("randomkey3"));
        asyncSessionDAO.put("randomkey3" , new SessionContext());
        Thread.sleep(100);
        Assert.assertNotNull(asyncSessionDAO.get("randomkey3"));
        asyncSessionDAO.remove("randomkey3");
        Thread.sleep(100);
        Assert.assertNull(asyncSessionDAO.get("randomkey3"));

    }

    private void initializeDao(JdbcTemplate jdbcTemplate) {
        JDBCSessionDAO.getInstance().setJdbcTemplate(jdbcTemplate);
        JDBCIdentityContextDAO.getInstance().setJdbcTemplate(jdbcTemplate);
    }
}
