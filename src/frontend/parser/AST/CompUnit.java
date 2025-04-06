package frontend.parser.AST;

import utils.SyntaxType;

import java.util.ArrayList;

public class CompUnit extends Node {
    public CompUnit(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.CompUnit, children, startLine, endLine);
    }

}
