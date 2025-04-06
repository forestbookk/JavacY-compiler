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

// AddExp → MulExp | AddExp ('+' | '−') MulExp

public class AddExp extends Node {
    public AddExp(ArrayList<Node> children, int startLine, int endLine) {
        super(SyntaxType.AddExp, children, startLine, endLine);
    }

    public int compute() {
        int ans = 0;
        TokenType op = TokenType.PLUS;
        for (int i = 0; i < children.size(); i++) {
            if ((i & 1) == 1) {
                op = ((Terminator) children.get(i)).getTokenType();
            } else {
                switch (op) {
                    case PLUS -> ans += ((MulExp) children.get(i)).compute();
                    case MINU -> ans -= ((MulExp) children.get(i)).compute();
                }
            }
        }
        return ans;
    }

    @Override
    public Value genIR() {
        Value op1 = children.get(0).genIR();
        Value op2;
        Instruction instr;

        for (int i = 1; i < children.size(); i++) {
            if (((i & 1) == 1)) {
                if (!op1.getResultType().isInt32()) {
                    op1 = IRBuilder.genNInsZExtOrTruncInstr(op1.getResultType(), op1, INT_32, true);
                }
                TokenType opTokenType = ((Terminator) children.get(i)).getTokenType();
                op2 = children.get(++i).genIR();
                // i 比刚开始大 2
                if (!op2.getResultType().isInt32()) {
                    op2 = IRBuilder.genNInsZExtOrTruncInstr(op2.getResultType(), op2, INT_32, true);
                }
                if (opTokenType == TokenType.PLUS) {
                    op1 = IRBuilder.genNInsAluInstr(INT_32, AluInstr.AluOp.ADD, op1, op2);
                } else {
                    op1 = IRBuilder.genNInsAluInstr(INT_32, AluInstr.AluOp.SUB, op1, op2);
                }
            }
        }

        return op1;
    }
}
