/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.identity.core.model.XMPPSettingsDO;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;

public class XMPPSettingsDAO extends AbstractDAO<XMPPSettingsDO> {

    protected Log log = LogFactory.getLog(XMPPSettingsDAO.class);

    /**
     * @param registry
     */
    public XMPPSettingsDAO(Registry registry) {
        this.registry = registry;
    }

    /**
     * @param resource
     * @return
     */
    protected XMPPSettingsDO resourceToObject(Resource resource) {

        XMPPSettingsDO xmppSettingsDO = null;

        if (resource != null) {
            xmppSettingsDO = new XMPPSettingsDO();
            xmppSettingsDO.setXmppServer(resource
                    .getProperty(IdentityRegistryResources.XMPP_SERVER));
            xmppSettingsDO.setXmppUserName(resource
                    .getProperty(IdentityRegistryResources.XMPP_USERNAME));
            xmppSettingsDO.setUserCode(resource
                    .getProperty(IdentityRegistryResources.XMPP_USERCODE));

            if (resource.getProperty(IdentityRegistryResources.XMPP_ENABLED).trim().equals("true")) {
                xmppSettingsDO.setXmppEnabled(true);
            } else {
                xmppSettingsDO.setXmppEnabled(false);
            }

            if (resource.getProperty(IdentityRegistryResources.XMPP_PIN_ENABLED).trim().equals(
                    "true")) {
                xmppSettingsDO.setPINEnabled(true);
            } else {
                xmppSettingsDO.setPINEnabled(false);
            }
        }

        return xmppSettingsDO;
    }

    /**
     * Adding XMPP Settings corresponding to a user
     *
     * @param userId
     * @param xmppServer
     * @param xmppUserName
     * @param xmppUserCode
     * @throws IdentityException
     */
    public void addXmppSettings(String userId, String xmppServer, String xmppUserName,
                                String xmppUserCode, boolean enabled, boolean pinEnabled) throws IdentityException {

        String path = null;
        Resource resource = null;
        Collection userResource = null;
        String xmppEnabled = "false";
        String isPINEnabled = "false";

        if (enabled) {
            xmppEnabled = "true";
        }

        if (pinEnabled) {
            isPINEnabled = "true";
        }

        try {
            if (userId != null) {
                path = IdentityRegistryResources.XMPP_SETTINGS_ROOT + userId;
            }

            if (registry.resourceExists(path)) {
                if (log.isInfoEnabled()) {
                    log.info("XMPP Settings already exists for user " + userId);
                }
                return;
            }

            resource = registry.newResource();
            resource.addProperty(IdentityRegistryResources.XMPP_SERVER, xmppServer);
            resource.addProperty(IdentityRegistryResources.XMPP_USERNAME, xmppUserName);
            resource.addProperty(IdentityRegistryResources.XMPP_USERCODE, xmppUserCode);
            resource.addProperty(IdentityRegistryResources.XMPP_ENABLED, xmppEnabled);
            resource.addProperty(IdentityRegistryResources.XMPP_PIN_ENABLED, isPINEnabled);
            boolean transactionStarted = Transaction.isStarted();
            try {

                if (!transactionStarted) {
                    registry.beginTransaction();
                }
                registry.put(path, resource);

                if (!registry.resourceExists(RegistryConstants.PROFILES_PATH + userId)) {
                    userResource = registry.newCollection();
                    registry.put(RegistryConstants.PROFILES_PATH + userId, userResource);
                } else {
                    //userResource = (Collection) registry.get(RegistryConstants.PROFILES_PATH  + userId);
                }

                registry.addAssociation(RegistryConstants.PROFILES_PATH + userId, path,
                        IdentityRegistryResources.ASSOCIATION_USER_XMPP_SETTINGS);
                if (!transactionStarted) {
                    registry.commitTransaction();
                }
            } catch (Exception e) {
                if (!transactionStarted) {
                    registry.rollbackTransaction();
                }
                if (e instanceof RegistryException) {
                    throw (RegistryException) e;
                } else {
                    throw IdentityException.error("Error occured while adding XMPP Settings", e);
                }
            }
            if (log.isInfoEnabled()) {
                log.info("XMPP Settings for " + userId + " added successfully.");
            }

        } catch (RegistryException e) {
            log.error("Error occured while adding XMPP Settings.", e);
            throw IdentityException.error("Error occured while adding XMPP Settings.", e);

        }
    }

