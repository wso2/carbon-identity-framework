/*
 * Copyright (c) 2018, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

import java.util.Objects;

/**
 * Abstract wrapper class for objects used inside the javascript.
 *
 * @param <T> Wrapped object type
 */
public abstract class AbstractJSObjectWrapper<T> extends AbstractJSContextMemberObject {

    private T wrapped;

    public AbstractJSObjectWrapper(T wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("Wrapped object cannot be null.");
        }
        this.wrapped = wrapped;
    }

    public T getWrapped() {
        return wrapped;
    }
    public Object getMember(String name) {

        Objects.requireNonNull(name);
        if ("getWrapped".equals(name)) {
            return getWrapped();
        }
        return null;
    }

    public boolean hasMember(String name) {

        Objects.requireNonNull(name);
        return "getWrapped".equals(name);
    }

    public void setMember(String name, Object value) {

        Objects.requireNonNull(name);
    }
}
