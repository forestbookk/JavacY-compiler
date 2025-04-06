package frontend.parser.AST.Func;

import frontend.parser.AST.Node;
import utils.SyntaxType;

import java.util.ArrayList;

public class FuncFParams extends Node {
    public FuncFParams(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.FuncFParams, children, startLine, endLine);
    }
}
