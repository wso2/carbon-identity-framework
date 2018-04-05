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

package org.wso2.carbon.identity.application.authentication.framework.context;

import java.io.Serializable;
import java.lang.ref.WeakReference;

/**
 * A non-serializable object to be carried across the authentication context.
 * Authentication context is a serializable object. Some objects should not be serialized as they are valid only to the current request.
 * e.g. HTTP Request object.
 * The data is held only for the lifetime of the current request. After that GC will remove the data freely.
 */
public class TransientObjectWrapper<T> implements Serializable {

    private transient WeakReference<T> wrapped;

    public TransientObjectWrapper(T wrapped) {

        this.wrapped = new WeakReference<>(wrapped);
    }

    public T getWrapped() {

        return wrapped.get();
    }
}
