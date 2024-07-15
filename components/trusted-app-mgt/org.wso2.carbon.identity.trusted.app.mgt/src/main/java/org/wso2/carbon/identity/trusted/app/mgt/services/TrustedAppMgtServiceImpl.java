/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.trusted.app.mgt.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.TrustedApp;
import org.wso2.carbon.identity.trusted.app.mgt.exceptions.TrustedAppMgtException;
import org.wso2.carbon.identity.trusted.app.mgt.internal.TrustedAppMgtDataHolder;
import org.wso2.carbon.identity.trusted.app.mgt.model.TrustedAndroidApp;
import org.wso2.carbon.identity.trusted.app.mgt.model.TrustedIosApp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.PlatformType;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.ANDROID_CREDENTIAL_PERMISSION;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.ANDROID_HANDLE_URLS_PERMISSION;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.IOS_CREDENTIAL_PERMISSION;

/**
 * Trusted App Management Service Implementation.
 */
public class TrustedAppMgtServiceImpl implements TrustedAppMgtService {

    private static final Log LOG = LogFactory.getLog(TrustedAppMgtServiceImpl.class);

    @Override
    public List<TrustedAndroidApp> getTrustedAndroidApps() throws TrustedAppMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving trusted apps for Android platform.");
        }
        List<TrustedAndroidApp> trustedAndroidApps = new ArrayList<>();
        try {
            List<TrustedApp> trustedAppSet =
                    TrustedAppMgtDataHolder.getInstance().getApplicationManagementService()
                            .getTrustedApps(PlatformType.ANDROID);
            for (TrustedApp trustedApp : trustedAppSet) {
                TrustedAndroidApp trustedAndroidApp = new TrustedAndroidApp();
                trustedAndroidApp.setPackageName(trustedApp.getAppIdentifier());
                String[] thumbprints = resolveThumbprints(trustedApp);
                Set<String> permissions = resolveAppPermissions(trustedApp);
                if (thumbprints.length == 0 || permissions.isEmpty()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format("No thumbprints or permissions found for the app: %s. Hence removing " +
                                "the app from trusted app list.", trustedApp.getAppIdentifier()));
                    }
                    continue;
                }
                trustedAndroidApp.setThumbprints(thumbprints);
                trustedAndroidApp.setPermissions(permissions);
                trustedAndroidApps.add(trustedAndroidApp);
            }
        } catch (IdentityApplicationManagementException e) {
            throw new TrustedAppMgtException("Error occurred while retrieving trusted apps for " +
                    "Android platform.", e);
        }
        return trustedAndroidApps;
    }

    @Override
    public List<TrustedIosApp> getTrustedIosApps() throws TrustedAppMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving trusted apps for iOS platform.");
        }
        List<TrustedIosApp> trustedIosApps = new ArrayList<>();
        try {
            List<TrustedApp> trustedAppSet =
                    TrustedAppMgtDataHolder.getInstance().getApplicationManagementService().
                            getTrustedApps(PlatformType.IOS);
            for (TrustedApp trustedApp : trustedAppSet) {
                TrustedIosApp trustedIosApp = new TrustedIosApp();
                trustedIosApp.setAppId(trustedApp.getAppIdentifier());
                Set<String> permissions = resolveAppPermissions(trustedApp);
                if (permissions.isEmpty()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format("No permissions found for the app: %s. Hence removing the app from " +
                                "trusted app list.", trustedApp.getAppIdentifier()));
                    }
                    continue;
                }
                trustedIosApp.setPermissions(permissions);
                trustedIosApps.add(trustedIosApp);
            }
        } catch (IdentityApplicationManagementException e) {
            throw new TrustedAppMgtException("Error occurred while retrieving trusted apps for " +
                    "iOS platform.", e);
        }
        return trustedIosApps;
    }

    private String[] resolveThumbprints(TrustedApp trustedApp) {

        String[] thumbprints = trustedApp.getThumbprints();
        if (thumbprints != null) {
            return thumbprints;
        }
        return new String[0];
    }

    private Set<String> resolveAppPermissions(TrustedApp trustedApp) throws TrustedAppMgtException {

        Set<String> appPermissions = new HashSet<>();
        if (trustedApp.getIsFIDOTrusted()) {
            if (PlatformType.ANDROID.equals(trustedApp.getPlatformType())) {
                appPermissions.add(ANDROID_CREDENTIAL_PERMISSION);
                appPermissions.add(ANDROID_HANDLE_URLS_PERMISSION);
            } else if (PlatformType.IOS.equals(trustedApp.getPlatformType())) {
                appPermissions.add(IOS_CREDENTIAL_PERMISSION);
            }
        }
        return appPermissions;
    }
}
