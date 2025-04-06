package frontend.parser.AST.Func;

import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import frontend.symbol.SymbolManager;
import utils.SyntaxType;

import java.util.ArrayList;

public class FuncType extends Node {
    public FuncType(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.FuncType, children, startLine, endLine);
    }
}
