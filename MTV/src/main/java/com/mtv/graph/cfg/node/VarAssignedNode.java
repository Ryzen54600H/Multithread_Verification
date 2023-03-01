package com.mtv.graph.cfg.node;

import com.mtv.DebugHelper.DebugHelper;
import com.mtv.graph.cfg.utils.ExpressionHelper;
import com.mtv.graph.cfg.utils.ExpressionModifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class VarAssignedNode extends CFGNode{
    private IASTStatement statement;
    private IASTFunctionDefinition func;

    public VarAssignedNode(IASTStatement statement) {
        this.statement = statement;
    }
    public VarAssignedNode(IASTStatement statement, IASTFunctionDefinition func) {
        this.statement = changeName(statement, func);
        this.setFunc(func);
    }

    private IASTStatement changeName(IASTStatement statement, IASTFunctionDefinition func) {
        return (IASTStatement) ExpressionModifier.changeVariableName(statement, func);
    }
    public IASTFunctionDefinition getFunc() {
        return func;
    }

    public void setFunc(IASTFunctionDefinition func) {
        this.func = func;
    }
    @Override
    public void printNode() {
        if (statement != null) {
            DebugHelper.print("Variable Assigned Node: " + ExpressionHelper.toString(statement));
        }
    }
}