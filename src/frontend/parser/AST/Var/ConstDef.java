package frontend.parser.AST.Var;

import frontend.parser.AST.Exp.ConstExp;
import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import frontend.symbol.SymbolManager;
import frontend.symbol.symbols.Array.Array;
import frontend.symbol.symbols.Var.ConstChar;
import frontend.symbol.symbols.Array.ConstCharArray;
import frontend.symbol.symbols.Var.ConstInt;
import frontend.symbol.symbols.Array.ConstIntArray;
import frontend.symbol.symbols.Symbol;
import llvm_ir.IRBuilder;
import llvm_ir.Value;
import llvm_ir.component.Initial;
import llvm_ir.component.type.ArrayType;
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

public class ConstDef extends Node {
    private Symbol symbol;

    public ConstDef(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.ConstDef, children, startLine, endLine);
    }

    // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal // b
    public void createSymbol() {
        String ident = ((Terminator) children.get(0)).getContent();
        if (children.get(1) instanceof Terminator) {
            TokenType type = ((Terminator) children.get(1)).getTokenType();
            if (type == TokenType.LBRACK) {
                // ConstBfTypeArray
                switch (SymbolManager.bType) {
                    case INTTK -> {
                        symbol = new ConstIntArray(ident);
                    }
                    case CHARTK -> {
                        symbol = new ConstCharArray(ident);
                    }
                }
            } else {
                // ConstBfType
                switch (SymbolManager.bType) {
                    case INTTK -> {
                        symbol = new ConstInt(ident);
                    }
                    case CHARTK -> {
                        symbol = new ConstChar(ident);
                    }
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
        Initial initial = new Initial(new ArrayList<>(), type);
        Node lastNode = children.get(children.size() - 1);
        if (lastNode instanceof ConstInitVal) {
            initial = new Initial(((ConstInitVal) lastNode).compute(), type);
        } else if (!(symbol instanceof Array)) {
            initial.addValue(0);
        }
        symbol.setInitial(initial);
        value = IRBuilder.genNInsGlobalVarDef(new PointerType(type), initial, true);


        // 设置llvm值
        symbol.setLLVMValue(value);

        return null;
    }
}
