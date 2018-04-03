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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

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
    private JsonArray users = new JsonArray();

    public JSONConverter() {

    }

    /**
     * Convert csv format user information into JSON format. The input stream should be in following format
     * <p>
     * UserName, Password, Claims.
     *
     * @return : JSON representation of the input csv stream.
     * @throws IOException :
     */
    public String csvToJSON(InputStream sourceStream) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sourceStream));

        CSVReader csvReader = new CSVReader(bufferedReader, ',', '"', 0);
        String[] headers = csvReader.readNext();

        String[] line = csvReader.readNext();

        if (log.isDebugEnabled()) {
            log.debug("Converting csv to json.");
        }

        while (line != null) {
            JsonObject user = new JsonObject();
            user.addProperty(headers[0], line[0]);
            user.addProperty(headers[1], line[1]);
            JsonArray claims = new JsonArray();

            if (line.length > 3 && headers.length > 2) {
                for (int i = 2; i < line.length; i++) {
                    JsonObject claim = new JsonObject();
                    String[] claimVal = line[i].split("=");
                    claim.addProperty(claimVal[0], claimVal[1]);
                    claims.add(claim);
                }
                user.add(headers[2], claims);
            }
            users.add(user);
            line = csvReader.readNext();
        }

        content.add("users", users);
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
        Row headers = sheet.getRow(0);
        Cell userName = headers.getCell(0);

        if (log.isDebugEnabled()) {
            log.debug("Converting XLS sheet to json.");
        }

        for (int i = 1; i < limit + 1; i++) {
            JsonObject userJson = new JsonObject();
            Row row = sheet.getRow(i);
            Cell cell = row.getCell(0);
            String name = cell.getStringCellValue();
            userJson.addProperty(userName.getStringCellValue(), name);
            users.add(userJson);
        }
        content.add("users", users);

        return content.toString();
    }
}
