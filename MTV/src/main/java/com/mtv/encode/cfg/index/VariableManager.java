package com.mtv.encode.cfg.index;

import com.mtv.encode.ast.ASTFactory;
import com.mtv.encode.ast.FunctionHelper;
import org.eclipse.cdt.core.dom.ast.*;

import java.util.ArrayList;

/**
 * new VariableManager()
 * new VariableManager(IASTFunctionDefinition func)
 * build(IASTFunctionDefinition func): void
 * getVariableList() : ArrayList<Variable>
 * getVariable(int index) : Variable
 * getVariable(String name) : Variable
 * addVariable(Variable var) : void
 * addVariable(String type, String name, String funcName, int index) : void
 *
 * @author va
 */

public class VariableManager {
    private ArrayList<Variable> variableList;

    public VariableManager() {
        this.variableList = new ArrayList<>();
    }

    /**
     * @return
     */
    public ArrayList<Variable> getVariableList() {
        return variableList;
    }

    public void setVariableList(ArrayList<Variable> variableList) {
        this.variableList = variableList;
    }

    /**
     * them bien moi vao day VariableManager
     * luu y neu da co trong danh sach => danh dau isDuplicated == true;
     *
     * @param otherVM
     */
    public void concat(VariableManager otherVM) {
        ArrayList<Variable> otherList = otherVM.getVariableList();
        for (Variable var : otherList) {
            if (!(isHas(var.getName()))) {
                this.getVariableList().add(var);
            } else {
                var.setIsDuplicated(true);
            }
        }
    }


    public Variable getVariable(int index) {
        return variableList.get(index);
    }

    public Variable getVariable(String name) {
        for (Variable var : variableList) {
            if (var.getName().equals(name)) {
                return var;
            }
        }
        return null;
    }

    /**
     * kiem tra xem bien truyen vao da co trong danh sach chua?
     *
     * @param name
     * @return true/ false
     */
    public boolean isHas(String name) {
        if (this.variableList == null) return false;
        for (Variable var : this.variableList) {
            if (name.equals(var.getName())) {
                return true;
            }
        }
        return false;
    }

    public int getSize() {
        return this.variableList.size();
    }

    /**
     * tao variableList voi dau vao
     */

    public void build(IASTFunctionDefinition func) {
        ArrayList<Variable> params = getParameters(func);
        ArrayList<Variable> localVars = new ArrayList<>();
        ArrayList<Variable> globalVars = getGlobalVars(func);

        String funcName = FunctionHelper.getShortenName(func);

        localVars = getLocalVar(func, funcName, localVars);
        for (Variable var : globalVars) {
            this.variableList.add(var);
        }
        for (Variable param : params) {
            this.variableList.add(param);
        }
        for (Variable var : localVars) {
            this.variableList.add(var);
        }
        //Neu function khac void, them bien return
        if (!(FunctionHelper.getFunctionType(func).equals("void"))) {
            this.variableList.add(getReturn(func));
        }

    }

    /**
     * Lay cac bien global
     *
     * @param func
     * @return
     */

    private ArrayList<Variable> getGlobalVars(IASTFunctionDefinition func) {
        ASTFactory ast = new ASTFactory(func.getTranslationUnit());
        ArrayList<Variable> variableList = new ArrayList<>();
        ArrayList<IASTDeclaration> declarations = ast.getGlobalVarList();
        for (IASTDeclaration declaration : declarations) {
            IASTSimpleDeclaration simpDecl = (IASTSimpleDeclaration) declaration;
            String type = simpDecl.getDeclSpecifier().toString();
            //Chu y truong hop int a, b, c, ...;
            for (IASTDeclarator declarator : simpDecl.getDeclarators()) {
                String nameVar = declarator.getName().toString();
                Variable var = new Variable(type, nameVar);
                variableList.add(var);
            }
        }
        return variableList;
    }

    private Variable getReturn(IASTFunctionDefinition func) {
        IASTNode typeFunction = func.getDeclSpecifier();
        Variable var = new Variable(typeFunction.getRawSignature(), "return" + "_" + FunctionHelper.getShortenName(func));

        return var;
    }

    /**
     * tra ve danh sach tham so truyen vao ham (neu co)

     * @return List Variable
     */
    private ArrayList<Variable> getParameters(IASTFunctionDefinition func) {
        ArrayList<Variable> params = new ArrayList<>();
        IASTNode[] nodes = func.getDeclarator().getChildren();

        IASTParameterDeclaration paramDecl = null;
        for (IASTNode node : nodes) {
            if (node instanceof IASTParameterDeclaration) {
                if (node.getRawSignature().equals("void")) {
                    return params;
                }
                paramDecl = (IASTParameterDeclaration) node;

                IASTNode[] paramDecls = paramDecl.getChildren();
                for (int i = 0; i < paramDecls.length; i++) {
                    if (paramDecls[i] instanceof IASTSimpleDeclSpecifier
                            && paramDecls[i + 1] instanceof IASTDeclarator) {
                        Variable var = new Variable(paramDecls[i].getRawSignature(),
                                paramDecls[i + 1].getRawSignature() + "_" +
                                        FunctionHelper.getShortenName(func), 0);
                        params.add(var);
                    }
                }
            }
        }
        return params;
    }

    /**
     * @param node
     * @param funcName
     * @param list
     * @return danh sach bien dia phuong trong ham
     */
    private ArrayList<Variable> getLocalVar(IASTNode node, String funcName, ArrayList<Variable> list) {
        // find init
        IASTNode[] children = node.getChildren();
        String type;
        String name;
        Variable var;

        IASTDeclarator[] declarations;

        if (node instanceof IASTSimpleDeclaration) {
            int init = -1;
            type = ((IASTSimpleDeclaration) node).getDeclSpecifier().getRawSignature();

            declarations = ((IASTSimpleDeclaration) node).getDeclarators();
            name = declarations[0].getName().getRawSignature();

            var = new Variable(type, name + "_" + FunctionHelper.getShortenName(funcName), init);
            list.add(var);
        }
        /*
         * Neu co chua loi goi ham, them vao bien co ten giong loi goi ham
         * Ex: a = sum(3); -> a = sum_3
         */
        if (node instanceof IASTFunctionCallExpression) {
            if (!((IASTFunctionCallExpression) node).getExpressionType().toString().equals("void")) {
                //System.err.println(((IASTFunctionCallExpression) node).getExpressionType());
                IASTFunctionCallExpression call = (IASTFunctionCallExpression) node;
                String callName = call.getFunctionNameExpression().toString();
                String params = "";
                if (call.getArguments().length > 0) {
                    for (IASTNode param : FunctionHelper.getArguments(call)) {
                        params += "_" + param.toString();
                    }

                }
                var = new Variable(call.getExpressionType().toString(), callName + params, 0);
                list.add(var);
            }
        }
        // return

        for (IASTNode run : children) {
            getLocalVar(run, funcName, list);
        }
        return list;
    }

}
