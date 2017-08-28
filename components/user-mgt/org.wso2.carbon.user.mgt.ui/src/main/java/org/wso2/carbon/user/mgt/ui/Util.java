/*
 * Copyright (c) 2010 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.user.mgt.ui;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.governance.stub.bean.ConnectorConfig;
import org.wso2.carbon.identity.governance.stub.bean.Property;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.mgt.common.DefaultPasswordGenerator;
import org.wso2.carbon.user.mgt.common.RandomPasswordGenerator;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.user.mgt.stub.types.carbon.UserRealmInfo;
import org.wso2.carbon.user.mgt.stub.types.carbon.UserStoreInfo;
import org.wso2.carbon.user.mgt.ui.client.IdentityGovernanceAdminClient;
import org.wso2.carbon.utils.DataPaginator;
import org.wso2.carbon.utils.ServerConstants;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Util {

    public static final String ALL = "ALL";
    private static final Log log = LogFactory.getLog(Util.class);
    private static final String EMAIL_VERIFICATION_ENABLE_PROP_NAME = "EmailVerification.Enable";
    private static final String ASK_PASSWORD_TEMP_PASSWORD_GENERATOR = "EmailVerification.AskPassword.PasswordGenerator";

    private static boolean isAskPasswordEnabled = true;

    static {
        InputStream is = null;
        try {
            boolean identityMgtListenerEnabled = true;
            IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty(
                    UserOperationEventListener.class.getName(), "org.wso2.carbon.identity.mgt.IdentityMgtEventListener");
            if (identityEventListenerConfig != null) {
                identityMgtListenerEnabled = Boolean.parseBoolean(identityEventListenerConfig.getEnable());
            }
            if (identityMgtListenerEnabled) {
                File file = new File(IdentityUtil.getIdentityConfigDirPath() + "/identity-mgt.properties");
                if (file.exists() && file.isFile()) {
                    is = new FileInputStream(file);
                    Properties identityMgtProperties = new Properties();
                    identityMgtProperties.load(is);
                    boolean tempPasswordEnabled = Boolean.parseBoolean(identityMgtProperties.getProperty("Temporary" +
                            ".Password.Enable"));
                    boolean acctVerificationEnabled = Boolean.parseBoolean(identityMgtProperties.getProperty
                            ("UserAccount.Verification.Enable"));
                    if (!tempPasswordEnabled || !acctVerificationEnabled) {
                        isAskPasswordEnabled = false;
                    }
                } else {
                    isAskPasswordEnabled = false;
                }
            } else {
                isAskPasswordEnabled = false;
            }
        } catch (FileNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("identity-mgt.properties file not found in " + IdentityUtil.getIdentityConfigDirPath());
            }
            isAskPasswordEnabled = false;
        } catch (IOException e) {
            log.error("Error while reading identity-mgt.properties file");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error("Error occurred while closing the FileInputStream for identity-mgt.properties");
                }
            }
        }
    }

    private Util() {

    }

    public static String getDN(String roleName, int index, FlaggedName[] names) {
        if (names != null && names.length > index) {
            return names[index].getDn();
        }
        return null;
    }

    public static UserStoreInfo getUserStoreInfo(String domainName, UserRealmInfo realmInfo) {

        for (UserStoreInfo info : realmInfo.getUserStoresInfo()) {
            if (domainName != null && domainName.equalsIgnoreCase(info.getDomainName())) {
                return info;
            }
        }

        return null;
    }

    public static UserStoreInfo getUserStoreInfoForUser(String userName, UserRealmInfo realmInfo) {

        if (userName.contains("/")) {
            String domainName = userName.substring(0, userName.indexOf("/"));
            for (UserStoreInfo info : realmInfo.getUserStoresInfo()) {
                if (domainName != null && domainName.equalsIgnoreCase(info.getDomainName())) {
                    return info;
                }
            }
        }

        return realmInfo.getPrimaryUserStoreInfo();
    }

    public static DataHandler buildDataHandler(byte[] content) {
        DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(content,
                "application/octet-stream"));
        return dataHandler;
    }

    public static PaginatedNamesBean retrievePaginatedFlaggedName(int pageNumber, String[] names) {

        List<FlaggedName> list = new ArrayList<>();
        FlaggedName flaggedName;
        for (String name : names) {
            flaggedName = new FlaggedName();
            flaggedName.setItemName(name);
            list.add(flaggedName);
        }
        return retrievePaginatedFlaggedName(pageNumber, list);
    }

    public static PaginatedNamesBean retrievePaginatedFlaggedName(int pageNumber, List<FlaggedName> names) {

        PaginatedNamesBean bean = new PaginatedNamesBean();
        DataPaginator.doPaging(pageNumber, names, bean);
        return bean;
    }

    public static void updateCheckboxStateMap(Map<String, Boolean> checkBoxMap, Map<Integer, PaginatedNamesBean> flaggedNamesMap,
                                              String selectedBoxesStr, String unselectedBoxesStr, String regex) {
        if (selectedBoxesStr != null || unselectedBoxesStr != null) {
            if (selectedBoxesStr != null && ALL.equals(selectedBoxesStr) || unselectedBoxesStr != null && ALL.equals(unselectedBoxesStr)) {
                if (selectedBoxesStr != null && ALL.equals(selectedBoxesStr) && flaggedNamesMap != null) {
                    for (Map.Entry<Integer, PaginatedNamesBean> entry : flaggedNamesMap.entrySet()) {
                        FlaggedName[] flaggedNames = entry.getValue().getNames();
                        for (FlaggedName flaggedName : flaggedNames) {
                            if (flaggedName.getEditable()) {
                                checkBoxMap.put(flaggedName.getItemName(), true);
                            }
                        }
                    }

                }
                if (unselectedBoxesStr != null && ALL.equals(unselectedBoxesStr) && flaggedNamesMap != null) {
                    for (Map.Entry<Integer, PaginatedNamesBean> entry : flaggedNamesMap.entrySet()) {
                        FlaggedName[] flaggedNames = entry.getValue().getNames();
                        for (FlaggedName flaggedName : flaggedNames) {
                            if (flaggedName.getEditable()) {
                                checkBoxMap.put(flaggedName.getItemName(), false);
                            }
                        }
                    }
                }
                return;
            }
            if (selectedBoxesStr != null && !"".equals(selectedBoxesStr)) {
                String[] selectedBoxes = selectedBoxesStr.split(regex);
                for (String selectedBox : selectedBoxes) {
                    checkBoxMap.put(selectedBox, true);
                }
            }
            if (unselectedBoxesStr != null && !"".equals(unselectedBoxesStr)) {
                String[] unselectedBoxes = unselectedBoxesStr.split(regex);
                for (String unselectedBox : unselectedBoxes) {
                    checkBoxMap.put(unselectedBox, false);
                }
            }
        }
    }

    public static boolean isAskPasswordEnabled() {
        return isAskPasswordEnabled;
    }

    public static boolean isUserOnBoardingEnabled(ServletContext context, HttpSession session) {

        String backendServerURL = CarbonUIUtil.getServerURL(context, session);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        ConfigurationContext configContext =
                (ConfigurationContext) context.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        Map<String, Map<String, List<ConnectorConfig>>> connectorList;
        try {
            IdentityGovernanceAdminClient client =
                    new IdentityGovernanceAdminClient(cookie, backendServerURL, configContext);
            connectorList = client.getConnectorList();
        } catch (Exception e) {
            log.error("Error while getting connector list from governance service, at URL :" +
                    backendServerURL, e);
            return false;
        }

        if (connectorList != null) {
            for (Map.Entry<String, Map<String, List<ConnectorConfig>>> entry : connectorList.entrySet()) {
                Map<String, List<ConnectorConfig>> subCatList = entry.getValue();
                for (String subCatKey : subCatList.keySet()) {
                    List<ConnectorConfig> connectorConfigs = subCatList.get(subCatKey);
                    for (ConnectorConfig connectorConfig : connectorConfigs) {
                        Property[] properties = connectorConfig.getProperties();
                        for (Property property : properties) {
                            if (EMAIL_VERIFICATION_ENABLE_PROP_NAME.equals(property.getName())) {
                                String propValue = property.getValue();
                                boolean isEmailVerificationEnabled = false;

                                if (!StringUtils.isEmpty(propValue)) {
                                    isEmailVerificationEnabled = Boolean.parseBoolean(propValue);
                                }
                                return isEmailVerificationEnabled;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static char[] generateRandomPassword(ServletContext context, HttpSession session) {
        char[] tempPass = "password".toCharArray();
        try {
            return getAskPasswordTempPassGenerator(context, session).generatePassword();
        } catch (Exception e) {
            log.error("Error while generating the temporary password. Used the default password as temp password", e);
            return tempPass;
        }
    }

    public static RandomPasswordGenerator getAskPasswordTempPassGenerator(ServletContext context, HttpSession session) {

        String randomPasswordGenerationClass = "org.wso2.carbon.user.mgt.common.DefaultPasswordGenerator";


        if (isUserOnBoardingEnabled(context, session)) {

            String backendServerURL = CarbonUIUtil.getServerURL(context, session);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            ConfigurationContext configContext =
                    (ConfigurationContext) context.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            Map<String, Map<String, List<ConnectorConfig>>> connectorList;
            try {
                IdentityGovernanceAdminClient client =
                        new IdentityGovernanceAdminClient(cookie, backendServerURL, configContext);
                connectorList = client.getConnectorList();

                if (connectorList != null) {
                    for (Map.Entry<String, Map<String, List<ConnectorConfig>>> entry : connectorList.entrySet()) {
                        Map<String, List<ConnectorConfig>> subCatList = entry.getValue();
                        for (String subCatKey : subCatList.keySet()) {
                            List<ConnectorConfig> connectorConfigs = subCatList.get(subCatKey);
                            for (ConnectorConfig connectorConfig : connectorConfigs) {
                                Property[] properties = connectorConfig.getProperties();
                                for (Property property : properties) {
                                    if (ASK_PASSWORD_TEMP_PASSWORD_GENERATOR.equals(property.getName())) {
                                        randomPasswordGenerationClass = property.getValue();
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error while getting connector list from governance service, at URL :" +
                        backendServerURL, e);
            }

        } else {
            String randomPasswordGenerationClassFromFile =
                    IdentityUtil.getProperty(ASK_PASSWORD_TEMP_PASSWORD_GENERATOR);
            if (StringUtils.isNotBlank(randomPasswordGenerationClassFromFile)) {
                randomPasswordGenerationClass = randomPasswordGenerationClassFromFile;
            }
        }

        try {
            Class clazz = Class.forName(randomPasswordGenerationClass);
            return (RandomPasswordGenerator) clazz.newInstance();
        } catch (Exception e) {
            log.error("Error while loading random password generator class. " +
                    "Default random password generator would be used", e);
        }

        return new DefaultPasswordGenerator();
    }
}
