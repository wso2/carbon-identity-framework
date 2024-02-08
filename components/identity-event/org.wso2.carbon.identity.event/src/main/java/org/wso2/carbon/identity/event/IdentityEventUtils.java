/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Util functionality for MessageSending Components
 */
@SuppressWarnings("unused")
public class IdentityEventUtils {

    private static final Log log = LogFactory.getLog(IdentityEventUtils.class);

    private IdentityEventUtils() {
    }

    /**
     * Returns a set of properties which has keys starting with the given prefix
     *
     * @param prefix     prefix of the property key
     * @param properties Set of properties which needs be filtered for the given prefix
     * @return A set of properties which has keys starting with given prefix
     */
    public static Properties getPropertiesWithPrefix(String prefix, Properties properties) {

        if (StringUtils.isEmpty(prefix) || properties == null) {
            throw new IllegalArgumentException("Prefix and properties should not be null to extract properties with " +
                    "certain prefix");
        }

        Properties subProperties = new Properties();
        Enumeration propertyNames = properties.propertyNames();

        while (propertyNames.hasMoreElements()) {
            String key = (String) propertyNames.nextElement();
            if (key.startsWith(prefix)) {
                // Remove from original properties to hold property schema. ie need to get the set of properties which
                // remains after consuming all required specific properties
                subProperties.setProperty(key, (String) properties.remove(key));
            }
        }
        return subProperties;
    }

    /**
     * Returns a sub set of properties which has the given prefix key.
     *
     * @param prefix     Prefix of the key
     * @param properties Set of properties which needs be filtered for the given prefix
     * @return Set of sub properties which has keys starting with given prefix
     */
    public static Properties getSubProperties(String prefix, Properties properties) {

        // Stop proceeding if required arguments are not present
        if (StringUtils.isEmpty(prefix) || properties == null) {
            throw new IllegalArgumentException("Prefix and Properties should not be null to get sub properties");
        }

        int i = 1;
        Properties subProperties = new Properties();
        while (properties.getProperty(prefix + "." + i) != null) {
            // Remove from original properties to hold property schema. ie need to get the set of properties which
            // remains after consuming all required specific properties.
            subProperties.put(prefix + "." + i, properties.remove(prefix + "." + i++));
        }
        return subProperties;
    }

    /**
     * Returns the module names using a given prefix key. ie properties which has numbers at the end.
     *
     * @param prefix     Prefix of the properties used to define module names.
     * @param properties Set of properties which needs be filtered for the given prefix.
     * @return Map of module names.
     */
    public static Properties getModuleNames(String prefix, Properties properties) {

        // Stop proceeding if required arguments are not present.
        if (StringUtils.isEmpty(prefix) || properties == null) {
            throw new IllegalArgumentException("Prefix and Properties should not be null to get sub properties");
        }

        Properties subProperties = new Properties();
        // Remove from original properties to hold property schema. ie need to get the set of properties which
        // remains after consuming all required specific properties.
        subProperties.putAll(getPropertiesWithPrefix(prefix, properties));
        return subProperties;
    }

    /**
     * @param prefix                 Prefix of the property key
     * @param propertiesWithFullKeys Set of properties which needs to be converted to single word key properties
     * @return Set of properties which has keys containing single word.
     */
    public static Properties buildSingleWordKeyProperties(String prefix, Properties propertiesWithFullKeys) {

        // Stop proceeding if required arguments are not present
        if (StringUtils.isEmpty(prefix) || propertiesWithFullKeys == null) {
            throw new IllegalArgumentException("Prefix and properties should not be null to get  properties with " +
                    "single word keys.");
        }

        propertiesWithFullKeys = IdentityEventUtils.getPropertiesWithPrefix(prefix, propertiesWithFullKeys);
        Properties properties = new Properties();
        Enumeration propertyNames = propertiesWithFullKeys.propertyNames();

        while (propertyNames.hasMoreElements()) {
            String key = (String) propertyNames.nextElement();
            String newKey = key.substring(key.lastIndexOf(".") + 1, key.length());
            if (!newKey.trim().isEmpty()) {
                // Remove from original properties to hold property schema. ie need to get the set of properties which
                // remains after consuming all required specific properties
                properties.put(newKey, propertiesWithFullKeys.remove(key));
            }
        }
        return properties;
    }

    /**
     * Replace place holders in the given string with properties
     *
     * @param content                Original content of the message which has place holders
     * @param replaceRegexStartsWith Placeholders starting regex
     * @param replaceRegexEndsWith   Placeholders ending regex
     * @param properties             Set of properties which are to be used for replacing
     * @return New content, place holders are replaced
     */
    public static String replacePlaceHolders(String content, String replaceRegexStartsWith,
                                             String replaceRegexEndsWith,
                                             Properties properties) {

        // Stop proceeding if required arguments are not present
        if (properties == null || StringUtils.isEmpty(content) || StringUtils.isEmpty(replaceRegexEndsWith) ||
                StringUtils.isEmpty(replaceRegexStartsWith)) {
            throw new IllegalArgumentException("Missing required arguments for replacing place holders");
        }

        if (log.isDebugEnabled()) {
            log.debug("Place holders starting regex : " + replaceRegexStartsWith + ". End regex with : " +
                    replaceRegexEndsWith);
            log.debug("Replacing place holders of String " + content);
        }
            // For each property check whether there is a place holder and replace the place
            // holders exist.
            for (String key : properties.stringPropertyNames()) {
                if (log.isDebugEnabled()) {
                    log.debug("Replacing place holder with property key :" + key + " from value :" + properties
                            .getProperty(key));
                }
                content = content.replaceAll(replaceRegexStartsWith + key + replaceRegexEndsWith,
                        properties.getProperty(key));
            }
            if (log.isDebugEnabled()) {
                log.debug("Place holders replaced String " + content);
            }
        return content;
    }

    /**
     * Read the file which is in given path and build the message template
     *
     * @param filePath Path of the message template file
     * @return String which contains message template
     */
    public static String readMessageTemplate(String filePath) {

        BufferedReader bufferedReader = null;
        String template = null;
        // Stop proceeding if required arguments are not present
        if (StringUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("File path should not be empty");
        }

        // Reading the content of the file
        if (log.isDebugEnabled()) {
            log.debug("Reading template file in " + filePath);
        }
        try {
            String currentLine;
            bufferedReader = new BufferedReader(new FileReader(filePath));
            StringBuilder templateBuilder = new StringBuilder();

            while ((currentLine = bufferedReader.readLine()) != null) {
                templateBuilder.append(currentLine);
                templateBuilder.append(System.getProperty("line.separator"));
            }
            template = templateBuilder.toString();

        } catch (IOException e) {
            log.error("Error while reading email template from location " + filePath, e);

        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                log.error("Error while closing buffered reader after reading file " + filePath, e);
            }
        }
        return template;
    }
}
