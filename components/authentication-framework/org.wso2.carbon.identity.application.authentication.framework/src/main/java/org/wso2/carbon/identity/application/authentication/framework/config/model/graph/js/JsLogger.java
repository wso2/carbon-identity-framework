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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Logger For javascript engine.
 * Supports Log.log, Log.warn, Log.error and Log.info
 */
public class JsLogger {

    private static final Log logger = LogFactory.getLog(JsLogger.class);

    private static JsLogger jsLogger = new JsLogger();

    /**
     * Logs with list of objects.
     * @param values
     */
    public void log(Object... values) {

        if (values != null) {
            if (values.length <= 0) {
                logger.debug("");
            } else if (values.length == 1) {
                logger.debug(String.valueOf(values[0]));
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                for (Object value : values) {
                    stringBuilder.append(String.valueOf(value));
                    stringBuilder.append(" ");
                }
                logger.debug(stringBuilder.toString());
            }
        }
    }

    public void debug(String value) {

        logger.debug(value);
    }

    public void info(String value) {

        logger.info(value);
    }

    public void error(String value) {

        logger.error(value);
    }

    public void log(String message, Object... values) {

    }

    /**
     * Returns an instance to log the javascript errors.
     *
     * @return
     */
    public static JsLogger getInstance() {

        return jsLogger;
    }
}
