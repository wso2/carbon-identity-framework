package org.wso2.carbon.identity.framework.async.status.mgt.util;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.internal.CarbonContextDataHolder;
import org.wso2.carbon.identity.framework.async.status.mgt.internal.util.Utils;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.util.DatabaseUtil;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;

/**
 * Util methods needed for testing of Asynchronous Operation Status Management component.
 */
public class TestUtils {

    public static final String DB_NAME = "testAsyncOperationStatusMgt_db";
    public static final String H2_SCRIPT_NAME = "h2.sql";
    public static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();

    public static String getFilePath(String fileName) {

        if (StringUtils.isNotBlank(fileName)) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbScripts",
                    fileName).toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }

    public static void initiateH2Base() throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + DB_NAME);
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("select 1");
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate(getExecuteUpdateQuery());
        }
        dataSourceMap.put(DB_NAME, dataSource);
    }

    public static void closeH2Base() throws Exception {

        BasicDataSource dataSource = dataSourceMap.remove(DB_NAME);
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public static void mockDataSource() throws Exception {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome,
                "repository/conf").toString());

        DataSource dataSource = dataSourceMap.get(DB_NAME);

        setStatic(DatabaseUtil.class.getDeclaredField("dataSource"), dataSource);

        Field carbonContextHolderField =
                CarbonContext.getThreadLocalCarbonContext().getClass().getDeclaredField("carbonContextHolder");
        carbonContextHolderField.setAccessible(true);
        CarbonContextDataHolder carbonContextHolder
                = (CarbonContextDataHolder) carbonContextHolderField.get(CarbonContext.getThreadLocalCarbonContext());
        carbonContextHolder.setUserRealm(mock(UserRealm.class));
        setStatic(Utils.class.getDeclaredField("dataSource"), dataSource);
    }

    private static void setStatic(Field field, Object newValue) throws Exception {

        field.setAccessible(true);
        field.set(null, newValue);
    }

    private static String getExecuteUpdateQuery() {

        return "RUNSCRIPT FROM '" + getFilePath(H2_SCRIPT_NAME) + "'";
    }
}
