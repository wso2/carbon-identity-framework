/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.extensions.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.flow.extensions.InFlowExtensionConstants.HandoverPolicy;
import org.wso2.carbon.identity.flow.extensions.model.FlowContextHandoverConfig;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Builds a filtered defensive copy of a {@link FlowExecutionContext} containing only the
 * attributes permitted by the supplied {@link FlowContextHandoverConfig}.
 *
 * <p>This class mirrors the engine's {@code FlowExecutionContextFilter} but lives in
 * the inflow module to avoid a cross-bundle dependency on {@code engine.config.*}. When the
 * toml-based dynamic configuration PR is merged, this class can be removed and the engine's
 * filter used directly.</p>
 *
 * <p>Implementation reflects over the JavaBean property descriptors of
 * {@link FlowExecutionContext} and {@link FlowUser}. Descriptors are cached on class load.
 * The original context is never mutated; non-permitted attributes are left null/empty on
 * the copy.</p>
 *
 * <p>Map fields receive a defensive shallow {@link HashMap} copy. The
 * {@code userCredentials} field receives a per-entry {@code char[]} clone so that the
 * request builder's post-extraction wipe zeroes the copy, not the source.</p>
 */
public final class InFlowExtensionContextFilterUtil {

    private static final Log LOG = LogFactory.getLog(InFlowExtensionContextFilterUtil.class);

    private static final Map<String, PropertyDescriptor> CONTEXT_PROPERTIES;
    private static final Map<String, PropertyDescriptor> USER_PROPERTIES;

    static {
        CONTEXT_PROPERTIES = Collections.unmodifiableMap(introspect(FlowExecutionContext.class));
        USER_PROPERTIES = Collections.unmodifiableMap(introspect(FlowUser.class));
    }

    private InFlowExtensionContextFilterUtil() {

    }

    /**
     * Build a filtered copy of {@code original} according to {@code config}.
     *
     * @param original the source context (untouched).
     * @param config   the handover policy.
     * @return a new {@link FlowExecutionContext} carrying only whitelisted attributes,
     *         or {@code null} if {@code original} is {@code null}.
     */
    public static FlowExecutionContext filter(FlowExecutionContext original,
                                             FlowContextHandoverConfig config) {

        if (original == null) {
            return null;
        }

        FlowExecutionContext copy = new FlowExecutionContext();

        // contextIdentifier is engine-internal and always propagated regardless of config.
        copy.setContextIdentifier(original.getContextIdentifier());

        // Top-level attributes (flowUser and contextIdentifier are handled separately).
        for (String name : config.getIncludedAttributes()) {
            if (HandoverPolicy.ATTR_FLOW_USER.equals(name)
                    || HandoverPolicy.ATTR_CONTEXT_IDENTIFIER.equals(name)) {
                continue;
            }
            copyProperty(CONTEXT_PROPERTIES, name, original, copy);
        }

        // User attributes — a fresh non-null FlowUser is always set on the copy so that
        // request builders / response processors don't need to null-guard the user object.
        FlowUser dstUser = new FlowUser();
        copy.setFlowUser(dstUser);
        FlowUser srcUser = original.getFlowUser();
        if (srcUser != null) {
            Set<String> userAttrs = config.isFullUserPassthrough()
                    ? USER_PROPERTIES.keySet()
                    : config.getIncludedUserAttributes();
            for (String name : userAttrs) {
                copyProperty(USER_PROPERTIES, name, srcUser, dstUser);
            }
        }

        return copy;
    }

    private static <T> void copyProperty(Map<String, PropertyDescriptor> descriptors, String name,
                                         T source, T destination) {

        PropertyDescriptor pd = descriptors.get(name);
        if (pd == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Skipping unknown handover attribute: " + name);
            }
            return;
        }
        Method reader = pd.getReadMethod();
        Method writer = pd.getWriteMethod();
        if (reader == null || writer == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Skipping handover attribute without readable+writable accessors: " + name);
            }
            return;
        }
        try {
            Object value = reader.invoke(source);
            value = defensivelyCopy(name, value);
            writer.invoke(destination, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOG.warn("Failed to copy handover attribute '" + name + "': " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static Object defensivelyCopy(String name, Object value) {

        if (!(value instanceof Map)) {
            return value;
        }
        if (HandoverPolicy.ATTR_USER_CREDENTIALS.equals(name)) {
            Map<String, char[]> src = (Map<String, char[]>) value;
            Map<String, char[]> out = new LinkedHashMap<>();
            for (Map.Entry<String, char[]> entry : src.entrySet()) {
                char[] v = entry.getValue();
                out.put(entry.getKey(), v == null ? null : v.clone());
            }
            return out;
        }
        return new HashMap<>((Map<?, ?>) value);
    }

    private static Map<String, PropertyDescriptor> introspect(Class<?> beanClass) {

        Map<String, PropertyDescriptor> result = new HashMap<>();
        try {
            for (PropertyDescriptor pd :
                    Introspector.getBeanInfo(beanClass, Object.class).getPropertyDescriptors()) {
                if (pd.getReadMethod() != null && pd.getWriteMethod() != null) {
                    result.put(pd.getName(), pd);
                }
            }
        } catch (IntrospectionException e) {
            LOG.error("Failed to introspect " + beanClass.getName() + " for handover filtering.", e);
        }
        return result;
    }
}
