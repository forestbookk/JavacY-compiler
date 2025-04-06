package frontend.parser.AST.Var;

import frontend.parser.AST.Exp.ConstExp;
import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import frontend.symbol.SymbolManager;
import frontend.symbol.symbols.Array.Array;
import frontend.symbol.symbols.Var.Char;
import frontend.symbol.symbols.Array.CharArray;
import frontend.symbol.symbols.Var.Int;
import frontend.symbol.symbols.Array.IntArray;
import frontend.symbol.symbols.Symbol;
import llvm_ir.IRBuilder;
import llvm_ir.Value;
import llvm_ir.component.Constant;
import llvm_ir.component.Initial;
import llvm_ir.component.type.ArrayType;
import llvm_ir.component.type.ConstantType;
import llvm_ir.component.type.IntegerType;
import llvm_ir.component.type.LLVMType;
import llvm_ir.component.type.PointerType;
import utils.ErrorType;
import utils.Printer;
import utils.SyntaxType;
import utils.TokenType;

import java.util.ArrayList;

import static llvm_ir.component.type.IntegerType.CHAR;
import static llvm_ir.component.type.IntegerType.INT_32;

public class VarDef extends Node {
    Symbol symbol;

    public VarDef(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.VarDef, children, startLine, endLine);
    }

    // VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
    public void createSymbol() {
        String ident = ((Terminator) children.get(0)).getContent();
        if (children.size() > 1 && children.get(1) instanceof Terminator) {
            TokenType type = ((Terminator) children.get(1)).getTokenType();
            /* Ident[ConstExp] or Ident[ConstExp]=InitVal */
            if (type == TokenType.LBRACK) {
                // BfTypeArray
                switch (SymbolManager.bType) {
                    case INTTK -> {
                        symbol = new IntArray(ident);
                    }
                    case CHARTK -> {
                        symbol = new CharArray(ident);
                    }
                }
            }
            /* Ident=InitVal */
            else {
                // BfType
                switch (SymbolManager.bType) {
                    case INTTK -> {
                        symbol = new Int(ident);
                    }
                    case CHARTK -> {
                        symbol = new Char(ident);
                    }
                }
            }
        }
        /* Ident */
        else {
            // BfType
            switch (SymbolManager.bType) {
                case INTTK -> {
                    symbol = new Int(ident);
                }
                case CHARTK -> {
                    symbol = new Char(ident);
                }
            }
        }
        // 设置全局属性
        if (SymbolManager.isGlobal()) {
            symbol.setGlobal(true);
        }
    }

    @Override
    public void analyseSemantic() {
        createSymbol();
        /* check error */
        // b: 名字重定义
        if (!SymbolManager.tryAddSymbol(symbol)) {
            Printer.addError(children.get(0).getEndLine(), ErrorType.b);
        }
        super.analyseSemantic();
    }

    @Override
    public Value genIR() {
        // gen IR
        Value value;
        int numElements = 0;
        // 确定type
        boolean isInt = symbol.isInt();
        LLVMType type = isInt ? INT_32 : CHAR;
        if (symbol instanceof Array) {
            for (Node child : children) {
                if (child instanceof ConstExp) {
                    numElements = ((ConstExp) child).compute();
                }
            }
            type = new ArrayType(((IntegerType) type), numElements);
        }

        // 确定initial。
        // 全局变量，initVal是可以确定的
        if (symbol.isGlobal()) {
            Initial initial = new Initial(new ArrayList<>(), type);
            Node lastNode = children.get(children.size() - 1);
            if (lastNode instanceof InitVal) {
                initial = new Initial(((InitVal) lastNode).compute(), type);
            } else if (!(symbol instanceof Array)) {
                initial.addValue(0);
            }
            symbol.setInitial(initial);
            value = IRBuilder.genNInsGlobalVarDef(new PointerType(type), initial, false);
        }
        // 局部变量
        else {
            value = IRBuilder.genNInsAllocaInstr(type);
            Node lastNode = children.get(children.size() - 1);
            // 有初值
            if (lastNode instanceof InitVal) {
                ArrayList<Value> irList = ((InitVal) lastNode).genIRList();
                int zeroCnt = numElements - irList.size();
                for (int i = 0; i < zeroCnt; i++) {
                    irList.add(new Constant(0));
                }
                // 数组
                if (symbol instanceof Array) {
                    // 初始化数组 GEP+Store
                    for (int i = 0; i < numElements; i++) {
                        Value srcValue = irList.get(i);
                        if (srcValue instanceof Constant) {
                            ((ConstantType) srcValue.getResultType()).setAssignType(isInt ? INT_32 : CHAR);
                        }
                        LLVMType srcType = srcValue.getResultType();
                        // 扩展
                        if (isInt && !srcType.isBaseTypeSame(INT_32)) {
                            srcValue = IRBuilder.genNInsZExtOrTruncInstr(CHAR, srcValue, INT_32, true);
                        }
                        // 截断
                        else if (!isInt && !srcType.isBaseTypeSame(CHAR)) {
                            srcValue = IRBuilder.genNInsZExtOrTruncInstr(INT_32, srcValue, CHAR, false);
                        }
                        IRBuilder.genNInsStoreInstr(srcValue, IRBuilder.genNInsGEPInstr(value, new Constant(i)));
                    }
                }
                // 非数组
                else {
                    Value srcValue = irList.get(0);
                    if (srcValue instanceof Constant) {
                        ((ConstantType) srcValue.getResultType()).setAssignType(isInt ? INT_32 : CHAR);
                    }
                    LLVMType srcType = srcValue.getResultType();

                    // 扩展
                    if (isInt && !srcType.isBaseTypeSame(INT_32)) {
                        srcValue = IRBuilder.genNInsZExtOrTruncInstr(CHAR, srcValue, INT_32, true);
                    }
                    // 截断
                    else if (!isInt && !srcType.isBaseTypeSame(CHAR)) {
                        srcValue = IRBuilder.genNInsZExtOrTruncInstr(INT_32, srcValue, CHAR, false);
                    }
                    IRBuilder.genNInsStoreInstr(srcValue, value);
                }
            }
        }
        // 设置llvm值
        symbol.setLLVMValue(value);

        return null;
    }
}
