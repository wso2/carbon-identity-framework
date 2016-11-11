/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.common.internal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.internal.IdentityCommonDataHolder;
import org.wso2.carbon.identity.common.util.url.URLUtils;

import java.util.Properties;

public class ConfigParser {

    private static Logger logger = LoggerFactory.getLogger(ConfigParser.class);

    private static ConfigParser parser;
    private static Object lock = new Object();

    private ConfigParser() {
        initConfiguration();
    }

    public static ConfigParser getInstance() {
        if (parser == null) {
            synchronized (lock) {
                if (parser == null) {
                    parser = new ConfigParser();
                }
            }
        }
        return parser;
    }

    /**
     * Read configuration elements from the identity.yaml
     *
     * @param key fully qualified element name
     * @return Element text value.
     */
    public static String getProperty(String key) {

        String strValue = null;
        // implement to get property value
        strValue = fillPlaceHolders(strValue);
        return strValue;
    }

    /**
     * To read properties from the identity.yaml
     *
     * @param key
     * @return Properties defined under that configuration.
     */
    public static Properties getProperties(String key) {
        return null;
    }

    public static String fillPlaceHolders(String key) {
        key = URLUtils.fillURLPlaceholders(key);
        //fill carbon home place holder
        return key;
    }

    private void initConfiguration() {
        buildHandlerConfig();
        buildCacheConfig();
        buildCookieConfig();
    }

    private void buildHandlerConfig() {
        IdentityCommonDataHolder.getInstance().setHandlerConfig(null);
    }

    private void buildCacheConfig() {
        IdentityCommonDataHolder.getInstance().setCacheConfig(null);
    }

    private void buildCookieConfig()    {
        IdentityCommonDataHolder.getInstance().setCookieConfig(null);
    }

}
