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

    default Object newObject(final Object... args) {
        throw new UnsupportedOperationException("newObject");
    }

    default Object eval(final String s) {
        throw new UnsupportedOperationException("eval");
    }

    default Object getMember(final String name) {
        Objects.requireNonNull(name);
        return null;
    }

    default Object getSlot(final int index) {
        return null;
    }

    /**
     * @implSpec This implementation always returns false
     */
    default boolean hasMember(final String name) {
        Objects.requireNonNull(name);
        return false;
    }

    /**
     * @implSpec This implementation always returns false
     */
    default boolean hasSlot(final int slot) {
        return false;
    }

    /**
     * @implSpec This implementation is a no-op
     */
    default void removeMember(final String name) {
        Objects.requireNonNull(name);
        //empty
    }

    /**
     * @implSpec This implementation is a no-op
     */
    default void setMember(final String name, final Object value) {
        Objects.requireNonNull(name);
        //empty
    }

    /**
     * @implSpec This implementation is a no-op
     */
    default void setSlot(final int index, final Object value) {
        //empty
    }

    // property and value iteration

    /**
     * @implSpec This implementation returns empty set
     */
    default Set<String> keySet() {
        return Collections.emptySet();
    }

    /**
     * @implSpec This implementation returns empty set
     */
    default Collection<Object> values() {
        return Collections.emptySet();
    }

    // JavaScript instanceof check

    /**
     * @implSpec This implementation always returns false
     */
    default boolean isInstance(final Object instance) {
        return false;
    }

    default boolean isInstanceOf(final Object clazz) {
        if (clazz instanceof JSObject) {
            return ((JSObject) clazz).isInstance(this);
        }

        return false;
    }

    default String getClassName() {
        return getClass().getName();
    }

    /**
     * @implSpec This implementation always returns false
     */
    default boolean isFunction() {
        return false;
    }

    /**
     * @implSpec This implementation always returns false
     */
    default boolean isStrictFunction() {
        return false;
    }

    /**
     * @implSpec This implementation always returns false
     */
    default boolean isArray() {
        return false;
    }

    /**
     * Returns this object's numeric value.
     *
     * @return this object's numeric value.
     * @deprecated use {@link #getDefaultValue(Class)} with {@link Number} hint instead.
     */
    @Override @Deprecated
    default double toNumber() {
        return JSType.toNumber(JSType.toPrimitive(this, Number.class));
    }

}
