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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.core.util.IdentityIOStreamUtils;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.user.mgt.bulkimport.util.JSONConverter;
import org.wso2.carbon.user.mgt.common.UserAdminException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class to import users from Excel format files.
 */
public class ExcelUserBulkImport extends UserBulkImport {

    private static final Log log = LogFactory.getLog(ExcelUserBulkImport.class);
    private BulkImportConfig config;

    public ExcelUserBulkImport(BulkImportConfig config) {

        super();
        this.config = config;
    }

    public void addUserList(UserStoreManager userStore) throws UserAdminException {

        Workbook wb = this.createWorkbook();
        Sheet sheet = wb.getSheet(wb.getSheetName(0));
        userStoreDomain = config.getUserStoreDomain();

        if (sheet == null || sheet.getLastRowNum() == -1) {
            throw new UserAdminException("The first sheet is empty");
        }
        int limit = sheet.getLastRowNum();
        boolean isDuplicate = false;
        boolean fail = false;
        for (int i = 1; i < limit + 1; i++) {
            Row row = sheet.getRow(i);
            Cell cell = row.getCell(0);
            String userName = cell.getStringCellValue();

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
                        userStore.addUser(userName, null, null, null, null, true);
                        successCount++;
                        if (log.isDebugEnabled()) {
                            log.debug("User import successful - Username : " + userName);
                        }
                    } else {
                        duplicateCount++;
                        duplicateUsers.add(userName);
                        isDuplicate = true;
                        log.error("User import unsuccessful - Username : " + userName + " - Error: Duplicate user");
                        duplicateUsers.add(userName);
                    }
                } catch (UserStoreException e) {
                    fail = true;
                    failCount++;
                    log.error("User import unsuccessful - Username : " + userName + " - Error: " +
                            e.getMessage());
                    errorUsersMap.put(userName, e.getMessage());
                }
            }
        }

        String summeryLog = super.buildBulkImportSummary();
        log.info(summeryLog);

        JSONConverter jsonConverter = new JSONConverter();
        String importedUsers = jsonConverter.xlsToJSON(sheet);
        auditLog.info(String.format(UserMgtConstants.AUDIT_LOG_FORMAT, tenantUser, UserMgtConstants.OPERATION_NAME,
                userStoreDomain, importedUsers, summeryLog));

        if (fail || isDuplicate) {
            throw new UserAdminException(String.format(UserMgtConstants.ERROR_MESSAGE, successCount, failCount,
                    duplicateCount));
        }
    }

    /**
     * Generate a WorkBook object from the excel file.
     *
     * @return : The generated workbook
     * @throws UserAdminException : Throws if there is any error occurred in the process of creating the workbook.
     */
    private Workbook createWorkbook() throws UserAdminException {

        String filename = config.getFileName();
        InputStream ins = config.getInStream();
        Workbook wb;
        try {
            if (filename.endsWith(".xlsx")) {
                wb = new XSSFWorkbook(ins);
            } else {
                POIFSFileSystem fs = new POIFSFileSystem(ins);
                wb = new HSSFWorkbook(fs);
            }
        } catch (IOException e) {
            throw new UserAdminException("Error reading the xls file " + e.getMessage(), e);
        } finally {
            IdentityIOStreamUtils.closeInputStream(ins);
        }
        return wb;
    }
}
