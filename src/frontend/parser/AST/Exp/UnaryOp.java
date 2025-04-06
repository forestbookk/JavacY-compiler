package frontend.parser.AST.Exp;

import frontend.parser.AST.Node;
import utils.SyntaxType;

import java.util.ArrayList;

public class UnaryOp extends Node {
    public UnaryOp(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.UnaryOp, children, startLine, endLine);
    }

}
