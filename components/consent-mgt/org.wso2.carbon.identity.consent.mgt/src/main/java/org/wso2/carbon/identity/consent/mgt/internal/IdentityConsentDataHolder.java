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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.consent.mgt.internal;

import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.PrivilegedConsentManager;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.SSOConsentService;

/**
 * Holds data for Identity Consent Mgt component.
 */
public class IdentityConsentDataHolder {

    private static IdentityConsentDataHolder instance = new IdentityConsentDataHolder();
    private ConsentManager consentManager = null;
    private PrivilegedConsentManager privilegedConsentManager = null;
    private SSOConsentService ssoConsentService = null;

    private IdentityConsentDataHolder() {

    }

    /**
     * Returns an instance of IdentityConsentDataHolder.
     * @return IdentityConsentDataHolder.
     */
    public static IdentityConsentDataHolder getInstance() {

        return instance;
    }

    /**
     * Get {@link ConsentManager} service.
     *
     * @return Consent manager service
     */
    public ConsentManager getConsentManager() {

        return consentManager;
    }

    /**
     * Set {@link ConsentManager} service.
     *
     * @param consentManager Instance of {@link ConsentManager} service.
     */
    public void setConsentManager(ConsentManager consentManager) {

        this.consentManager = consentManager;
    }

    public PrivilegedConsentManager getPrivilegedConsentManager() {

        return privilegedConsentManager;
    }

    public void setPrivilegedConsentManager(PrivilegedConsentManager privilegedConsentManager) {

        this.privilegedConsentManager = privilegedConsentManager;
    }

    /**
     * Get {@link SSOConsentService} service.
     *
     * @return Consent consent service
     */
    public SSOConsentService getSSOConsentService() {

        return ssoConsentService;
    }

    /**
     * Set {@link SSOConsentService} service.
     *
     * @param ssoConsentService Instance of {@link SSOConsentService} service.
     */
    public void setSSOConsentService(SSOConsentService ssoConsentService) {

        this.ssoConsentService = ssoConsentService;
    }

}
