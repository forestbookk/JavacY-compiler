package frontend.parser.AST.Exp;

import frontend.parser.AST.Leaf.Terminator;
import frontend.parser.AST.Node;
import llvm_ir.IRBuilder;
import llvm_ir.Value;
import llvm_ir.component.Instruction;
import llvm_ir.instr.AluInstr;
import utils.SyntaxType;
import utils.TokenType;

import java.util.ArrayList;

import static llvm_ir.component.type.IntegerType.INT_32;

public class MulExp extends Node {
    public MulExp(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.MulExp, children, startLine, endLine);
    }

    public int compute() {
        int ans = 1;
        TokenType op = TokenType.MULT;
        for (int i = 0; i < children.size(); i++) {
            if ((i & 1) == 1) {
                op = ((Terminator) children.get(i)).getTokenType();
            } else {
                switch (op) {
                    case MULT -> {
                        ans *= ((UnaryExp) children.get(i)).compute();
                    }
                    case DIV -> {
                        ans /= ((UnaryExp) children.get(i)).compute();
                    }
                    case MOD -> {
                        ans %= ((UnaryExp) children.get(i)).compute();
                    }
                }
            }
        }
        return ans;
    }

    // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp

    @Override
    public Value genIR() {
        Value op1 = children.get(0).genIR(); // op1可能为INT_32, INT_8, 会有INT_1吗
        Value op2;
        Instruction instr;

        for (int i = 1; i < children.size(); i++) {
            if ((i & 1) == 1) {
                // 如果op1不是i32类型的，则扩展为i32
                if (!op1.getResultType().isInt32()) {
                    op1 = IRBuilder.genNInsZExtOrTruncInstr(op1.getResultType(), op1, INT_32,true);
                }
                TokenType opTokenType = ((Terminator) children.get(i)).getTokenType();
                op2 = children.get(++i).genIR();
                if (!op2.getResultType().isInt32()) {
                    op2 = IRBuilder.genNInsZExtOrTruncInstr(op2.getResultType(), op2, INT_32, true);
                }
                // i 比刚开始大 2
                switch (opTokenType) {
                    case MULT -> {
                        instr = IRBuilder.genNInsAluInstr(INT_32, AluInstr.AluOp.MUL, op1, op2);
                        op1 = instr;
                    }
                    case DIV -> {
                        instr = IRBuilder.genNInsAluInstr(INT_32, AluInstr.AluOp.SDIV, op1, op2);
                        op1 = instr;
                    }
                    case MOD -> {
                        instr = IRBuilder.genNInsAluInstr(INT_32, AluInstr.AluOp.SREM, op1, op2);
                        op1 = instr;
                    }
                }
            }
        }

        return op1;
    }
}
