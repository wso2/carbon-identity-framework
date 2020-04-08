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
package org.wso2.carbon.identity.template.mgt.internal;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.template.mgt.TemplateManager;
import org.wso2.carbon.identity.template.mgt.TemplateManagerImpl;
import org.wso2.carbon.identity.template.mgt.TemplateMgtConstants;
import org.wso2.carbon.identity.template.mgt.model.Template;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * OSGi declarative services component which handles registration and un-registration of template management service.
 */
@Component(
        name = "carbon.identity.template.mgt.component",
        immediate = true
)
public class TemplateManagerComponent {

    private static Log log = LogFactory.getLog(TemplateManagerComponent.class);
    private static Map<TemplateMgtConstants.TemplateType, Map<String, Template>> fileBasedTemplates = new HashMap<>();

    public static Map<TemplateMgtConstants.TemplateType, Map<String, Template>> getFileBasedTemplates() {
        return fileBasedTemplates;
    }

    /**
     * Register Template Manager as an OSGi service.
     *
     * @param componentContext OSGi service component context.
     */
    @Activate
    protected void activate(ComponentContext componentContext) {

        try {
            BundleContext bundleContext = componentContext.getBundleContext();

            bundleContext.registerService(TemplateManager.class, new TemplateManagerImpl(), null);
            loadDefaultSPTemplates();
            loadDefaultIDPTemplates();
            if (log.isDebugEnabled()) {
                log.debug("Template Manager bundle is activated.");
            }
        } catch (Throwable e) {
            log.error("Error while activating TemplateManagerComponent.", e);
        }
    }

