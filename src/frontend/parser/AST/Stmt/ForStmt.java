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
import llvm_ir.component.Constant;
import llvm_ir.component.type.ConstantType;
import llvm_ir.component.type.IntegerType;
import llvm_ir.component.type.LLVMType;
import utils.ErrorType;
import utils.Printer;
import utils.SyntaxType;

import java.util.ArrayList;

public class ForStmt extends Node {
    public ForStmt(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.ForStmt, children, startLine, endLine);
    }

    // ForStmt → LVal '=' Exp

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
        Value lVal = ((LVal) children.get(0)).genIR(true);
        boolean isLValTypeInt = lVal.getResultType().isInt32();

        Value exp = children.get(2).genIR();
        LLVMType expType = exp.getResultType();
        if (exp instanceof Constant) {
            ((ConstantType) exp.getResultType()).setAssignType(isLValTypeInt ? IntegerType.INT_32 : IntegerType.CHAR);
        }

        // 截断
        if (!isLValTypeInt && !expType.isBaseTypeSame(IntegerType.CHAR)) {
            exp = IRBuilder.genNInsZExtOrTruncInstr(IntegerType.INT_32, exp, IntegerType.CHAR, false);
        }
        // 扩展
        else if (isLValTypeInt && !expType.isBaseTypeSame(IntegerType.INT_32)) {
            exp = IRBuilder.genNInsZExtOrTruncInstr(IntegerType.CHAR, exp, IntegerType.INT_32, true);
        }

        return IRBuilder.genNInsStoreInstr(exp, lVal);
    }
}
