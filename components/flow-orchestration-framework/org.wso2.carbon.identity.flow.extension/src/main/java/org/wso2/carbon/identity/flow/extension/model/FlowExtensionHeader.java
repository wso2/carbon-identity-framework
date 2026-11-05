/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.extension.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.wso2.carbon.identity.action.execution.api.model.Header;
import org.wso2.carbon.identity.flow.mgt.utils.FlowVariableUtils;

/**
 * A request header of the flow, as sent to the Flow Extension action. Its value is the array of the header's values,
 * or, when the header is exposed encrypted, the JWE compact string of that array as JSON. Its name is always in lower
 * case, so the action request filter keeps it as it is rather than copying it into a plain {@link Header}, which would
 * send an encrypted value as an array.
 */
public class FlowExtensionHeader extends Header {

    private final boolean encrypted;
    private final String encryptedValue;

    public FlowExtensionHeader(String name, String[] values) {

        super(FlowVariableUtils.normalizeHeaderName(name), values);
        this.encrypted = false;
        this.encryptedValue = null;
    }

    public FlowExtensionHeader(String name, String encryptedValue) {

        super(FlowVariableUtils.normalizeHeaderName(name), new String[]{encryptedValue});
        this.encrypted = true;
        this.encryptedValue = encryptedValue;
    }

    @Override
    @JsonIgnore
    public String[] getValue() {

        return super.getValue();
    }

    /**
     * The value as sent: the array of values, or the encrypted string.
     *
     * @return The value.
     */
    @JsonProperty("value")
    public Object getSentValue() {

        return encrypted ? encryptedValue : super.getValue();
    }
}
