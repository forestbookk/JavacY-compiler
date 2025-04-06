package frontend.parser.AST.Exp;

import frontend.parser.AST.Node;
import llvm_ir.Value;
import utils.SyntaxType;

import java.util.ArrayList;

public class ConstExp extends Node {
    public ConstExp(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.ConstExp, children, startLine, endLine);
    }

    // ConstExp → AddExp

    public int compute() {
        return ((AddExp) children.get(0)).compute();
    }

    @Override
    public Value genIR() {
        return children.get(0).genIR();
    }
}
