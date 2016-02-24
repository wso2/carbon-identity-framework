/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.core.persistence;

import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.dao.OpenIDAdminDAO;
import org.wso2.carbon.identity.core.dao.OpenIDUserDAO;
import org.wso2.carbon.identity.core.dao.ParameterDAO;
import org.wso2.carbon.identity.core.dao.SAMLSSOServiceProviderDAO;
import org.wso2.carbon.identity.core.dao.XMPPSettingsDAO;
import org.wso2.carbon.identity.core.model.OpenIDAdminDO;
import org.wso2.carbon.identity.core.model.OpenIDUserDO;
import org.wso2.carbon.identity.core.model.ParameterDO;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.model.XMPPSettingsDO;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.user.core.UserRealm;

public class IdentityPersistenceManager {

    private static IdentityPersistenceManager manager = new IdentityPersistenceManager();

    private IdentityPersistenceManager() {
    }

    /**
     * Returning the identity persistence manager instance : singleton pattern
     *
     * @return
     * @throws IdentityException
     */
    public static IdentityPersistenceManager getPersistanceManager() throws IdentityException {
        return manager;
    }

    /**
     * @param registry
     * @param paramName
     * @param value
     * @throws IdentityException
     */
    public void createOrUpdateParameter(Registry registry, String paramName, String value)
            throws IdentityException {

        if (paramName == null || value == null) {
            throw IdentityException.error("Invalid inputs");
        }

        ParameterDO param = null;
        param = new ParameterDO();
        paramName = paramName.trim();
        param.setName(paramName);

        if (value != null) {
            param.setValue(value);
        }

        ParameterDAO dao = new ParameterDAO(registry);
        dao.createOrUpdateParameter(param);
    }

    /**
     * @param registry
     * @param paramName
     * @return
     * @throws IdentityException
     */
    public String getParameterValue(Registry registry, String paramName) throws IdentityException {
        String value = null;
        ParameterDO param = null;

        param = getParameter(registry, paramName);
        if (param != null) {
            value = param.getValue();
        }
        return value;
    }

    /**
     * @param paramName
     * @return
     * @throws IdentityException
     */
    public ParameterDO getParameter(Registry registry, String paramName) throws IdentityException {
        ParameterDAO dao = new ParameterDAO(registry);
        return dao.getParameter(paramName);
    }

    /**
     * @param parameterDO
     * @throws IdentityException
     */
    public void removeParameter(Registry registry, ParameterDO parameterDO)
            throws IdentityException {
        ParameterDAO dao = new ParameterDAO(registry);
        dao.removeParameter(parameterDO);
    }


    /**
     * Add XMPP settings.
     *
     * @param userId
     * @param xmppServer
     * @param xmppUserName
     * @param xmppUserCode
     * @param enabled
     * @throws IdentityException
     */
    public void addXmppSettings(Registry registry, String userId, String xmppServer,
                                String xmppUserName, String xmppUserCode, boolean enabled, boolean isPINEnabled)
            throws IdentityException {
        XMPPSettingsDAO dao = new XMPPSettingsDAO(registry);
        dao.addXmppSettings(userId, xmppServer, xmppUserName, xmppUserCode, enabled, isPINEnabled);
    }

    /**
     * get the existing settings.
     *
     * @param userId
     * @return XmppSettingsDAO instance representing the XMPP Settings
     */
    public XMPPSettingsDO getXmppSettings(Registry registry, String userId) {
        XMPPSettingsDAO dao = new XMPPSettingsDAO(registry);
        return dao.getXmppSettings(userId);
    }

    /**
     * Update the existing settings.
     *
     * @param userId
     * @param xmppServer
     * @param xmppUserName
     * @param xmppUserCode
     * @param enabled
     * @throws IdentityException
     */
    public void updateXmppSettings(Registry registry, String userId, String xmppServer,
                                   String xmppUserName, String xmppUserCode, boolean enabled, boolean isPINEnabled)
            throws IdentityException {
        XMPPSettingsDAO dao = new XMPPSettingsDAO(registry);
        dao.updateXmppSettings(userId, xmppServer, xmppUserName, xmppUserCode, enabled,
                isPINEnabled);
    }

    /**
     * Checks whether the settings are enabled.
     *
     * @param userId
     * @return
     */
    public boolean isXmppSettingsEnabled(Registry registry, String userId) {
        XMPPSettingsDAO dao = new XMPPSettingsDAO(registry);
        return dao.isXmppSettingsEnabled(userId);
    }

