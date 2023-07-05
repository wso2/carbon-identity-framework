/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.extension.mgt.internal;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.extension.mgt.ExtensionManager;
import org.wso2.carbon.identity.extension.mgt.ExtensionManagerImpl;
import org.wso2.carbon.identity.extension.mgt.ExtensionStoreImpl;
import org.wso2.carbon.identity.extension.mgt.exception.ExtensionManagementException;
import org.wso2.carbon.identity.extension.mgt.function.JSONObjectToExtensionInfo;
import org.wso2.carbon.identity.extension.mgt.model.ExtensionInfo;
import org.wso2.carbon.identity.extension.mgt.utils.ExtensionMgtUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.wso2.carbon.identity.extension.mgt.utils.ExtensionMgtConstants.INFO_FILE_NAME;
import static org.wso2.carbon.identity.extension.mgt.utils.ExtensionMgtConstants.METADATA_FILE_NAME;
import static org.wso2.carbon.identity.extension.mgt.utils.ExtensionMgtConstants.TEMPLATE_FILE_NAME;
import static org.wso2.carbon.identity.extension.mgt.utils.ExtensionMgtConstants.UTF8;

/**
 * OSGi declarative services component which handles registration and un-registration of extension management service.
 */
@Component(
        name = "carbon.identity.extension.mgt.component",
        immediate = true
)
public class ExtensionManagerComponent {

    private static Log log = LogFactory.getLog(ExtensionManagerComponent.class);

    /**
     * Register Extension Manager as an OSGi service.
     *
     * @param componentContext OSGi service component context.
     */
    @Activate
    protected void activate(ComponentContext componentContext) {

        try {
            BundleContext bundleContext = componentContext.getBundleContext();

            bundleContext.registerService(ExtensionManager.class, new ExtensionManagerImpl(), null);

            // Load extension data from the file system.
            ExtensionManagerDataHolder.getInstance().setExtensionStore(new ExtensionStoreImpl());
            loadExtensionResources();

            if (log.isDebugEnabled()) {
                log.debug("Extension Manager bundle is activated.");
            }
        } catch (Throwable e) {
            log.error("Error while activating ExtensionManagerComponent.", e);
        }
    }

