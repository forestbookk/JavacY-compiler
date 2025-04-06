package frontend.parser.AST.Func;

import frontend.parser.AST.Node;
import frontend.parser.AST.Stmt.ReturnStmt;
import frontend.symbol.SymbolManager;
import frontend.symbol.symbols.Func.Func;
import frontend.symbol.symbols.Func.IntFunc;
import frontend.symbol.symbols.Symbol;
import llvm_ir.IRBuilder;
import llvm_ir.Value;
import llvm_ir.component.function.Function;
import utils.ErrorType;
import utils.Printer;
import utils.SyntaxType;
import utils.TokenType;

import java.util.ArrayList;

import static llvm_ir.component.type.IntegerType.INT_32;

public class MainFuncDef extends Node {
    Symbol symbol;

    public MainFuncDef(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.MainFuncDef, children, startLine, endLine);
    }

    // MainFuncDef → 'int' 'main' '(' ')' Block // g
    public void createSymbol() {
        SymbolManager.setFType(TokenType.INTTK);
        symbol = new IntFunc("main");
        ((Func) symbol).setReturnType(TokenType.INTTK);
    }

    @Override
    public void analyseSemantic() {
        SymbolManager.setGlobal(false);
        // analyse Semantic
        // 主函数不进符号表，但其fType用于判断return，需要注册
        createSymbol();
        // 解析函数体
        // 由于主函数无参数，所以不设置params
        super.analyseSemantic();
        SymbolManager.setGlobal(true);
        // g: 有返回值的函数缺少return语句
        /*check error*/
        Node block = children.get(children.size() - 1);
        boolean isCorrect = checkReturnWithValue(block);
        if (!isCorrect) {
            Printer.addError(block.getEndLine(), ErrorType.g);
        }
    }

    @Override
    public Value genIR() {
        // gen IR
        // 创建函数，实例化的同时加入module容器
        Function function = IRBuilder.genNInsFunction(symbol.content, INT_32);
        // 将函数设为symbol的llvm值
        symbol.setLLVMValue(function);

        // generate and insert a new basic block
        IRBuilder.genNInsBasicBlock();

        // 解析函数体
        // 由于主函数无参数，所以不设置params
        super.genIR();


        return null;
    }

    private static boolean checkReturnWithValue(Node block) {
        ArrayList<Node> childrenOfBlock = block.children;
        Node blockItem = childrenOfBlock.get(childrenOfBlock.size() - 2);

        boolean isCorrect = false;
        if (blockItem instanceof BlockItem) {
            if (blockItem.children.get(0) instanceof ReturnStmt) {
                isCorrect = true;
            }
        }
        return isCorrect;
    }
}
