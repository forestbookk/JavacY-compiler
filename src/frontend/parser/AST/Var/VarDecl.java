package frontend.parser.AST.Var;

import frontend.parser.AST.Node;
import utils.SyntaxType;

import java.util.ArrayList;

public class VarDecl extends Node {
    public VarDecl(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.VarDecl, children, startLine, endLine);
    }
}
