package frontend.parser.AST.Var;

import frontend.parser.AST.Node;
import utils.SyntaxType;

import java.util.ArrayList;

public class Decl extends Node {
    public Decl(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.Decl, children, startLine, endLine);
    }
}
