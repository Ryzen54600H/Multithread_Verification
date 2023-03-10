package com.mtv.encode.cfg.utils;

import com.mtv.encode.cfg.index.Variable;
import com.mtv.encode.cfg.index.VariableManager;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;


public class Index {

    private static CPPNodeFactory factory = new CPPNodeFactory();

    public static IASTNode index(IASTNode node, VariableManager vm) {
        if (node instanceof IASTDeclarationStatement) {
            node = indexDeclarationStatement((IASTDeclarationStatement) node, vm); //cau lenh khoi tao

        } else if (node instanceof IASTExpressionStatement) { //cau lenh gan va so sanh
            node = indexExpressionStatement((IASTExpressionStatement) node, vm);

        } else if (node instanceof IASTBinaryExpression) { //phep gan va so sanh
            node = indexIASTBinaryExpression((IASTBinaryExpression) node, vm);

        } else if (node instanceof IASTUnaryExpression) { // i++
            node = indexUranyExpression((IASTUnaryExpression) node, vm);

        } else if (node instanceof IASTIdExpression) { //bien (khong tinh trong phep khoi tao)
            node = indexIdExpression((IASTIdExpression) node, vm);

        } else if (node instanceof IASTReturnStatement) {
            node = indexReturnStatement((IASTReturnStatement) node, vm);
        }
        return node;
    }

    public static IASTNode indexInvariant(IASTNode node, VariableManager vm) {
        if (node instanceof IASTExpressionStatement) { //cau lenh gan va so sanh
            node = indexExpressionStatementInvariant((IASTExpressionStatement) node, vm);

        } else if (node instanceof IASTBinaryExpression) { //phep gan va so sanh
            node = indexIASTBinaryExpressionInvariant((IASTBinaryExpression) node, vm);

        } else if (node instanceof IASTUnaryExpression) { // i++
            node = indexUranyExpressionInvariant((IASTUnaryExpression) node, vm);

        } else if (node instanceof IASTIdExpression) { //bien (khong tinh trong phep khoi tao)
            node = indexIdExpressionInvariant((IASTIdExpression) node, vm);
        }
        return node;
    }

    /**
     * Gan index cho cac bien ve phai (khong tang index)
     */
    private static IASTNode indexIdExpression(IASTIdExpression node, VariableManager vm) {
        String name = ExpressionHelper.toString(node);
        Variable var = vm.getVariable(name);
        if (var == null) return node;
        if (var.getIndexInvariant() > var.getIndex()) {
            var.setIndex(var.getIndexInvariant());
        }
        IASTName nameId = factory.newName(var.getVariableWithIndex().toCharArray());
        IASTIdExpression newExp = factory.newIdExpression(nameId);
        return newExp;
    }

    /**
     * index invariant, kh??ng thay ?????i gi?? tr??? index
     * @param node
     * @param vm
     * @return
     */
    private static IASTNode indexIdExpressionInvariant(IASTIdExpression node, VariableManager vm) {
        String name = ExpressionHelper.toString(node);
        Variable var = vm.getVariable(name);
        if (var == null) return node;
        IASTName nameId = factory.newName(var.getVariableWithIndex().toCharArray());
        IASTIdExpression newExp = factory.newIdExpression(nameId);
        return newExp;
    }

    /**
     * Gan index cho cac bien ve trai cua phep gan (index ++)
     */
    private static IASTNode indexVariable(IASTIdExpression node, VariableManager vm) {
        String name = ExpressionHelper.toString(node);
        Variable var = vm.getVariable(name);
        if (var == null) return node;
        if (var.getIndexInvariant() != -1) { //have change index in inv
            if (var.getIndex() < var.getIndexInvariant()) {
                var.setIndex(var.getIndexInvariant() + 1);
            } else {
                var.increase();
            }
        } else { //dont have in invariant
            var.increase();
        }

        IASTName nameId = factory.newName(var.getVariableWithIndex().toCharArray());
        IASTIdExpression newNode = factory.newIdExpression(nameId);
        return newNode;
    }
    /*
        Gan index cho invariant, index trong vm khong thay doi???
     */
    private static IASTNode indexVariableInvariant(IASTIdExpression node, VariableManager vm) {
        String name = ExpressionHelper.toString(node);
        Variable var = vm.getVariable(name);
        if (var == null) return node;
        var.increase();
        var.setIndexInvariant(var.getIndex()); //TODO
        IASTName nameId = factory.newName(var.getVariableWithIndex().toCharArray());
        var.decrease();
        IASTIdExpression newNode = factory.newIdExpression(nameId);
        return newNode;
    }

