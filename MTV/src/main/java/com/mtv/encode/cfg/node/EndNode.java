package com.mtv.encode.cfg.node;

// node exit all of statements

public class EndNode extends CFGNode {
    public EndNode() {
    }

    public EndNode(CFGNode node) {
        super(node);
    }

    @Override
    public void printNode() {
        System.out.println("End node");
    }

}
