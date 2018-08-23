/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.identity.application.mgt.dao;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.SpTemplate;

import java.util.List;

/**
 * This interface access the data storage layer to retrieve, store, delete and update service provider templates.
 */
public interface ApplicationTemplateDAO {

    /**
     * Create an application template.
     *
     * @param spTemplate   SP template info
     * @param tenantDomain tenant domain
     * @throws IdentityApplicationManagementException
     */
    void createApplicationTemplate(SpTemplate spTemplate, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Load an application template.
     *
     * @param templateName name of the template
     * @param tenantDomain tenant domain
     * @return SP template info
     * @throws IdentityApplicationManagementException
     */
    SpTemplate getApplicationTemplate(String templateName, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Delete an application template.
     *
     * @param templateName name of the template
     * @param tenantDomain tenant domain
     * @throws IdentityApplicationManagementException
     */
    void deleteApplicationTemplate(String templateName, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Update an application template.
     *
     * @param spTemplate   SP template info to be updated
     * @param tenantDomain tenant domain
     * @throws IdentityApplicationManagementException
     */
    void updateApplicationTemplate(String templateName, SpTemplate spTemplate, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Check Existence of a application template.
     *
     * @param templateName name of the template
     * @param tenantDomain tenant domain
     * @return true if a template with the specified template name exists
     * @throws IdentityApplicationManagementException
     */
    boolean isExistingTemplate(String templateName, String tenantDomain) throws IdentityApplicationManagementException;

    /**
     * Get all application templates.
     *
     * @param tenantDomain tenant domain
     * @return Info of the list of all application templates
     * @throws IdentityApplicationManagementException
     */
    List<SpTemplate> getAllApplicationTemplateInfo(String tenantDomain) throws IdentityApplicationManagementException;

}