    @Reference(
            name = "identityCoreInitializedEventService",
            service = IdentityCoreInitializedEvent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityCoreInitializedEventService"
    )
    protected void setIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started. */
    }

    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started. */
    }

    @Reference(
            name = "carbon.configuration.mgt.component",
            service = ConfigurationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationManager")
    protected void setConfigurationManager(ConfigurationManager configurationManager) {

        if (log.isDebugEnabled()) {
            log.debug("Configuration Manager service is set in the Template Manager component.");
        }
        TemplateManagerDataHolder.getInstance().setConfigurationManager(configurationManager);
    }

    protected void unsetConfigurationManager(ConfigurationManager configurationManager) {

        if (log.isDebugEnabled()) {
            log.debug("Configuration Manager service is unset in the Template Manager component.");
        }
        TemplateManagerDataHolder.getInstance().setConfigurationManager(null);
    }

    private void loadDefaultSPTemplates() {
        // Load  file based SP templates on server startup.
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        File spTemplateDir = new File(TemplateMgtConstants.SP_TEMPLATES_DIR_PATH);
        Map<String, Template> templateList = new HashMap<>();
        if (spTemplateDir.exists() && spTemplateDir.isDirectory()) {
            File[] jsonFiles = spTemplateDir.listFiles((d, name) -> name.endsWith(TemplateMgtConstants.FILE_EXT_JSON));
            if (jsonFiles != null) {
                for (File jsonFile : jsonFiles) {
                    if (jsonFile.isFile()) {
                        try {
                            Template applicationTemplate = new Template();
                            String templateJsonString = FileUtils.readFileToString(jsonFile);
                            JSONObject templateObj = new JSONObject(templateJsonString);

                            // Set the additional properties specific to the Application Template as properties map
                            // in the template object.
                            Map<String, String> properties = new HashMap<>();
                            if (StringUtils.isNotEmpty(templateObj.getString(TemplateMgtConstants
                                    .AUTHENTICATION_PROTOCOL))) {
                                properties.put(TemplateMgtConstants.AUTHENTICATION_PROTOCOL, templateObj.getString
                                        (TemplateMgtConstants.AUTHENTICATION_PROTOCOL));
                            }
                            if (templateObj.getJSONArray(TemplateMgtConstants.TYPES) != null) {
                                JSONArray typesJSONArray = templateObj.getJSONArray(TemplateMgtConstants.TYPES);
                                List<String> types = new ArrayList<>();
                                for (int i = 0; i < typesJSONArray.length(); i++) {
                                    types.add(typesJSONArray.getString(i));
                                }
                                properties.put(TemplateMgtConstants.TYPES, String.join(",", types));
                            }
                            if (StringUtils.isNotEmpty(templateObj.getString(TemplateMgtConstants.CATEGORY))) {
                                properties.put(TemplateMgtConstants.CATEGORY, templateObj.getString
                                        (TemplateMgtConstants.CATEGORY));
                            }
                            if (StringUtils.isNotEmpty(String.valueOf(templateObj.getInt(TemplateMgtConstants
                                    .DISPLAY_ORDER)))) {
                                properties.put(TemplateMgtConstants.DISPLAY_ORDER, Integer.toString(
                                        templateObj.getInt(TemplateMgtConstants.DISPLAY_ORDER)));
                            }
                            applicationTemplate.setTemplateName(templateObj.getString("name"));
                            applicationTemplate.setDescription(templateObj.getString("description"));
                            applicationTemplate.setImageUrl(templateObj.getString("image"));
                            applicationTemplate.setTenantId(IdentityTenantUtil.getTenantId(tenantDomain));
                            applicationTemplate.setTemplateType(TemplateMgtConstants.TemplateType.APPLICATION_TEMPLATE);
                            applicationTemplate.setPropertiesMap(properties);
                            applicationTemplate.setTemplateScript(templateObj.getJSONObject("application").toString());

                            templateList.put(templateObj.getString("name"), applicationTemplate);

                        } catch (IOException e) {
                            e.printStackTrace();
                            log.error("Error while loading application templates from file system.", e);
                        }
                    }
                }
            }
            fileBasedTemplates.put(TemplateMgtConstants.TemplateType.APPLICATION_TEMPLATE, templateList);
        } else {
            log.warn("Application templates directory not found at " + spTemplateDir.getPath());
        }
    }

    // Load  file based IDP templates on server startup.
    private void loadDefaultIDPTemplates() {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        File idpTemplateDir = new File(TemplateMgtConstants.IDP_TEMPLATES_DIR_PATH);
        Map<String, Template> templateList = new HashMap<>();
        if (idpTemplateDir.exists() && idpTemplateDir.isDirectory()) {
            File[] jsonFiles = idpTemplateDir.listFiles((d, name) -> name.endsWith(TemplateMgtConstants.FILE_EXT_JSON));
            if (jsonFiles != null) {
                for (File jsonFile : jsonFiles) {
                    if (jsonFile.isFile()) {
                        try {
                            Template identityProviderTemplate = new Template();
                            String templateJsonString = FileUtils.readFileToString(jsonFile);
                            JSONObject templateObj = new JSONObject(templateJsonString);

                            Map<String, String> properties = new HashMap<>();
                            if (StringUtils.isNotEmpty(templateObj.getString(TemplateMgtConstants.PROP_CATEGORY))) {
                                properties.put(TemplateMgtConstants.PROP_CATEGORY, templateObj.getString
                                        (TemplateMgtConstants.PROP_CATEGORY));
                            }
                            if (StringUtils.isNotEmpty(String.valueOf(templateObj.getInt(TemplateMgtConstants
                                    .PROP_DISPLAY_ORDER))
                            )) {
                                properties.put(TemplateMgtConstants.PROP_DISPLAY_ORDER, Integer.toString(templateObj
                                        .getInt(TemplateMgtConstants.PROP_DISPLAY_ORDER)));
                            }
                            identityProviderTemplate.setTemplateName(templateObj.getString("name"));
                            identityProviderTemplate.setDescription(templateObj.getString("description"));
                            identityProviderTemplate.setImageUrl(templateObj.getString("image"));
                            identityProviderTemplate.setTenantId(IdentityTenantUtil.getTenantId(tenantDomain));
                            identityProviderTemplate.setTemplateType(TemplateMgtConstants.TemplateType.IDP_TEMPLATE);
                            identityProviderTemplate.setPropertiesMap(properties);
                            identityProviderTemplate.setTemplateScript(templateObj.getJSONObject("idp").toString());

                            templateList.put(templateObj.getString("name"), identityProviderTemplate);

                        } catch (IOException e) {
                            log.error("Error while loading idp templates from file system.", e);
                        }
                    }
                }
            }
            fileBasedTemplates.put(TemplateMgtConstants.TemplateType.IDP_TEMPLATE, templateList);
        } else {
            log.warn("IDP templates directory not found at " + idpTemplateDir.getPath());
        }
    }
}
