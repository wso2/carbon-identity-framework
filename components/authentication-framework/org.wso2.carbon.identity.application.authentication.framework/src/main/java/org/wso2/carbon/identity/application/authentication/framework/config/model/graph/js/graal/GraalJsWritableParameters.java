/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graal;


import org.graalvm.polyglot.Value;

import java.util.List;
import java.util.Map;

/**
 * Javascript wrapper for Java level HashMap of HTTP headers/cookies for Nashorn Execution.
 */
public class GraalJsWritableParameters extends GraalJsParameters {


    public GraalJsWritableParameters(Map wrapped) {
        super(wrapped);
    }

    @Override
    public Object getMember(String name) {

        Object member = getWrapped().get(name);
        if (member instanceof Map) {
            return new GraalJsWritableParameters((Map) member);
        }
        return member;
    }

    @Override
    public boolean removeMember(String name) {

        getWrapped().remove(name);
        return false;
    }

    @Override
    public void putMember(String key, Value value) {
        getWrapped().put(key, value.as(List.class));
    }
}
