package frontend.parser.AST.Exp;

import frontend.parser.AST.Func.FuncRParams;
import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import frontend.symbol.SymbolManager;
import frontend.symbol.symbols.Func.Func;
import frontend.symbol.symbols.Symbol;
import llvm_ir.IRBuilder;
import llvm_ir.Value;
import llvm_ir.component.Constant;
import llvm_ir.component.type.ConstantType;
import llvm_ir.component.type.LLVMType;
import llvm_ir.component.function.Function;
import llvm_ir.instr.AluInstr;
import llvm_ir.instr.IcmpInstr;
import utils.ErrorType;
import utils.ParamType;
import utils.Printer;
import utils.SyntaxType;
import utils.TokenType;

import java.util.ArrayList;

import static llvm_ir.component.type.IntegerType.CHAR;
import static llvm_ir.component.type.IntegerType.INT_32;

public class UnaryExp extends Node {
    public UnaryExp(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.UnaryExp, children, startLine, endLine);
    }

    public int compute() {
        // PrimaryExp
        if (children.size() == 1) {
            return ((PrimaryExp) children.get(0)).compute();
        }
        // UnaryOp UnaryExp
        else if (children.get(0) instanceof UnaryOp) {
            TokenType op = ((Terminator) children.get(0).children.get(0)).getTokenType();
            switch (op) {
                case PLUS -> {
                    return ((UnaryExp) children.get(1)).compute();
                }
                case MINU -> {
                    return -((UnaryExp) children.get(1)).compute();
                }
                case NOT -> {
                    // TODO: only appear in Cond, how to deal?
                    return ((UnaryExp) children.get(1)).compute() == 0 ? 1 : 0;
                }
            }
            return 0;
        }
        // Ident '(' [FuncRParams] ')'
        else {

            return 0;
        }
    }

    @Override
    public ParamType getTypeAsParam() {
        if (children.size() >= 2 && children.get(0) instanceof Terminator && children.get(1) instanceof Terminator &&
                ((Terminator) children.get(0)).getTokenType() == TokenType.IDENFR &&
                ((Terminator) children.get(1)).getTokenType() == TokenType.LPARENT) {
            // func: should check returnType
            return ParamType.VAR;
        }
        return super.getTypeAsParam();
    }

    /* UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp  // c d e j */

    @Override
    public void analyseSemantic() {
        int flagCheckError = 0;
        Symbol searchRes = null;
        /* check error */
        if (children.size() >= 2 && children.get(0) instanceof Terminator && children.get(1) instanceof Terminator &&
                ((Terminator) children.get(0)).getTokenType() == TokenType.IDENFR &&
                ((Terminator) children.get(1)).getTokenType() == TokenType.LPARENT) {
            flagCheckError = 1;
            // c: 未定义的名字。只针对第二个分支。第一个分支由Primary中的LVal来处理
            searchRes = SymbolManager.getSymbolByNameInGlobalStack(((Terminator) children.get(0)).getContent());
            if (searchRes == null) {
                flagCheckError = 2;
                Printer.addError(children.get(0).getEndLine(), ErrorType.c);
            }
        }

        // 必须先检查函数参数是否有错误，即名字未定义 error c
        super.analyseSemantic();
        // 而后再检查参数类型
        if (flagCheckError == 1) {
            // d：函数参数个数未匹配 e: 类型未匹配
            for (Node node : children) {
                if (node instanceof FuncRParams) {
                    ArrayList<ParamType> RealParams = new ArrayList<>();
                    for (int i = 0; i < node.children.size(); i += 2) {
                        RealParams.add(node.children.get(i).getTypeAsParam());
                    }
                    ErrorType ifErrorType = ((Func) searchRes).checkParams(RealParams);
                    if (ifErrorType != null) {
                        Printer.addError(children.get(0).getEndLine(), ifErrorType);
                    }
                }
            }
        }
    }

    @Override
    public Value genIR() {
        Value ret = null;

        // gen IR
        // PrimaryExp
        if (children.size() == 1) {
            ret = children.get(0).genIR();
        }
        // UnaryOp UnaryExp
        else if (children.get(0) instanceof UnaryOp) {
            TokenType op = ((Terminator) children.get(0).children.get(0)).getTokenType();
            Value op1 = children.get(1).genIR();
            Value op2 = new Constant(0);

            switch (op) {
                case PLUS -> {
                    ret = op1;
                }
                case MINU -> {
                    ret = IRBuilder.genNInsAluInstr(INT_32, AluInstr.AluOp.SUB, op2, op1);
                }
                case NOT -> {
                    ret = IRBuilder.genNInsIcmpInstr(IcmpInstr.Op.eq, op2, op1);
                    ret = IRBuilder.genNInsZExtOrTruncInstr(ret.getResultType(), ret, INT_32, true);
                }
            }

        }
        // Ident '(' [FuncRParams] ')'
        else {
            String funcName = ((Terminator) children.get(0)).getContent();
            ArrayList<LLVMType> funcFParamTypes = ((Function) SymbolManager.getSymbolByNameInGlobalStack(funcName).getLLVMValue()).getParamTypeList();
            int paramCnt = 0;
            ArrayList<Value> funcRParams = new ArrayList<>();
            for (int i = 1; i < children.size(); i++) {
                if (children.get(i) instanceof FuncRParams) {
                    for (Node node : children.get(i).children) {
                        if (node instanceof Exp) {
                            Value rParamValue = node.genIR();
                            LLVMType rParamType = rParamValue.getResultType();
                            boolean isFParamInt = funcFParamTypes.get(paramCnt).isInt32();
                            // 根据形参类型设置常数的类型
                            if (rParamValue instanceof Constant) {
                                ((ConstantType) rParamType).setAssignType(isFParamInt ? INT_32 : CHAR);
                            }
                            // 拓展：i8 -> i32
                            else if (isFParamInt && !rParamType.isBaseTypeSame(INT_32)) {
                                rParamValue = IRBuilder.genNInsZExtOrTruncInstr(CHAR, rParamValue, INT_32, true);
                            }
                            // 截断：i32 -> i8
                            else if (!isFParamInt && !rParamType.isBaseTypeSame(CHAR)) {
                                rParamValue = IRBuilder.genNInsZExtOrTruncInstr(INT_32, rParamValue, CHAR, false);
                            }
                            funcRParams.add(rParamValue);
                            paramCnt++;
                        }
                    }
                    break;
                }
            }
            ret = IRBuilder.genNInsCallInstr(((Terminator) children.get(0)).getContent(), funcRParams, funcFParamTypes);
        }
        return ret;
    }

}
