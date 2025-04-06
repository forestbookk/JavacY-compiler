package frontend.parser.AST;

import frontend.parser.Parser;
import llvm_ir.Value;
import utils.ParamType;
import utils.SyntaxType;

import java.util.ArrayList;

public class Node {
    private int startLine;
    private int endLine;
    private SyntaxType type;

    public ArrayList<Node> children;

    public Node(SyntaxType type, ArrayList<Node> children, int startLine, int endLine) {
        this.type = type;
        this.children = children;
        this.startLine = startLine;
        this.endLine = endLine;
        Parser.addASTNodes(this);
    }

    public int getEndLine() {
        return this.endLine;
    }

    public void analyseSemantic() {
        if (children.isEmpty()) {
            return;
        }
        for (Node child : children) {
            child.analyseSemantic();
        }
    }

    public Value genIR() {
        if (children.isEmpty()) {
            return null;
        }
        for (Node child : children) {
            child.genIR();
        }
        return null;
    }



    /* to check error e */
    public ParamType getTypeAsParam() {
        ParamType res = ParamType.VAR;
        for (Node node : children) {
            res = node.getTypeAsParam();
            if (res != ParamType.VAR) {
                break;
            }
        }
        return res;
    }

}
