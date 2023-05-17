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
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.template.mgt.TemplateManager;
import org.wso2.carbon.identity.template.mgt.TemplateManagerImpl;
import org.wso2.carbon.identity.template.mgt.TemplateMgtConstants;
import org.wso2.carbon.identity.template.mgt.handler.ReadOnlyTemplateHandler;
import org.wso2.carbon.identity.template.mgt.handler.TemplateHandler;
import org.wso2.carbon.identity.template.mgt.handler.impl.ConfigStoreBasedTemplateHandler;
import org.wso2.carbon.identity.template.mgt.handler.impl.FileBasedTemplateHandler;
import org.wso2.carbon.identity.template.mgt.model.Template;
import org.wso2.carbon.identity.xds.client.mgt.XDSClientService;
import org.wso2.carbon.security.SecurityServiceHolder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OSGi declarative services component which handles registration and un-registration of template management service.
 */
@Component(
        name = "carbon.identity.template.mgt.component",
        immediate = true
)
public class TemplateManagerComponent {

    private static Log log = LogFactory.getLog(TemplateManagerComponent.class);

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

            // Add default template handlers.
            ReadOnlyTemplateHandler fileBasedTemplateHandler = new FileBasedTemplateHandler();
            TemplateManagerDataHolder.getInstance().addReadOnlyTemplateHandler(fileBasedTemplateHandler);
            TemplateHandler configStoreBasedTemplateHandler = new ConfigStoreBasedTemplateHandler();
            TemplateManagerDataHolder.getInstance().setReadWriteTemplateHandler(configStoreBasedTemplateHandler);
            loadDefaultTemplates();
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

