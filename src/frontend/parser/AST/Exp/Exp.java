package frontend.parser.AST.Exp;

import frontend.parser.AST.Node;
import llvm_ir.Value;
import utils.SyntaxType;

import java.util.ArrayList;

public class Exp extends Node {
    public Exp(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.Exp, children, startLine, endLine);
    }

    public int compute() {
        return ((AddExp) children.get(0)).compute();
    }

    @Override
    public Value genIR() {
        return children.get(0).genIR();
    }
}