    private static int changeOperator(int unaryOp) {
        int binaryOp = 0;
        if (unaryOp == IASTUnaryExpression.op_postFixDecr || unaryOp == IASTUnaryExpression.op_prefixDecr) {
            binaryOp = IASTBinaryExpression.op_minus;
        } else if (unaryOp == IASTUnaryExpression.op_postFixIncr || unaryOp == IASTUnaryExpression.op_prefixIncr) {
            binaryOp = IASTBinaryExpression.op_plus;
        }
        return binaryOp;
    }

    /**
     * chuyen doi cac toan tu dac biet
     *
     * @param node
     * @return
     */
    private static IASTExpression changeUnarytoBinary(IASTUnaryExpression node) {
        IASTExpression operand = ((IASTUnaryExpression) node).getOperand().copy();
        int operator = changeOperator(node.getOperator());
        if (operator == 0) {
            return node.getOperand();
        }

        IASTLiteralExpression number = factory.newLiteralExpression(IASTLiteralExpression.lk_integer_constant, "1");
        IASTExpression right = factory.newBinaryExpression(operator, operand, number);
        IASTExpression expression = factory.newBinaryExpression(IASTBinaryExpression.op_assign, operand, right);
        return expression;
    }

    private static IASTNode indexUranyExpression(IASTUnaryExpression node, VariableManager vm) {
        return index(changeUnarytoBinary(node), vm);
    }
    private static IASTNode indexUranyExpressionInvariant(IASTUnaryExpression node, VariableManager vm) {
        return indexInvariant(changeUnarytoBinary(node),vm);
    }
    /**
     * @param node
     * @param vm
     * @return bieu thuc don da duoc danh chi so
     */
    private static IASTNode indexIASTBinaryExpression(IASTBinaryExpression node, VariableManager vm) {
        boolean isAssignment = (node.getOperator() == IASTBinaryExpression.op_assign);
        if (isAssignment) { //neu la phep gan
           // System.out.println(ExpressionHelper.toString(node));
            IASTExpression right = (IASTExpression) index (node.getOperand2().copy(), vm);
            IASTExpression left = null;
            if (node.getOperand1() instanceof IASTIdExpression) { // neu la 1 bien gan
                left = (IASTExpression) indexVariable((IASTIdExpression) node.getOperand1().copy(), vm);
            } else if (node.getOperand1() instanceof IASTBinaryExpression) { //neu la binary expression gan
                left = (IASTExpression) index (node.getOperand1().copy(), vm);
            }
            IASTBinaryExpression newNode = factory.newBinaryExpression(node.getOperator(), left, right);
            return newNode;
        } else { //neu la phep so sanh
            IASTExpression left = node.getOperand1().copy();
            IASTExpression right = node.getOperand2().copy();
            IASTBinaryExpression newNode = factory.newBinaryExpression(node.getOperator(), (IASTExpression) index(left, vm), (IASTExpression) index(right, vm));
            return newNode;
        }
    }

    private static IASTNode indexIASTBinaryExpressionInvariant(IASTBinaryExpression node, VariableManager vm) {
        boolean isAssignment = (node.getOperator() == IASTBinaryExpression.op_assign);
        IASTExpression right = null;
        if (node.getOperand2() instanceof IASTIdExpression) {
            right = (IASTExpression) indexVariableInvariant(((IASTIdExpression) node.getOperand2()).copy(), vm);
        } else  {
            right = (IASTExpression) indexInvariant(node.getOperand2().copy(), vm);
        }

        IASTExpression left = null;
        if (node.getOperand1() instanceof IASTIdExpression) { // neu la 1 bien gan
            left = (IASTExpression) indexVariableInvariant((IASTIdExpression) node.getOperand1().copy(), vm);
        } else  { //neu la binary expression gan
            left = (IASTExpression) indexInvariant (node.getOperand1().copy(), vm);
        }
        IASTBinaryExpression newNode = factory.newBinaryExpression(node.getOperator(), left, right);
        return newNode;

    }

