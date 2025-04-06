package frontend.parser.AST.Exp;

import frontend.parser.AST.Node;
import llvm_ir.IRBuilder;
import llvm_ir.Value;
import llvm_ir.component.BasicBlock;
import llvm_ir.component.Instruction;
import utils.SyntaxType;

import java.util.ArrayList;

public class LAndExp extends Node {
    public LAndExp(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.LAndExp, children, startLine, endLine);
    }

    // LAndExp → EqExp | LAndExp '&&' EqExp

    public void genIRWithBBs(BasicBlock thenBB, BasicBlock elseBB) {
        for (int i = 0; i < children.size(); i += 2) {
            if (i != children.size() - 1) {
                BasicBlock nextCondBB = new BasicBlock(IRBuilder.genBBName());
                IRBuilder.genNInsCondBrInstr(children.get(i).genIR(), nextCondBB, elseBB);
                IRBuilder.connectBasicBlockWithIRBuilder(nextCondBB);
            } else {
                IRBuilder.genNInsCondBrInstr(children.get(i).genIR(), thenBB, elseBB);
            }
        }
    }
}
