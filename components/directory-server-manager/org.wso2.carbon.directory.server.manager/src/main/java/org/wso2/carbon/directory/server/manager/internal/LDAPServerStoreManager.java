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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.directory.server.manager.internal;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.directory.server.manager.DirectoryServerManagerException;
import org.wso2.carbon.directory.server.manager.common.ServerPrinciple;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.ldap.LDAPConnectionContext;
import org.wso2.carbon.user.core.ldap.LDAPConstants;
import org.wso2.carbon.user.core.util.JNDIUtil;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LDAP implementation of server store. Provide functionality add, update, delete and list
 * service principles.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class LDAPServerStoreManager {

    private static final Log log = LogFactory.getLog(LDAPServerStoreManager.class);

    private LDAPConnectionContext connectionSource;
    private RealmConfiguration realmConfiguration;

    public LDAPServerStoreManager(RealmConfiguration realmConfig) {

        this.realmConfiguration = realmConfig;
        try {
            this.connectionSource = new LDAPConnectionContext(realmConfig);
        } catch (UserStoreException e) {
            log.error("Error occurred while instantiating LDAPConnectionContext", e);
        }
    }

    protected boolean isServerNameValid(String serverName) {

        String serviceNamePolicyRegEx = this.realmConfiguration.getUserStoreProperty
                (LDAPServerManagerConstants.SERVICE_PRINCIPLE_NAME_REGEX_PROPERTY);

        if (serviceNamePolicyRegEx == null) {
            serviceNamePolicyRegEx = LDAPServerManagerConstants.DEFAULT_BE_SERVICE_NAME_REGULAR_EXPRESSION;
        }

        log.info("Using service name format - " + serviceNamePolicyRegEx);

        return isFormatCorrect(serviceNamePolicyRegEx, serverName);

    }

    protected boolean isPasswordValid(String password) {

        String regularExpression = this.realmConfiguration.getUserStoreProperty
                (LDAPServerManagerConstants.SERVICE_PASSWORD_REGEX_PROPERTY);

        if (regularExpression == null) {
            regularExpression = LDAPServerManagerConstants.DEFAULT_BE_PASSWORD_REGULAR_EXPRESSION;
        }

        log.info("Using service password format - " + regularExpression);

        return StringUtils.isNotEmpty(password) && isFormatCorrect(regularExpression, password);

    }

    private boolean isFormatCorrect(String regularExpression, String attribute) {

        Pattern p = Pattern.compile(regularExpression);
        Matcher m = p.matcher(attribute);
        return m.matches();

    }

    public String getServiceName(String serverName)
            throws DirectoryServerManagerException {

        String[] components = serverName.split("/");
        if (components.length != 2) {
            throw new DirectoryServerManagerException("Invalid server name provided. " +
                                                      "Could not retrieve service component.");
        }

        // Check whether there is a uid by that name
        if (isExistingServiceUid(components[0])) {
            return getUniqueServiceUid(serverName);
        }

        return components[0];

    }

    protected String getUniqueServiceUid(String serviceName) {
        String[] parts = serviceName.split("/");

        if (parts.length == 1) {
            return parts[0];
        }

        StringBuilder uniqueId = new StringBuilder(parts[0]);
        String[] domainParts = parts[1].split("\\.");

        for (String domainPart : domainParts) {
            uniqueId.append("-").append(domainPart);
        }

        return uniqueId.toString();
    }

    protected String getServerPrincipleExcludeString() {
        return getServiceFilteringExpression(true);
    }

    protected String getServerPrincipleIncludeString() {
        return getServiceFilteringExpression(false);
    }

    private String getServiceFilteringExpression(boolean excludeServer) {

        if (excludeServer) {
            return "(!(" + LDAPServerManagerConstants.SERVER_PRINCIPAL_ATTRIBUTE_NAME + "=" +
                   LDAPServerManagerConstants.SERVER_PRINCIPAL_ATTRIBUTE_VALUE + "))";
        } else {
            return "(" + LDAPServerManagerConstants.SERVER_PRINCIPAL_ATTRIBUTE_NAME + "=" +
                   LDAPServerManagerConstants.SERVER_PRINCIPAL_ATTRIBUTE_VALUE + ")";
        }

    }

    public boolean isExistingServiceUid(String uid)
            throws DirectoryServerManagerException {

        DirContext dirContext;
        try {
            dirContext = this.connectionSource.getContext();
        } catch (UserStoreException e) {
            log.error("Unable to retrieve directory context.", e);
            throw new DirectoryServerManagerException("Unable to retrieve directory context.", e);
        }

        //first search the existing user entry.
        String searchBase = realmConfiguration.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);

        String filter =
                "(&(" + LDAPServerManagerConstants.LDAP_UID + "=" + uid + ")" + getServerPrincipleIncludeString() + ")";

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(new String[]{LDAPServerManagerConstants.LDAP_UID});

        try {
            NamingEnumeration<SearchResult> namingEnumeration = dirContext.search(searchBase, filter, searchControls);
            return namingEnumeration.hasMore();

        } catch (NamingException e) {
            log.error("Unable to check whether service exists in directory server. UID - " + uid, e);
            throw new DirectoryServerManagerException("Can not access the directory service", e);
        } finally {
            try {
                JNDIUtil.closeContext(dirContext);
            } catch (UserStoreException e) {
                log.error("Unable to close directory context.", e);
            }
        }
    }

    public boolean isExistingServicePrinciple(String servicePrinciple)
            throws DirectoryServerManagerException {

        DirContext dirContext;
        try {
            dirContext = this.connectionSource.getContext();
        } catch (UserStoreException e) {
            log.error("Unable to retrieve directory context.", e);
            throw new DirectoryServerManagerException("Unable to retrieve directory context.", e);
        }

        //first search the existing user entry.
        String searchBase = realmConfiguration.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);

        String filter = getServicePrincipleFilter(servicePrinciple);

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(new String[]{LDAPServerManagerConstants.LDAP_UID});

        try {
            NamingEnumeration<SearchResult> namingEnumeration = dirContext.search(searchBase, filter, searchControls);
            return namingEnumeration.hasMore();

        } catch (NamingException e) {
            String message = "Unable to search entry with search base " + searchBase + ", filter -" + filter;
            log.error(message, e);
            throw new DirectoryServerManagerException("Can not access the directory service", e);
        } finally {
            try {
                JNDIUtil.closeContext(dirContext);
            } catch (UserStoreException e) {
                log.error("Unable to close directory context.", e);
            }
        }
    }

    public void addServicePrinciple(String serverName, String serverDescription, Object credentials)
            throws DirectoryServerManagerException {

        if (!(credentials instanceof String)) {
            throw new DirectoryServerManagerException("Invalid credentials provided");
        }

        DirContext dirContext;
        try {
            dirContext = this.connectionSource.getContext();
        } catch (UserStoreException e) {
            throw new DirectoryServerManagerException("An error occurred while retrieving LDAP connection context.", e);
        }

        String searchBase = this.realmConfiguration.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        try {

            dirContext = (DirContext) dirContext.lookup(searchBase);

            BasicAttributes basicAttributes = new BasicAttributes(true);

            // Put only service name as uid. i.e. if server name is like ftp/wso2.example.com
            // then add only ftp as uid
            String serverUid = getServiceName(serverName);

            constructBasicAttributes(basicAttributes, serverUid, serverName, credentials, serverDescription,
                                     LDAPServerManagerConstants.SERVER_PRINCIPAL_ATTRIBUTE_VALUE);

            dirContext.bind(LDAPServerManagerConstants.LDAP_UID + "=" + serverUid, null, basicAttributes);

        } catch (NamingException e) {
            String message = "Can not access the directory context or user " +
                             "already exists in the system";
            log.error(message, e);
            throw new DirectoryServerManagerException(message, e);
        } finally {
            try {
                JNDIUtil.closeContext(dirContext);
            } catch (UserStoreException e) {
                log.error("Unable to close directory context.", e);
            }
        }
    }

    private void constructBasicAttributes(BasicAttributes basicAttributes, String id, String principleName,
                                          Object credential, String commonName, String surName)
            throws DirectoryServerManagerException {

        // set the objectClass type for schema
        BasicAttribute objectClass = new BasicAttribute(LDAPServerManagerConstants.LDAP_OBJECT_CLASS);
        objectClass.add(LDAPServerManagerConstants.LDAP_INTET_ORG_PERSON);
        objectClass.add(LDAPServerManagerConstants.LDAP_ORG_PERSON);
        objectClass.add(LDAPServerManagerConstants.LDAP_PERSON);
        objectClass.add(LDAPServerManagerConstants.LDAP_TOP);

        // Add Kerberos specific object classes
        objectClass.add(LDAPServerManagerConstants.LDAP_KRB5_PRINCIPLE);
        objectClass.add(LDAPServerManagerConstants.LDAP_KRB5_KDC);
        objectClass.add(LDAPServerManagerConstants.LDAP_SUB_SCHEMA);

        basicAttributes.put(objectClass);

        BasicAttribute uid = new BasicAttribute(LDAPServerManagerConstants.LDAP_UID);
        uid.add(id);
        basicAttributes.put(uid);

        String principal = getFullyQualifiedPrincipalName(principleName);

        BasicAttribute principalAttribute = new BasicAttribute
                (LDAPServerManagerConstants.KRB5_PRINCIPAL_NAME_ATTRIBUTE);
        principalAttribute.add(principal);
        basicAttributes.put(principalAttribute);

        BasicAttribute versionNumberAttribute = new BasicAttribute
                (LDAPServerManagerConstants.KRB5_KEY_VERSION_NUMBER_ATTRIBUTE);
        versionNumberAttribute.add("0");
        basicAttributes.put(versionNumberAttribute);

        BasicAttribute userPassword = new BasicAttribute(LDAPServerManagerConstants.LDAP_PASSWORD);

        //Since we are using the KDC, we will always use plain text password.
        //KDC does not support other types of passwords
        String password = getPasswordToStore((String) credential,
                                             LDAPServerManagerConstants.PASSWORD_HASH_METHOD_PLAIN_TEXT);

        userPassword.add(password.getBytes());
        basicAttributes.put(userPassword);

        if (commonName == null || commonName.isEmpty()) {
            commonName = principleName + " Service";
        }

        BasicAttribute cn = new BasicAttribute(LDAPServerManagerConstants.LDAP_COMMON_NAME);
        cn.add(commonName);
        basicAttributes.put(cn);

        BasicAttribute sn = new BasicAttribute(LDAPServerManagerConstants.SERVER_PRINCIPAL_ATTRIBUTE_NAME);
        sn.add(surName);
        basicAttributes.put(sn);
    }

    public ServerPrinciple[] listServicePrinciples(String filter)
            throws DirectoryServerManagerException {

        ServerPrinciple[] serverNames = null;

        int maxItemLimit = Integer.parseInt(this.realmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig
                                                                                                 .PROPERTY_MAX_USER_LIST));

        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtls.setCountLimit(maxItemLimit);

        if (filter.contains("?") || filter.contains("**")) {
            log.error("Invalid search character " + filter);
            throw new DirectoryServerManagerException(
                    "Invalid character sequence entered for service principle search. Please enter valid sequence.");
        }

        StringBuilder searchFilter;
        searchFilter = new StringBuilder(this.realmConfiguration.getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER));
        String searchBase = this.realmConfiguration.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);

        StringBuilder buff = new StringBuilder();
        buff.append("(&").append(searchFilter).append("(")
                .append(LDAPServerManagerConstants.KRB5_PRINCIPAL_NAME_ATTRIBUTE).append("=")
                .append(filter).append(")").append(getServerPrincipleIncludeString()).append(")");

        String[] returnedAtts = {LDAPServerManagerConstants.KRB5_PRINCIPAL_NAME_ATTRIBUTE,
                                 LDAPServerManagerConstants.LDAP_COMMON_NAME};
        searchCtls.setReturningAttributes(returnedAtts);
        DirContext dirContext = null;
        try {
            dirContext = connectionSource.getContext();
            NamingEnumeration<SearchResult> answer = dirContext.search(searchBase, buff.toString(),
                                                                       searchCtls);
            List<ServerPrinciple> list = new ArrayList<ServerPrinciple>();
            int i = 0;
            while (answer.hasMoreElements() && i < maxItemLimit) {
                SearchResult sr = answer.next();
                if (sr.getAttributes() != null) {
                    Attribute serverNameAttribute = sr.getAttributes()
                            .get(LDAPServerManagerConstants.KRB5_PRINCIPAL_NAME_ATTRIBUTE);
                    Attribute serverDescription = sr.getAttributes().get(LDAPServerManagerConstants.LDAP_COMMON_NAME);
                    if (serverNameAttribute != null) {

                        ServerPrinciple principle;
                        String serviceName;
                        String serverPrincipleFullName = (String) serverNameAttribute.get();

                        if (serverPrincipleFullName.toLowerCase(Locale.ENGLISH)
                                .contains(LDAPServerManagerConstants.KERBEROS_TGT)) {
                            continue;
                        }

                        if (serverPrincipleFullName.contains("@")) {
                            serviceName = serverPrincipleFullName.split("@")[0];
                        } else {
                            serviceName = serverPrincipleFullName;
                        }

                        if (serverDescription != null) {
                            principle = new ServerPrinciple(serviceName,
                                                            (String) serverDescription.get());
                        } else {

                            principle = new ServerPrinciple(serviceName);
                        }

                        list.add(principle);
                        i++;
                    }
                }
            }

            serverNames = list.toArray(new ServerPrinciple[list.size()]);
            Arrays.sort(serverNames);

        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new DirectoryServerManagerException("Unable to list service principles.", e);
        } catch (UserStoreException e) {
            log.error("Unable to retrieve LDAP connection context.", e);
            throw new DirectoryServerManagerException("Unable to list service principles.", e);
        } finally {
            try {
                JNDIUtil.closeContext(dirContext);
            } catch (UserStoreException e) {
                log.error("Unable to close directory context.", e);
            }
        }
        return serverNames;

    }

    private String getFullyQualifiedPrincipalName(String principleName) {

        String defaultRealmName = getRealmName();
        return principleName.toLowerCase(Locale.US) + "@" + defaultRealmName.toUpperCase(Locale.ENGLISH);
    }

    private String getPasswordToStore(String password, String passwordHashMethod)
            throws DirectoryServerManagerException {

        String passwordToStore = password;

        if (passwordHashMethod != null) {
            try {

                if (passwordHashMethod.equals(LDAPServerManagerConstants.PASSWORD_HASH_METHOD_PLAIN_TEXT)) {
                    return passwordToStore;
                }

                MessageDigest messageDigest = MessageDigest.getInstance(passwordHashMethod);
                byte[] digestValue = messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));
                passwordToStore = "{" + passwordHashMethod + "}" + Base64.encode(digestValue);

            } catch (NoSuchAlgorithmException e) {
                throw new DirectoryServerManagerException("Invalid hashMethod", e);
            }
        }

        return passwordToStore;
    }

    private String getServicePrincipleFilter(String servicePrincipleName) {

        String serverNameInRealm = getFullyQualifiedPrincipalName(
                LDAPServerStoreManagerUtil.escapeSpecialCharactersForFilter(servicePrincipleName));
        return "(&(" + LDAPServerManagerConstants.KRB5_PRINCIPAL_NAME_ATTRIBUTE + "=" + serverNameInRealm + ")" +
               getServerPrincipleIncludeString() + ")";
    }

    private Attribute getChangePasswordAttribute(Attribute oldPasswordAttribute, Object oldCredential,
                                                 Object newPassword)
            throws DirectoryServerManagerException {

        String passwordHashMethod = null;
        // when admin changes other user passwords he do not have to provide
        // the old password.
        if (oldCredential != null) {
            // here it is only possible to have one password, if there are more
            // every one should match with the given old password

            try {
                NamingEnumeration passwords = oldPasswordAttribute.getAll();

                if (passwords.hasMore()) {
                    byte[] byteArray = (byte[]) passwords.next();
                    String password = new String(byteArray, StandardCharsets.UTF_8);

                    if (password.startsWith("{")) {
                        passwordHashMethod = password.substring(password.indexOf("{") + 1, password.indexOf("}"));
                    }

                    if (!password.equals(getPasswordToStore((String) oldCredential, passwordHashMethod))) {
                        throw new DirectoryServerManagerException("Old password does not match");
                    }
                }
            } catch (NamingException e) {
                log.error("Unable to retrieve old password details.", e);
                throw new DirectoryServerManagerException("Could not find old password details");
            }
        }

        Attribute passwordAttribute = new BasicAttribute(LDAPServerManagerConstants.LDAP_PASSWORD);
        passwordAttribute.add(getPasswordToStore((String) newPassword, passwordHashMethod));

        return passwordAttribute;

    }

    public void updateServicePrinciplePassword(String serverName, Object oldCredential, Object newCredentials)
            throws DirectoryServerManagerException {

        DirContext dirContext;

        try {
            dirContext = this.connectionSource.getContext();
        } catch (UserStoreException e) {
            throw new DirectoryServerManagerException("Unable to retrieve directory connection.", e);
        }

        //first search the existing user entry.
        String searchBase = this.realmConfiguration.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        String searchFilter = getServicePrincipleFilter(serverName);

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(new String[]{LDAPServerManagerConstants.LDAP_PASSWORD});

        try {
            NamingEnumeration<SearchResult> namingEnumeration = dirContext
                    .search(searchBase, searchFilter, searchControls);
            // here we assume only one user
            while (namingEnumeration.hasMore()) {

                BasicAttributes basicAttributes = new BasicAttributes(true);

                SearchResult searchResult = namingEnumeration.next();
                Attributes attributes = searchResult.getAttributes();

                Attribute userPassword = attributes.get(LDAPServerManagerConstants.LDAP_PASSWORD);
                Attribute newPasswordAttribute =
                        getChangePasswordAttribute(userPassword, oldCredential, newCredentials);
                basicAttributes.put(newPasswordAttribute);

                String dnName = searchResult.getName();
                dirContext = (DirContext) dirContext.lookup(searchBase);

                dirContext.modifyAttributes(dnName, DirContext.REPLACE_ATTRIBUTE, basicAttributes);
            }

        } catch (NamingException e) {
            log.error("Unable to update server principle password details. Server name - " + serverName);
            throw new DirectoryServerManagerException("Can not access the directory service", e);
        } finally {
            try {
                JNDIUtil.closeContext(dirContext);
            } catch (UserStoreException e) {
                log.error("Unable to close directory context.", e);
            }
        }
    }

    public ServerPrinciple getServicePrinciple(String serverName) throws DirectoryServerManagerException {

        DirContext dirContext;

        try {
            dirContext = this.connectionSource.getContext();
        } catch (UserStoreException e) {
            throw new DirectoryServerManagerException("Unable to retrieve directory connection.", e);
        }

        //first search the existing user entry.
        String searchBase = this.realmConfiguration.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        String searchFilter = getServicePrincipleFilter(serverName);

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(new String[]{LDAPServerManagerConstants.LDAP_PASSWORD,
                LDAPServerManagerConstants.LDAP_COMMON_NAME});

        try {
            NamingEnumeration<SearchResult> namingEnumeration = dirContext.search(searchBase, searchFilter,
                    searchControls);
            // here we assume only
            while (namingEnumeration.hasMore()) {

                BasicAttributes basicAttributes = new BasicAttributes(true);

                SearchResult searchResult = namingEnumeration.next();
                Attributes attributes = searchResult.getAttributes();

                String userPassword = (String)attributes.get(LDAPServerManagerConstants.LDAP_PASSWORD).get();
                String description = (String)attributes.get(LDAPServerManagerConstants.LDAP_COMMON_NAME).get();

                return new ServerPrinciple(serverName, description, userPassword);
            }
            return null;
        } catch (NamingException e) {
            throw new DirectoryServerManagerException("Can not access the directory service", e);
        } finally {
            try {
                JNDIUtil.closeContext(dirContext);
            } catch (UserStoreException e) {
                log.error("Unable to close directory context.", e);
            }
        }
    }

    public boolean isValidPassword(String serverName, Object existingCredentials)
            throws DirectoryServerManagerException {

        DirContext dirContext;
        try {
            dirContext = this.connectionSource.getContext();
        } catch (UserStoreException e) {
            throw new DirectoryServerManagerException("Unable to retrieve directory connection.", e);
        }

        //first search the existing user entry.
        String searchBase = this.realmConfiguration.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        String searchFilter = getServicePrincipleFilter(serverName);

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(new String[]{LDAPServerManagerConstants.LDAP_PASSWORD});

        try {
            NamingEnumeration<SearchResult> namingEnumeration = dirContext
                    .search(searchBase, searchFilter, searchControls);
            // here we assume only one user
            while (namingEnumeration.hasMore()) {

                SearchResult searchResult = namingEnumeration.next();
                Attributes attributes = searchResult.getAttributes();

                Attribute userPassword = attributes.get(LDAPServerManagerConstants.LDAP_PASSWORD);

                NamingEnumeration passwords = userPassword.getAll();

                String passwordHashMethod = null;
                if (passwords.hasMore()) {
                    byte[] byteArray = (byte[]) passwords.next();
                    String password = new String(byteArray, StandardCharsets.UTF_8);

                    if (password.startsWith("{")) {
                        passwordHashMethod = password.substring(password.indexOf("{") + 1, password.indexOf("}"));
                    }

                    return password.equals(getPasswordToStore((String) existingCredentials, passwordHashMethod));
                }
            }

        } catch (NamingException e) {
            log.error("Failed, validating password. Can not access the directory service", e);
            throw new DirectoryServerManagerException("Failed, validating password. " +
                                                      "Can not access the directory service", e);
        } finally {
            try {
                JNDIUtil.closeContext(dirContext);
            } catch (UserStoreException e) {
                log.error("Unable to close directory context.", e);
            }
        }

        return false;
    }

    private String lookupUserId(String serverName) throws DirectoryServerManagerException {

        DirContext dirContext;
        try {
            dirContext = this.connectionSource.getContext();
        } catch (UserStoreException e) {
            throw new DirectoryServerManagerException("Unable to retrieve directory connection.", e);
        }

        String searchBase = this.realmConfiguration.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);

        //first search the existing user entry.
        String searchFilter = getServicePrincipleFilter(serverName);

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(new String[]{"uid"});
        try {
            NamingEnumeration<SearchResult> namingEnumeration = dirContext.
                    search(searchBase, searchFilter, searchControls);

            // here we assume only one user
            if (namingEnumeration.hasMore()) {

                SearchResult searchResult;

                searchResult = namingEnumeration.next();

                Attributes attributes = searchResult.getAttributes();

                Attribute userId = attributes.get("uid");
                return (String) userId.get();
            } else {
                return null;
            }

        } catch (NamingException e) {
            log.error("Could not find user id for given server " + serverName, e);
            throw new DirectoryServerManagerException("Could not find user id for given server " + serverName, e);
        } finally {
            try {
                JNDIUtil.closeContext(dirContext);
            } catch (UserStoreException e) {
                log.error("Unable to close directory context.", e);
            }
        }

    }

    public void deleteServicePrinciple(String serverName)
            throws DirectoryServerManagerException {

        DirContext dirContext;
        try {
            dirContext = this.connectionSource.getContext();
        } catch (UserStoreException e) {
            throw new DirectoryServerManagerException("Unable to retrieve directory connection.", e);
        }

        String searchBase = this.realmConfiguration.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);

        String userId = lookupUserId(serverName);

        if (userId == null) {
            throw new DirectoryServerManagerException("Could not find user id for given server principle " +
                                                      serverName);
        }

        try {
            dirContext = (DirContext) dirContext.lookup(searchBase);
            dirContext.unbind("uid=" + userId);

        } catch (NamingException e) {
            log.error("Could not remove service principle " + serverName, e);
            throw new DirectoryServerManagerException("Could not remove service principle " + serverName, e);
        } finally {
            try {
                JNDIUtil.closeContext(dirContext);
            } catch (UserStoreException e) {
                log.error("Unable to close directory context.", e);
            }
        }

    }

    private String getRealmName() {

        // First check whether realm name is defined in the configuration
        String defaultRealmName = this.realmConfiguration
                .getUserStoreProperty(UserCoreConstants.RealmConfig.DEFAULT_REALM_NAME);

        if (defaultRealmName != null) {
            return defaultRealmName;
        }

        // If not build the realm name from the search base.
        // Here the realm name will be a concatenation of dc components in the search base.
        String searchBase = this.realmConfiguration.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);

        String[] domainComponents = searchBase.split("dc=");

        StringBuilder builder = new StringBuilder();

        for (String dc : domainComponents) {
            if (!dc.contains("=")) {
                String trimmedDc = dc.trim();
                if (trimmedDc.endsWith(",")) {
                    builder.append(trimmedDc.replace(',', '.'));
                } else {
                    builder.append(trimmedDc);
                }
            }
        }

        return builder.toString().toUpperCase(Locale.ENGLISH);
    }
}
