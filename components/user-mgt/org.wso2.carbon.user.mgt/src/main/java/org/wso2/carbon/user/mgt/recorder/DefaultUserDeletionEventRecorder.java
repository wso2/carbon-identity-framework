/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.mgt.recorder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.Map;

/**
 * Default user delete event recorder is used to record the delete events in to a file in CSV format.
 */
public class DefaultUserDeletionEventRecorder implements UserDeletionEventRecorder {

    private static final Log log = LogFactory.getLog(DefaultUserDeletionEventRecorder.class);
    private static final Log DELETE_EVENT_LOGGER = LogFactory.getLog("DELETE_EVENT_LOGGER");

    private static final String PATH_PROPERTY_NAME = "path";
    private static final String COMMA = ",";

    @Override
    public void recordUserDeleteEvent(String username, String domainName, String tenantDomain, int tenantId,
                                      Date timeStamp, Map<String, String> properties) throws RecorderException {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(LoggerUtils.isLogMaskingEnable ? LoggerUtils.getMaskedContent(username) : username)
                .append(COMMA)
                .append(domainName)
                .append(COMMA)
                .append(tenantDomain)
                .append(COMMA)
                .append(tenantId)
                .append(COMMA)
                .append(timeStamp.toString())
                .append(COMMA);

        // Write the given properties after above values.
        for (Map.Entry<String, String> values : properties.entrySet()) {

            // This property is specific to this recorder. No use of writing it.
            if (values.getKey().equals(PATH_PROPERTY_NAME)) {
                continue;
            }
            stringBuilder.append(values.getValue()).append(COMMA);
        }

        // If a custom file path is given as a property to this recorder, we will write to that. Else we will use the
        // given appender.
        String path = properties.get(PATH_PROPERTY_NAME);
        if (StringUtils.isNotEmpty(path)) {

            if (log.isDebugEnabled()) {
                log.debug("CSV file path is set to: " + path);
            }

            path = IdentityUtil.fillURLPlaceholders(path);

            if (log.isDebugEnabled()) {
                log.debug("CSV path after resolving the carbon placeholders: " + path);
            }

            writeToCustomFile(path, stringBuilder.toString());
        } else {
            DELETE_EVENT_LOGGER.info(stringBuilder.toString());
        }

        if (log.isDebugEnabled()) {
            log.debug("Following line was successfully written to file: " + stringBuilder.toString());
        }
    }

    private void writeToCustomFile(String path, String content) throws RecorderException {

        // Create the file if it does not exist. Open with write permission and append to the end.
        try (OutputStream outputStream = Files.newOutputStream(Paths.get(path), StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND)) {
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            bufferedWriter.write(content);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new RecorderException("Error while writing content to the file.", e);
        }
    }
}
