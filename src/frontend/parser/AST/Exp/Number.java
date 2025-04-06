package frontend.parser.AST.Exp;

import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import llvm_ir.Value;
import llvm_ir.component.Constant;
import utils.SyntaxType;

import java.util.ArrayList;

public class Number extends Node {
    private int value;

    public Number(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.Number, children, startLine, endLine);
        this.value = Integer.parseInt(((Terminator) children.get(0)).getContent());
    }

    public int getValue() {
        return value;
    }

    @Override
    public Value genIR() {
        return new Constant( value);
    }
}
