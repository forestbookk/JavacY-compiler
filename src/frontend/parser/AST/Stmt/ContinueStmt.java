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

public class ContinueStmt extends Node {
    public ContinueStmt(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.Stmt, children, startLine, endLine);
    }

    @Override
    public void analyseSemantic() {
        if (!SymbolManager.isLastSymbolTableInLoop()) {
            Printer.addError(children.get(0).getEndLine(), ErrorType.m);
        }
    }

    /**
     * 在 bodyBB 中遇到 continue 时，直接跳转到 updateBB。
     * 跳过当前迭代的剩余代码，但仍会执行 updateBB 和 condBB。
     *
     * @return null
     */
    @Override
    public Value genIR() {
        Loop curLoop = IRBuilder.getCurLoop();
        IRBuilder.genNInsBrInstr(curLoop.getUpdateBB());

        return null;
    }
}
