/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.identity.event;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.io.*;
import java.util.Properties;

public class IdentityEventUtilsTest {

    private Properties properties;
    private Properties loadedProperties;
    private Properties subProperties;

    @BeforeMethod
    public void setUp() throws Exception {
        setProperties();
    }

    private void setProperties() {

        properties = new Properties();
        properties.setProperty("key", "replaced");
        loadedProperties = new Properties();
        loadedProperties.setProperty("x.1","value");
        loadedProperties.setProperty("x.2","value");
        loadedProperties.setProperty("y.1","value");
        loadedProperties.setProperty("z.1","value");
        loadedProperties.setProperty("z.2","value");
        subProperties = new Properties();
        subProperties.setProperty("x.1","value");
        subProperties.setProperty("x.2","value");
    }

    @Test
    public void testReadMessageTemplate() throws IOException {

        String identityEventUtils = IdentityEventUtils.readMessageTemplate("src/test/resources/sample-file.xml");
        String fileContent = "<file></file>\n";
        Assert.assertTrue(fileContent.equals(identityEventUtils));

    }

    @Test
    public void testReplacePlaceHolder() {

        String replaceRegexStartsWit = "start";
        String replaceRegexEndsWit = "end";
        String finalContent = "replacedextra";
        String contentBefore = "startkeyendextra";
        String replacePlaceHolder = IdentityEventUtils.replacePlaceHolders(contentBefore, replaceRegexStartsWit, replaceRegexEndsWit, properties);
        Assert.assertEquals(finalContent, replacePlaceHolder);
    }

    @Test
    public void testGetPropertiesWithPrefix(){


        Properties finalProperties = IdentityEventUtils.getPropertiesWithPrefix("x",loadedProperties);
        Assert.assertEquals(finalProperties,subProperties);
    }

    @Test
    public void testGetSubProperties(){

        Properties finalProperties1 = IdentityEventUtils.getSubProperties("x",loadedProperties);
        Assert.assertTrue(((Integer)2).equals(finalProperties1.size()));

    }

}



