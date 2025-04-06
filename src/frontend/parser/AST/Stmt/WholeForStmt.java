package frontend.parser.AST.Stmt;

import frontend.parser.AST.Exp.Cond;
import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import frontend.symbol.SymbolManager;
import llvm_ir.IRBuilder;
import llvm_ir.Value;
import llvm_ir.component.BasicBlock;
import utils.SyntaxType;
import utils.TokenType;

import java.util.ArrayList;

public class WholeForStmt extends Node {
    public WholeForStmt(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.Stmt, children, startLine, endLine);
    }

    /* 循环体可能有两种情况：
     *  1. for(...) BlockStmt: 需要通过SymbolManager的inLoop判断。事先准备：WholeForStmt的语义分析函数设置Loop，退出时还原。
     *  2. for(...) BreakStmt|ReturnStmt: 需要在上面的基础上特判 */

    /* 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt */
    @Override
    public void analyseSemantic() {
        Node stmt = children.get(children.size() - 1);
        if (stmt instanceof BreakStmt || stmt instanceof ContinueStmt) {
            for (int i = 0; i < children.size() - 1; i++) {
                children.get(i).analyseSemantic();
            }
        } else if (stmt instanceof BlockStmt) {
            SymbolManager.setLoopForNextBlock(true);
            super.analyseSemantic();
            SymbolManager.setLoopForNextBlock(false);
        } else {
            super.analyseSemantic();
        }
    }

    @Override
    public Value genIR() {
        int semicnCnt = 0;
        BasicBlock condBB, updateBB, bodyBB, endBB;

        bodyBB = new BasicBlock(IRBuilder.genBBName() + "_body");
        condBB = new BasicBlock(IRBuilder.genBBName() + "_cond");
        updateBB = new BasicBlock(IRBuilder.genBBName() + "_update");
        endBB = new BasicBlock(IRBuilder.genBBName() + "_end");

        // 进入循环，新建loop
        IRBuilder.genNInsLoop(condBB, bodyBB, updateBB, endBB);

        for (int i = 0; i < children.size(); i++) {
            // init
            if (children.get(i) instanceof Terminator && ((Terminator) children.get(i)).getTokenType() == TokenType.LPARENT) {
                // 解析ForStmt children.get(i+1)
                if (children.get(i + 1) instanceof ForStmt) {
                    children.get(i + 1).genIR();
                }
                // 跳转到条件判断块：cond
                IRBuilder.genNInsBrInstr(condBB);
            }
            // cond | update
            else if (children.get(i) instanceof Terminator && ((Terminator) children.get(i)).getTokenType() == TokenType.SEMICN) {
                semicnCnt++;
                // Cond
                if (semicnCnt == 1) {
                    // 设置condBB为当前块
                    IRBuilder.connectBasicBlockWithIRBuilder(condBB);
                    // 解析Cond
                    if (children.get(i + 1) instanceof Cond) {
                        ((Cond) children.get(i + 1)).genIRWithBBs(bodyBB, endBB);
                    }
                    // cond缺省：跳转body
                    else {
                        IRBuilder.genNInsBrInstr(bodyBB);
                    }
                }
                // update
                else {
                    // 设置updateBB为当前块
                    IRBuilder.connectBasicBlockWithIRBuilder(updateBB);
                    // 解析ForStmt
                    if (children.get(i + 1) instanceof ForStmt) {
                        children.get(i + 1).genIR();
                    }
                    IRBuilder.genNInsBrInstr(condBB);
                }
            } else if (children.get(i) instanceof Terminator && ((Terminator) children.get(i)).getTokenType() == TokenType.RPARENT) {
                IRBuilder.connectBasicBlockWithIRBuilder(bodyBB);
                Node stmt = children.get(i + 1);
                stmt.genIR();
                IRBuilder.genNInsBrInstr(updateBB);
            }
        }

        // 离开循环
        IRBuilder.exitCurLoop();

        IRBuilder.connectBasicBlockWithIRBuilder(endBB);
        return null;
    }
}
