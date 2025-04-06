package frontend.parser.AST.Func;

import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import frontend.parser.AST.Stmt.ReturnStmt;
import frontend.symbol.SymbolManager;
import frontend.symbol.symbols.Func.CharFunc;
import frontend.symbol.symbols.Func.Func;
import frontend.symbol.symbols.Func.IntFunc;
import frontend.symbol.symbols.Symbol;
import frontend.symbol.symbols.Func.VoidFunc;
import llvm_ir.IRBuilder;
import llvm_ir.Value;
import llvm_ir.component.type.LLVMType;
import llvm_ir.component.function.Function;
import utils.ErrorType;
import utils.Printer;
import utils.SyntaxType;
import utils.TokenType;

import java.util.ArrayList;

import static llvm_ir.component.type.IntegerType.CHAR;
import static llvm_ir.component.type.IntegerType.INT_32;
import static llvm_ir.component.type.IntegerType.VOID;

public class FuncDef extends Node {
    Symbol symbol;

    public FuncDef(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.FuncDef, children, startLine, endLine);
    }

    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // b g
    public void createSymbolAndGenFunction() {
        TokenType fType = ((Terminator) (children.get(0).children.get(0))).getTokenType();
        SymbolManager.setFType(fType);

        String ident = ((Terminator) children.get(1)).getContent();
        switch (SymbolManager.fType) {
            case INTTK -> {
                symbol = new IntFunc(ident);
            }
            case CHARTK -> {
                symbol = new CharFunc(ident);
            }
            case VOIDTK -> {
                symbol = new VoidFunc(ident);
            }
        }
        ((Func) symbol).setReturnType(fType);
    }

    @Override
    public void analyseSemantic() {
        SymbolManager.setGlobal(false);

        createSymbolAndGenFunction();
        SymbolManager.setLastFunc((Func) symbol);

        /* check error */
        // b：名字重定义 **函数名或者变量名在当前作用域下重复定义**
        if (!SymbolManager.tryAddSymbol(symbol)) {
            Printer.addError(children.get(1).getEndLine(), ErrorType.b);
        }

        // 解析children，同时设置Param
        super.analyseSemantic();

        SymbolManager.setGlobal(true);

        if (SymbolManager.fType == TokenType.VOIDTK) {
            return;
        }
        /* check error */
        // g：有返回值的函数缺少return语句
        Node block = children.get(children.size() - 1);
        boolean isCorrect = checkReturnWithValue(block);
        if (!isCorrect) {
            Printer.addError(block.getEndLine(), ErrorType.g);
        }
    }

    @Override
    public Value genIR() {
        // 生成llvm-function
        LLVMType funcType = (symbol instanceof VoidFunc) ? VOID : (
                (symbol instanceof CharFunc) ? CHAR : INT_32
        );
        Function function = IRBuilder.genNInsFunction(symbol.content, funcType);
        // 将函数设为symbol的llvm值
        symbol.setLLVMValue(function);

        // generate and insert a new basic block
        IRBuilder.genNInsBasicBlock();
        super.genIR();

        // 判断函数是否以return结尾，如果没有，且函数是void类型，则插入ret
        if (!IRBuilder.getCurFunction().isHaveRetIfVoid()) {
            IRBuilder.genNInsRetInstr(null);
        }

        return null;
    }

    private static boolean checkReturnWithValue(Node block) {
        Node blockItem = block.children.get(block.children.size() - 2);

        boolean isCorrect = false;
        if (blockItem instanceof BlockItem) {
            if (blockItem.children.get(0) instanceof ReturnStmt) {
                isCorrect = true;
            }
        }
        return isCorrect;
    }
}
