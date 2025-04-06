package frontend.symbol.symbols;

import frontend.symbol.symbols.Array.CharArray;
import frontend.symbol.symbols.Array.ConstCharArray;
import frontend.symbol.symbols.Array.ConstIntArray;
import frontend.symbol.symbols.Array.IntArray;
import frontend.symbol.symbols.Var.Char;
import frontend.symbol.symbols.Var.ConstChar;
import frontend.symbol.symbols.Var.ConstInt;
import frontend.symbol.symbols.Var.Int;
import llvm_ir.Value;
import llvm_ir.component.Initial;

public class Symbol {
    public String content;
    protected Value LLVMValue;
    protected Initial initial;
    protected boolean isGlobal = false;

    public Symbol(String content) {
        this.content = content;
    }

    public String getSymbolInfo() {
        return content;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setLLVMValue(Value LLVMValue) {
        this.LLVMValue = LLVMValue;
    }

    public Value getLLVMValue() {
        return LLVMValue;
    }

    public void setInitial(Initial initial) {
        this.initial = initial;
    }

    public int getInitialValue(int pos) {
        return initial.getValue(pos);
    }

    public boolean isInt() {
        return this instanceof Int || this instanceof IntArray ||
                this instanceof ConstInt || this instanceof ConstIntArray;
    }

    public boolean isChar() {
        return this instanceof Char || this instanceof CharArray ||
                this instanceof ConstChar || this instanceof ConstCharArray;
    }

    public boolean isConstant() {
        return this instanceof ConstCharArray || this instanceof ConstChar ||
                this instanceof ConstInt || this instanceof ConstIntArray;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Symbol)) {
            return false;
        }
        return this.content.equals(((Symbol) obj).content);
    }
}
