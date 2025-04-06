package frontend.parser.AST.Stmt;

import frontend.parser.AST.Node;
import utils.SyntaxType;

import java.util.ArrayList;

public class BlockStmt extends Node {
    public BlockStmt(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.Stmt, children, startLine, endLine);
    }

}
