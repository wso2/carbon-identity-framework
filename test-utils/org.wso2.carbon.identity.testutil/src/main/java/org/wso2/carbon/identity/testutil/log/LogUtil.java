/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.testutil.log;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.wso2.carbon.identity.testutil.log.NullAppender;

/**
 * This utility class is used to configure log Appenders and log levels by IdentityBaseTest and
 * PowerMockIdentityBaseTest classes.
 *
 */
public class LogUtil {

	private LogUtil() {
	}

	public static void configureAndAddConsoleAppender() {
		NullAppender appender = new NullAppender();
		LogManager.getRootLogger().addAppender(appender);
	}

	public static void configureLogLevel(String logLevel) {
		Level level = Level.toLevel(logLevel);
		LogManager.getRootLogger().setLevel(level);
	}
}
