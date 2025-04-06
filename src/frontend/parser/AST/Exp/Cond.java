package frontend.parser.AST.Exp;

import frontend.parser.AST.Node;
import llvm_ir.Value;
import llvm_ir.component.BasicBlock;
import utils.SyntaxType;

import java.util.ArrayList;

public class Cond extends Node {
    public Cond(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.Cond, children, startLine, endLine);
    }

    // Cond → LOrExp

    public void genIRWithBBs(BasicBlock thenBB, BasicBlock elseBB) {
        ((LOrExp) children.get(0)).genIRWithBBs(thenBB, elseBB);
    }
}
