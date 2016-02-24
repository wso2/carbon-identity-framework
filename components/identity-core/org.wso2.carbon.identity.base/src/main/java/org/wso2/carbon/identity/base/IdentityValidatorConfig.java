/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.wso2.carbon.identity.base;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This class manages validation patterns
 */
public class IdentityValidatorConfig {

    private Map<String, Pattern> patterns;

    public IdentityValidatorConfig() {
        patterns = new HashMap<>();
    }

    /**
     * Adds a validation pattern and stores it against the provided key.
     * Throws an IllegalArgumentException if pattern key or pattern is empty, or if a pattern exists for the given key
     *
     * @param key   pattern key
     * @param regex pattern regex
     */
    public void addPattern(String key, String regex) {

        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("Pattern identification key cannot be empty.");
        }

        if (StringUtils.isBlank(regex)) {
            throw new IllegalArgumentException("Pattern cannot be empty.");
        }

        try {
            if (patterns.containsKey(key)) {
                throw new IllegalArgumentException("A pattern already exists for key " + key);
            }
            patterns.put(key, Pattern.compile(regex));
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Failed to parse given pattern " + regex, e);
        }
    }

    /**
     * Returns a Pattern instance for the given key
     *
     * @param key pattern key
     * @return Pattern instance stored against the key
     */
    public Pattern getPattern(String key) {
        return patterns.get(key);
    }

    /**
     * Removes pattern from the memory
     *
     * @param key pattern key
     */
    public void removePattern(String key) {
        patterns.remove(key);
    }

    /**
     * Checks if a pattern exists for the provided key
     *
     * @param key pattern key
     * @return true if pattern exists or false if pattern does not exist
     */
    public boolean patternExists(String key) {
        return patterns.containsKey(key);
    }

}