    private static IASTNode indexExpressionStatement(IASTExpressionStatement node, VariableManager vm) {
        IASTExpression expression = node.getExpression().copy();
        IASTExpressionStatement newNode = factory.newExpressionStatement((IASTExpression) index(expression, vm));
        return newNode;
    }
    private static IASTNode indexExpressionStatementInvariant(IASTExpressionStatement node, VariableManager vm) {
        // After done index a invariant node, increase var that exist in invariant
        for (Variable variable : vm.getVariableList()) {
            if (variable.getIndexInvariant() != -1) {
                //variable.increase();
            }
        }
        IASTExpression expression = node.getExpression().copy();
        IASTExpressionStatement newNode = factory.newExpressionStatement((IASTExpression) indexInvariant(expression, vm));
        return newNode;
    }
    /**
     * danh chi so danh rieng cho khoi tao trong for
     *
     * @param node
     * @param vm
     * @return node moi
     */
    public static IASTNode resetIndex(IASTDeclarationStatement node, VariableManager vm) {
        String name = "";
        IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) node.getDeclaration().copy();
        for (IASTNode run : simpleDecl.getChildren()) {
            if (run instanceof IASTDeclSpecifier) {
            }
            if (run instanceof IASTDeclarator) {
                int reset = -1;
                IASTEqualsInitializer init = (IASTEqualsInitializer) (((IASTDeclarator) run).getInitializer());
                if (init != null) {
                    IASTInitializerClause initClause = (IASTInitializerClause) index(init.getChildren()[0], vm);
                    init.setInitializerClause(initClause);
                    reset = 0;
                }
                IASTName nameVar = ((IASTDeclarator) run).getName();
                name = nameVar.toString();

                Variable var = vm.getVariable(name);
                if (var == null) return node;
                var.setIndex(reset);
                IASTName nameId = factory.newName(var.getVariableWithIndex().toCharArray());
                ((IASTDeclarator) run).setName(nameId);
            }
        }

        IASTDeclarationStatement newNode = factory.newDeclarationStatement(simpleDecl);
        return newNode;
    }

    /**
     * danh chi so cho cau lenh khoi tao
     *
     * @param statement
     * @param vm
     * @return
     */
    private static IASTNode indexDeclarationStatement(IASTDeclarationStatement statement, VariableManager vm) {
        String name = "";
        IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) statement.getDeclaration().copy();
        for (IASTNode run : simpleDecl.getChildren()) {
            // type
            if (run instanceof IASTDeclSpecifier) {
            }

            if (run instanceof IASTDeclarator) {
                IASTEqualsInitializer init = (IASTEqualsInitializer) (((IASTDeclarator) run).getInitializer());
                if (init != null) {
                    IASTInitializerClause initClause = (IASTInitializerClause) index(init.getChildren()[0], vm);
                    init.setInitializerClause(initClause);
                }
                IASTName nameVar = ((IASTDeclarator) run).getName();
                name = nameVar.toString();
                Variable var = vm.getVariable(name);

                // xu ly rieng cho bien cuc bo duoc khoi tao trong ham con
                if (var == null) return statement;

                if (var.getIsDuplicated()) {
                    var.increase();
                }

                IASTName nameId = factory.newName(var.getVariableWithIndex().toCharArray());
                ((IASTDeclarator) run).setName(nameId);
            }
        }
        IASTDeclarationStatement newNode = factory.newDeclarationStatement(simpleDecl);
        return newNode;
    }

    private static IASTNode indexReturnStatement(IASTReturnStatement returnState, VariableManager vm) {
        return null; //da xu ly return statement o returnNode
    }
}