    /**
     * Check whether the user has added the settings.
     *
     * @param userId
     * @return
     */
    public boolean hasXMPPSettings(Registry registry, String userId) {
        XMPPSettingsDAO dao = new XMPPSettingsDAO(registry);
        return dao.hasXmppSettings(userId);
    }

    /**
     * @param openId
     * @param userId
     * @return
     */
    public boolean doOpenIdSignUp(Registry registry, UserRealm realm, String openId, String userId) {
        OpenIDUserDO userDO = new OpenIDUserDO();
        userDO.setUserName(userId);
        userDO.setOpenID(openId);

        OpenIDUserDAO userDOA = new OpenIDUserDAO(registry, realm);
        return userDOA.addAssociation(userDO);
    }

    /**
     * @param openId
     * @return
     */
    public String getUserIdForOpenIDSignUp(Registry registry, UserRealm realm, String openId) {
        OpenIDUserDAO userDOA = new OpenIDUserDAO(registry, realm);
        return userDOA.getUserIdForAssociation(openId);
    }

    /**
     * @param openID
     * @return
     */
    public boolean hasSignedUpForOpenId(Registry registry, UserRealm realm, String openID) {
        OpenIDUserDAO userDOA = new OpenIDUserDAO(registry, realm);
        return userDOA.hasAssociation(openID);
    }

    /**
     * Get all OpenIDs for a given user
     *
     * @param username
     * @return
     */
    public String[] getOpenIDsForUser(Registry registry, UserRealm realm, String username) {
        OpenIDUserDAO openIDUserDOA = new OpenIDUserDAO(registry, realm);
        return openIDUserDOA.getOpenIDsForUser(username);
    }

    public void removeOpenIDSignUp(Registry registry, UserRealm realm, String openID) {
        OpenIDUserDAO userDOA = new OpenIDUserDAO(registry, realm);
        userDOA.removeOpenIDSignUp(openID);
    }

    /**
     * Add a relying party service provider for SAML SSO
     *
     * @param serviceProviderDO
     * @return
     * @throws IdentityException
     */
    public boolean addServiceProvider(Registry registry, SAMLSSOServiceProviderDO serviceProviderDO)
            throws IdentityException {
        SAMLSSOServiceProviderDAO serviceProviderDAO = new SAMLSSOServiceProviderDAO(registry);
        return serviceProviderDAO.addServiceProvider(serviceProviderDO);
    }

    /**
     * Get all the relying party service providers
     *
     * @return
     * @throws IdentityException
     */
    public SAMLSSOServiceProviderDO[] getServiceProviders(Registry registry)
            throws IdentityException {
        SAMLSSOServiceProviderDAO serviceProviderDOA = new SAMLSSOServiceProviderDAO(registry);
        return serviceProviderDOA.getServiceProviders();
    }

    public boolean removeServiceProvider(Registry registry, String issuer) throws IdentityException {
        SAMLSSOServiceProviderDAO serviceProviderDAO = new SAMLSSOServiceProviderDAO(registry);
        return serviceProviderDAO.removeServiceProvider(issuer);
    }

    public SAMLSSOServiceProviderDO getServiceProvider(Registry registry, String issuer)
            throws IdentityException {
        SAMLSSOServiceProviderDAO serviceProviderDAO = new SAMLSSOServiceProviderDAO(registry);
        return serviceProviderDAO.getServiceProvider(issuer);
    }

    public boolean isServiceProviderExists(Registry registry, String issuer) throws IdentityException {
        SAMLSSOServiceProviderDAO serviceProviderDAO = new SAMLSSOServiceProviderDAO(registry);
        return serviceProviderDAO.isServiceProviderExists(issuer);
    }

    public void createOrUpdateOpenIDAdmin(Registry registry, OpenIDAdminDO opAdmin)
            throws IdentityException {
        OpenIDAdminDAO opDAO = new OpenIDAdminDAO(registry);
        opDAO.createOrUpdate(opAdmin);
    }

    public OpenIDAdminDO getOpenIDAdmin(Registry registry) throws IdentityException {
        OpenIDAdminDAO opDAO = new OpenIDAdminDAO(registry);
        return opDAO.getOpenIDAdminDO();
    }
}