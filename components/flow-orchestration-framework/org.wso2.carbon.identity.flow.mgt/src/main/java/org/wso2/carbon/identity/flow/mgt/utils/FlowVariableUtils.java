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
import org.wso2.carbon.identity.flow.mgt.Constants.FlowVariableConfigs;
import org.wso2.carbon.identity.flow.mgt.Constants.SystemRuntimeValues;
import org.wso2.carbon.identity.flow.mgt.model.FlowRuntimeValueDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDataType;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariablesDTO;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Reads the server configurations for flow variables, and checks and converts the values of defined headers and
 * runtime values.
 * <p>
 * A value is kept in one form for each type: a String, a Long for an integer, a Double for a decimal, a Boolean, and
 * a list of Strings or Doubles for the array types. Every value that reaches the flow execution context, from the
 * flow configuration, a request or the stored context, passes through here, so it never holds anything else.
 */
public final class FlowVariableUtils {

    private static final Log LOG = LogFactory.getLog(FlowVariableUtils.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // Letters and digits only, up to the length of the name column.
    private static final Pattern RUNTIME_VALUE_NAME = Pattern.compile("^[A-Za-z0-9]{1,255}$");
    private static final Pattern HEADER_NAME = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9_-]{0,63}$");
    private static final Pattern INTEGER = Pattern.compile("^-?\\d{1,18}$");
    private static final Pattern DECIMAL = Pattern.compile("^-?\\d{1,15}(\\.\\d{1,15})?$");
    private static final String WILDCARD = "*";

    private static volatile Pattern valuePattern;
    private static volatile String valuePatternSource;

    private FlowVariableUtils() {

    }

