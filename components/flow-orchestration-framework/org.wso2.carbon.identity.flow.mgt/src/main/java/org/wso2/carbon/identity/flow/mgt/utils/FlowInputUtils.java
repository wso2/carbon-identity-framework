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

package org.wso2.carbon.identity.flow.mgt.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.mgt.Constants.FlowInputConfigs;
import org.wso2.carbon.identity.flow.mgt.Constants.SystemVariables;
import org.wso2.carbon.identity.flow.mgt.model.FlowInputDataType;
import org.wso2.carbon.identity.flow.mgt.model.FlowInputsDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableInputDTO;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Reads the server configurations for flow inputs, and checks and converts the values of defined headers,
 * query parameters and variables.
 * <p>
 * A value is kept in one form for each type: a String, a Long for an integer, a Double for a decimal, a Boolean, and
 * a list of Strings or Doubles for the array types. Every value that reaches the flow execution context, from the
 * flow configuration, a request or the stored context, passes through here, so it never holds anything else.
 */
public final class FlowInputUtils {

    private static final Log LOG = LogFactory.getLog(FlowInputUtils.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // Letters and digits only, up to the length of the name column.
    private static final Pattern VARIABLE_NAME = Pattern.compile("^[A-Za-z0-9]{1,255}$");
    private static final Pattern HEADER_NAME = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9_-]{0,63}$");
    private static final Pattern QUERY_PARAM_NAME = Pattern.compile("^[A-Za-z0-9_][A-Za-z0-9_.-]{0,63}$");
    private static final Pattern INTEGER = Pattern.compile("^-?\\d{1,18}$");
    private static final Pattern DECIMAL = Pattern.compile("^-?\\d{1,15}(\\.\\d{1,15})?$");
    private static final String WILDCARD = "*";

    private static volatile Pattern valuePattern;
    private static volatile String valuePatternSource;

    private FlowInputUtils() {

    }

    /**
     * Reads a value as an A/B testing key.
     *
     * @param value The value, as received or configured.
     * @return The key, or null when the value is not a whole number in the allowed range.
     */
    public static Long toAbTestingKey(Object value) {

        Object key = toValue(FlowInputDataType.INTEGER, value);
        if (key == null || (Long) key < SystemVariables.AB_TESTING_KEY_MIN
                || (Long) key > SystemVariables.AB_TESTING_KEY_MAX) {
            return null;
        }
        return (Long) key;
    }

    /**
     * The most headers a flow may define.
     *
     * @return The configured limit, or the default.
     */
    public static int getMaxHeaders() {

        return FlowMgtUtils.getPositiveIntProperty(FlowInputConfigs.MAX_HEADERS, FlowInputConfigs.DEFAULT_MAX_HEADERS);
    }

    /**
     * The most query parameters a flow may define.
     *
     * @return The configured limit, or the default.
     */
    public static int getMaxQueryParams() {

        return FlowMgtUtils.getPositiveIntProperty(FlowInputConfigs.MAX_QUERY_PARAMS, FlowInputConfigs.DEFAULT_MAX_QUERY_PARAMS);
    }

    /**
     * The most variables a flow may define. Variables every flow has without defining them do not count.
     *
     * @return The configured limit, or the default.
     */
    public static int getMaxVariables() {

        return FlowMgtUtils.getPositiveIntProperty(FlowInputConfigs.MAX_VARIABLES, FlowInputConfigs.DEFAULT_MAX_VARIABLES);
    }

    /**
     * The longest text a value, or an entry of an array value, may have.
     *
     * @return The configured limit, or the default.
     */
    public static int getMaxValueLength() {

        return FlowMgtUtils.getPositiveIntProperty(FlowInputConfigs.MAX_VALUE_LENGTH, FlowInputConfigs.DEFAULT_MAX_VALUE_LENGTH);
    }

    /**
     * The most entries an array value may have.
     *
     * @return The configured limit, or the default.
     */
    public static int getMaxArraySize() {

        return FlowMgtUtils.getPositiveIntProperty(FlowInputConfigs.MAX_ARRAY_SIZE, FlowInputConfigs.DEFAULT_MAX_ARRAY_SIZE);
    }

    /**
     * Whether a header is excluded on this server, and so can neither be defined nor read.
     *
     * @param name Header name.
     * @return True when the name matches an excluded header, ignoring case.
     */
    public static boolean isExcludedHeader(String name) {

        return isExcluded(name, excluded(FlowInputConfigs.EXCLUDED_HEADERS,
                FlowInputConfigs.DEFAULT_EXCLUDED_HEADERS), true);
    }

    /**
     * Whether a query parameter is excluded on this server, and so can neither be defined nor read.
     *
     * @param name Query parameter name.
     * @return True when the name matches an excluded query parameter.
     */
    public static boolean isExcludedQueryParam(String name) {

        return isExcluded(name, excluded(FlowInputConfigs.EXCLUDED_QUERY_PARAMS,
                FlowInputConfigs.DEFAULT_EXCLUDED_QUERY_PARAMS), false);
    }

    public static boolean isValidVariableName(String name) {

        return name != null && VARIABLE_NAME.matcher(name).matches();
    }

