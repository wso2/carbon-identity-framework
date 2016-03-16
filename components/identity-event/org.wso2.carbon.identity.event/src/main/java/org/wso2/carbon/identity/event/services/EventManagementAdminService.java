/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.event.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.event.EventMgtException;
import org.wso2.carbon.identity.event.bean.EventMgtConfiguration;
import org.wso2.carbon.identity.event.internal.EventMgtServiceDataHolder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is the admin service for the identity management. Some of these
 * operations are can only be carried out by admins. The other operations are
 * allowed to all logged in users.
 */
public class EventManagementAdminService extends AbstractAdmin {

    private static Log log = LogFactory.getLog(EventManagementAdminService.class);

    /**
     * Get all the configurations belong to a tenant.
     *
     * @return Configurations for the tenant ID
     */
    public EventMgtConfiguration[] getConfiguration() throws EventMgtException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventMgtService identityMgtService = EventMgtServiceDataHolder.getInstance().getEventMgtService();
        Map<String, String> configurationDetails = identityMgtService.getConfiguration(tenantId);
        EventMgtConfiguration[] configurations = convertBeanToDto(configurationDetails);
        return configurations;
    }

    /**
     * Update configurations of a tenant in database
     *
     * @param configurations Configurations
     */
    public void updateConfiguration(EventMgtConfiguration[] configurations) throws EventMgtException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, String> configurationDetails = convertDtoToBean(configurations);
        EventMgtService identityMgtService = EventMgtServiceDataHolder.getInstance().getEventMgtService();
        identityMgtService.updateConfiguration(tenantId, configurationDetails);
    }

    /**
     * Convert Bean object to DTO object
     * @param configurationDetails
     * @return
     */
    private EventMgtConfiguration[] convertBeanToDto(Map<String, String> configurationDetails){

        EventMgtConfiguration[] configurations = new EventMgtConfiguration[configurationDetails.size()];
        Iterator<Map.Entry<String, String>> iterator = configurationDetails.entrySet().iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Map.Entry<String, String> pair = iterator.next();
            EventMgtConfiguration configuration = new EventMgtConfiguration(pair.getKey(), pair.getValue());
            configurations[count] = configuration;
            iterator.remove();
            ++count;
        }
        return configurations;
    }


    /**
     * Convert DTO object to Bean object
     * @param configurations
     * @return
     */
    private Map<String, String> convertDtoToBean(EventMgtConfiguration[] configurations){

        Map<String, String> configurationDetails = new HashMap<>();
        for (int i = 0; i < configurations.length; i++) {
            configurationDetails.put(configurations[i].getProperty(), configurations[i].getPropertyValue());
        }
        return configurationDetails;
    }

}
