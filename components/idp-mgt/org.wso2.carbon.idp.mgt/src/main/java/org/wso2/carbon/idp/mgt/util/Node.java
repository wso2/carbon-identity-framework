package org.wso2.carbon.idp.mgt.util;

/**
 * Tree node representation.
 */
public abstract class Node {

    private Node leftNode;
    private Node rightNode;

    public Node getRightNode() {
        return rightNode;
    }

    public void setRightNode(Node rightNode) {
        this.rightNode = rightNode;
    }

    public Node getLeftNode() {
        return leftNode;
    }

    public void setLeftNode(Node leftNode) {
        this.leftNode = leftNode;
    }
}
