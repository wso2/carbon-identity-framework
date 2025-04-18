/*
 * Copyright (c) 2022 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.framework.async.status.mgt.internal.filter;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.model.Node;
import org.wso2.carbon.identity.core.model.OperationNode;
import org.wso2.carbon.identity.framework.async.status.mgt.api.exception.AsyncStatusMgtClientException;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.wso2.carbon.identity.framework.async.status.mgt.api.constants.ErrorMessage.ERROR_CODE_INVALID_REQUEST_BODY;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.AsyncStatusMgtConstants.AND;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.AsyncStatusMgtConstants.NOT;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.AsyncStatusMgtConstants.OR;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.util.Utils.handleClientException;

/**
 * This class is basically for creating a binary tree which preserves the precedence order with simple
 * filter (eg : userName eq vindula) expressions as terminals of the tree and all the logical operators
 * (and, or, not)as the non-terminals of the tree. ie) it sets a lists of token.
 * <p>
 * All terminals are filter expressions hence denoted by ExpressionNodes and all non terminal nodes are operators hence
 * denoted by OperatorNodes.
 */
public class FilterTreeBuilder {

    private List<String> tokenList;
    private String symbol;
    private Node root;

    public FilterTreeBuilder(String filterString) throws IOException {

        StreamTokenizer input = new StreamTokenizer(new StringReader(filterString));
        input.resetSyntax();
        // Default settings in StreamTokenizer syntax initializer.
        input.wordChars('a', 'z');
        input.wordChars('A', 'Z');
        // Specifies that all extended ASCII characters defined in HTML 4 standard, are word constituents.
        input.wordChars(128 + 32, 255);
        input.whitespaceChars(0, ' ');
        input.commentChar('/');
        input.quoteChar('"');
        input.quoteChar('\'');

        // Adding other string possible values.
        input.wordChars('@', '@');
        input.wordChars(':', ':');
        input.wordChars('_', '_');
        input.wordChars('0', '9');
        input.wordChars('-', '-');
        input.wordChars('+', '+');
        input.wordChars('.', '.');
        input.wordChars('*', '*');
        input.wordChars('/', '/');
        input.wordChars('!', '!');

        tokenList = new ArrayList<>();
        StringBuilder concatenatedString = new StringBuilder();

        while (input.nextToken() != StreamTokenizer.TT_EOF) {
            // The ttype 40 is for the '('.
            if (input.ttype == 40) {
                tokenList.add("(");
            } else if (input.ttype == 41) {
                // The ttype 41 is for the ')'.
                concatenatedString = new StringBuilder(concatenatedString.toString().trim());
                tokenList.add(concatenatedString.toString());
                concatenatedString = new StringBuilder();
                tokenList.add(")");
            } else if (input.ttype == StreamTokenizer.TT_WORD) {
                // Concatenate the string by adding spaces in between.
                if (!(input.sval.equalsIgnoreCase(AND) ||
                        input.sval.equalsIgnoreCase(OR) ||
                        input.sval.equalsIgnoreCase(NOT))) {
                    concatenatedString.append(" ").append(input.sval);
                } else {
                    concatenatedString = new StringBuilder(concatenatedString.toString().trim());
                    if (!concatenatedString.toString().equals("")) {
                        tokenList.add(concatenatedString.toString());
                        concatenatedString = new StringBuilder();
                    }
                    tokenList.add(input.sval);
                }
            } else if (input.ttype == '\"' || input.ttype == '\'') {
                concatenatedString.append(" ").append(input.sval);
            }
        }
        // Add to the list, if the filter is a simple filter.
        if (!(concatenatedString.toString().equals(""))) {
            tokenList.add(concatenatedString.toString());
        }
    }

    /**
     * Builds the binary tree from the filterString.
     *
     * @return the filter tree as root.
     */
    public Node buildTree() throws AsyncStatusMgtClientException {

        expression();
        return root;
    }

    /**
     * We build the parser using the recursive descent parser technique.
     */
    private void expression() throws AsyncStatusMgtClientException {

        term();
        while (symbol.equals("or")) {
            OperationNode or = new OperationNode("or");
            or.setLeftNode(root);
            term();
            or.setRightNode(root);
            root = or;
        }
    }

    /**
     * We build the parser using the recursive descent parser technique.
     */
    private void term() throws AsyncStatusMgtClientException {

        factor();
        while (symbol.equals("and")) {
            OperationNode and = new OperationNode("and");
            and.setLeftNode(root);
            factor();
            and.setRightNode(root);
            root = and;
        }
    }

    /**
     * We build the parser using the recursive descent parser technique.
     */
    private void factor() throws AsyncStatusMgtClientException {

        symbol = nextSymbol();
        if (symbol.equals(NOT)) {
            OperationNode not = new OperationNode(NOT);
            factor();
            not.setRightNode(root);
            root = not;
        } else if (symbol.equals("(")) {
            expression();
            symbol = nextSymbol(); // We don't care about ')'.
        } else {
            if (!(symbol.equals(")"))) {
                ExpressionNode expressionNode = new ExpressionNode();
                validateAndBuildFilterExpression(symbol, expressionNode);
                root = expressionNode;
                symbol = nextSymbol();
            } else {
                throw handleClientException(ERROR_CODE_INVALID_REQUEST_BODY, symbol);
            }
        }
    }

