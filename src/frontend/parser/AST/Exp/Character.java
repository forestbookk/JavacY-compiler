package frontend.parser.AST.Exp;

import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import llvm_ir.Value;
import llvm_ir.component.Constant;
import utils.SyntaxType;

import java.util.ArrayList;

public class Character extends Node {
    private int intValue;

    public Character(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.Character, children, startLine, endLine);
        // TO/DO: Character是怎么存的？
        this.intValue = (((Terminator) children.get(0)).getToken()).getAsciiList().get(0);
    }

    public int getIntValue() {
        return intValue;
    }

    @Override
    public Value genIR() {
        return new Constant(intValue);
    }
}
