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

package org.wso2.carbon.identity.testutil;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.wso2.carbon.identity.testutil.log.NullAppender;

/**
 * This utility class is used to configure log Appenders and log levels by IdentityBaseTest and
 * PowerMockIdentityBaseTest classes.
 *
 */
public class LogUtil {

	public static final String DEBUG_LEVEL = "debug";


	public static void configureAndAddConsoleAppender() {
        //create an appender
        NullAppender nullAppender = new NullAppender();
        LogManager.getRootLogger().addAppender(nullAppender);
	}

	public static void configureLogLevel(String logLevel) {
		if (DEBUG_LEVEL.equals(logLevel)) {
			setLogLevelDebug();
		} else {
			setLogLevelInfo();
		}
	}

    private static void setLogLevelDebug() {
        LogManager.getRootLogger().setLevel(Level.DEBUG);
    }

    private static void setLogLevelInfo() {
        LogManager.getRootLogger().setLevel(Level.INFO);
    }
}
