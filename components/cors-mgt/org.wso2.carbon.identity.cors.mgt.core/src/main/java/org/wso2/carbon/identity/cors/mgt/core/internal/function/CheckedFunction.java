package org.wso2.carbon.identity.cors.mgt.core.internal.function;

/**
 * CheckedFunction interface.
 * @param <T> Input to the function.
 * @param <R> Result of the function.
 */
@FunctionalInterface
public interface CheckedFunction<T, R> {

    R apply(T t) throws Exception;
}
