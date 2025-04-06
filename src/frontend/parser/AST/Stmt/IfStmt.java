package frontend.parser.AST.Stmt;

import frontend.parser.AST.Exp.Cond;
import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import llvm_ir.IRBuilder;
import llvm_ir.Value;
import llvm_ir.component.BasicBlock;
import llvm_ir.component.Instruction;
import utils.SyntaxType;

import java.util.ArrayList;

public class IfStmt extends Node {
    public IfStmt(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.Stmt, children, startLine, endLine);
    }

    //'if' '(' Cond ')' Stmt [ 'else' Stmt ]

    @Override
    public Value genIR() {
        BasicBlock thenBB/*处理if分支的代码*/, elseBB/*处理else分支的代码*/, mergeBB/*then和else分支的汇合点*/;
        thenBB = new BasicBlock(IRBuilder.genBBName());
        // there is else
        if (children.size() > 5) {
            elseBB = new BasicBlock(IRBuilder.genBBName());
            mergeBB = new BasicBlock(IRBuilder.genBBName());
            ((Cond) children.get(2)).genIRWithBBs(thenBB, elseBB); // Cond
            for (int i = 3; i < children.size(); i++) {
                if (!(children.get(i) instanceof Terminator)) {
                    IRBuilder.connectBasicBlockWithIRBuilder(thenBB);
                    children.get(i).genIR();
                    // 解析thenBB内容后，跳转到mergeBB
                    IRBuilder.genNInsBrInstr(mergeBB);
                    IRBuilder.connectBasicBlockWithIRBuilder(elseBB);
                    children.get(i + 2).genIR();
                    // 解析elseBB内容后，跳转到mergeBB
                    IRBuilder.genNInsBrInstr(mergeBB);
                    break;
                }
            }

            IRBuilder.connectBasicBlockWithIRBuilder(mergeBB);
            return null;
        }

        // there is no else
        mergeBB = new BasicBlock(IRBuilder.genBBName());
        ((Cond) children.get(2)).genIRWithBBs(thenBB, mergeBB); // Cond
        for (int i = 3; i < children.size(); i++) {
            if (!(children.get(i) instanceof Terminator)) {
                IRBuilder.connectBasicBlockWithIRBuilder(thenBB);
                children.get(i).genIR();
                // 解析thenBB内容后，跳转到mergeBB
                IRBuilder.genNInsBrInstr(mergeBB);
            }
        }

        IRBuilder.connectBasicBlockWithIRBuilder(mergeBB);
        return null;
    }
}
