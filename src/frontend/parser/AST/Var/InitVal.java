package frontend.parser.AST.Var;

import frontend.parser.AST.Exp.Exp;
import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import llvm_ir.Value;
import llvm_ir.component.Constant;
import utils.SyntaxType;
import utils.TokenType;

import java.util.ArrayList;

// InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
public class InitVal extends Node {
    public InitVal(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.InitVal, children, startLine, endLine);
    }

    public ArrayList<Integer> compute() {
        ArrayList<Integer> res = new ArrayList<>();

        // Exp
        if (children.get(0) instanceof Exp) {
            res.add(((Exp) children.get(0)).compute());
        }
        // StringConst
        else if (children.get(0) instanceof Terminator && ((Terminator) children.get(0)).getTokenType() == TokenType.STRCON) {
            return (((Terminator) children.get(0)).getToken().getAsciiList());
        }
        // '{' [ Exp { ',' Exp } ] '}'
        else {
            if (((Terminator) children.get(0)).getTokenType() == TokenType.LBRACE &&
                    !(children.get(1) instanceof Terminator)) {
                for (int i = 1; i < children.size(); i += 2) {
                    res.add(((Exp) children.get(i)).compute());
                }
            }
        }
        return res;
    }

    public ArrayList<Value> genIRList() {
        ArrayList<Value> res = new ArrayList<>();
        for (Node child : children) {
            if (child instanceof Exp) {
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
