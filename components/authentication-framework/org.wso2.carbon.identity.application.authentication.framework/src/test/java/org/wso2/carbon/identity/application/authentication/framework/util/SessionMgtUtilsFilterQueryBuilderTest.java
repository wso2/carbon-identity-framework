/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.util;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.core.model.ExpressionNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link SessionMgtUtils#getSQLFilterQueryBuilder(List)} and
 * {@link SessionFilterQueryBuilder}.
 */
public class SessionMgtUtilsFilterQueryBuilderTest {

    private static ExpressionNode node(String attribute, String operation, String value) {

        ExpressionNode n = new ExpressionNode();
        n.setAttributeValue(attribute);
        n.setOperation(operation);
        n.setValue(value);
        return n;
    }

    /**
     * A null expression list should produce a builder where every FilterType has an empty query and
     * no bound parameters — callers must be able to safely call getFilterQuery / getFilterParams
     * without null-checks.
     */
    @Test
    public void testNullFilterNodes_returnsEmptyBuilder() throws UserSessionException {

        SessionFilterQueryBuilder builder = SessionMgtUtils.getSQLFilterQueryBuilder(null);

        for (SessionMgtConstants.FilterType type : SessionMgtConstants.FilterType.values()) {
            Assert.assertEquals(builder.getFilterQuery(type), "",
                    "Expected empty query for FilterType " + type);
            Assert.assertTrue(builder.getFilterParams(type).isEmpty(),
                    "Expected no params for FilterType " + type);
        }
    }

    /**
     * An EQ filter on "sessionId" must be routed to the SESSION FilterType with a parameterised
     * "= ?" fragment (not an inline literal), and all other filter types must remain empty.
     */
    @Test
    public void testEqFilter_sessionId_routesToSessionFilter() throws UserSessionException {

        SessionFilterQueryBuilder builder = SessionMgtUtils.getSQLFilterQueryBuilder(
                Collections.singletonList(node("sessionId", "eq", "abc-123")));

        Assert.assertEquals(builder.getFilterQuery(SessionMgtConstants.FilterType.SESSION),
                " AND SESSION_ID = ?");
        Assert.assertEquals(builder.getFilterParams(SessionMgtConstants.FilterType.SESSION),
                Collections.singletonList("abc-123"));

        Assert.assertEquals(builder.getFilterQuery(SessionMgtConstants.FilterType.APPLICATION), "");
        Assert.assertEquals(builder.getFilterQuery(SessionMgtConstants.FilterType.USER), "");
        Assert.assertEquals(builder.getFilterQuery(SessionMgtConstants.FilterType.MAIN), "");
    }

    /**
     * A SW (starts-with) filter on "appName" must use a LIKE placeholder and escape SQL wildcard
     * characters in the bound value so that a value like "my%app" cannot widen the match
     * unintentionally. The APPLICATION query must carry a WHERE prefix, and the USER query must
     * carry an AND prefix when an app filter is already present.
     */
    @Test
    public void testSwFilter_appName_likePrefixAndEscaping() throws UserSessionException {

        // "my%app" → escapeLikeChars → "my\%app" (% escaped), then SW appends "%" → "my\%app%"
        SessionFilterQueryBuilder builderWithAppOnly = SessionMgtUtils.getSQLFilterQueryBuilder(
                Collections.singletonList(node("appName", "sw", "my%app")));

        String appQuery = builderWithAppOnly.getFilterQuery(SessionMgtConstants.FilterType.APPLICATION);
        Assert.assertTrue(appQuery.startsWith("WHERE "),
                "APPLICATION filter must start with WHERE when no prior filter exists");
        Assert.assertTrue(appQuery.contains("LIKE ?"),
                "APPLICATION filter must use a ? placeholder for the LIKE value");

        List<Object> appParams = builderWithAppOnly.getFilterParams(SessionMgtConstants.FilterType.APPLICATION);
        Assert.assertEquals(appParams.size(), 1);
        Assert.assertEquals(appParams.get(0), "my\\%app%",
                "% in the search value must be escaped to \\% and SW suffix % must be appended");

        // When both app and user filters are present, the user fragment must use AND (not WHERE).
        SessionFilterQueryBuilder builderWithBoth = SessionMgtUtils.getSQLFilterQueryBuilder(
                Arrays.asList(
                        node("appName", "eq", "console"),
                        node("loginId", "eq", "alice")));

        String userQuery = builderWithBoth.getFilterQuery(SessionMgtConstants.FilterType.USER);
        Assert.assertTrue(userQuery.startsWith(" AND "),
                "USER filter must use AND prefix when an APPLICATION filter is also present");
    }

