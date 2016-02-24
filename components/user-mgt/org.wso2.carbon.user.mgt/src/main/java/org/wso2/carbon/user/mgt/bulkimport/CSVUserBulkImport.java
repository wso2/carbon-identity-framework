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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.common.UserAdminException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class CSVUserBulkImport {

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

            String domain = config.getUserStoreDomain();
            String[] line = csvReader.readNext();
            boolean isDuplicate = false;
            boolean fail = false;
            boolean success = false;
            String lastError = "UNKNOWN";
            int successCount = 0;
            int failCount = 0;
            int duplicateCount = 0;
            while (line != null && line.length > 0) {
                String userName = line[0];

                int index;
                index = userName.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
                if (index > 0) {
                    String domainFreeName = userName.substring(index + 1);
                    userName = UserCoreUtil.addDomainToName(domainFreeName, domain);
                } else {
                    userName = UserCoreUtil.addDomainToName(userName, domain);
                }

                if (userName != null && userName.trim().length() > 0) {
                    try {
                        if (!userStore.isExistingUser(userName)) {
                            if (line.length == 1) {
                                userStore.addUser(userName, null, null, null, null, true);
                                success = true;
                                successCount++;
                            } else {
                                try {
                                    addUserWithClaims(userName, line, userStore);
                                    success = true;
                                    successCount++;
                                    if (log.isDebugEnabled()){
                                        log.debug("User import successful - Username : " + userName);
                                    }
                                }catch (UserAdminException e){
                                    fail = true;
                                    failCount++;
                                    lastError = e.getMessage();
                                    log.error("User import unsuccessful - Username : " + userName + " - Error: " +
                                            e.getMessage());
                                }
                            }
                        } else {
                            isDuplicate = true;
                            duplicateCount++;
                            log.error("User import unsuccessful - Username : " + userName + " - Error: Duplicate user");
                        }
                    } catch (UserStoreException e) {
                        if (log.isDebugEnabled()) {
                            log.debug(e);
                        }
                        lastError = e.getMessage();
                        fail = true;
                        failCount++;
                        log.error("User import unsuccessful - Username : " + userName + " - Error: " + e.getMessage());
                    }
                }
                line = csvReader.readNext();
            }

            log.info("Success count: " + successCount + ", Fail count: " + failCount + ", Duplicate count: " +
                    duplicateCount);

            if (fail && success) {
                throw new UserAdminException("Error occurs while importing user names. " +
                        "Success count: " + successCount + ", Fail count: " + failCount + ", Duplicate count: " +
                        duplicateCount+". Last error was : " + lastError);
            }

            if(fail && !success){
                throw new UserAdminException("Error occurs while importing user names. " +
                        "All user names were not imported. Last error was : " + lastError);
            }
            if (isDuplicate) {
                throw new UserAdminException("Detected " + duplicateCount +" duplicate user names. " +
                        "Failed to import duplicate users. Non-duplicate user names were successfully imported.");
            }
        } catch (UserAdminException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occured while adding userlist", e);
            throw new UserAdminException("Error occured while adding userlist", e);
        } finally {
            try {
                if (csvReader != null) {
                    csvReader.close();
                }
            } catch (IOException e) {
                log.error("Error occurred while closing CSV Reader", e);
            }
        }
    }

    private void addUserWithClaims(String username, String[] line, UserStoreManager userStore)
            throws UserStoreException, UserAdminException {
        String roleString = null;
        String[] roles = null;
        String password = line[1];
        Map<String, String> claims = new HashMap<String, String>();
        for (int i = 2; i < line.length; i++) {
            if (line[i] != null && !line[i].isEmpty()) {
                String[] claimStrings = line[i].split("=");
                if (claimStrings.length != 2) {
                    throw new UserAdminException("Claims and values are not in correct format");
                } else {
                    if (claimStrings[0].contains("role")) {
                        roleString = claimStrings[1];
                    } else {
                        claims.put(claimStrings[0], claimStrings[1]);
                    }
                }

            }
        }

        if (roleString != null && !roleString.isEmpty()) {
            roles = roleString.split(":");
        }

        userStore.addUser(username, password, roles, claims, null, true);
    }
}
