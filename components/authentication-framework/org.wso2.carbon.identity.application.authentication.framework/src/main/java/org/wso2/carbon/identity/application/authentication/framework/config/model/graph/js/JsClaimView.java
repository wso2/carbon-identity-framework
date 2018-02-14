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

import org.wso2.carbon.identity.application.common.model.ClaimMapping;

import java.util.Map;

/**
 * Provides an array like view inJavascript for the Claim Mapping in the User attibutes.
 */
public class JsClaimView extends AbstractJSObjectWrapper<Map<ClaimMapping, String>> {

    private IdMapper idMapper;

    public JsClaimView(Map<ClaimMapping, String> wrapped, IdMapper idMapper) {

        super(wrapped);
        this.idMapper = idMapper;
    }

    @Override
    public boolean isArray() {

        return true;
    }

    @Override
    public Object getMember(String name) {

        String value = getWrapped().entrySet().stream().filter(e -> name.equals(idMapper.toId(e.getKey()))).findAny()
                .map(e -> e.getValue()).orElse(null);
        if (value != null) {
            return value;
        }
        return super.getMember(name);
    }

    @Override
    public void setMember(String name, Object value) {

        String valueStr = String.valueOf(value);
        getWrapped().entrySet().stream().filter(e -> name.equals(idMapper.toId(e.getKey())))
                .forEach(e -> e.setValue(valueStr));
    }

    @FunctionalInterface
    public interface IdMapper {

        /**
         * Get the Identifier for the claim mapping.
         *
         * @param claimMapping
         * @return
         */
        String toId(ClaimMapping claimMapping);
    }
}
