/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.user.mgt.bulkimport.util;

import au.com.bytecode.opencsv.CSVReader;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.wso2.carbon.user.mgt.UserMgtConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Class to convert CSV/ XLS format files to JSON format.
 */
public class JSONConverter {

    private static final Log log = LogFactory.getLog(JSONConverter.class);
    private JsonObject content = new JsonObject();
    private JsonArray users;

    public JSONConverter() {
    }

    /**
     * Convert csv format user information into JSON format. The input stream should be in following format
     * <p>
     * UserName, Password, Claims.
     *
     * @return : JSON representation of the input csv stream.
     * @throws IOException : Throws if there is any error occurred when reading from the input stream.
     */
    public String csvToJSON(InputStream sourceStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sourceStream));
        CSVReader csvReader = new CSVReader(bufferedReader, ',', '"', 1);
        String[] line = csvReader.readNext();
        users = new JsonArray();

        if (log.isDebugEnabled()) {
            log.debug("Converting csv to json.");
        }
        while (line != null) {
            JsonPrimitive user = new JsonPrimitive(line[0]);
            users.add(user);
            line = csvReader.readNext();
        }

        content.add(UserMgtConstants.USERS, users);
        return content.toString();
    }

    /**
     * Converts xls sheet to json format.
     * Currently considering the username.
     *
     * @param sheet : The XLS sheet that needs to be converted.
     * @return : Json string which represents the sheet.
     */
    public String xlsToJSON(Sheet sheet) {
        int limit = sheet.getLastRowNum();
        users = new JsonArray();

        if (log.isDebugEnabled()) {
            log.debug("Converting XLS sheet to json.");
        }

        for (int i = 1; i < limit + 1; i++) {
            Row row = sheet.getRow(i);
            Cell cell = row.getCell(0);
            String name = cell.getStringCellValue();
            JsonPrimitive userJson = new JsonPrimitive(name);
            users.add(userJson);
        }
        content.add(UserMgtConstants.USERS, users);
        return content.toString();
    }
}