    /**
     * Update XMPP Settings of a user
     *
     * @param userId
     * @param xmppServer
     * @param xmppUserName
     * @param xmppUserCode
     * @throws IdentityException
     */
    public void updateXmppSettings(String userId, String xmppServer, String xmppUserName,
                                   String xmppUserCode, boolean enabled, boolean pinEnabled) throws IdentityException {
        String path = null;
        Resource resource = null;

        String xmppEnabled = "false";
        String isPINEnabled = "false";

        if (enabled) {
            xmppEnabled = "true";
        }

        if (pinEnabled) {
            isPINEnabled = "true";
        }

        try {
            if (userId != null) {
                path = IdentityRegistryResources.XMPP_SETTINGS_ROOT + userId;
            }

            if (!registry.resourceExists(path)) {
                if (log.isInfoEnabled()) {
                    log.info("XMPP Settings does not exist for the user " + userId);
                }
                return;
            }

            resource = registry.get(path);
            resource.setProperty(IdentityRegistryResources.XMPP_SERVER, xmppServer);
            resource.setProperty(IdentityRegistryResources.XMPP_USERNAME, xmppUserName);
            resource.setProperty(IdentityRegistryResources.XMPP_USERCODE, xmppUserCode);
            resource.setProperty(IdentityRegistryResources.XMPP_ENABLED, xmppEnabled);
            resource.setProperty(IdentityRegistryResources.XMPP_PIN_ENABLED, isPINEnabled);

            registry.put(path, resource);

            if (log.isInfoEnabled()) {
                log.info("XMPP Settings are updated for the user " + userId);
            }

        } catch (RegistryException e) {
            log.error("Error occured while updating the XMPP Settings.", e);
            throw IdentityException.error("Error occured while updating the XMPP Settings.", e);
        }
    }

    /**
     * retrieve XMPP Settings of a user by providing the userId
     *
     * @param userId
     * @return
     */
    public XMPPSettingsDO getXmppSettings(String userId) {

        XMPPSettingsDO xmppSettings = null;

        try {
            if (registry.resourceExists(IdentityRegistryResources.XMPP_SETTINGS_ROOT + userId)) {
                xmppSettings = resourceToObject(registry
                        .get(IdentityRegistryResources.XMPP_SETTINGS_ROOT + userId));
            }

        } catch (RegistryException e) {
            log.error("Cannot retrieve the XMPP Settings for the user " + userId, e);
        }
        return xmppSettings;
    }

    /**
     * Checks whether the given user has enabled XMPP based multifactor auth.
     *
     * @param userId
     * @return
     */
    public boolean isXmppSettingsEnabled(String userId) {

        boolean isEnabled = false;
        XMPPSettingsDO xmppSettings;
        try {
            if (registry.resourceExists(IdentityRegistryResources.XMPP_SETTINGS_ROOT + userId)) {
                xmppSettings = resourceToObject(registry
                        .get(IdentityRegistryResources.XMPP_SETTINGS_ROOT + userId));
                isEnabled = xmppSettings.isXmppEnabled();
            }
        } catch (RegistryException e) {
            log.error("Error when checking the availability of the user " + userId, e);
        }

        return isEnabled;
    }

    public boolean hasXmppSettings(String userId) {
        boolean hasSettings = false;

        try {
            hasSettings = registry.resourceExists(IdentityRegistryResources.XMPP_SETTINGS_ROOT
                    + userId);
        } catch (RegistryException e) {
            log.error("Error when checking the availability of the user " + userId, e);
        }

        return hasSettings;
    }
}
