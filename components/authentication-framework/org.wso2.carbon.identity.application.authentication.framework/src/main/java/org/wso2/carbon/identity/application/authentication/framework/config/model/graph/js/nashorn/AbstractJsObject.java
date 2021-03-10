package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn;

import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.internal.runtime.JSType;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Abstract JavaScript Proxy Object for Nashorn Implementation.
 * Interface Created from modifying jdk.nashorn.api.scripting.AbstractJsObject
 */
public interface AbstractJsObject extends JSObject {

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
