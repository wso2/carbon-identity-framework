/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder for {@link DebugFrameworkResponse}.
 */
public class DebugFrameworkResponseBuilder {

    private String debugId;
    private String status;
    private String message;
    private String errorCode;
    private Map<String, Object> data = new HashMap<>();

    public DebugFrameworkResponseBuilder debugId(String debugId) {

        this.debugId = debugId;
        return this;
    }

    public DebugFrameworkResponseBuilder status(String status) {

        this.status = status;
        return this;
    }

    public DebugFrameworkResponseBuilder message(String message) {

        this.message = message;
        return this;
    }

    public DebugFrameworkResponseBuilder errorCode(String errorCode) {

        this.errorCode = errorCode;
        return this;
    }

    public DebugFrameworkResponseBuilder data(Map<String, Object> data) {

        this.data = data != null ? data : new HashMap<>();
        return this;
    }

    public DebugFrameworkResponseBuilder addData(String key, Object value) {

        this.data.put(key, value);
        return this;
    }

    public DebugFrameworkResponse build() {

        DebugFrameworkResponse response = new DebugFrameworkResponse();
        response.setDebugId(debugId);
        response.setStatus(status);
        response.setMessage(message);
        response.setErrorCode(errorCode);
        response.setData(data);
        return response;
    }
}
