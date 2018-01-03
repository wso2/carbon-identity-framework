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

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.testutil.IdentityBaseTest;
import static org.testng.Assert.assertNotNull;


public class IdentityEventConfigBuilderTest extends IdentityBaseTest {

    @BeforeSuite
    public void setup() throws NoSuchFieldException, IllegalAccessException {

        String home = IdentityEventConfigBuilder.class.getResource("/").getFile();
        String config = IdentityEventConfigBuilder.class.getResource("/").getFile();
        System.setProperty("carbon.home", home);
        System.setProperty("carbon.config.dir.path", config);
    }

   @Test
    public void testGetInstance() throws IdentityEventException {

        IdentityEventConfigBuilder identityEventConfigBuilder =  IdentityEventConfigBuilder.getInstance();
        assertNotNull(identityEventConfigBuilder);
    }
}
