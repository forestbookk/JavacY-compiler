package frontend.parser.AST.Var;

import frontend.parser.AST.Exp.ConstExp;
import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import llvm_ir.Value;
import llvm_ir.component.Constant;
import utils.SyntaxType;
import utils.TokenType;

import java.util.ArrayList;

public class ConstInitVal extends Node {
    public ConstInitVal(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.ConstInitVal, children, startLine, endLine);
    }

    // ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' |  StringConst

    public ArrayList<Integer> compute() {
        ArrayList<Integer> res = new ArrayList<>();
        // ConstExp
        if (children.get(0) instanceof ConstExp) {
            res.add(((ConstExp) children.get(0)).compute());
        }
        // StringConst
        else if (children.get(0) instanceof Terminator && ((Terminator) children.get(0)).getTokenType() == TokenType.STRCON) {
            return (((Terminator) children.get(0)).getToken().getAsciiList());
        }
        // '{' [ ConstExp { ',' ConstExp } ] '}'
        else {
            if (((Terminator) children.get(0)).getTokenType() == TokenType.LBRACE &&
                    !(children.get(1) instanceof Terminator)) {
                for (int i = 1; i < children.size(); i += 2) {
                    res.add(((ConstExp) children.get(i)).compute());
                }
            }
        }
        return res;
    }

    public ArrayList<Value> genIRList() {
        ArrayList<Value> res = new ArrayList<>();
        for (Node child : children) {
            if (child instanceof ConstExp) {
                res.add(child.genIR());
            } else if (child instanceof Terminator && ((Terminator) child).getTokenType() == TokenType.STRCON) {
                ArrayList<Integer> asciiList = ((Terminator) child).getToken().getAsciiList();
                for (Integer ascii : asciiList) {
                    res.add(new Constant(ascii));
                }
                break;
            }
        }
        return res;
    }
}
