package frontend.parser.AST.Stmt;

import frontend.parser.AST.Exp.LVal;
import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import frontend.symbol.SymbolManager;
import frontend.symbol.symbols.Array.ConstCharArray;
import frontend.symbol.symbols.Array.ConstIntArray;
import frontend.symbol.symbols.Symbol;
import frontend.symbol.symbols.Var.ConstChar;
import frontend.symbol.symbols.Var.ConstInt;
import llvm_ir.IRBuilder;
import llvm_ir.Value;
import llvm_ir.component.type.IntegerType;
import utils.ErrorType;
import utils.Printer;
import utils.SyntaxType;

import java.util.ArrayList;

public class GetCharStmt extends Node {
    public GetCharStmt(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.Stmt, children, startLine, endLine);
    }

    @Override
    public void analyseSemantic() {
        /* check error */
        // h
        String ident = ((Terminator) children.get(0).children.get(0)).getContent();
        Symbol searchRes = SymbolManager.getSymbolByNameInGlobalStack(ident);
        if (searchRes == null) {/*交给children的LVal处理*/}
        /* 此处只处理error h */
        else {
            if (searchRes instanceof ConstChar || searchRes instanceof ConstInt ||
                    searchRes instanceof ConstCharArray || searchRes instanceof ConstIntArray) {
                Printer.addError(children.get(0).getEndLine(), ErrorType.h);
            }
        }

        super.analyseSemantic();
    }

    @Override
    public Value genIR() {
        Value lValAddress = ((LVal) children.get(0)).genIR(true);
        // 由于getchar返回值是i32类型的，接下来需要将其截断(Trunc)为i8类型
        Value getCharValue = IRBuilder.genNInsZExtOrTruncInstr(IntegerType.INT_32,
                IRBuilder.genNINsGetIntOrChar(false),
                IntegerType.CHAR, false);
        return IRBuilder.genNInsStoreInstr(
                getCharValue,
                lValAddress);
    }
}