    /**
     * Reads a value as an A/B testing key.
     *
     * @param value The value, as received or configured.
     * @return The key, or null when the value is not a whole number in the allowed range.
     */
    public static Long toAbTestingKey(Object value) {

        Object key = toValue(FlowVariableDataType.INTEGER, value);
        if (key == null || (Long) key < SystemRuntimeValues.AB_TESTING_KEY_MIN
                || (Long) key > SystemRuntimeValues.AB_TESTING_KEY_MAX) {
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

        return FlowMgtUtils.getPositiveIntProperty(FlowVariableConfigs.MAX_HEADERS,
                FlowVariableConfigs.DEFAULT_MAX_HEADERS);
    }

    /**
     * The most runtime values a flow may define. Runtime values every flow has without defining them do not count.
     *
     * @return The configured limit, or the default.
     */
    public static int getMaxRuntimeValues() {

        return FlowMgtUtils.getPositiveIntProperty(FlowVariableConfigs.MAX_RUNTIME_VALUES,
                FlowVariableConfigs.DEFAULT_MAX_RUNTIME_VALUES);
    }

    /**
     * The longest text a value, or an entry of an array value, may have.
     *
     * @return The configured limit, or the default.
     */
    public static int getMaxValueLength() {

        return FlowMgtUtils.getPositiveIntProperty(FlowVariableConfigs.MAX_VALUE_LENGTH,
                FlowVariableConfigs.DEFAULT_MAX_VALUE_LENGTH);
    }

    /**
     * The most entries an array value may have.
     *
     * @return The configured limit, or the default.
     */
    public static int getMaxArraySize() {

        return FlowMgtUtils.getPositiveIntProperty(FlowVariableConfigs.MAX_ARRAY_SIZE,
                FlowVariableConfigs.DEFAULT_MAX_ARRAY_SIZE);
    }

    /**
     * Whether a header is excluded on this server, and so can neither be defined nor read.
     *
     * @param name Header name.
     * @return True when the name matches an excluded header, ignoring case.
     */
    public static boolean isExcludedHeader(String name) {

        return isExcluded(name, excluded(FlowVariableConfigs.EXCLUDED_HEADERS,
                FlowVariableConfigs.DEFAULT_EXCLUDED_HEADERS));
    }

    public static boolean isValidRuntimeValueName(String name) {

        return name != null && RUNTIME_VALUE_NAME.matcher(name).matches();
    }

    public static boolean isValidHeaderName(String name) {

        return name != null && HEADER_NAME.matcher(name).matches();
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
     * Whether a runtime value is one every flow defines, such as the A/B testing key.
     *
     * @param name Name of the runtime value.
     * @return True for a system runtime value.
     */
    public static boolean isSystemRuntimeValue(String name) {

        return SystemRuntimeValues.AB_TESTING_KEY.equals(name);
    }

    /**
     * Adds the system runtime values the variables leave out, with their initial values. Nothing is added when flow
     * variables are disabled.
     *
     * @param variables Variables of a flow, or null when it defines nothing.
     * @return The variables with the system runtime values, or the given variables when flow variables are disabled.
     */
    public static FlowVariablesDTO withSystemRuntimeValues(FlowVariablesDTO variables) {

        if (!FlowMgtUtils.isFlowVariablesEnabled()) {
            return variables;
        }
        FlowVariablesDTO withSystemRuntimeValues = variables == null ? new FlowVariablesDTO() : variables;
        for (FlowRuntimeValueDTO runtimeValue : withSystemRuntimeValues.getRuntimeValues()) {
            if (runtimeValue != null && SystemRuntimeValues.AB_TESTING_KEY.equals(runtimeValue.getName())) {
                runtimeValue.setClientWritable(true);
                return withSystemRuntimeValues;
            }
        }
        FlowRuntimeValueDTO abTestingKey =
                new FlowRuntimeValueDTO(SystemRuntimeValues.AB_TESTING_KEY, FlowVariableDataType.INTEGER);
        abTestingKey.setValue(SystemRuntimeValues.AB_TESTING_KEY_INITIAL_VALUE);
        abTestingKey.setClientWritable(true);
        withSystemRuntimeValues.getRuntimeValues().add(0, abTestingKey);
        return withSystemRuntimeValues;
    }

    /**
     * Converts a value that already has a type, such as an initial value from the flow configuration or a value read
     * back from the stored context, to the form kept for its type.
     *
     * @param type  Type of the value.
     * @param value The value.
     * @return The value in the form kept for the type, or null when it is not a valid value of the type.
     */
    public static Object toValue(FlowVariableDataType type, Object value) {

        if (type == null || value == null) {
            return null;
        }
        switch (type) {
            case STRING:
                return value instanceof String text && isSafeText(text) ? value : null;
            case INTEGER:
                return toLong(value);
            case DECIMAL:
                return toDouble(value);
            case BOOLEAN:
                return value instanceof Boolean ? value : null;
            case STRING_ARRAY, NUMBER_ARRAY:
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
    public static Object parse(FlowVariableDataType type, String text) {

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
            case STRING_ARRAY, NUMBER_ARRAY:
                return parseJsonArray(type, text);
            default:
                return null;
        }
    }

    /**
     * Reads a value a client sent as JSON, such as the initial value of a runtime value. Text, a number or a
     * boolean is read for the type, so both 10 and "10" are an integer and both "true" and true a boolean, and an
     * array is read entry by entry.
     *
     * @param type  Type of the value.
     * @param value The value, as read from JSON.
     * @return The value in the form kept for the type, or null when it is not a valid value of the type.
     */
    public static Object fromInput(FlowVariableDataType type, Object value) {

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
     * Reads an array value from repeated texts, such as the lines of a header.
     * Each text is one entry, as it is: a comma in it does not split it.
     *
     * @param type  An array type.
     * @param texts The texts.
     * @return The array value, or null when the type is not an array type or any text is not a valid entry.
     */
    public static List<Object> parseEach(FlowVariableDataType type, List<String> texts) {

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

    private static Object parseEntry(FlowVariableDataType type, String text) {

        if (text == null) {
            return null;
        }
        if (type == FlowVariableDataType.NUMBER_ARRAY) {
            return DECIMAL.matcher(text).matches() ? Double.valueOf(text) : null;
        }
        return isSafeText(text) ? text : null;
    }

    private static List<Object> parseEntries(FlowVariableDataType type, List<?> values) {

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

        if (value instanceof String text) {
            return text;
        }
        if (value instanceof BigInteger bigInteger) {
            return bigInteger.toString();
        }
        if (value instanceof Number number) {
            return Double.isNaN(number.doubleValue()) || Double.isInfinite(number.doubleValue()) ? null : toText(number);
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        return null;
    }

    private static List<Object> parseJsonArray(FlowVariableDataType type, String text) {

        // Bounds the parsing before it starts: no valid array is longer than its entries and their separators. An
        // entry may write each of its characters as a six-character unicode escape.
        long longest;
        try {
            longest = Math.addExact(Math.multiplyExact(getMaxArraySize(), getMaxValueLength() * 6L + 3), 2);
        } catch (ArithmeticException e) {
            longest = Long.MAX_VALUE;
        }
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

    private static List<Object> toList(FlowVariableDataType type, Object value) {

        if (!(value instanceof List) || ((List<?>) value).size() > getMaxArraySize()) {
            return null;
        }
        List<Object> entries = new ArrayList<>(((List<?>) value).size());
        for (Object entry : (List<?>) value) {
            Object converted;
            if (type == FlowVariableDataType.NUMBER_ARRAY) {
                converted = toDouble(entry);
            } else {
                converted = entry instanceof String text && isSafeText(text) ? entry : null;
            }
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
        if (value instanceof BigInteger bigInteger && bigInteger.bitLength() < Long.SIZE) {
            return bigInteger.longValue();
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

        String source = IdentityUtil.getProperty(FlowVariableConfigs.VALUE_PATTERN);
        if (StringUtils.isBlank(source)) {
            source = FlowVariableConfigs.DEFAULT_VALUE_PATTERN;
        }
        Pattern pattern = valuePattern;
        if (pattern != null && source.equals(valuePatternSource)) {
            return pattern;
        }
        try {
            pattern = Pattern.compile(source);
        } catch (PatternSyntaxException e) {
            LOG.warn("Invalid value configured for " + FlowVariableConfigs.VALUE_PATTERN + ". Using the default.");
            pattern = Pattern.compile(FlowVariableConfigs.DEFAULT_VALUE_PATTERN);
        }
        valuePattern = pattern;
        valuePatternSource = source;
        return pattern;
    }

    private static List<String> excluded(String property, List<String> defaults) {

        List<String> configured = IdentityUtil.getPropertyAsList(property);
        return configured.isEmpty() ? defaults : configured;
    }

    private static boolean isExcluded(String name, List<String> excluded) {

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
                if (StringUtils.startsWithIgnoreCase(name, prefix)) {
                    return true;
                }
            } else if (trimmed.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
