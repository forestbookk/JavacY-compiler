package frontend.parser.AST.Exp;

import frontend.parser.AST.Node;
import llvm_ir.Value;
import utils.SyntaxType;

import java.util.ArrayList;

public class PrimaryExp extends Node {
    public PrimaryExp(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.PrimaryExp, children, startLine, endLine);
    }

    public int compute() {
        if (children.size() > 1) {
            return ((Exp) children.get(1)).compute();
        } else if (children.get(0) instanceof LVal) {
            return ((LVal) children.get(0)).compute();
        } else if (children.get(0) instanceof Number) {
            return ((Number) children.get(0)).getValue();
        }
        // Character
        else {
            return ((Character) children.get(0)).getIntValue();
        }
    }

    // PrimaryExp → '(' Exp ')' | LVal | Number | Character

    @Override
    public Value genIR() {
        // '(' Exp ')'
        if (children.size() > 1) {
            return children.get(1).genIR();
        } else if (children.get(0) instanceof LVal) {
            return ((LVal) children.get(0)).genIR(false);
        } else {
            return children.get(0).genIR();
        }
    }
}
