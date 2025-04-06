package frontend.parser.AST.Func;

import frontend.parser.AST.Node;
import utils.SyntaxType;

import java.util.ArrayList;

public class FuncRParams extends Node {
    public FuncRParams(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.FuncRParams, children, startLine, endLine);
    }
}