    public static boolean isValidHeaderName(String name) {

        return name != null && HEADER_NAME.matcher(name).matches();
    }

    public static boolean isValidQueryParamName(String name) {

        return name != null && QUERY_PARAM_NAME.matcher(name).matches();
    }

    /**
     * Header names are kept in lower case, since HTTP ignores their case.
     *
     * @param name Header name.
     * @return The name in lower case.
     */
    public static String normalizeHeaderName(String name) {

        return name == null ? null : name.toLowerCase(Locale.ROOT);
    }

    /**
     * Whether a variable is one every flow defines, such as the A/B testing key.
     *
     * @param name Name of the variable.
     * @return True for a system variable.
     */
    public static boolean isSystemVariable(String name) {

        return SystemVariables.AB_TESTING_KEY.equals(name);
    }

    /**
     * Adds the system variables the inputs leave out, with their initial values. Nothing is added when flow
     * inputs are disabled.
     *
     * @param inputs Inputs of a flow, or null when it defines nothing.
     * @return The inputs with the system variables, or the given inputs when flow inputs are
     * disabled.
     */
    public static FlowInputsDTO withSystemVariables(FlowInputsDTO inputs) {

        if (!FlowMgtUtils.isFlowInputsEnabled()) {
            return inputs;
        }
        FlowInputsDTO withSystemVariables = inputs == null ? new FlowInputsDTO() : inputs;
        for (FlowVariableInputDTO variable : withSystemVariables.getVariables()) {
            if (variable != null && SystemVariables.AB_TESTING_KEY.equals(variable.getName())) {
                return withSystemVariables;
            }
        }
        FlowVariableInputDTO abTestingKey =
                new FlowVariableInputDTO(SystemVariables.AB_TESTING_KEY, FlowInputDataType.INTEGER);
        abTestingKey.setValue(SystemVariables.AB_TESTING_KEY_INITIAL_VALUE);
        withSystemVariables.getVariables().add(0, abTestingKey);
        return withSystemVariables;
    }

    /**
     * Converts a value that already has a type, such as an initial value from the flow configuration or a value read
     * back from the stored context, to the form kept for its type.
     *
     * @param type  Type of the value.
     * @param value The value.
     * @return The value in the form kept for the type, or null when it is not a valid value of the type.
     */
    public static Object toValue(FlowInputDataType type, Object value) {

        if (type == null || value == null) {
            return null;
        }
        switch (type) {
            case STRING:
                return value instanceof String && isSafeText((String) value) ? value : null;
            case INTEGER:
                return toLong(value);
            case DECIMAL:
                return toDouble(value);
            case BOOLEAN:
                return value instanceof Boolean ? value : null;
            case STRING_ARRAY:
            case NUMBER_ARRAY:
                return toList(type, value);
            default:
                return null;
        }
    }

    /**
     * Reads a value from text, such as a user input. An array is given as a JSON array.
     *
     * @param type Type of the value.
     * @param text The text.
     * @return The value in the form kept for the type, or null when the text is not a valid value of the type.
     */
    public static Object parse(FlowInputDataType type, String text) {

        if (type == null || text == null) {
            return null;
        }
        switch (type) {
            case STRING:
                return isSafeText(text) ? text : null;
            case INTEGER:
                return INTEGER.matcher(text).matches() ? Long.valueOf(text) : null;
            case DECIMAL:
                return DECIMAL.matcher(text).matches() ? Double.valueOf(text) : null;
            case BOOLEAN:
                if ("true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text)) {
                    return Boolean.valueOf(text);
                }
                return null;
            case STRING_ARRAY:
            case NUMBER_ARRAY:
                return parseJsonArray(type, text);
            default:
                return null;
        }
    }

    /**
     * Reads a value a client sent as JSON, such as a variable in the request that starts a flow. Text, a number or a
     * boolean is read for the type, so both 10 and "10" are an integer and both "true" and true a boolean, and an
     * array is read entry by entry.
     *
     * @param type  Type of the value.
     * @param value The value, as read from JSON.
     * @return The value in the form kept for the type, or null when it is not a valid value of the type.
     */
    public static Object fromInput(FlowInputDataType type, Object value) {

        if (type == null || value == null) {
            return null;
        }
        if (value instanceof List) {
            return type.isArray() ? parseEntries(type, (List<?>) value) : null;
        }
        String text = toInputText(value);
        if (text == null) {
            return null;
        }
        return parse(type, text);
    }

    /**
     * Reads an array value from repeated texts, such as the lines of a header or the repeats of a query parameter.
     * Each text is one entry, as it is: a comma in it does not split it.
     *
     * @param type  An array type.
     * @param texts The texts.
     * @return The array value, or null when the type is not an array type or any text is not a valid entry.
     */
    public static List<Object> parseEach(FlowInputDataType type, List<String> texts) {

        if (type == null || !type.isArray() || texts == null || texts.isEmpty()
                || texts.size() > getMaxArraySize()) {
            return null;
        }
        List<Object> entries = new ArrayList<>(texts.size());
        for (String text : texts) {
            Object entry = parseEntry(type, text);
            if (entry == null) {
                return null;
            }
            entries.add(entry);
        }
        return entries;
    }