    /**
     * Validate the simple filter and build a ExpressionNode
     *
     * @param filterString   the filter string.
     * @param expressionNode the expression node.
     */
    private void validateAndBuildFilterExpression(String filterString, ExpressionNode expressionNode)
            throws AsyncStatusMgtClientException {

        if (StringUtils.isNotBlank(filterString) && !filterString.equals("-1")) {
            String trimmedFilter = filterString.trim();
            String[] filterParts;

            if (Pattern.compile(Pattern.quote(" eq "), Pattern.CASE_INSENSITIVE).matcher(filterString).find()) {
                filterParts = trimmedFilter.split(" eq | EQ | eQ | Eq ");
                if (filterParts.length >= 2) {
                    setExpressionNodeValues(filterParts[0], " eq ", filterParts[1], expressionNode);
                }
            } else if (Pattern.compile(Pattern.quote(" ne "), Pattern.CASE_INSENSITIVE).matcher(filterString).find()) {
                filterParts = trimmedFilter.split(" ne | NE | nE | Ne ");
                if (filterParts.length >= 2) {
                    setExpressionNodeValues(filterParts[0], " ne ", filterParts[1], expressionNode);
                }
            } else if (Pattern.compile(Pattern.quote(" co "), Pattern.CASE_INSENSITIVE).matcher(filterString).find()) {
                filterParts = trimmedFilter.split(" co | CO | cO | Co ");
                if (filterParts.length >= 2) {
                    setExpressionNodeValues(filterParts[0], " co ", filterParts[1], expressionNode);
                }
            } else if (Pattern.compile(Pattern.quote(" sw "), Pattern.CASE_INSENSITIVE).matcher(filterString).find()) {
                filterParts = trimmedFilter.split(" sw | SW | sW | Sw ");
                if (filterParts.length >= 2) {
                    setExpressionNodeValues(filterParts[0], " sw ", filterParts[1], expressionNode);
                }
            } else if (Pattern.compile(Pattern.quote(" pr"), Pattern.CASE_INSENSITIVE).matcher(filterString).find()) {
                //with filter PR, there should not be whitespace after.
                filterParts = trimmedFilter.split(" pr| PR| pR| Pr");
                if (filterParts.length >= 2) {
                    setExpressionNodeValues(filterParts[0], " pr", null, expressionNode);
                }
            } else if (Pattern.compile(Pattern.quote(" gt "), Pattern.CASE_INSENSITIVE).matcher(filterString).find()) {
                filterParts = trimmedFilter.split(" gt | GT | gT | Gt ");
                if (filterParts.length >= 2) {
                    setExpressionNodeValues(filterParts[0], " gt ", filterParts[1], expressionNode);
                }
            } else if (Pattern.compile(Pattern.quote(" ge "), Pattern.CASE_INSENSITIVE).matcher(filterString).find()) {
                filterParts = trimmedFilter.split(" ge | GE | gE | Ge ");
                if (filterParts.length >= 2) {
                    setExpressionNodeValues(filterParts[0], " ge ", filterParts[1], expressionNode);
                }
            } else if (Pattern.compile(Pattern.quote(" lt "), Pattern.CASE_INSENSITIVE).matcher(filterString).find()) {
                filterParts = trimmedFilter.split(" lt | LT | lT | Lt ");
                if (filterParts.length >= 2) {
                    setExpressionNodeValues(filterParts[0], " lt ", filterParts[1], expressionNode);
                }
            } else if (Pattern.compile(Pattern.quote(" le "), Pattern.CASE_INSENSITIVE).matcher(filterString).find()) {
                filterParts = trimmedFilter.split(" le | LE | lE | Le ");
                if (filterParts.length >= 2) {
                    setExpressionNodeValues(filterParts[0], " le ", filterParts[1], expressionNode);
                }
            } else {
                throw handleClientException(ERROR_CODE_INVALID_REQUEST_BODY, filterString);
            }
        }
    }

    /**
     * create an expression node from the given values
     *
     * @param attributeValue Attribute value.
     * @param operation      operation value.
     * @param value          the value of the filter
     * @param expressionNode filter index.
     */
    private void setExpressionNodeValues(String attributeValue, String operation, String value,
                                         ExpressionNode expressionNode) throws AsyncStatusMgtClientException {

        if (StringUtils.isNotBlank(attributeValue) || StringUtils.isNotBlank(operation)) {
            expressionNode.setAttributeValue(attributeValue.trim());
            expressionNode.setOperation(operation.trim());
            if (value != null) {
                expressionNode.setValue(value.trim());
            }
        } else {
            throw handleClientException(ERROR_CODE_INVALID_REQUEST_BODY, attributeValue, operation);
        }
    }

    /**
     * returns the first item in the list and rearrange the list
     */
    private String nextSymbol() {

        if (tokenList.size() == 0) {
            // No tokens are present in the list anymore/at all.
            return String.valueOf(-1);
        } else {
            String value = tokenList.get(0);
            tokenList.remove(0);
            return value;
        }
    }
}
