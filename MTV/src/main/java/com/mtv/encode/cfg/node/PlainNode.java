package com.mtv.encode.cfg.node;

import com.mtv.debug.DebugHelper;
import com.mtv.encode.cfg.utils.ExpressionHelper;
import com.mtv.encode.cfg.utils.Index;
import com.mtv.encode.cfg.index.FormulaCreater;
import com.mtv.encode.cfg.index.VariableManager;
import com.mtv.encode.cfg.utils.ExpressionModifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class PlainNode extends CFGNode {
    private IASTStatement statement;
    private IASTFunctionDefinition func;

    public PlainNode() {
        super();
    }

    public PlainNode(IASTStatement statement) {
        this.statement = statement;
    }

    public PlainNode(IASTStatement statement, IASTFunctionDefinition func) {
        this.statement = changeName(statement, func);
        this.setFunc(func);
    }

    private IASTStatement changeName(IASTStatement statement, IASTFunctionDefinition func) {
        return (IASTStatement) ExpressionModifier.changeVariableName(statement, func);
    }

    public IASTStatement getStatement() {
        return statement;
    }

    public void setStatement(IASTStatement statement) {
        this.statement = statement;
    }

    public void index(VariableManager vm) {
        statement = (IASTStatement) Index.index(statement, vm);
    }

    public String getFormula() {
        return FormulaCreater.createFormula(statement);
    }

    public String getInfixFormula() {
        return FormulaCreater.createInfixFormula(statement);
    }

    public String toString() {
        return ExpressionHelper.toString(statement);
    }

    @Override
    public void printNode() {
        if (statement != null) {
            DebugHelper.print("PlainNode: " + ExpressionHelper.toString(statement));
        } else System.out.println(this);

    }

    public boolean isFunctionCall() {
        return hasCallExpression(statement);
    }

    private boolean hasCallExpression(IASTNode statement) {
        boolean result = false;
        IASTNode[] nodes = statement.getChildren();
        for (IASTNode node : nodes) {
            if (node instanceof IASTFunctionCallExpression) {
                result = true;
                return result;
            } else {
                result = hasCallExpression(node);
            }
        }
        return result;
    }

    public IASTFunctionDefinition getFunc() {
        return func;
    }

    public void setFunc(IASTFunctionDefinition func) {
        this.func = func;
    }
}
