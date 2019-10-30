package org.wso2.carbon.idp.mgt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.idp.mgt.object.ExpressionNode;
import org.wso2.carbon.idp.mgt.object.Node;
import org.wso2.carbon.idp.mgt.object.OperationNode;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class is basically for creating a binary tree which preserves the precedence order with simple
 * filter (eg : userName eq vindula) expressions as terminals of the tree and all the logical operators
 * (and, or, not)as the non-terminals of the tree. ie) it sets a lists of token.
 * <p>
 * All terminals are filter expressions hence denoted by ExpressionNodes and all non terminal nodes are operators hence
 * denoted by OperatorNodes.
 */

public class FilterTreeBuilder {

    private static final Log log = LogFactory.getLog(FilterTreeBuilder.class);
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

        //Adding other string possible values
        input.wordChars('@', '@');
        input.wordChars(':', ':');
        input.wordChars('_', '_');
        input.wordChars('0', '9');
        input.wordChars('-', '-');
        input.wordChars('+', '+');
        input.wordChars('.', '.');
        input.wordChars('*', '*');
        input.wordChars('/', '/');

        tokenList = new ArrayList<>();
        StringBuilder concatenatedString = new StringBuilder();

        while (input.nextToken() != StreamTokenizer.TT_EOF) {
            //ttype 40 is for the '('
            if (input.ttype == 40) {
                tokenList.add("(");
            } else if (input.ttype == 41) {
                //ttype 40 is for the ')'
                concatenatedString = new StringBuilder(concatenatedString.toString().trim());
                tokenList.add(concatenatedString.toString());
                concatenatedString = new StringBuilder();
                tokenList.add(")");
            } else if (input.ttype == StreamTokenizer.TT_WORD) {
                //concatenate the string by adding spaces in between
                if (!(input.sval.equalsIgnoreCase("and") || input.sval.equalsIgnoreCase("or") || input.sval
                        .equalsIgnoreCase("not")))
                    concatenatedString.append(" ").append(input.sval);
                else {
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
        //Add to the list, if the filter is a simple filter
        if (!(concatenatedString.toString().equals(""))) {
            tokenList.add(concatenatedString.toString());
        }
    }

    /**
     * Builds the binary tree from the filterString.
     *
     * @return the filter tree as root.
     */
    public Node buildTree() {
        expression();
        return root;
    }

    /**
     * We build the parser using the recursive descent parser technique.
     */
    private void expression() {
        term();
        while (symbol.equals(String.valueOf("or"))) {
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
    private void term() {
        factor();
        while (symbol.equals(String.valueOf("and"))) {
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
    private void factor() {
        symbol = nextSymbol();
        if (symbol.equals(String.valueOf("not"))) {
            OperationNode not = new OperationNode("not");
            factor();
            not.setRightNode(root);
            root = not;
        } else if (symbol.equals(String.valueOf("("))) {
            expression();
            symbol = nextSymbol(); // we don't care about ')'
        } else {
            if (!(symbol.equals(String.valueOf(")")))) {
                ExpressionNode expressionNode = new ExpressionNode();
                validateAndBuildFilterExpression(symbol, expressionNode);
                root = expressionNode;
                symbol = nextSymbol();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid argument: Identity Provider Name value is empty");
                }
            }
        }
    }

    /**
     * Validate the simple filter and build a ExpressionNode
     *
     * @param filterString   the filter string.
     * @param expressionNode the expression node.
     */
    private void validateAndBuildFilterExpression(String filterString, ExpressionNode expressionNode) {

        String trimmedFilter = filterString.trim();
        String[] filterParts;

        if (Pattern.compile(Pattern.quote(" eq "), Pattern.CASE_INSENSITIVE).matcher(filterString).find()) {
            filterParts = trimmedFilter.split(" eq | EQ | eQ | Eq ");
            setExpressionNodeValues(filterParts[0], " eq ", filterParts[1], expressionNode);
        } else if (Pattern.compile(Pattern.quote(" ne "), Pattern.CASE_INSENSITIVE).matcher(filterString).find()) {
            filterParts = trimmedFilter.split(" ne | NE | nE | Ne ");
            setExpressionNodeValues(filterParts[0], " ne ", filterParts[1], expressionNode);
        } else if (Pattern.compile(Pattern.quote(" co "), Pattern.CASE_INSENSITIVE).matcher(filterString).find()) {
            filterParts = trimmedFilter.split(" co | CO | cO | Co ");
            setExpressionNodeValues(filterParts[0], " co ", filterParts[1], expressionNode);
        } else if (Pattern.compile(Pattern.quote(" sw "), Pattern.CASE_INSENSITIVE).matcher(filterString).find()) {
            filterParts = trimmedFilter.split(" sw | SW | sW | Sw ");
            setExpressionNodeValues(filterParts[0], " sw ", filterParts[1], expressionNode);
        } else if (Pattern.compile(Pattern.quote(" ew "), Pattern.CASE_INSENSITIVE).matcher(filterString).find()) {
            filterParts = trimmedFilter.split(" ew | EW | eW | Ew ");
            setExpressionNodeValues(filterParts[0], " ew ", filterParts[1], expressionNode);
        } else if (Pattern.compile(Pattern.quote(" pr"), Pattern.CASE_INSENSITIVE).matcher(filterString).find()) {
            //with filter PR, there should not be whitespace after.
            filterParts = trimmedFilter.split(" pr| PR| pR| Pr");
            setExpressionNodeValues(filterParts[0], " pr", null, expressionNode);
        } else if (Pattern.compile(Pattern.quote(" gt "), Pattern.CASE_INSENSITIVE).matcher(filterString).find()) {
            filterParts = trimmedFilter.split(" gt | GT | gT | Gt ");
            setExpressionNodeValues(filterParts[0], " gt ", filterParts[1], expressionNode);
        } else if (Pattern.compile(Pattern.quote(" ge "), Pattern.CASE_INSENSITIVE).matcher(filterString).find()) {
            filterParts = trimmedFilter.split(" ge | GE | gE | Ge ");
            setExpressionNodeValues(filterParts[0], " ge ", filterParts[1], expressionNode);
        } else if (Pattern.compile(Pattern.quote(" lt "), Pattern.CASE_INSENSITIVE).matcher(filterString).find()) {
            filterParts = trimmedFilter.split(" lt | LT | lT | Lt ");
            setExpressionNodeValues(filterParts[0], " lt ", filterParts[1], expressionNode);
        } else if (Pattern.compile(Pattern.quote(" le "), Pattern.CASE_INSENSITIVE).matcher(filterString).find()) {
            filterParts = trimmedFilter.split(" le | LE | lE | Le ");
            setExpressionNodeValues(filterParts[0], " le ", filterParts[1], expressionNode);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Given filter operator is not supported.");
            }
        }
    }

    /**
     * create a expression node from the given values
     *
     * @param attributeValue Attribute value.
     * @param operation      operation value.
     * @param value          the value of the filter
     * @param expressionNode filter index.
     */
    private void setExpressionNodeValues(String attributeValue, String operation, String value,
            ExpressionNode expressionNode) {

        expressionNode.setAttributeValue(attributeValue.trim());
        expressionNode.setOperation(operation.trim());
        if (value != null) {
            expressionNode.setValue(value.trim());
        }
    }

    /**
     * returns the first item in the list and rearrange the list
     */
    private String nextSymbol() {

        if (tokenList.size() == 0) {
            //no tokens are present in the list anymore/at all
            return String.valueOf(-1);
        } else {
            String value = tokenList.get(0);
            tokenList.remove(0);
            return value;
        }
    }
}
