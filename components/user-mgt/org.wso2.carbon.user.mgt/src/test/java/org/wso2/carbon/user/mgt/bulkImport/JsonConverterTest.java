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
package org.wso2.carbon.user.mgt.bulkImport;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.user.mgt.bulkimport.util.JSONConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 * Test cases for JsonConverter class.
 */
public class JsonConverterTest {

    private static final String CSV_FILENAME = "users.csv";
    private static final String XLS_FILENAME = "users.xls";
    private static final String USERS_LIST_JSON = "{\"users\":[\"testUser1\",\"testUser2\",\"testUser3\"]}";
    private static final String RESOURCE_LOCATION = Paths.get(System.getProperty("user.dir"), "src", "test",
            "resources").toString();
    private JSONConverter jsonConverter;

    @BeforeClass
    public void init() {
        jsonConverter = new JSONConverter();
    }

    @Test (description = "Test the conversion of CSV format to JSON.")
    public void testConvertCSVToJSON() throws IOException {
        InputStream inputStream = getInputStreamForFile(CSV_FILENAME);
        String usersJson = jsonConverter.csvToJSON(inputStream);
        Assert.assertEquals(USERS_LIST_JSON, usersJson);
    }

    @Test (description = "Test the conversion of XLS to JSON.")
    public void testConvertXLSToJSON() throws IOException {
        InputStream inputStream = getInputStreamForFile(XLS_FILENAME);
        Workbook testWorkBook = new HSSFWorkbook(inputStream);
        Sheet sheet = testWorkBook.getSheet(testWorkBook.getSheetName(0));

        String usersJson = jsonConverter.xlsToJSON(sheet);
        Assert.assertEquals(usersJson, USERS_LIST_JSON);
    }

    /**
     * Get the input stream of the given file
     *
     * @param fileName : The name of the file.
     * @return : The input stream of the given file.
     */
    private InputStream getInputStreamForFile(String fileName) throws FileNotFoundException {
        File userFile = new File(RESOURCE_LOCATION + File.separatorChar + fileName);
        return new FileInputStream(userFile);
    }
}
