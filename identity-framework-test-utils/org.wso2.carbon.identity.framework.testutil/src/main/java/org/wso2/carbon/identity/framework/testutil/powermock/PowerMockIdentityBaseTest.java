/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.carbon.identity.framework.testutil.powermock;

import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.wso2.carbon.identity.framework.testutil.IdentityBaseTest;
import org.wso2.carbon.identity.framework.testutil.log.LogUtil;

/**
 * PowerMock based TestNG test that extended from PowerMockIdentityBaseTest class can read "log-level" parameter from
 * testng.xml configuration file and set that value as the root log level before executing each test method.
 * <p>
 * Example :
 * {@code
 * <test name="basic-authenticator-tests-with-debug-logs" preserve-order="true" parallel="false">
 * <parameter name="log-level" value="debug"/>
 * <classes>
 * <class name="org.wso2.carbon.identity.application.authenticator.basicauth.BasicAuthenticatorTestCase"/>
 * </classes>
 * </test>
 * }
 * <p>
 * Test cases that should run twice with debug and info or any other log levels need to be extended from this class.
 * PowerMockIdentityBaseTest class also set ignore package list for PowerMock framework.
 *
 * @see IdentityBaseTest
 */

@PowerMockIgnore({"javax.management.*", "javax.script.*"})
public abstract class PowerMockIdentityBaseTest extends PowerMockTestCase {

    public PowerMockIdentityBaseTest() {

        LogUtil.configureAndAddConsoleAppender();
    }

    @Parameters({"log-level"})
    @BeforeMethod
    public void setUp(@Optional String logLevel) throws Exception {

        LogUtil.configureLogLevel(logLevel);
    }

}
