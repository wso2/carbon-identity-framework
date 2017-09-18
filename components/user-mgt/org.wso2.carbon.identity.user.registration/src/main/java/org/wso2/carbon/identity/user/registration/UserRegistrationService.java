/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.user.registration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.IdentityClaimManager;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.registration.dto.PasswordRegExDTO;
import org.wso2.carbon.identity.user.registration.dto.TenantRegistrationConfig;
import org.wso2.carbon.identity.user.registration.dto.UserDTO;
import org.wso2.carbon.identity.user.registration.dto.UserFieldDTO;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRegistrationService {

    private static final Log log = LogFactory.getLog(UserRegistrationService.class);

    /**
     * This service method will return back all available password validation regular expressions
     * against the corresponding domain names.
     *
     * @return
     * @throws IdentityException
     */
    public PasswordRegExDTO[] getPasswordRegularExpressions() throws IdentityException {
        UserRealm realm = null;
        realm = IdentityTenantUtil.getRealm(null, null);
        List<PasswordRegExDTO> passwordRegExList = new ArrayList<PasswordRegExDTO>();
        PasswordRegExDTO passwordRegEx;

        try {
            UserStoreManager manager = realm.getUserStoreManager();
            String domainName;
            String regEx;

            while (manager != null) {
                domainName = manager.getRealmConfiguration().getUserStoreProperty(
                        UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                regEx = manager.getRealmConfiguration().getUserStoreProperty(
                        UserCoreConstants.RealmConfig.PROPERTY_JS_REG_EX);
                if (regEx != null && regEx.length() > 0) {
                    passwordRegEx = new PasswordRegExDTO();
                    passwordRegEx.setDomainName(domainName);
                    passwordRegEx.setRegEx(regEx);
                    passwordRegExList.add(passwordRegEx);
                }
                manager = manager.getSecondaryUserStoreManager();
            }
        } catch (UserStoreException e) {
            log.error(e);
            throw IdentityException.error(
                    "Error occured while loading password validation regular expressions.");
        }
        return passwordRegExList.toArray(new PasswordRegExDTO[passwordRegExList.size()]);
    }

    public UserFieldDTO[] readUserFieldsForUserRegistration(String dialect)
            throws IdentityException {

        IdentityClaimManager claimManager = null;
        Claim[] claims = null;
        List<UserFieldDTO> claimList = null;
        UserRealm realm = null;

        claimManager = IdentityClaimManager.getInstance();
        realm = IdentityTenantUtil.getRealm(null, null);
        claims = claimManager.getAllSupportedClaims(dialect, realm);

        if (claims == null || claims.length == 0) {
            return new UserFieldDTO[0];
        }

        claimList = new ArrayList<UserFieldDTO>();

        for (Claim claim : claims) {
            if (claim.getDisplayTag() != null
                    && !IdentityConstants.PPID_DISPLAY_VALUE.equals(claim.getDisplayTag())) {
                if (UserCoreConstants.ClaimTypeURIs.ACCOUNT_STATUS.equals(claim.getClaimUri())) {
                    continue;
                }
                if (!claim.isReadOnly()) {
                    claimList.add(getUserFieldDTO(claim.getClaimUri(), claim.getDisplayTag(), claim.isRequired(),
                            claim.getDisplayOrder(), claim.getRegEx(), claim.isSupportedByDefault()));
                }
            }
        }
        return claimList.toArray(new UserFieldDTO[claimList.size()]);
    }

    public void addUser(UserDTO user) throws Exception {
        UserFieldDTO[] userFieldDTOs = null;
        Map<String, String> userClaims = null;

        userFieldDTOs = user.getUserFields();
        userClaims = new HashMap<String, String>();

        if (userFieldDTOs != null) {
            for (UserFieldDTO userFieldDTO : userFieldDTOs) {
                userClaims.put(userFieldDTO.getClaimUri(), userFieldDTO.getFieldValue());
            }
        }

        UserRealm realm = null;
        String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(user.getUserName());
        String tenantName = MultitenantUtils.getTenantDomain(user.getUserName());
        realm = IdentityTenantUtil.getRealm(tenantName, null);
        addUser(tenantAwareUserName, user.getPassword(), userClaims, null, realm);
    }

    public boolean isAddUserEnabled() throws Exception {

        UserRealm userRealm = IdentityTenantUtil.getRealm(null, null);
        if (userRealm != null) {
            UserStoreManager userStoreManager = userRealm.getUserStoreManager();
            if (userStoreManager != null) {
                return !userStoreManager.isReadOnly();
            }
        }
        return false;
    }

    public boolean isAddUserWithOpenIDEnabled() throws Exception {
        return false;
    }

    public boolean isAddUserWithInfoCardEnabled() throws Exception {
        return false;
    }

    /**
     * Check whether the user exist.
     * @param username Username of the user.
     * @return True if exist.
     * @throws Exception
     */
    public boolean isUserExist(String username) throws UserRegistrationException {

        try {
            return CarbonContext.getThreadLocalCarbonContext().getUserRealm().
                    getUserStoreManager().isExistingUser(username);
        } catch (UserStoreException e) {
            log.error("Unable to connect to the user store.", e);
            throw new UserRegistrationException("Internal error occurred while connecting to the user store.", e);
        }
    }

    private UserFieldDTO getUserFieldDTO(String claimUri, String displayName, boolean isRequired,
                                         int displayOrder, String regex, boolean isSupportedByDefault) {

        UserFieldDTO fieldDTO = null;
        fieldDTO = new UserFieldDTO();
        fieldDTO.setClaimUri(claimUri);
        fieldDTO.setFieldName(displayName);
        fieldDTO.setRequired(isRequired);
        fieldDTO.setDisplayOrder(displayOrder);
        fieldDTO.setSupportedByDefault(isSupportedByDefault);
        fieldDTO.setRegEx(regex);
        return fieldDTO;
    }

    private void addUser(String userName, String password, Map<String, String> claimList,
                         String profileName, UserRealm realm) throws IdentityException {
        UserStoreManager admin = null;
        Permission permission = null;
        try {
            // get config from tenant registry
            TenantRegistrationConfig tenantConfig = getTenantSignUpConfig(realm.getUserStoreManager().getTenantId());
            // set tenant config specific sign up domain
            if (tenantConfig != null && !"".equals(tenantConfig.getSignUpDomain())) {
                int index = userName.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
                if (index > 0) {
                    userName = tenantConfig.getSignUpDomain().toUpperCase() + UserCoreConstants.DOMAIN_SEPARATOR
                            + userName.substring(index + 1);
                } else {
                    userName = tenantConfig.getSignUpDomain().toUpperCase() + UserCoreConstants.DOMAIN_SEPARATOR
                            + userName;
                }
            }

            // add user to the relevant user store

            admin = realm.getUserStoreManager();
            if (!isUserNameWithAllowedDomainName(userName, realm)) {
                throw IdentityException.error("Domain does not permit self registration");
            }
            // add user
            admin.addUser(userName, password, null, claimList, profileName);

            // after adding the user, assign specif roles
            List<String> roleNamesArr = getRoleName(userName, tenantConfig);
            if (claimList.get(SelfRegistrationConstants.SIGN_UP_ROLE_CLAIM_URI) != null) {
                // check is a user role is specified as a claim by the client, if so add it to the roles list
                if (tenantConfig != null) {
                    roleNamesArr.add(tenantConfig.getSignUpDomain().toUpperCase()
                            + UserCoreConstants.DOMAIN_SEPARATOR
                            + claimList.get(SelfRegistrationConstants.SIGN_UP_ROLE_CLAIM_URI));
                } else {
                    roleNamesArr.add(UserCoreConstants.INTERNAL_DOMAIN
                            + UserCoreConstants.DOMAIN_SEPARATOR
                            + claimList.get(SelfRegistrationConstants.SIGN_UP_ROLE_CLAIM_URI));
                }
            }
            String[] identityRoleNames = roleNamesArr.toArray(new String[roleNamesArr.size()]);

            for (int i = 0; i < identityRoleNames.length; i++) {
                // if this is the first time a user signs up, needs to create role
                doAddUser(i, admin, identityRoleNames, userName, permission);
            }
        } catch (UserStoreException e) {
            throw IdentityException.error("Error occurred while adding user : " + userName + ". " + e.getMessage(), e);
        }
    }

    private void doAddUser(int i, UserStoreManager admin, String[] identityRoleNames, String userName,
                           Permission permission) throws IdentityException, UserStoreException {
        try {
            if (!admin.isExistingRole(identityRoleNames[i], false)) {
                permission = new Permission("/permission/admin/login", UserMgtConstants.EXECUTE_ACTION);
                admin.addRole(identityRoleNames[i], new String[]{userName}, new Permission[]{permission}, false);
            } else {
                // if role already exists, just add user to role
                admin.updateUserListOfRole(identityRoleNames[i], new String[]{}, new String[]{userName});
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            // If something goes wrong here - then remove the already added user.
            admin.deleteUser(userName);
            throw IdentityException.error("Error occurred while adding user : " + userName + ". " +
                    e.getMessage(), e);
        }
    }

    private boolean isUserNameWithAllowedDomainName(String userName, UserRealm realm)
            throws IdentityException {
        int index;
        index = userName.indexOf("/");

        // Check whether we have a secondary UserStoreManager setup.
        if (index > 0) {
            // Using the short-circuit. User name comes with the domain name.
            try {
                return !realm.getRealmConfiguration().isRestrictedDomainForSlefSignUp(
                        userName.substring(0, index));
            } catch (UserStoreException e) {
                throw IdentityException.error(e.getMessage(), e);
            }
        }
        return true;
    }

    private List<String> getRoleName(String userName, TenantRegistrationConfig tenantConfig) {
        // check for tenant config, if available return roles specified in tenant config
        if (tenantConfig != null) {
            List<String> roleNamesArr = new ArrayList<String>();
            Map<String, Boolean> roles = tenantConfig.getRoles();
            for (Map.Entry<String, Boolean> entry : roles.entrySet()) {
                String roleName;
                if (entry.getValue()) {
                    // external role
                    roleName = tenantConfig.getSignUpDomain().toUpperCase() + UserCoreConstants.DOMAIN_SEPARATOR +
                            entry.getKey();
                } else {
                    // internal role
                    roleName = UserCoreConstants.INTERNAL_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR + entry.getKey();
                }
                roleNamesArr.add(roleName);
            }
            // return, don't need to worry about roles specified in identity.xml
            return roleNamesArr;
        }

        String roleName = IdentityUtil.getProperty(SelfRegistrationConstants.ROLE_NAME_PROPERTY);
        boolean externalRole = Boolean.parseBoolean(IdentityUtil.getProperty(
                SelfRegistrationConstants.ROLE_EXTERNAL_PROPERTY));

        String domainName = UserCoreConstants.INTERNAL_DOMAIN;
        if (externalRole) {
            domainName = IdentityUtil.extractDomainFromName(userName);
        }

        if (roleName == null || roleName.trim().length() == 0) {
            roleName = IdentityConstants.IDENTITY_DEFAULT_ROLE;
        }

        if (domainName != null && domainName.trim().length() > 0) {
            roleName = domainName.toUpperCase() + CarbonConstants.DOMAIN_SEPARATOR + roleName;
        }
        return new ArrayList<String>(Arrays.asList(roleName));
    }

    private TenantRegistrationConfig getTenantSignUpConfig(int tenantId) throws IdentityException {
        TenantRegistrationConfig config;
        NodeList nodes;
        try {
            // start tenant flow to load tenant registry
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            Registry registry = (Registry) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .getRegistry(RegistryType.SYSTEM_GOVERNANCE);
            if (registry.resourceExists(SelfRegistrationConstants.SIGN_UP_CONFIG_REG_PATH)) {
                Resource resource = registry.get(SelfRegistrationConstants.SIGN_UP_CONFIG_REG_PATH);
                // build config from tenant registry resource
                DocumentBuilder builder = getSecuredDocumentBuilder();
                String configXml = new String((byte[]) resource.getContent());
                InputSource configInputSource = new InputSource();
                configInputSource.setCharacterStream(new StringReader(configXml.trim()));
                Document doc = builder.parse(configInputSource);
                nodes = doc.getElementsByTagName(SelfRegistrationConstants.SELF_SIGN_UP_ELEMENT);
                if (nodes.getLength() > 0) {
                    config = new TenantRegistrationConfig();
                    config.setSignUpDomain(((Element) nodes.item(0)).getElementsByTagName(SelfRegistrationConstants
                            .SIGN_UP_DOMAIN_ELEMENT)
                            .item(0).getTextContent());
                    // there can be more than one <SignUpRole> elements, iterate through all elements
                    NodeList rolesEl = ((Element) nodes.item(0))
                            .getElementsByTagName(SelfRegistrationConstants.SIGN_UP_ROLE_ELEMENT);
                    for (int i = 0; i < rolesEl.getLength(); i++) {
                        Element tmpEl = (Element) rolesEl.item(i);
                        String tmpRole = tmpEl.getElementsByTagName(SelfRegistrationConstants.ROLE_NAME_ELEMENT)
                                .item(0).getTextContent();
                        boolean tmpIsExternal = Boolean.parseBoolean(tmpEl.getElementsByTagName(
                                SelfRegistrationConstants.IS_EXTERNAL_ELEMENT).item(0).getTextContent());
                        config.getRoles().put(tmpRole, tmpIsExternal);
                    }
                    return config;
                } else {
                    return null;
                }
            }
        } catch (RegistryException e) {
            throw IdentityException.error("Error retrieving sign up config from registry " + e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            throw IdentityException.error("Error parsing tenant sign up configuration " + e.getMessage(), e);
        } catch (SAXException e) {
            throw IdentityException.error("Error parsing tenant sign up configuration " + e.getMessage(), e);
        } catch (IOException e) {
            throw IdentityException.error("Error parsing tenant sign up configuration " + e.getMessage(), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return null;
    }

    /**
     * * This method provides a secured document builder which will secure XXE attacks.
     *
     * @return DocumentBuilder
     * @throws ParserConfigurationException
     */
    private DocumentBuilder getSecuredDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = IdentityUtil.getSecuredDocumentBuilderFactory();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder;
    }
}
