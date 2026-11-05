/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.extension.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Writes a flow variable value with each number without an exponent, such as 0.0000001 rather than 1.0E-7, whether
 * the value is a number or an array of them. A decimal keeps its decimal point, so 10.0 stays 10.0. Other values are
 * written as usual.
 */
public class PlainNumberSerializer extends StdSerializer<Object> {

    private static final long serialVersionUID = 1L;

    public PlainNumberSerializer() {

        super(Object.class);
    }

    @Override
    public void serialize(Object value, JsonGenerator generator, SerializerProvider provider) throws IOException {

        switch (value) {
            case Number number -> generator.writeNumber(toText(number));
            case List<?> list -> {
                generator.writeStartArray();
                for (Object entry : list) {
                    serialize(entry, generator, provider);
                }
                generator.writeEndArray();
            }
            // A null entry of an array is written as null.
            case null, default -> provider.defaultSerializeValue(value, generator);
        }
    }

    /**
     * The text of a number without an exponent. A decimal has at least one digit after the point.
     *
     * @param number The number.
     * @return Its text.
     */
    public static String toText(Number number) {

        if (!(number instanceof Double || number instanceof Float)) {
            return number.toString();
        }
        BigDecimal decimal = BigDecimal.valueOf(number.doubleValue()).stripTrailingZeros();
        return (decimal.scale() > 0 ? decimal : decimal.setScale(1)).toPlainString();
    }
}
