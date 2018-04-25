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
package org.wso2.carbon.user.mgt.bulkimport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.logging.LoggingUtils;
import org.wso2.carbon.utils.logging.TenantAwareLoggingEvent;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Log Appender class for Bulk User import operation.
 */
public class BulkUserImportLogAppender extends DailyRollingFileAppender {

    private static final Log log = LogFactory.getLog(BulkUserImportLogAppender.class);
    private static final String LOG_FILE_PATH = org.wso2.carbon.utils.CarbonUtils.getCarbonHome() +
            File.separator + "repository" + File.separator + "logs" + File.separator;
    private static final String LOG_FILE_NAME = "bulkuserimport.log";

    @Override
    protected void subAppend(LoggingEvent loggingEvent) {
        int tenantId = AccessController.doPrivileged(new PrivilegedAction<Integer>() {
            public Integer run() {

                return CarbonContext.getThreadLocalCarbonContext().getTenantId();
            }
        });

        try {
            this.setFile(LOG_FILE_PATH + LOG_FILE_NAME, this.fileAppend, this.bufferedIO, this.bufferSize);
        } catch (IOException e) {
            log.error("Error while setting the log file " + LOG_FILE_PATH + LOG_FILE_NAME, e);
        }
        String serviceName = CarbonContext.getThreadLocalCarbonContext().getApplicationName();
        final TenantAwareLoggingEvent tenantAwareLoggingEvent = LoggingUtils.getTenantAwareLogEvent(loggingEvent,
                tenantId, serviceName);
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {

                BulkUserImportLogAppender.super.subAppend(tenantAwareLoggingEvent);
                return null;
            }
        });
    }
}
