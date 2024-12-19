/*
 * Copyright (c) 2007 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.user.mgt.bulkimport;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.user.mgt.bulkimport.util.JSONConverter;
import org.wso2.carbon.user.mgt.common.UserAdminException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to handle import users from a CSV file.
 */
public class CSVUserBulkImport extends UserBulkImport {

    private static final Log log = LogFactory.getLog(CSVUserBulkImport.class);
    private BufferedReader reader;
    private BulkImportConfig config;

    public CSVUserBulkImport(BulkImportConfig config) {
        this.config = config;
        this.reader = new BufferedReader(new InputStreamReader(config.getInStream(), Charset.forName("UTF-8")));
    }

    public void addUserList(UserStoreManager userStore) throws UserAdminException {
        CSVReader csvReader = new CSVReader(reader, ',', '"', 1);
        try {
            userStoreDomain = config.getUserStoreDomain();
            String[] line = csvReader.readNext();
            boolean isDuplicate = false;
            boolean fail = false;
            while (line != null && line.length > 0) {
                String userName = line[0];

                int index;
                index = userName.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
                if (index > 0) {
                    String domainFreeName = userName.substring(index + 1);
                    userName = UserCoreUtil.addDomainToName(domainFreeName, userStoreDomain);
                } else {
                    userName = UserCoreUtil.addDomainToName(userName, userStoreDomain);
                }

                if (StringUtils.isNotBlank(userName)) {
                    try {
                        if (!userStore.isExistingUser(userName)) {
                            if (line.length == 1) {
                                userStore.addUser(userName, null, null, null, null, true);
                                successCount++;
                            } else {
                                try {
                                    addUserWithClaims(userName, line, userStore);
                                    successCount++;
                                    if (log.isDebugEnabled()) {
                                        log.debug("User import successful - Username : " + userName);
                                    }
                                } catch (IllegalArgumentException e) {
                                    fail = true;
                                    failCount++;
                                    errorUsersMap.put(userName, e.getMessage());
                                    log.error("User import unsuccessful - Username : " + userName + " - Error: " +
                                            e.getMessage(), e);
                                }
                            }
                        } else {
                            isDuplicate = true;
                            duplicateCount++;
                            duplicateUsers.add(userName);
                            log.error("User import unsuccessful - Username : " + userName + " - Error: Duplicate user");
                        }
                    } catch (UserStoreException e) {
                        fail = true;
                        failCount++;
                        errorUsersMap.put(userName, e.getMessage());
                        log.error("User import unsuccessful - Username : " + userName + " - Error: " +
                                e.getMessage(), e);
                    }
                }
                line = csvReader.readNext();
            }

            InputStream inputStream = config.getInStream();
            inputStream.reset();
            JSONConverter jsonConverter = new JSONConverter();
            String usersImported = jsonConverter.csvToJSON(inputStream);
            String summaryLog = buildBulkImportSummary();

            auditLog.info(String.format(UserMgtConstants.AUDIT_LOG_FORMAT, tenantUser,
                    UserMgtConstants.OPERATION_NAME, userStoreDomain, usersImported, summaryLog));
            log.info(summaryLog);

            if (fail || isDuplicate) {
                throw new UserAdminException(String.format(UserMgtConstants.ERROR_MESSAGE, successCount, failCount,
                        duplicateCount));
            }
        } catch (IOException e) {
            throw new UserAdminException("Error occurred while adding user list", e);
        } finally {
            try {
                if (csvReader != null) {
                    csvReader.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                log.error("Error occurred while closing Reader", e);
            }
        }
    }

    /**
     * Method to handle adding users with claim values.
     *
     * @param username : The name of the importing user.
     * @param line : The line read from the CSV file.
     * @param userStore : The user store which the user should be imported to.
     * @throws UserStoreException : Throws when there is any error occurred while adding the user to user store.
     */
    private void addUserWithClaims(String username, String[] line, UserStoreManager userStore)
            throws UserStoreException {
        String roleString = null;
        String[] roles = null;
        String password = line[1];
        Map<String, String> claims = new HashMap<>();
        for (int i = 2; i < line.length; i++) {
            if (StringUtils.isNotBlank(line[i])) {
                String[] claimStrings = line[i].split("=");
                if (claimStrings.length != 2) {
                    throw new IllegalArgumentException("Claims and values are not in correct format");
                } else {
                    String claimURI = claimStrings[0];
                    String claimValue = claimStrings[1];
                    if (claimURI.contains("role")) {
                        roleString = claimValue;
                    } else {
                        if (!claimURI.isEmpty()) {
                            // Not trimming the claim values as we should not restrict the claim values not to have
                            // leading or trailing whitespaces.
                            claims.put(claimURI.trim(), claimValue);
                        }
                    }
                }
            }
        }

        if (StringUtils.isNotBlank(roleString)) {
            roles = roleString.split(":");
        }

        userStore.addUser(username, password, roles, claims, null, true);
    }
}
