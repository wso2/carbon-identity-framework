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

import org.wso2.carbon.identity.template.mgt.TemplateMgtConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * A data model class to define the Template element.
 */
public class Template {

    private String templateId;
    private int tenantId;
    private String templateName;
    private String description;
    private String imageUrl;
    private TemplateMgtConstants.TemplateType templateType;
    private String templateScript;
    private Map<String, String> propertiesMap = new HashMap<>();

    public Template(String templateId, int tenantId, String templateName, String description, String
            imageUrl, TemplateMgtConstants.TemplateType templateType, String templateScript, Map<String, String>
                            propertiesMap) {

        this.templateId = templateId;
        this.tenantId = tenantId;
        this.templateName = templateName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.templateType = templateType;
        this.templateScript = templateScript;
        this.propertiesMap = propertiesMap;
    }

    public Template(int tenantId, String templateName, String description, String templateScript) {

        this.tenantId = tenantId;
        this.templateName = templateName;
        this.description = description;
        this.templateScript = templateScript;
    }

    public Template(String templateName, String description, String templateScript) {

        this.templateName = templateName;
        this.description = description;
        this.templateScript = templateScript;
    }

    public Template() {

    }

    /**
     * Get tenant ID.
     *
     * @return tenant ID.
     */
    public int getTenantId() {

        return tenantId;
    }

    /**
     * Set tenant ID.
     *
     * @param tenantId tenant ID.
     */
    public void setTenantId(int tenantId) {

        this.tenantId = tenantId;
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
     * Set template name.
     *
     * @param templateName template name.
     */
    public void setTemplateName(String templateName) {

        this.templateName = templateName;
    }

    /**
     * Get template description.
     *
     * @return template description.
     */
    public String getDescription() {

        return description;
    }

    /**
     * Set template description.
     *
     * @param description template description.
     */
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * Get template script.
     *
     * @return templateScript.
     */
    public String getTemplateScript() {

        return templateScript;
    }

    /**
     * Set template script.
     *
     * @param content templateScript.
     */
    public void setTemplateScript(String content) {

        this.templateScript = content;
    }

    /**
     * Get the unique identifier of the template.
     *
     * @return template resource id.
     */
    public String getTemplateId() {
        return templateId;
    }

    /**
     * Set the unique identifier of the template.
     *
     * @param templateId unique id for the template.
     */
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    /**
     * Get the image url of the template.
     *
     * @return image url.
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Set the image url of the template.
     *
     * @param imageUrl image url.
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Get the list of properties defined for the template.
     *
     * @return list of properties.
     */
    public Map<String, String> getPropertiesMap() {
        return propertiesMap;
    }

    /**
     * Set the list of properties of the template.
     *
     * @param propertiesMap list of properties.
     */
    public void setPropertiesMap(Map<String, String> propertiesMap) {
        this.propertiesMap = propertiesMap;
    }

    /**
     * Get the type of the template. This can be application or identity provider.
     *
     * @return template type.
     */
    public TemplateMgtConstants.TemplateType getTemplateType() {
        return templateType;
    }

    /**
     * Set the type of the template.
     *
     * @param templateType template type.
     */
    public void setTemplateType(TemplateMgtConstants.TemplateType templateType) {
        this.templateType = templateType;
    }
}
