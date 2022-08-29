/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.openjdk.nashorn;

import org.openjdk.nashorn.api.scripting.JSObject;
import org.openjdk.nashorn.internal.runtime.JSType;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Abstract JavaScript Proxy Object for OpenJDk Nashorn Implementation.
 * Interface Created from modifying openjdk.nashorn.api.scripting.AbstractJsObject
 * Since Nashorn is deprecated in JDK 11 and onwards. We are introducing OpenJDK Nashorn engine.
 */
public interface AbstractOpenJdkNashornJsObject extends JSObject {

    @Override
    default Object call(final Object thiz, final Object... args) {
        throw new UnsupportedOperationException("call");
    }

    @Override
    default Object newObject(final Object... args) {
        throw new UnsupportedOperationException("newObject");
    }

    @Override
    default Object eval(final String s) {
        throw new UnsupportedOperationException("eval");
    }

    @Override
    default Object getMember(final String name) {
        Objects.requireNonNull(name);
        return null;
    }

    @Override
    default Object getSlot(final int index) {
        return null;
    }

    @Override
    default boolean hasMember(final String name) {
        Objects.requireNonNull(name);
        return false;
    }

    @Override
    default boolean hasSlot(final int slot) {
        return false;
    }

    @Override
    default void removeMember(final String name) {
        Objects.requireNonNull(name);
        //empty
    }

    @Override
    default void setMember(final String name, final Object value) {
        Objects.requireNonNull(name);
        //empty
    }

    @Override
    default void setSlot(final int index, final Object value) {
        //empty
    }

    // property and value iteration

    @Override
    default Set<String> keySet() {
        return Collections.emptySet();
    }

    @Override
    default Collection<Object> values() {
        return Collections.emptySet();
    }

    // JavaScript instanceof check

    @Override
    default boolean isInstance(final Object instance) {
        return false;
    }

    @Override
    default boolean isInstanceOf(final Object clazz) {
        if (clazz instanceof JSObject) {
            return ((JSObject) clazz).isInstance(this);
        }

        return false;
    }

    @Override
    default String getClassName() {
        return getClass().getName();
    }

    @Override
    default boolean isFunction() {
        return false;
    }

    @Override
    default boolean isStrictFunction() {
        return false;
    }

    @Override
    default boolean isArray() {
        return false;
    }

    @Override @Deprecated
    default double toNumber() {
        return JSType.toNumber(JSType.toPrimitive(this, Number.class));
    }
}
