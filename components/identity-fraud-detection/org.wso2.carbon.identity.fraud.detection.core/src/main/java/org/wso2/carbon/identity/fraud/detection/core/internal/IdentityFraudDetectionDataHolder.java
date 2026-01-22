/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.carbon.identity.fraud.detection.core.internal;

import org.apache.http.impl.client.CloseableHttpClient;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.fraud.detection.core.IdentityFraudDetector;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.HashMap;
import java.util.Map;

/**
 * Data holder for Identity Fraud Detection component.
 */
public class IdentityFraudDetectionDataHolder {

    private static final IdentityFraudDetectionDataHolder instance = new IdentityFraudDetectionDataHolder();
    private final Map<String, IdentityFraudDetector> identityFraudDetectors = new HashMap<>();
    private CloseableHttpClient httpClient;
    private ConfigurationManager configurationManager;
    private RealmService realmService;
    private OrganizationManager organizationManager;

    /**
     * Private constructor to prevent instantiation.
     */
    private IdentityFraudDetectionDataHolder() {

    }

    /**
     * Get the singleton instance of the data holder.
     *
     * @return IdentityFraudDetectorDataHolder instance.
     */
    public static IdentityFraudDetectionDataHolder getInstance() {

        return instance;
    }

    /**
     * Get the HTTP client.
     *
     * @return CloseableHttpClient instance.
     */
    public CloseableHttpClient getHttpClient() {

        return httpClient;
    }

    /**
     * Set the HTTP client.
     *
     * @param httpClient CloseableHttpClient instance.
     */
    public void setHttpClient(CloseableHttpClient httpClient) {

        this.httpClient = httpClient;
    }

    /**
     * Add an Identity Fraud Detector.
     *
     * @param name                  Name of the fraud detector.
     * @param identityFraudDetector IdentityFraudDetector instance.
     */
    public void addIdentityFraudDetector(String name, IdentityFraudDetector identityFraudDetector) {

        this.identityFraudDetectors.put(name, identityFraudDetector);
    }

    /**
     * Remove an Identity Fraud Detector.
     *
     * @param name Name of the fraud detector.
     */
    public void removeIdentityFraudDetector(String name) {

        this.identityFraudDetectors.remove(name);
    }

    /**
     * Get an Identity Fraud Detector by name.
     *
     * @param name Name of the fraud detector.
     * @return IdentityFraudDetector instance.
     */
    public IdentityFraudDetector getIdentityFraudDetector(String name) {

        return this.identityFraudDetectors.get(name);
    }

    /**
     * Get all registered Identity Fraud Detectors.
     *
     * @return Map of IdentityFraudDetectors.
     */
    public Map<String, IdentityFraudDetector> getIdentityFraudDetectors() {

        return this.identityFraudDetectors;
    }

    /**
     * Get the ConfigurationManager.
     *
     * @return ConfigurationManager instance.
     */
    public ConfigurationManager getConfigurationManager() {

        return configurationManager;
    }

    /**
     * Set the ConfigurationManager.
     *
     * @param configurationManager ConfigurationManager instance.
     */
    public void setConfigurationManager(ConfigurationManager configurationManager) {

        this.configurationManager = configurationManager;
    }

    /**
     * Get the RealmService.
     *
     * @return RealmService.
     */
    public RealmService getRealmService() {

        return realmService;
    }

    /**
     * Set the RealmService.
     *
     * @param realmService RealmService.
     */
    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    /**
     * Set the OrganizationManager.
     *
     * @param organizationManager OrganizationManager instance.
     */
    public void setOrganizationManager(OrganizationManager organizationManager) {

        this.organizationManager = organizationManager;
    }

    /**
     * Get the OrganizationManager.
     *
     * @return OrganizationManager instance.
     */
    public OrganizationManager getOrganizationManager() {

        return organizationManager;
    }
}
