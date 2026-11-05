/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.action.execution.api.model;

import java.util.Locale;

/**
 * This class models the Header.
 * Header is the entity that represents additional headers sent in the action request.
 */
public class Header {

    private final String name;
    private final String[] value;

    public Header(String name, String[] value) {
        
        this.name = name;
        this.value = value;
    }

    public String getName() {

        return name;
    }

    public String[] getValue() {

        return value;
    }

    /**
     * Returns a copy of this header with its name in lower case, as header names are case-insensitive (RFC 7230).
     * A subclass that serializes its value differently must override this to return a copy of its own type, or the
     * copy loses that form.
     *
     * @return A copy of this header with its name in lower case.
     */
    public Header withNormalizedName() {

        return new Header(name.toLowerCase(Locale.ROOT), value);
    }
}
