package com.mtv.encode.cfg.node;

import com.mtv.encode.ast.FunctionHelper;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;

public class LabelNode extends CFGNode {
    private IASTLabelStatement statement;
    private IASTFunctionDefinition function;

    public LabelNode() {
    }

    public LabelNode(IASTLabelStatement stmt) {
        statement = stmt;
    }

    public LabelNode(IASTLabelStatement stmt, IASTFunctionDefinition func) {
        statement = stmt;
        function = func;
    }

    public IASTLabelStatement getLabel() {
        return statement;
    }

    public IASTFunctionDefinition getFunction() {
        return function;
    }

    @Override
    public void printNode() {
        if (statement != null)
            System.out.println("LabelNode: " + statement.getName() + "_" + FunctionHelper.getShortenName(function));
        System.out.print("     "); // to indent content of label nodes
    }
}