    /**
     * The text of a number, without trailing zeros or a decimal point for a whole number, so that 18 and 18.0 read
     * the same when compared as text.
     *
     * @param number The number.
     * @return Its text.
     */
    public static String toText(Number number) {

        if (number instanceof Long || number instanceof Integer) {
            return number.toString();
        }
        return BigDecimal.valueOf(number.doubleValue()).stripTrailingZeros().toPlainString();
    }

    /**
     * Whether a text may be kept as a defined value: it is no longer than the limit and matches the value pattern.
     *
     * @param text The text.
     * @return True when the text is safe to keep.
     */
    public static boolean isSafeText(String text) {

        return text != null && text.length() <= getMaxValueLength() && getValuePattern().matcher(text).matches();
    }

    private static Object parseEntry(FlowInputDataType type, String text) {

        if (text == null) {
            return null;
        }
        if (type == FlowInputDataType.NUMBER_ARRAY) {
            return DECIMAL.matcher(text).matches() ? Double.valueOf(text) : null;
        }
        return isSafeText(text) ? text : null;
    }

    private static List<Object> parseEntries(FlowInputDataType type, List<?> values) {

        if (values.size() > getMaxArraySize()) {
            return null;
        }
        List<Object> entries = new ArrayList<>(values.size());
        for (Object value : values) {
            String text = value instanceof List ? null : toInputText(value);
            Object entry = parseEntry(type, text);
            if (entry == null) {
                return null;
            }
            entries.add(entry);
        }
        return entries;
    }

    /**
     * The text of a scalar a client sent, or null for anything else, such as an object.
     */
    private static String toInputText(Object value) {

        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Number) {
            return toText((Number) value);
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        return null;
    }

    private static List<Object> parseJsonArray(FlowInputDataType type, String text) {

        // Bounds the parsing before it starts: no valid array is longer than its entries and their separators.
        long longest = (long) getMaxArraySize() * (getMaxValueLength() + 3) + 2;
        if (text.length() > longest) {
            return null;
        }
        try {
            return toList(type, OBJECT_MAPPER.readValue(text, List.class));
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("The value is not a JSON array: " + e.getMessage());
            }
            return null;
        }
    }

    private static List<Object> toList(FlowInputDataType type, Object value) {

        if (!(value instanceof List) || ((List<?>) value).size() > getMaxArraySize()) {
            return null;
        }
        List<Object> entries = new ArrayList<>(((List<?>) value).size());
        for (Object entry : (List<?>) value) {
            Object converted = type == FlowInputDataType.NUMBER_ARRAY
                    ? toDouble(entry)
                    : entry instanceof String && isSafeText((String) entry) ? entry : null;
            if (converted == null) {
                return null;
            }
            entries.add(converted);
        }
        return entries;
    }

    private static Long toLong(Object value) {

        if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
            return ((Number) value).longValue();
        }
        if (value instanceof BigInteger && ((BigInteger) value).bitLength() < Long.SIZE) {
            return ((BigInteger) value).longValue();
        }
        return null;
    }

    private static Double toDouble(Object value) {

        if (!(value instanceof Number)) {
            return null;
        }
        double number = ((Number) value).doubleValue();
        return Double.isFinite(number) ? number : null;
    }

    private static Pattern getValuePattern() {

        String source = IdentityUtil.getProperty(FlowInputConfigs.VALUE_PATTERN);
        if (StringUtils.isBlank(source)) {
            source = FlowInputConfigs.DEFAULT_VALUE_PATTERN;
        }
        Pattern pattern = valuePattern;
        if (pattern != null && source.equals(valuePatternSource)) {
            return pattern;
        }
        try {
            pattern = Pattern.compile(source);
        } catch (PatternSyntaxException e) {
            LOG.warn("Invalid value configured for " + FlowInputConfigs.VALUE_PATTERN + ". Using the default.");
            pattern = Pattern.compile(FlowInputConfigs.DEFAULT_VALUE_PATTERN);
        }
        valuePattern = pattern;
        valuePatternSource = source;
        return pattern;
    }

    private static List<String> excluded(String property, List<String> defaults) {

        List<String> configured = IdentityUtil.getPropertyAsList(property);
        return configured.isEmpty() ? defaults : configured;
    }

    private static boolean isExcluded(String name, List<String> excluded, boolean ignoreCase) {

        if (name == null) {
            return false;
        }
        for (String entry : excluded) {
            if (StringUtils.isBlank(entry)) {
                continue;
            }
            String trimmed = entry.trim();
            if (trimmed.endsWith(WILDCARD)) {
                String prefix = trimmed.substring(0, trimmed.length() - 1);
                if (ignoreCase ? StringUtils.startsWithIgnoreCase(name, prefix) : name.startsWith(prefix)) {
                    return true;
                }
            } else if (ignoreCase ? trimmed.equalsIgnoreCase(name) : trimmed.equals(name)) {
                return true;
            }
        }
        return false;
    }
}