    /**
     * getSQLFilterQueryBuilder must throw UserSessionException for an unrecognised attribute,
     * an unrecognised operation, and a non-numeric value supplied for a numeric attribute ("since").
     * These cases guard against malformed API inputs reaching the SQL layer.
     */
    @Test
    public void testInvalidFilterInput_throwsUserSessionException() {

        // unrecognised attribute
        try {
            SessionMgtUtils.getSQLFilterQueryBuilder(
                    Collections.singletonList(node("unknownAttr", "eq", "val")));
            Assert.fail("Expected UserSessionException for invalid attribute");
        } catch (UserSessionException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid filter attribute"),
                    "Exception message should mention 'Invalid filter attribute'");
        }

        // unrecognised operation
        try {
            SessionMgtUtils.getSQLFilterQueryBuilder(
                    Collections.singletonList(node("sessionId", "notAnOp", "val")));
            Assert.fail("Expected UserSessionException for invalid operation");
        } catch (UserSessionException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid filter operation"),
                    "Exception message should mention 'Invalid filter operation'");
        }

        // non-numeric value for a numeric attribute
        try {
            SessionMgtUtils.getSQLFilterQueryBuilder(
                    Collections.singletonList(node("since", "eq", "not-a-number")));
            Assert.fail("Expected UserSessionException for non-numeric value on numeric attribute");
        } catch (UserSessionException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid numeric filter value"),
                    "Exception message should mention 'Invalid numeric filter value'");
        }
    }

    /**
     * A GE filter on the numeric "since" attribute must route to the MAIN filter, bind the value
     * as a Long (not a String), and produce a ">= ?" SQL fragment — ensuring timestamp comparisons
     * are type-safe and cannot be injected via the filter value.
     */
    @Test
    public void testNumericFilter_sinceGe_routesToMainWithLongParam() throws UserSessionException {

        long timestamp = 1700000000000L;
        SessionFilterQueryBuilder builder = SessionMgtUtils.getSQLFilterQueryBuilder(
                Collections.singletonList(node("since", "ge", String.valueOf(timestamp))));

        String mainQuery = builder.getFilterQuery(SessionMgtConstants.FilterType.MAIN);
        Assert.assertTrue(mainQuery.contains("TIME_CREATED"),
                "MAIN filter must reference the TIME_CREATED column");
        Assert.assertTrue(mainQuery.contains(">= ?"),
                "GE operation must produce a '>= ?' placeholder");

        List<Object> mainParams = builder.getFilterParams(SessionMgtConstants.FilterType.MAIN);
        Assert.assertEquals(mainParams.size(), 1);
        Assert.assertEquals(mainParams.get(0), timestamp,
                "Numeric filter value must be bound as a Long, not a String");

        Assert.assertEquals(builder.getFilterQuery(SessionMgtConstants.FilterType.SESSION), "");
        Assert.assertEquals(builder.getFilterQuery(SessionMgtConstants.FilterType.APPLICATION), "");
        Assert.assertEquals(builder.getFilterQuery(SessionMgtConstants.FilterType.USER), "");
    }

    /**
     * A USER filter (loginId) with no APPLICATION filter present must use a WHERE prefix so the
     * generated sub-query is syntactically valid on its own. This is the mirror of the AND-prefix
     * case already tested when both APP and USER filters co-exist.
     */
    @Test
    public void testUserFilter_noAppFilter_usesWherePrefix() throws UserSessionException {

        SessionFilterQueryBuilder builder = SessionMgtUtils.getSQLFilterQueryBuilder(
                Collections.singletonList(node("loginId", "eq", "Alice@Example.com")));

        String userQuery = builder.getFilterQuery(SessionMgtConstants.FilterType.USER);
        Assert.assertTrue(userQuery.startsWith("WHERE "),
                "USER filter must start with WHERE when no APPLICATION filter is present");

        List<Object> userParams = builder.getFilterParams(SessionMgtConstants.FilterType.USER);
        Assert.assertEquals(userParams.size(), 1);
        // loginId values are lowercased before binding
        Assert.assertEquals(userParams.get(0), "alice@example.com",
                "loginId value must be lower-cased before being bound as a parameter");

        Assert.assertEquals(builder.getFilterQuery(SessionMgtConstants.FilterType.APPLICATION), "");
    }

    /**
     * An EW (ends-with) filter must prepend "%" to the (escaped) value, and a CO (contains)
     * filter must wrap it with "%" on both sides — both using a "?" placeholder, not inline SQL.
     * Special characters in the value must still be escaped so they cannot alter the match width.
     */
    @Test
    public void testEwAndCoFilters_correctLikePaddingAndEscaping() throws UserSessionException {

        // EW on userAgent: "Chrome_99" → escaped: "Chrome\_99", prepend % → "%Chrome\_99"
        SessionFilterQueryBuilder ewBuilder = SessionMgtUtils.getSQLFilterQueryBuilder(
                Collections.singletonList(node("userAgent", "ew", "Chrome_99")));

        String ewMainQuery = ewBuilder.getFilterQuery(SessionMgtConstants.FilterType.MAIN);
        Assert.assertTrue(ewMainQuery.contains("LIKE ?"),
                "EW filter must use a LIKE ? placeholder");
        Assert.assertEquals(ewBuilder.getFilterParams(SessionMgtConstants.FilterType.MAIN).get(0),
                "%chrome\\_99",
                "EW value must be lower-cased, '_' escaped to '\\_', and prefixed with '%'");

        // CO on ipAddress: "192.168%" → escaped: "192.168\%", wrap with % → "%192.168\%%"
        SessionFilterQueryBuilder coBuilder = SessionMgtUtils.getSQLFilterQueryBuilder(
                Collections.singletonList(node("ipAddress", "co", "192.168%")));

        String coMainQuery = coBuilder.getFilterQuery(SessionMgtConstants.FilterType.MAIN);
        Assert.assertTrue(coMainQuery.contains("LIKE ?"),
                "CO filter must use a LIKE ? placeholder");
        Assert.assertEquals(coBuilder.getFilterParams(SessionMgtConstants.FilterType.MAIN).get(0),
                "%192.168\\%%",
                "CO value must have '%' escaped to '\\%' and be wrapped with '%' on both sides");
    }

    /**
     * A blank attribute or a null value in an expression node must be rejected immediately with a
     * UserSessionException before any SQL is generated — preventing a malformed node from silently
     * producing an empty or broken query fragment.
     */
    @Test
    public void testBlankAttributeOrNullValue_throwsInvalidNodeException() {

        // blank attribute
        try {
            SessionMgtUtils.getSQLFilterQueryBuilder(
                    Collections.singletonList(node("", "eq", "val")));
            Assert.fail("Expected UserSessionException for blank attribute");
        } catch (UserSessionException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid filter node"),
                    "Exception for blank attribute must mention 'Invalid filter node'");
        }

        // null value (node() helper sets null directly)
        try {
            SessionMgtUtils.getSQLFilterQueryBuilder(
                    Collections.singletonList(node("sessionId", "eq", null)));
            Assert.fail("Expected UserSessionException for null filter value");
        } catch (UserSessionException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid filter node"),
                    "Exception for null value must mention 'Invalid filter node'");
        }
    }
}