    @Reference(
            name = "identity.template.handler",
            service = ReadOnlyTemplateHandler.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetReadOnlyTemplateHandler")
    protected void setReadOnlyTemplateHandler(ReadOnlyTemplateHandler readOnlyTemplateHandler) {

        if (log.isDebugEnabled()) {
            log.debug("Template handler " + readOnlyTemplateHandler.getClass().getName() + " is added.");
        }
        TemplateManagerDataHolder.getInstance().addReadOnlyTemplateHandler(readOnlyTemplateHandler);
    }

    protected void unsetReadOnlyTemplateHandler(ReadOnlyTemplateHandler readOnlyTemplateHandler) {

        if (log.isDebugEnabled()) {
            log.debug("Template handler " + readOnlyTemplateHandler.getClass().getName() + " is removed.");
        }
        TemplateManagerDataHolder.getInstance().removeReadOnlyTemplateHandler(readOnlyTemplateHandler);
    }

    private void loadDefaultTemplates() throws FileNotFoundException {

        List<Path> paths = Arrays.asList(TemplateMgtConstants.SP_TEMPLATES_DIR_PATH, TemplateMgtConstants
                .IDP_TEMPLATES_DIR_PATH);
        for (Path path : paths) {
            if (!Files.exists(path) || !Files.isDirectory(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("No file-based idp/application templates");
                }
            } else {
                try {
                    Files.walk(path)
                            .filter(filePath -> Files.isRegularFile(filePath) && filePath.toString().endsWith
                                    (TemplateMgtConstants.FILE_EXT_JSON))
                            .forEach(filePath -> {
                                try {
                                    String templateJsonString = FileUtils.readFileToString(filePath.toFile());
                                    JSONObject templateObj = new JSONObject(templateJsonString);
                                    Template template = new Template();

                                    template.setTemplateId(templateObj.getString(TemplateMgtConstants.ID));
                                    template.setTemplateName(templateObj.getString(TemplateMgtConstants.NAME));
                                    template.setDescription(templateObj.getString(TemplateMgtConstants.DESCRIPTION));
                                    template.setImageUrl(templateObj.getString(TemplateMgtConstants.IMAGE));

                                    if (StringUtils.equals(TemplateMgtConstants.SP_TEMPLATES_DIR_PATH.toString(),
                                            path.toString())) {
                                        template.setTemplateType(TemplateMgtConstants.TemplateType
                                                .APPLICATION_TEMPLATE);
                                        template.setPropertiesMap(extractApplicationSpecificProperties(templateObj));
                                        template.setTemplateScript(templateObj.getJSONObject(TemplateMgtConstants
                                                .APPLICATION).toString());
                                    } else if (StringUtils.equals(TemplateMgtConstants.IDP_TEMPLATES_DIR_PATH
                                            .toString(), path.toString())) {
                                        template.setTemplateType(TemplateMgtConstants.TemplateType.IDP_TEMPLATE);
                                        template.setPropertiesMap(extractIDPSpecificProperties(templateObj));
                                        template.setTemplateScript(templateObj.getJSONObject(TemplateMgtConstants
                                                .IDP).toString());
                                    }
                                    // Add file based templates to FileBasedTemplates map.
                                    TemplateManagerDataHolder.getInstance()
                                            .addFileBasedTemplate(templateObj.getString(TemplateMgtConstants.ID),
                                                    template);
                                } catch (IOException e) {
                                    log.error("Error while reading  templates.", e);
                                }
                            });
                } catch (IOException e) {
                    log.error("Error while reading templates.", e);
                }
            }
        }
    }

    private Map<String, String> extractApplicationSpecificProperties(JSONObject templateObj) {

        Map<String, String> properties = new HashMap<>();
        if (StringUtils.isNotEmpty(templateObj.getString(TemplateMgtConstants.AUTHENTICATION_PROTOCOL))) {
            properties.put(TemplateMgtConstants.PROPERTY_AUTHENTICATION_PROTOCOL,
                    templateObj.getString(TemplateMgtConstants.AUTHENTICATION_PROTOCOL));
        }
        if (templateObj.getJSONArray(TemplateMgtConstants.TYPES) != null) {
            JSONArray typesJSONArray = templateObj.getJSONArray(TemplateMgtConstants.TYPES);
            List<String> types = new ArrayList<>();
            if (typesJSONArray != null) {
                for (int i = 0; i < typesJSONArray.length(); i++) {
                    types.add(typesJSONArray.getString(i));
                }
            }
            properties.put(TemplateMgtConstants.TYPES, String.join(",", types));
        }
        if (StringUtils.isNotEmpty(templateObj.getString(TemplateMgtConstants.CATEGORY))) {
            properties.put(TemplateMgtConstants.CATEGORY, templateObj.getString(TemplateMgtConstants.CATEGORY));
        }
        if (StringUtils.isNotEmpty(String.valueOf(templateObj.getInt(TemplateMgtConstants.DISPLAY_ORDER)))) {
            properties.put(TemplateMgtConstants.PROPERTY_DISPLAY_ORDER, Integer.toString(templateObj.getInt
                    (TemplateMgtConstants.DISPLAY_ORDER)));
        }
        if (StringUtils.isNotEmpty(templateObj.getString(TemplateMgtConstants.TEMPLATE_GROUP))) {
            properties.put(TemplateMgtConstants.PROPERTY_TEMPLATE_GROUP,
                    templateObj.getString(TemplateMgtConstants.TEMPLATE_GROUP));
        }
        return properties;
    }

    private Map<String, String> extractIDPSpecificProperties(JSONObject templateObj) {

        Map<String, String> properties = new HashMap<>();
        if (StringUtils.isNotEmpty(templateObj.getString(TemplateMgtConstants.PROP_CATEGORY))) {
            properties.put(TemplateMgtConstants.PROP_CATEGORY, templateObj.getString(TemplateMgtConstants
                    .PROP_CATEGORY));
        }
        if (StringUtils.isNotEmpty(String.valueOf(templateObj.getInt(TemplateMgtConstants.PROP_DISPLAY_ORDER)))) {
            properties.put(TemplateMgtConstants.PROP_DISPLAY_ORDER, Integer.toString(templateObj.getInt
                    (TemplateMgtConstants.PROP_DISPLAY_ORDER)));
        }
        if (templateObj.getJSONArray(TemplateMgtConstants.PROP_SERVICES) != null) {
            JSONArray servicesJSONArray = templateObj.getJSONArray(TemplateMgtConstants.PROP_SERVICES);
            List<String> services = new ArrayList<>();
            if (servicesJSONArray != null) {
                for (int i = 0; i < servicesJSONArray.length(); i++) {
                    services.add(servicesJSONArray.getString(i));
                }
            }
            properties.put(TemplateMgtConstants.PROP_SERVICES, String.join(",", services));
        }
        return properties;
    }

    @Reference(
            name = "xds.client.service",
            service = XDSClientService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetXDSClientService"
    )
    protected void setXDSClientService(XDSClientService xdsClientService) {

        TemplateManagerDataHolder.getInstance().setXdsClientService(xdsClientService);
    }

    protected void unsetXDSClientService(XDSClientService xdsClientService) {

        TemplateManagerDataHolder.getInstance().setXdsClientService(null);
    }
}
