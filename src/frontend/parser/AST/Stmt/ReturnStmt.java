package frontend.parser.AST.Stmt;

import frontend.parser.AST.Exp.Exp;
import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import frontend.symbol.SymbolManager;
import llvm_ir.IRBuilder;
import llvm_ir.Value;
import llvm_ir.component.Constant;
import llvm_ir.component.Instruction;
import llvm_ir.component.type.ConstantType;
import llvm_ir.component.type.IntegerType;
import llvm_ir.component.type.LLVMType;
import utils.ErrorType;
import utils.Printer;
import utils.SyntaxType;
import utils.TokenType;

import java.util.ArrayList;

import static llvm_ir.component.type.IntegerType.CHAR;
import static llvm_ir.component.type.IntegerType.INT_32;

// 'return' [Exp] ';'
public class ReturnStmt extends Node {
    public ReturnStmt(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.Stmt, children, startLine, endLine);
    }

    @Override
    public void analyseSemantic() {
        // 有返回值
        if (SymbolManager.fType != TokenType.VOIDTK) {
            super.analyseSemantic();
            return;
        }

        // 无返回值
        /* check error */
        // f: 无返回值的函数存在不匹配的return语句
        int standard = 1;
        if (((Terminator) (children.get(children.size() - 1))).getTokenType() == TokenType.SEMICN) {
            standard = 2;
        }
        if (children.size() > standard) {
            Printer.addError(children.get(0).getEndLine(), ErrorType.f);
        }
        super.analyseSemantic();
    }

    @Override
    public Value genIR() {
        // 设置return
        IRBuilder.getCurFunction().setHaveRetIfVoid(true);

        Instruction instr;
        if (!IRBuilder.getCurFunction().isTypeVoid()) {
            LLVMType retType = IRBuilder.getCurFunction().getResultType();
            Value returnValue;
            if (children.get(1) instanceof Exp) {
                returnValue = children.get(1).genIR();
            } else {
                returnValue = new Constant(0);
            }
            // 随返回类型设置常数的类型
            if (returnValue instanceof Constant) {
                ((ConstantType) returnValue.getResultType()).setAssignType((IntegerType) retType);
            }
            // 扩展 i8 -> i32
            else if (retType.isInt32() && !returnValue.getResultType().isInt32()) {
                returnValue = IRBuilder.genNInsZExtOrTruncInstr(CHAR, returnValue, INT_32, true);
            }
            // 截断 i32 -> i8
            else if (!retType.isInt32() && returnValue.getResultType().isInt32()) {
                returnValue = IRBuilder.genNInsZExtOrTruncInstr(INT_32, returnValue, CHAR, false);
            }
            instr = IRBuilder.genNInsRetInstr(returnValue);
            return instr;
        }

        instr = IRBuilder.genNInsRetInstr(null);
        return instr;
    }
}
