package com.mtv.encode.cfg.index;

public class Variable {

    private String type;
    private String name;
    private int index = -1; //mac dinh -1
    private int indexInvariant = -1; //the index use in java.invariant
    private boolean isDuplicated;

    public Variable(String type, String name) {
        this.type = type;
        this.name = name;
        this.isDuplicated = false;
    }

    public Variable(String type, String name, int index) {
        this.type = type;
        this.name = name;
        this.index = index;
        this.isDuplicated = false;
    }

    public Variable(Variable other) {
        name = other.name;
        type = other.type;
        index = other.index;
        isDuplicated = other.isDuplicated;
    }

    public boolean getIsDuplicated() {
        return isDuplicated;
    }

    public void setIsDuplicated(boolean restart) {
        this.isDuplicated = restart;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void increase() {
        index++;
    }
    public void decrease() {
        index--;
    }
    public void setIndexInvariant(int i) {
        indexInvariant = i;
    }
    public int getIndexInvariant() {
        return indexInvariant;
    }

    // -3 danh dau bien nhan gia tri tra ve cua ham
    public String getVariableWithIndex() {
        if (index == -3) return name;
        return name + "_" + index;
    }

    public Variable clone() {
        return new Variable(this);
    }

    public String toString() {
        return "type: " + type + ", name: " + name + ", index: " + index;
    }

}
