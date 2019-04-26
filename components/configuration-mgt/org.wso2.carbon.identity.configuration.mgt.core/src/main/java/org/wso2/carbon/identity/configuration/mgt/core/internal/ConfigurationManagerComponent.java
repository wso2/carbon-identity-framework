/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.configuration.mgt.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManagerImpl;
import org.wso2.carbon.identity.configuration.mgt.core.dao.ConfigurationDAO;
import org.wso2.carbon.identity.configuration.mgt.core.dao.impl.ConfigurationDAOImpl;
import org.wso2.carbon.identity.configuration.mgt.core.model.ConfigurationManagerConfigurationHolder;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.DB_SCHEMA_COLUMN_NAME_CREATED_TIME;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.GET_CREATED_TIME_COLUMN_MYSQL;

/**
 * OSGi declarative services component which handles registration and un-registration of configuration management
 * service.
 */
@Component(
        name = "carbon.configuration.mgt.component",
        immediate = true
)
public class ConfigurationManagerComponent {

    private static final Log log = LogFactory.getLog(ConfigurationManagerComponent.class);
    private List<ConfigurationDAO> configurationDAOs = new ArrayList<>();

    /**
     * Register ConfigurationManager as an OSGI service.
     *
     * @param componentContext OSGI service component context.
     */
    @Activate
    protected void activate(ComponentContext componentContext) {

        try {
            BundleContext bundleContext = componentContext.getBundleContext();

            bundleContext.registerService(ConfigurationDAO.class.getName(), new ConfigurationDAOImpl(),
                    null);

            ConfigurationManagerConfigurationHolder configurationManagerConfigurationHolder =
                    new ConfigurationManagerConfigurationHolder();
            configurationManagerConfigurationHolder.setConfigurationDAOS(configurationDAOs);

            bundleContext.registerService(ConfigurationManager.class.getName(),
                    new ConfigurationManagerImpl(configurationManagerConfigurationHolder), null);
            ConfigurationManagerComponentDataHolder.getInstance().setConfigurationManagementEnabled
                    (isConfigurationManagementEnabled());
            setUseCreatedTime();
        } catch (Throwable e) {
            log.error("Error while activating ConfigurationManagerComponent.", e);
        }
    }

    @Reference(
            name = "configuration.context.service",
            service = ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService"
    )
    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {

        /*
         * ConfigurationManagerComponent checks for the database column, 'CREATED_TIME' in the IDN_CONFIG_RESOURCE
         * table. Database connection creation requires in this task depends on the ConfigurationContextService.
         * This reference will ensure that the ConfigurationContextService is activated before the
         * ConfigurationManagerComponent is activated.
         */
        log.debug("ConfigurationContextService Instance registered.");
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {

        log.debug("ConfigurationContextService Instance was unset.");
    }

    @Reference(
            name = "configuration.dao",
            service = org.wso2.carbon.identity.configuration.mgt.core.dao.ConfigurationDAO.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfiguration"
    )
    protected void setConfiguration(ConfigurationDAO configurationDAO) {

        if (configurationDAO != null) {
            if (log.isDebugEnabled()) {
                log.debug("Resource DAO is registered in ConfigurationManager service.");
            }

            this.configurationDAOs.add(configurationDAO);
            this.configurationDAOs.sort(Comparator.comparingInt(ConfigurationDAO::getPriority));
        }
    }

    protected void unsetConfiguration(ConfigurationDAO configurationDAO) {

        if (log.isDebugEnabled()) {
            log.debug("Purpose DAO is unregistered in ConfigurationManager service.");
        }
        this.configurationDAOs.remove(configurationDAO);
    }

    private void setUseCreatedTime() throws DataAccessException {

        if (ConfigurationManagerComponentDataHolder.getInstance().isConfigurationManagementEnabled() &&
                isCreatedTimeFieldExists()) {
            ConfigurationManagerComponentDataHolder.setUseCreatedTime(true);
        } else {
            ConfigurationManagerComponentDataHolder.setUseCreatedTime(false);
        }
    }

    private boolean isCreatedTimeFieldExists() {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {

            /*
            DB scripts without CREATED_TIME field can exists for H2 and MYSQL 5.7.
             */
            String sql = GET_CREATED_TIME_COLUMN_MYSQL;

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                // Following statement will throw SQLException if the column is not found
                resultSet.findColumn(DB_SCHEMA_COLUMN_NAME_CREATED_TIME);
                // If we are here then the column exists.
                return true;
            }
        } catch (IdentityRuntimeException | SQLException e) {
            return false;
        }
    }

    private boolean isConfigurationManagementEnabled() {

        return FrameworkUtils.isTableExists("IDN_CONFIG_TYPE") && FrameworkUtils.isTableExists("IDN_CONFIG_RESOURCE")
                && FrameworkUtils.isTableExists("IDN_CONFIG_ATTRIBUTE") && FrameworkUtils
                .isTableExists("IDN_CONFIG_FILE");
    }
}
