package frontend.parser.AST.Func;

import frontend.parser.AST.Node;
import frontend.symbol.SymbolManager;
import llvm_ir.Value;
import utils.SyntaxType;

import java.util.ArrayList;

public class Block extends Node {
    public Block(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.Block, children, startLine, endLine);
    }

    @Override
    public void analyseSemantic() {
        SymbolManager.enterNewSymbolTable();
        if (SymbolManager.lastFunc != null &&
                !SymbolManager.lastFunc.isParamAlreadyAddedIntoSymbolTable()) {
            SymbolManager.dealFuncFParams();
        }
        super.analyseSemantic();
        SymbolManager.exitSymbolTable();
        return;
    }
}