    /**
     * Set ConfigurationContextService.
     *
     * @param configCtxtService ConfigurationContextService.
     */
    @Reference(
            name = "config.context.service",
            service = ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService"
    )
    protected void setConfigurationContextService(ConfigurationContextService configCtxtService) {

        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService set in EntitlementServiceComponent bundle.");
        }
    }

    /**
     * Unset ConfigurationContextService.
     *
     * @param configCtxtService ConfigurationContextService.
     */
    protected void unsetConfigurationContextService(ConfigurationContextService configCtxtService) {

        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService unset in EntitlementServiceComponent bundle.");
        }
    }

    /**
     * Load extension resources from the file system.
     */
    private void loadExtensionResources() {

        for (String extensionType : ExtensionMgtUtils.getExtensionTypes()) {
            Path path = ExtensionMgtUtils.getExtensionPath(extensionType);
            if (log.isDebugEnabled()) {
                log.debug("Loading default templates from: " + path);
            }

            // Check whether the given extension type directory exists.
            if (!Files.exists(path) || !Files.isDirectory(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("Default templates directory does not exist: " + path);
                }
                continue;
            }

            ExtensionManagerDataHolder.getInstance().getExtensionStore().addExtensionType(extensionType);

            // Load extensions from the given extension type directory.
            try (Stream<Path> directories = Files.list(path).filter(Files::isDirectory)) {
                directories.forEach(extensionDirectory -> {
                    try {
                        // Load extension info.
                        ExtensionInfo extensionInfo = loadExtensionInfo(extensionDirectory);
                        if (extensionInfo == null) {
                            throw new ExtensionManagementException("Error while loading extension info from: "
                                    + extensionDirectory);
                        }
                        extensionInfo.setType(extensionType);
                        ExtensionManagerDataHolder.getInstance().getExtensionStore().addExtension(extensionType,
                                extensionInfo.getId(), extensionInfo);
                        // Load templates.
                        JSONObject template = loadTemplate(extensionDirectory);
                        if (template != null) {
                            ExtensionManagerDataHolder.getInstance().getExtensionStore().addTemplate(extensionType,
                                    extensionInfo.getId(), template);
                        }
                        // Load metadata.
                        JSONObject metadata = loadMetadata(extensionDirectory);
                        if (metadata != null) {
                            ExtensionManagerDataHolder.getInstance().getExtensionStore().addMetadata(extensionType,
                                    extensionInfo.getId(), metadata);
                        }
                    } catch (ExtensionManagementException e) {
                        log.error("Error while loading resource files in: " + extensionDirectory, e);
                    }
                });
            } catch (IOException e) {
                log.error("Error while loading resource files in: " + path, e);
            }
        }
    }

    /**
     * Load metadata from the given extension resource path.
     *
     * @param extensionResourcePath Extension resource path.
     * @return Metadata as a JSON object.
     * @throws ExtensionManagementException ExtensionManagementException.
     */
    private ExtensionInfo loadExtensionInfo(Path extensionResourcePath) throws ExtensionManagementException {

        Path infoPath = extensionResourcePath.resolve(INFO_FILE_NAME);
        if (Files.exists(infoPath) && Files.isRegularFile(infoPath)) {
            try {
                String infoJsonString = FileUtils.readFileToString(infoPath.toFile(), UTF8);
                JSONObject infoJson = new JSONObject(infoJsonString);
                return new JSONObjectToExtensionInfo().apply(infoJson);
            } catch (JSONException e) {
                throw new ExtensionManagementException("Error while parsing info.json file in: " +
                        extensionResourcePath, e);
            } catch (IOException e) {
                throw new ExtensionManagementException("Error while reading info.json file in: " +
                        extensionResourcePath, e);
            }
        } else {
            log.warn("Info file not found in: " + extensionResourcePath);
            return null;
        }
    }

    /**
     * Load template from the given extension resource path.
     *
     * @param extensionResourcePath Extension resource path.
     * @return Template as a JSON object.
     * @throws ExtensionManagementException ExtensionManagementException.
     */
    private JSONObject loadTemplate(Path extensionResourcePath) throws ExtensionManagementException {

        Path templatePath = extensionResourcePath.resolve(TEMPLATE_FILE_NAME);
        if (Files.exists(templatePath) && Files.isRegularFile(templatePath)) {
            return readJSONFile(templatePath);
        }
        return null;
    }

    /**
     * Load metadata from the given extension resource path.
     *
     * @param extensionResourcePath Extension resource path.
     * @return Metadata as a JSON object.
     * @throws ExtensionManagementException ExtensionManagementException.
     */
    private JSONObject loadMetadata(Path extensionResourcePath) throws ExtensionManagementException {

        Path metadataPath = extensionResourcePath.resolve(METADATA_FILE_NAME);
        if (Files.exists(metadataPath) && Files.isRegularFile(metadataPath)) {
            return readJSONFile(metadataPath);
        }
        return null;
    }

    /**
     * Read JSON file and return as a JSON object.
     *
     * @param path Path of the JSON file.
     * @return JSON object.
     * @throws ExtensionManagementException ExtensionManagementException.
     */
    private JSONObject readJSONFile(Path path) throws ExtensionManagementException {

        if (Files.exists(path) && Files.isRegularFile(path)) {
            try {
                String jsonString = FileUtils.readFileToString(path.toFile(), UTF8);
                return new JSONObject(jsonString);
            } catch (JSONException e) {
                throw new ExtensionManagementException("Error while parsing JSON file: " + path, e);
            } catch (IOException e) {
                throw new ExtensionManagementException("Error while reading JSON file: " + path, e);
            }
        } else {
            throw new ExtensionManagementException("JSON file not found: " + path);
        }
    }
}
