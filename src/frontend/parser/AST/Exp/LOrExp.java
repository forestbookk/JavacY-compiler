package frontend.parser.AST.Exp;

import frontend.parser.AST.Node;
import llvm_ir.IRBuilder;
import llvm_ir.Value;
import llvm_ir.component.BasicBlock;
import utils.SyntaxType;

import java.util.ArrayList;

public class LOrExp extends Node {
    public LOrExp(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.LOrExp, children, startLine, endLine);
    }

    // LOrExp → LAndExp | LOrExp '||' LAndExp

    public void genIRWithBBs(BasicBlock thenBB, BasicBlock elseBB) {
        for (int i = 0; i < children.size(); i += 2) {
            if (i != children.size() - 1) {
                BasicBlock nextCondBB = new BasicBlock(IRBuilder.genBBName());
                ((LAndExp)children.get(i)).genIRWithBBs(thenBB, nextCondBB);
                IRBuilder.connectBasicBlockWithIRBuilder(nextCondBB);
            } else {
                ((LAndExp)children.get(i)).genIRWithBBs(thenBB, elseBB);
            }
        }
    }
}
