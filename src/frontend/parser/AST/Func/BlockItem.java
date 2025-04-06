package frontend.parser.AST.Func;

import frontend.parser.AST.Node;
import utils.SyntaxType;

import java.util.ArrayList;

public class BlockItem extends Node {
    public BlockItem(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.BlockItem, children, startLine, endLine);
    }
}
