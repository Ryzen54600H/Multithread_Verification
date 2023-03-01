package com.mtv.graph.cfg.node;

import com.mtv.DebugHelper.DebugHelper;

import java.util.ArrayList;
// begin Node ko chua du lieu
/* node begin the statements if/ for/ while....
 * don't contain data *
 */

public class BeginNode extends CFGNode {
    private CFGNode endNode;

    public CFGNode getEndNode() {
        return endNode;
    }

    public void setEndNode(CFGNode endNode) {
        this.endNode = endNode;
    }

    public ArrayList<CFGNode> adjacent() {
        ArrayList<CFGNode> adj = new ArrayList<>();
        adj.add(this.getNext());
        adj.add(endNode);
        return adj;
    }

    @Override
    public void printNode() {
        System.out.println("Begin node");
    }

    public DecisionNode getDecisionNode() {
        if (this.next.next instanceof DecisionNode) {
            return (DecisionNode) this.next.next;
        } else if (this.next instanceof DecisionNode) {
            return (DecisionNode) this.next;
        } else return null;
    }

}
