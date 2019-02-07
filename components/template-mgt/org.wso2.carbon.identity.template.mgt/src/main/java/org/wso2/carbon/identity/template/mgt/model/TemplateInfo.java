/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.template.mgt.model;

/**
 * A data model class to define the TemplateInfo element.
 */
public class TemplateInfo {

    private Integer templateId;
    private Integer tenantId;
    private String templateName;
    private String description;

    public TemplateInfo(Integer templateId, Integer tenantId, String templateName) {

        this.templateId = templateId;
        this.tenantId = tenantId;
        this.templateName = templateName;
    }

    public TemplateInfo(Integer tenantId, String templateName) {

        this.tenantId = tenantId;
        this.templateName = templateName;
    }

    public TemplateInfo(String templateName, String description) {

        this.templateName = templateName;
        this.description = description;
    }

    /**
     * Get template ID.
     *
     * @return template ID.
     */
    public Integer getTemplateId() {

        return templateId;
    }

    /**
     * Get tenant ID.
     *
     * @return tenant ID.
     */
    public Integer getTenantId() {

        return tenantId;
    }

    /**
     * Get template name.
     *
     * @return template name.
     */
    public String getTemplateName() {

        return templateName;
    }

    /**
     * Get template description.
     *
     * @return template description.
     */
    public String getDescription() {

        return description;
    }

}
