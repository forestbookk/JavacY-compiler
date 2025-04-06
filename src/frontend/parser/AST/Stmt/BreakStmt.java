package frontend.parser.AST.Stmt;

import frontend.parser.AST.Node;
import frontend.symbol.SymbolManager;
import llvm_ir.IRBuilder;
import llvm_ir.Value;
import llvm_ir.component.Loop;
import utils.ErrorType;
import utils.Printer;
import utils.SyntaxType;

import java.util.ArrayList;

public class BreakStmt extends Node {
    public BreakStmt(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.Stmt, children, startLine, endLine);
    }

    /* 循环体可能有两种情况：
     *  1. for(...) Block: 需要通过SymbolManager的inLoop判断。事先准备：WholeForStmt的语义分析函数设置Loop，退出时还原。
     *  2. for(...) BreakStmt|ReturnStmt: 需要在上面的基础上特判 */

    @Override
    public void analyseSemantic() {
        if (!SymbolManager.isLastSymbolTableInLoop()) {
            Printer.addError(children.get(0).getEndLine(), ErrorType.m);
        }
    }

    /**
     * LLVM IR 生成：
     * 在循环体 `bodyBB` 中遇到 `break` 时，直接生成跳转指令到 `endBB`。
     * 跳过 `condBB` 和 `updateBB`，退出循环。
     *
     * @return 始终返回 `null`，因为 `break` 不涉及计算结果。
     */
    @Override
    public Value genIR() {
        Loop curLoop = IRBuilder.getCurLoop(); // 获取当前循环上下文
        IRBuilder.genNInsBrInstr(curLoop.getEndBB()); // 生成跳转到 endBB 的无条件跳转指令

        return null;
    }
}
