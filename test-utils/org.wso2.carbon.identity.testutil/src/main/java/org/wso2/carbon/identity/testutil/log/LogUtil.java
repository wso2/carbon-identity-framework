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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

/**
 * This utility class is used to configure log Appenders and log levels by IdentityBaseTest and
 * PowerMockIdentityBaseTest classes.
 *
 */
public class LogUtil {

    private static final Log log = LogFactory.getLog(LogUtil.class);

    private LogUtil() {
    }

    public static void configureAndAddConsoleAppender() {
        NullAppender appender = new NullAppender();
        LogManager.getRootLogger().addAppender(appender);
    }

    public static void configureLogLevel(String logLevel) {
        Level level = Level.toLevel(logLevel);
        try {
            LogManager.getRootLogger().setLevel(level);
        } catch (Throwable t) {
            //We catch throwable as there is a case where logger level setting fails when old SLF4j library interferes.
            log.error("Could not set the log level to : " + level + ". Probably inconsistent Log4J class is loaded.",
                    t);
        }
    }
}
