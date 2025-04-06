package frontend.parser.AST.Var;

import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import frontend.symbol.SymbolManager;
import llvm_ir.Value;
import utils.SyntaxType;

import java.util.ArrayList;

public class BType extends Node {
    public BType(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.BType, children, startLine, endLine);
    }

    @Override
    public void analyseSemantic() {
        SymbolManager.setBType(((Terminator) children.get(0)).getTokenType());
    }
}
